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

import com.alibaba.cloud.ai.dataagent.entity.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysUserMapper {

	@Select("SELECT * FROM sys_user ORDER BY create_time DESC")
	List<SysUser> findAll();

	@Select("SELECT * FROM sys_user WHERE id = #{id}")
	SysUser findById(Long id);

	@Select("SELECT * FROM sys_user WHERE username = #{username}")
	SysUser findByUsername(String username);

	@Insert("INSERT INTO sys_user (username, password, status, create_time, update_time) "
			+ "VALUES (#{username}, #{password}, #{status}, NOW(), NOW())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(SysUser user);

	@Update("UPDATE sys_user SET status = #{status}, update_time = NOW() WHERE id = #{id}")
	int updateStatus(@Param("id") Long id, @Param("status") Integer status);

	@Update("UPDATE sys_user SET password = #{password}, update_time = NOW() WHERE id = #{id}")
	int updatePassword(@Param("id") Long id, @Param("password") String password);

}
