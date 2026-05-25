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
package com.alibaba.cloud.ai.dataagent.mapper;

import com.alibaba.cloud.ai.dataagent.entity.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRoleMapper {

	@Select("SELECT * FROM sys_role")
	List<SysRole> findAll();

	@Select("SELECT * FROM sys_role WHERE id = #{id}")
	SysRole findById(Long id);

	@Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
	SysRole findByRoleCode(String roleCode);

	@Select("""
			SELECT r.* FROM sys_role r
			INNER JOIN sys_user_role ur ON r.id = ur.role_id
			WHERE ur.user_id = #{userId}
			""")
	List<SysRole> findByUserId(Long userId);

	@Select("""
			SELECT r.role_code FROM sys_role r
			INNER JOIN sys_user_role ur ON r.id = ur.role_id
			WHERE ur.user_id = #{userId}
			""")
	List<String> findRoleCodesByUserId(Long userId);

}
