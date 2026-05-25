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

import com.alibaba.cloud.ai.dataagent.dto.knowledge.businessknowledge.CreateBusinessKnowledgeDTO;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.cloud.ai.dataagent.dto.knowledge.businessknowledge.UpdateBusinessKnowledgeDTO;
import com.alibaba.cloud.ai.dataagent.service.business.BusinessKnowledgeService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import com.alibaba.cloud.ai.dataagent.vo.BusinessKnowledgeVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/business-knowledge")
@AllArgsConstructor
public class BusinessKnowledgeController {

	private final BusinessKnowledgeService businessKnowledgeService;

	@GetMapping
	@SaCheckPermission("business-knowledge:view")
	public ApiResponse<List<BusinessKnowledgeVO>> list(@RequestParam(value = "agentId") String agentIdStr,
			@RequestParam(value = "keyword", required = false) String keyword) {
		List<BusinessKnowledgeVO> result;
		Long agentId = Long.parseLong(agentIdStr);

		if (StringUtils.hasText(keyword)) {
			result = businessKnowledgeService.searchKnowledge(agentId, keyword);
		}
		else {
			result = businessKnowledgeService.getKnowledge(agentId);
		}
		return ApiResponse.success("success list businessKnowledge", result);
	}

	@GetMapping("/{id}")
	@SaCheckPermission("business-knowledge:view")
	public ApiResponse<BusinessKnowledgeVO> get(@PathVariable(value = "id") Long id) {
		BusinessKnowledgeVO vo = businessKnowledgeService.getKnowledgeById(id);
		if (vo == null) {
			return ApiResponse.error("businessKnowledge not found");
		}
		return ApiResponse.success("success get businessKnowledge", vo);
	}

	@PostMapping
	@SaCheckPermission("business-knowledge:create")
	public ApiResponse<BusinessKnowledgeVO> create(@RequestBody @Validated CreateBusinessKnowledgeDTO knowledge) {
		return ApiResponse.success("success create businessKnowledge",
				businessKnowledgeService.addKnowledge(knowledge));
	}

	@PutMapping("/{id}")
	@SaCheckPermission("business-knowledge:update")
	public ApiResponse<BusinessKnowledgeVO> update(@PathVariable(value = "id") Long id,
			@RequestBody UpdateBusinessKnowledgeDTO knowledge) {

		return ApiResponse.success("success update businessKnowledge",
				businessKnowledgeService.updateKnowledge(id, knowledge));
	}

	@DeleteMapping("/{id}")
	@SaCheckPermission("business-knowledge:delete")
	public ApiResponse<Boolean> delete(@PathVariable(value = "id") Long id) {
		if (businessKnowledgeService.getKnowledgeById(id) == null) {
			return ApiResponse.error("businessKnowledge not found");
		}
		businessKnowledgeService.deleteKnowledge(id);
		return ApiResponse.success("success delete businessKnowledge");
	}

	@PostMapping("/recall/{id}")
	@SaCheckPermission("business-knowledge:update")
	public ApiResponse<Boolean> recallKnowledge(@PathVariable(value = "id") Long id,
			@RequestParam(value = "isRecall") Boolean isRecall) {
		businessKnowledgeService.recallKnowledge(id, isRecall);
		return ApiResponse.success("success update recall businessKnowledge");
	}

	@PostMapping("/refresh-vector-store")
	@SaCheckPermission("business-knowledge:update")
	public ApiResponse<Boolean> refreshAllKnowledgeToVectorStore(@RequestParam(value = "agentId") String agentId) {
		// 校验 agentId 不为空和空字符串
		if (!StringUtils.hasText(agentId)) {
			return ApiResponse.error("agentId cannot be empty");
		}

		try {
			businessKnowledgeService.refreshAllKnowledgeToVectorStore(agentId);
			return ApiResponse.success("success refresh vector store");
		}
		catch (Exception e) {
			log.error("Failed to refresh vector store for agentId: {}", agentId, e);
			return ApiResponse.error("Failed to refresh vector store");
		}
	}

	@PostMapping("/retry-embedding/{id}")
	@SaCheckPermission("business-knowledge:update")
	public ApiResponse<Boolean> retryEmbedding(@PathVariable(value = "id") Long id) {
		businessKnowledgeService.retryEmbedding(id);
		return ApiResponse.success("success retry embedding");
	}

}
