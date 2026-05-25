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

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysUserAgentVisibilityMapper {

	@Insert("INSERT INTO sys_user_agent_visibility (user_id, agent_id) VALUES (#{userId}, #{agentId})")
	int insert(@Param("userId") Long userId, @Param("agentId") Long agentId);

	@Delete("DELETE FROM sys_user_agent_visibility WHERE user_id = #{userId}")
	int deleteByUserId(Long userId);

	@Select("SELECT agent_id FROM sys_user_agent_visibility WHERE user_id = #{userId}")
	List<Long> findAgentIdsByUserId(Long userId);

}
