/*
 * Copyright 2024-2026 the original author or authors.
 */

import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus';
import routes from '@/router/routes';
import modelConfigService from '@/services/modelConfig';
import { useAuth } from '@/composables/useAuth';

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

let hasShownWarning = false;

function isAuthenticated() {
  try {
    const raw = sessionStorage.getItem('dataagent_auth');
    if (!raw) return false;
    const parsed = JSON.parse(raw);
    return !!parsed.token;
  } catch {
    return false;
  }
}

router.beforeEach(async (to, from, next) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} - Spring AI Alibaba Data Agent`;
  } else {
    document.title = 'Spring AI Alibaba Data Agent';
  }

  // 公开页面直接放行
  if (to.meta?.public) {
    if (isAuthenticated() && (to.path === '/login' || to.path === '/register')) {
      next('/agents');
      return;
    }
    next();
    return;
  }

  // 检查登录
  if (!isAuthenticated()) {
    next('/login');
    return;
  }

  // 检查角色权限
  const requireRole = to.meta?.requireRole;
  if (requireRole) {
    const { hasRole } = useAuth();
    if (!hasRole(requireRole)) {
      ElMessage.error('您没有权限访问该页面');
      next('/agents');
      return;
    }
  }

  // 模型配置检查（原逻辑保留）
  if (to.path === '/model-config') {
    next();
    return;
  }

  try {
    const result = await modelConfigService.checkReady();
    if (!result.ready) {
      const missingModels = [];
      if (!result.chatModelReady) missingModels.push('聊天模型');
      if (!result.embeddingModelReady) missingModels.push('嵌入模型');
      if (!hasShownWarning) {
        ElMessage.warning({
          message: `欢迎使用！检测到您尚未配置${missingModels.join('和')}，请先配置 OpenAI/阿里/Ollama 等模型参数以激活系统。`,
          duration: 5000,
        });
        hasShownWarning = true;
      }
      next('/model-config');
      return;
    }
    hasShownWarning = false;
    next();
  } catch (error) {
    console.error('检查模型配置失败:', error);
    if (!hasShownWarning) {
      ElMessage.error({
        message: '无法检查模型配置状态，请确保后端服务已启动并配置模型。',
        duration: 5000,
      });
      hasShownWarning = true;
    }
    next('/model-config');
  }
});

router.afterEach((to, from) => {
  console.log(`导航完成: ${to.path} ${from.path}`);
});

export default router;
