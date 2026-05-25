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

import com.alibaba.cloud.ai.dataagent.entity.SysUser;
import com.alibaba.cloud.ai.dataagent.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

	private final SysUserMapper sysUserMapper;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public List<SysUser> findAll() {
		return sysUserMapper.findAll();
	}

	public SysUser getById(Long id) {
		return sysUserMapper.findById(id);
	}

	public SysUser getByUsername(String username) {
		return sysUserMapper.findByUsername(username);
	}

	public SysUser validate(String username, String rawPassword) {
		SysUser user = sysUserMapper.findByUsername(username);
		if (user == null) {
			return null;
		}
		if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
			return null;
		}
		return user;
	}

	public SysUser createUser(String username, String rawPassword, Integer status) {
		if (sysUserMapper.findByUsername(username) != null) {
			throw new IllegalArgumentException("用户名已存在");
		}
		SysUser user = SysUser.builder()
			.username(username)
			.password(passwordEncoder.encode(rawPassword))
			.status(status != null ? status : 1)
			.build();
		sysUserMapper.insert(user);
		return user;
	}

	public void updateStatus(Long id, Integer status) {
		sysUserMapper.updateStatus(id, status);
	}

	public void resetPassword(Long id, String rawPassword) {
		sysUserMapper.updatePassword(id, passwordEncoder.encode(rawPassword));
	}

}
