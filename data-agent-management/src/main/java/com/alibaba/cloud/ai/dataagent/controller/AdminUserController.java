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
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.cloud.ai.dataagent.entity.SysUser;
import com.alibaba.cloud.ai.dataagent.mapper.AgentMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SysUserAgentVisibilityMapper;
import com.alibaba.cloud.ai.dataagent.service.rbac.UserRoleService;
import com.alibaba.cloud.ai.dataagent.service.rbac.UserService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@SaCheckRole("super-admin")
public class AdminUserController {

	private final UserService userService;
	private final UserRoleService userRoleService;
	private final SysUserAgentVisibilityMapper sysUserAgentVisibilityMapper;
	private final AgentMapper agentMapper;

	@GetMapping
	public ApiResponse<List<SysUser>> list() {
		return ApiResponse.success(userService.findAll());
	}

	@PostMapping
	public ApiResponse<SysUser> create(@RequestBody Map<String, Object> request) {
		String username = (String) request.get("username");
		String password = (String) request.get("password");
		@SuppressWarnings("unchecked")
		List<Long> roleIds = (List<Long>) request.get("roleIds");
		Integer status = request.get("status") != null ? (Integer) request.get("status") : 1;

		SysUser user = userService.createUser(username, password, status);
		if (roleIds != null && !roleIds.isEmpty()) {
			userRoleService.assignRoles(user.getId(), roleIds);
		}
		return ApiResponse.success("创建用户成功", user);
	}

	@PutMapping("/{id}/status")
	public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
		userService.updateStatus(id, status);
		// 如果禁用，踢出该用户所有登录
		if (status != null && status == 0) {
			StpUtil.kickout(id);
		}
		return ApiResponse.success("状态更新成功");
	}

	@PutMapping("/{id}/password")
	public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
		String password = body.get("password");
		if (password == null || password.length() < 6) {
			return ApiResponse.error(400, "密码长度至少为6位");
		}
		userService.resetPassword(id, password);
		return ApiResponse.success("密码重置成功");
	}

	@PutMapping("/{id}/roles")
	public ApiResponse<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
		userRoleService.assignRoles(id, roleIds);
		// 刷新该用户的登录状态权限
		StpUtil.logout(id);
		return ApiResponse.success("角色分配成功");
	}

	@GetMapping("/{id}/agent-visibility")
	public ApiResponse<List<Long>> getAgentVisibility(@PathVariable Long id) {
		return ApiResponse.success(sysUserAgentVisibilityMapper.findAgentIdsByUserId(id));
	}

	@PutMapping("/{id}/agent-visibility")
	@Transactional
	public ApiResponse<Void> updateAgentVisibility(@PathVariable Long id, @RequestBody List<Long> agentIds) {
		if (agentIds != null && !agentIds.isEmpty()) {
			int validCount = agentMapper.countByIds(agentIds);
			if (validCount != agentIds.size()) {
				return ApiResponse.error(400, "部分Agent不存在");
			}
		}
		sysUserAgentVisibilityMapper.deleteByUserId(id);
		if (agentIds != null) {
			for (Long agentId : agentIds) {
				sysUserAgentVisibilityMapper.insert(id, agentId);
			}
		}
		return ApiResponse.success("Agent可见性分配成功");
	}

}
