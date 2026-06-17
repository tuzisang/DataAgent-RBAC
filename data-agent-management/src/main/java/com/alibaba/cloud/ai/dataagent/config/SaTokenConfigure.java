/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class SaTokenConfigure {

	@Value("${data-agent.cors.allowed-origins:http://localhost:5173}")
	private String allowedOrigins;

	/**
	 * 注册 Sa-Token 全局过滤器
	 */
	@Bean
	@Order(0)
	public SaReactorFilter getSaReactorFilter() {
		return new SaReactorFilter()
			.addInclude("/**")
			.addExclude(
				"/favicon.ico",
				"/v3/api-docs",
				"/swagger-ui.html",
				"/swagger-ui/**",
				"/webjars/**",
				"/uploads/**"
			)
			.setBeforeAuth(obj -> {
				// SSE 流式接口：从 URL 参数读取 token 注入 Sa-Token 上下文
				String path = SaHolder.getRequest().getRequestPath();
				if (path != null && path.startsWith("/api/stream/search")) {
					String urlToken = SaHolder.getRequest().getParam("token");
					if (StringUtils.isNotBlank(urlToken)) {
						StpUtil.setTokenValue(urlToken);
					}
				}
			})
			.setAuth(obj -> SaRouter.match("/**")
				.notMatch("/api/auth/login", "/api/auth/register")
				.check(r -> StpUtil.checkLogin()))
			.setError(e -> {
				log.warn("Sa-Token auth error: {}", e.getMessage());
				return SaResult.error(e.getMessage());
			});
	}

	/**
	 * 上下文桥接过滤器：SaReactorFilter 会在 chain.filter() 前清除 ThreadLocal 上下文，
	 * 导致所有 Controller 无法使用 StpUtil。此过滤器在 SaReactorFilter 执行完毕
	 * 后重新建立 ThreadLocal 上下文，请求结束后自动清理。
	 */
	@Bean
	@Order(1)
	public WebFilter saTokenContextBridge() {
		return (exchange, chain) -> {
			SaReactorSyncHolder.setContext(exchange);
			return chain.filter(exchange)
				.doFinally(signalType -> SaReactorSyncHolder.clearContext());
		};
	}

	/**
	 * 全局 CORS 配置（替代所有 @CrossOrigin）
	 * 在 SaReactorFilter 之前执行，确保 OPTIONS 预检请求能被正确应答
	 */
	@Bean
	@Order(-1)
	public CorsWebFilter corsWebFilter() {
		CorsConfiguration config = new CorsConfiguration();
		List<String> origins = Arrays.asList(allowedOrigins.split(","));
		// 支持通配符模式（如 "http://*"），对部署到未知域名的场景更友好
		if (origins.stream().anyMatch(o -> o.contains("*"))) {
			config.setAllowedOriginPatterns(origins);
		} else {
			config.setAllowedOrigins(origins);
		}
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		config.setAllowedHeaders(Arrays.asList("*"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);
		config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsWebFilter(source);
	}

}
