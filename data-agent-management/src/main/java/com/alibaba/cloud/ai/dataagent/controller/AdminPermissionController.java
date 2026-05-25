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

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.cloud.ai.dataagent.entity.SysPermission;
import com.alibaba.cloud.ai.dataagent.service.rbac.PermissionService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@SaCheckRole("super-admin")
public class AdminPermissionController {

	private final PermissionService permissionService;

	@GetMapping
	public ApiResponse<Map<String, List<SysPermission>>> list() {
		return ApiResponse.success(permissionService.listAllGroupedByResource());
	}

}
