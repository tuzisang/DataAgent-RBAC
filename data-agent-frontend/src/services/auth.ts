/*
 * Copyright 2024-2026 the original author or authors.
 */

import axios from 'axios';
import { ApiResponse } from './common';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface UserInfo {
  id: number;
  username: string;
  nickname: string | null;
  email: string | null;
  status: number;
}

export interface LoginResult {
  token: string;
  tokenName: string;
  user: UserInfo;
  roles: string[];
  permissions: string[];
}

const AUTH_BASE = '/api/auth';

class AuthService {
  async login(data: LoginRequest): Promise<LoginResult> {
    const response = await axios.post<ApiResponse<LoginResult>>(
      `${AUTH_BASE}/login`,
      data,
    );
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.message || '登录失败');
  }

  async register(data: RegisterRequest): Promise<void> {
    const response = await axios.post<ApiResponse<void>>(
      `${AUTH_BASE}/register`,
      data,
    );
    if (!response.data.success) {
      throw new Error(response.data.message || '注册失败');
    }
  }

  async getUserInfo(): Promise<{ user: UserInfo; roles: string[]; permissions: string[] }> {
    const response = await axios.get<ApiResponse<{ user: UserInfo; roles: string[]; permissions: string[] }>>(
      `${AUTH_BASE}/info`,
    );
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.message || '获取用户信息失败');
  }

  async logout(): Promise<void> {
    try {
      await axios.post<ApiResponse<void>>(`${AUTH_BASE}/logout`);
    } catch {
      // ignore
    }
  }
}

export default new AuthService();
