/*
 * Copyright 2024-2026 the original author or authors.
 */

import axios from 'axios';
import { ElMessage } from 'element-plus';

const STORAGE_KEY = 'dataagent_auth';

let isRedirecting = false;

axios.interceptors.request.use(
  (config) => {
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (parsed.token) {
          config.headers.Authorization = `Bearer ${parsed.token}`;
        }
      }
    } catch {
      // ignore
    }
    return config;
  },
  (error) => Promise.reject(error),
);

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response) {
      const status = error.response.status;
      const data = error.response.data as { message?: string } | undefined;
      const msg = data?.message || error.message;

      if (status === 401) {
        if (!isRedirecting) {
          isRedirecting = true;
          ElMessage.error('登录已过期，请重新登录');
          sessionStorage.removeItem(STORAGE_KEY);
          import('@/router')
            .then((m) => m.default.push('/login'))
            .finally(() => {
              setTimeout(() => {
                isRedirecting = false;
              }, 1000);
            });
        }
      } else if (status === 403) {
        ElMessage.error('权限不足：' + msg);
      } else if (status >= 500) {
        ElMessage.error('服务器错误：' + msg);
      } else if (status !== 404) {
        ElMessage.error(msg);
      }
    }
    return Promise.reject(error);
  },
);
