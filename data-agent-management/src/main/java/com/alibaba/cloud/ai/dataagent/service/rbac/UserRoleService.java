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
package com.alibaba.cloud.ai.dataagent.service.rbac;

import com.alibaba.cloud.ai.dataagent.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {

	private final SysUserRoleMapper sysUserRoleMapper;

	@Transactional
	public void assignRoles(Long userId, List<Long> roleIds) {
		sysUserRoleMapper.deleteByUserId(userId);
		if (roleIds != null) {
			for (Long roleId : roleIds) {
				sysUserRoleMapper.insert(userId, roleId);
			}
		}
	}

}
