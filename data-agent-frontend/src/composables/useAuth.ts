/*
 * Copyright 2024-2026 the original author or authors.
 */

import { reactive, readonly } from 'vue';
import authService, { UserInfo } from '@/services/auth';

const STORAGE_KEY = 'dataagent_auth';

interface AuthState {
  token: string | null;
  user: UserInfo | null;
  roles: string[];
  permissions: string[];
  isLoggedIn: boolean;
}

function loadState(): AuthState {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      return {
        token: parsed.token || null,
        user: parsed.user || null,
        roles: parsed.roles || [],
        permissions: parsed.permissions || [],
        isLoggedIn: !!parsed.token,
      };
    }
  } catch {
    // ignore
  }
  return { token: null, user: null, roles: [], permissions: [], isLoggedIn: false };
}

const state = reactive<AuthState>(loadState());

function persist() {
  sessionStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      token: state.token,
      user: state.user,
      roles: state.roles,
      permissions: state.permissions,
    }),
  );
}

export function useAuth() {
  const login = async (username: string, password: string) => {
    const result = await authService.login({ username, password });
    state.token = result.token;
    state.user = result.user;
    state.roles = result.roles || [];
    state.permissions = result.permissions || [];
    state.isLoggedIn = true;
    persist();
    return result;
  };

  const logout = async () => {
    await authService.logout();
    state.token = null;
    state.user = null;
    state.roles = [];
    state.permissions = [];
    state.isLoggedIn = false;
    sessionStorage.removeItem(STORAGE_KEY);
  };

  const refreshUser = async () => {
    const result = await authService.getUserInfo();
    state.user = result.user;
    state.roles = result.roles || [];
    state.permissions = result.permissions || [];
    persist();
    return result;
  };

  const hasRole = (role: string): boolean => {
    return state.roles.includes(role);
  };

  const hasPermission = (perm: string): boolean => {
    return state.permissions.includes(perm);
  };

  return {
    state: readonly(state) as Readonly<AuthState>,
    login,
    logout,
    refreshUser,
    hasRole,
    hasPermission,
  };
}
