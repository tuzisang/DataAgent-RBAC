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

import com.alibaba.cloud.ai.dataagent.entity.SysPermission;
import com.alibaba.cloud.ai.dataagent.entity.SysRole;
import com.alibaba.cloud.ai.dataagent.mapper.SysPermissionMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SysRoleMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SysRolePermissionMapper;
import com.alibaba.cloud.ai.dataagent.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

	private final SysRoleMapper sysRoleMapper;
	private final SysPermissionMapper sysPermissionMapper;
	private final SysUserRoleMapper sysUserRoleMapper;
	private final SysRolePermissionMapper sysRolePermissionMapper;

	public List<SysRole> findAll() {
		return sysRoleMapper.findAll();
	}

	public List<String> getRoleCodesByUserId(Long userId) {
		return sysRoleMapper.findRoleCodesByUserId(userId);
	}

	public List<String> getPermissionCodesByUserId(Long userId) {
		List<Long> roleIds = sysUserRoleMapper.findRoleIdsByUserId(userId);
		if (roleIds == null || roleIds.isEmpty()) {
			return Collections.emptyList();
		}
		return sysPermissionMapper.findPermissionCodesByRoleIds(roleIds);
	}

	public List<SysPermission> getPermissionsByRoleId(Long roleId) {
		return sysPermissionMapper.findByRoleId(roleId);
	}

	@Transactional
	public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
		sysRolePermissionMapper.deleteByRoleId(roleId);
		if (permissionIds != null) {
			for (Long pid : permissionIds) {
				sysRolePermissionMapper.insert(roleId, pid);
			}
		}
	}

}
