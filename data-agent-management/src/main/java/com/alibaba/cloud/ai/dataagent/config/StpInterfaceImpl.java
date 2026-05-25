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

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.cloud.ai.dataagent.service.rbac.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

	private final RoleService roleService;

	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		return roleService.getPermissionCodesByUserId(Long.valueOf(loginId.toString()));
	}

	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		return roleService.getRoleCodesByUserId(Long.valueOf(loginId.toString()));
	}

}
