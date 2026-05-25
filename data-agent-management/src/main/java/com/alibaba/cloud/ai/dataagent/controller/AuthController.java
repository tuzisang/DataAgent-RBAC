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

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.cloud.ai.dataagent.entity.SysUser;
import com.alibaba.cloud.ai.dataagent.service.rbac.RoleService;
import com.alibaba.cloud.ai.dataagent.service.rbac.UserRoleService;
import com.alibaba.cloud.ai.dataagent.service.rbac.UserService;
import com.alibaba.cloud.ai.dataagent.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;
	private final RoleService roleService;
	private final UserRoleService userRoleService;

	@PostMapping("/login")
	public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> request,
												   ServerWebExchange exchange) {
		String username = request.get("username");
		String password = request.get("password");

		if (username == null || password == null) {
			return ApiResponse.error("用户名和密码不能为空");
		}

		SysUser user = userService.validate(username, password);
		if (user == null) {
			return ApiResponse.error(401, "用户名或密码错误");
		}
		if (user.getStatus() != null && user.getStatus() == 0) {
			return ApiResponse.error(403, "账号已被禁用");
		}

		SaReactorSyncHolder.setContext(exchange);
		try {
			StpUtil.login(user.getId());

			Map<String, Object> result = new HashMap<>();
			result.put("token", StpUtil.getTokenValue());
			result.put("tokenName", StpUtil.getTokenName());
			result.put("user", user);
			result.put("roles", roleService.getRoleCodesByUserId(user.getId()));
			result.put("permissions", roleService.getPermissionCodesByUserId(user.getId()));

			return ApiResponse.success("登录成功", result);
		} finally {
			SaReactorSyncHolder.clearContext();
		}
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout(ServerWebExchange exchange) {
		SaReactorSyncHolder.setContext(exchange);
		try {
			StpUtil.logout();
			return ApiResponse.success("登出成功");
		} finally {
			SaReactorSyncHolder.clearContext();
		}
	}

	@PostMapping("/register")
	public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, String> request,
													  ServerWebExchange exchange) {
		String username = request.get("username");
		String password = request.get("password");

		if (username == null || password == null || username.isBlank() || password.isBlank()) {
			return ApiResponse.error("用户名和密码不能为空");
		}

		try {
			SysUser user = userService.createUser(username, password, 1);

			SaReactorSyncHolder.setContext(exchange);
			try {
				userRoleService.assignRoles(user.getId(), List.of(4L));
			} finally {
				SaReactorSyncHolder.clearContext();
			}

			Map<String, Object> result = new HashMap<>();
			result.put("userId", user.getId());
			result.put("username", user.getUsername());
			return ApiResponse.success("注册成功", result);
		} catch (IllegalArgumentException e) {
			return ApiResponse.error(400, e.getMessage());
		}
	}

	@GetMapping("/info")
	@SaCheckLogin
	public ApiResponse<Map<String, Object>> info(ServerWebExchange exchange) {
		SaReactorSyncHolder.setContext(exchange);
		try {
			Long userId = Long.valueOf(StpUtil.getLoginId().toString());
			SysUser user = userService.getById(userId);
			if (user == null) {
				return ApiResponse.error(401, "用户不存在");
			}

			Map<String, Object> result = new HashMap<>();
			result.put("user", user);
			result.put("roles", roleService.getRoleCodesByUserId(userId));
			result.put("permissions", roleService.getPermissionCodesByUserId(userId));
			return ApiResponse.success(result);
		} finally {
			SaReactorSyncHolder.clearContext();
		}
	}

}
