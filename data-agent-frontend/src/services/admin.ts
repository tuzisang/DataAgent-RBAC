/*
 * Copyright 2024-2026 the original author or authors.
 */

import axios from 'axios';
import { ApiResponse } from './common';

export interface SysUser {
  id: number;
  username: string;
  nickname: string | null;
  email: string | null;
  status: number;
  createTime: string;
  updateTime: string;
}

export interface SysRole {
  id: number;
  roleCode: string;
  roleName: string;
  description: string | null;
}

export interface SysPermission {
  id: number;
  permissionCode: string;
  permissionName: string;
  resource: string;
  action: string;
}

const ADMIN_BASE = '/api/admin';

class AdminService {
  // Users
  async listUsers(): Promise<SysUser[]> {
    const response = await axios.get<ApiResponse<SysUser[]>>(`${ADMIN_BASE}/users`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.message || '获取用户列表失败');
  }

  async createUser(data: { username: string; password: string; roleIds: number[]; status?: number }): Promise<SysUser> {
    const response = await axios.post<ApiResponse<SysUser>>(`${ADMIN_BASE}/users`, data);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.message || '创建用户失败');
  }

  async updateUserStatus(id: number, status: number): Promise<void> {
    const response = await axios.put<ApiResponse<void>>(
      `${ADMIN_BASE}/users/${id}/status`,
      null,
      { params: { status } },
    );
    if (!response.data.success) {
      throw new Error(response.data.message || '更新状态失败');
    }
  }

  async resetUserPassword(id: number, password: string): Promise<void> {
    const response = await axios.put<ApiResponse<void>>(
      `${ADMIN_BASE}/users/${id}/password`,
      null,
      { params: { password } },
    );
    if (!response.data.success) {
      throw new Error(response.data.message || '重置密码失败');
    }
  }

  async assignUserRoles(id: number, roleIds: number[]): Promise<void> {
    const response = await axios.put<ApiResponse<void>>(
      `${ADMIN_BASE}/users/${id}/roles`,
      roleIds,
    );
    if (!response.data.success) {
      throw new Error(response.data.message || '分配角色失败');
    }
  }

  // Roles
  async listRoles(): Promise<SysRole[]> {
    const response = await axios.get<ApiResponse<SysRole[]>>(`${ADMIN_BASE}/roles`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.message || '获取角色列表失败');
  }

  async getRolePermissions(roleId: number): Promise<SysPermission[]> {
    const response = await axios.get<ApiResponse<SysPermission[]>>(
      `${ADMIN_BASE}/roles/${roleId}/permissions`,
    );
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.message || '获取角色权限失败');
  }

  async updateRolePermissions(roleId: number, permissionIds: number[]): Promise<void> {
    const response = await axios.put<ApiResponse<void>>(
      `${ADMIN_BASE}/roles/${roleId}/permissions`,
      permissionIds,
    );
    if (!response.data.success) {
      throw new Error(response.data.message || '更新权限失败');
    }
  }

  // Permissions
  async listPermissions(): Promise<Record<string, SysPermission[]>> {
    const response = await axios.get<ApiResponse<Record<string, SysPermission[]>>>(
      `${ADMIN_BASE}/permissions`,
    );
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.message || '获取权限列表失败');
  }
}

export default new AdminService();
