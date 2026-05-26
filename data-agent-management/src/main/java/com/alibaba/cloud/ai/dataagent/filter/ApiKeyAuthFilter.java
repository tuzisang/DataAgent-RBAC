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
package com.alibaba.cloud.ai.dataagent.filter;

import com.alibaba.cloud.ai.dataagent.entity.Agent;
import com.alibaba.cloud.ai.dataagent.service.agent.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class ApiKeyAuthFilter implements WebFilter {

	private final AgentService agentService;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");

		boolean isApiKeyPath = path.contains("/api-key/");
		boolean isStreamPath = path.startsWith("/api/stream/search");

		if (!isApiKeyPath && !isStreamPath) {
			return chain.filter(exchange);
		}

		if (StringUtils.isBlank(apiKey) || !apiKey.startsWith("sk-")) {
			return unauthorized(exchange, "Invalid API Key");
		}

		Agent agent = agentService.findByApiKey(apiKey);
		if (agent == null || !Integer.valueOf(1).equals(agent.getApiKeyEnabled())) {
			return unauthorized(exchange, "API Key disabled or not found");
		}

		exchange.getAttributes().put("API_KEY_AGENT_ID", agent.getId());
		return chain.filter(exchange);
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		String body = String.format("{\"code\":401,\"message\":\"%s\"}", msg);
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
	}

}
