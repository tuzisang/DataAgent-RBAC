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

import com.alibaba.cloud.ai.dataagent.entity.SysPermission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysPermissionMapper {

	@Select("SELECT * FROM sys_permission")
	List<SysPermission> findAll();

	@Select("""
			<script>
			SELECT DISTINCT p.permission_code FROM sys_permission p
			INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
			WHERE rp.role_id IN
				<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>
					#{roleId}
				</foreach>
			</script>
			""")
	List<String> findPermissionCodesByRoleIds(@Param("roleIds") List<Long> roleIds);

	@Select("""
			SELECT p.* FROM sys_permission p
			INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
			WHERE rp.role_id = #{roleId}
			""")
	List<SysPermission> findByRoleId(@Param("roleId") Long roleId);

}
