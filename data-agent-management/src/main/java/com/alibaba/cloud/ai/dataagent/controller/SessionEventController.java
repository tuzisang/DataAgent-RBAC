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

import com.alibaba.cloud.ai.dataagent.service.chat.SessionEventPublisher;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.cloud.ai.dataagent.vo.SessionUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.springframework.http.server.reactive.ServerHttpResponse;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionEventController {

	private final SessionEventPublisher sessionEventPublisher;

	@GetMapping(value = "/agent/{agentId}/sessions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@SaCheckPermission("chat:use")
	public Flux<ServerSentEvent<SessionUpdateEvent>> streamSessionUpdates(@PathVariable Integer agentId,
			ServerHttpResponse response) {
		response.getHeaders().add("Cache-Control", "no-cache");
		response.getHeaders().add("Connection", "keep-alive");

		log.debug("Client subscribed to session update stream for agent {}", agentId);
		return sessionEventPublisher.register(agentId)
			.doFinally(
					signal -> log.debug("Session update stream finished for agent {} with signal {}", agentId, signal));
	}

}
