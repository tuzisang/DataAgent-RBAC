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
package com.alibaba.cloud.ai.dataagent.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.cloud.ai.dataagent.dto.GraphRequest;
import com.alibaba.cloud.ai.dataagent.entity.Agent;
import com.alibaba.cloud.ai.dataagent.service.agent.AgentService;
import com.alibaba.cloud.ai.dataagent.service.graph.GraphService;
import com.alibaba.cloud.ai.dataagent.vo.GraphNodeResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import static com.alibaba.cloud.ai.dataagent.constant.Constant.STREAM_EVENT_COMPLETE;
import static com.alibaba.cloud.ai.dataagent.constant.Constant.STREAM_EVENT_ERROR;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class GraphController {

	private final GraphService graphService;

	private final AgentService agentService;

	@GetMapping(value = "/stream/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<GraphNodeResponse>> streamSearch(
			@RequestParam(value = "token", required = false) String token,
			@RequestParam(value = "agentId") String agentId,
			@RequestParam(value = "threadId", required = false) String threadId,
			@RequestParam("query") String query,
			@RequestParam(value = "humanFeedback", required = false) boolean humanFeedback,
			@RequestParam(value = "humanFeedbackContent", required = false) String humanFeedbackContent,
			@RequestParam(value = "rejectedPlan", required = false) boolean rejectedPlan,
			@RequestParam(value = "nl2sqlOnly", required = false) boolean nl2sqlOnly,
			@RequestHeader(value = "X-API-Key", required = false) String apiKey,
			ServerHttpResponse response) {

		// 先校验认证
		boolean authed = checkAuth(token, apiKey, agentId);
		if (!authed) {
			return Flux.just(ServerSentEvent.<GraphNodeResponse>builder()
				.event(STREAM_EVENT_ERROR)
				.data(GraphNodeResponse.builder().text("未登录或认证失败").build())
				.build());
		}

		response.getHeaders().add("Cache-Control", "no-cache");
		response.getHeaders().add("Connection", "keep-alive");

		Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink = Sinks.many().unicast().onBackpressureBuffer();

		GraphRequest request = GraphRequest.builder()
			.agentId(agentId)
			.threadId(threadId)
			.query(query)
			.humanFeedback(humanFeedback)
			.humanFeedbackContent(humanFeedbackContent)
			.rejectedPlan(rejectedPlan)
			.nl2sqlOnly(nl2sqlOnly)
			.build();
		graphService.graphStreamProcess(sink, request);

		return sink.asFlux().filter(sse -> {
			if (STREAM_EVENT_COMPLETE.equals(sse.event()) || STREAM_EVENT_ERROR.equals(sse.event())) {
				return true;
			}
			return sse.data() != null && sse.data().getText() != null && !sse.data().getText().isEmpty();
		})
			.doOnSubscribe(subscription -> log.info("Client subscribed to stream, threadId: {}", request.getThreadId()))
			.doOnCancel(() -> {
				log.info("Client disconnected from stream, threadId: {}", request.getThreadId());
				if (request.getThreadId() != null) {
					graphService.stopStreamProcessing(request.getThreadId());
				}
			})
			.doOnError(e -> {
				log.error("Error occurred during streaming, threadId: {}: ", request.getThreadId(), e);
				if (request.getThreadId() != null) {
					graphService.stopStreamProcessing(request.getThreadId());
				}
			})
			.doOnComplete(() -> log.info("Stream completed successfully, threadId: {}", request.getThreadId()));
	}

	private boolean checkAuth(String token, String apiKey, String agentId) {
		// 1. 尝试 Sa-Token 认证（SaTokenConfigure.setBeforeAuth 已注入 URL token）
		try {
			Object loginId = StpUtil.getLoginIdDefaultNull();
			if (loginId != null) {
				return true;
			}
		} catch (Exception ignored) {
		}

		// 2. 尝试 API Key 认证
		if (StringUtils.isNotBlank(apiKey) && apiKey.startsWith("sk-")) {
			Agent agent = agentService.findByApiKey(apiKey);
			if (agent != null && Integer.valueOf(1).equals(agent.getApiKeyEnabled())) {
				return agent.getId().toString().equals(agentId);
			}
		}

		return false;
	}

}
