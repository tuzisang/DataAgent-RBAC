<!--
 * Copyright 2025 the original author or authors.
-->
<template>
  <div class="base-layout">
    <header class="page-header">
      <div class="header-content">
        <div class="brand-section">
          <div class="brand-logo" @click="goToAgentList">
            <i class="bi bi-robot"></i>
            <span class="brand-text">Spring AI Alibaba Data Agent</span>
          </div>
          <nav class="header-nav">
            <div class="nav-item" :class="{ active: isAgentPage() }" @click="goToAgentList">
              <i class="bi bi-grid-3x3-gap"></i>
              <span>智能体列表</span>
            </div>
            <div class="nav-item" :class="{ active: isModelConfigPage() }" @click="goToModelConfig">
              <i class="bi bi-gear"></i>
              <span>模型配置</span>
            </div>
            <div v-if="isSuperAdmin" class="nav-item" :class="{ active: isAdminPage() }" @click="goToAdminUsers">
              <i class="bi bi-shield-lock"></i>
              <span>系统管理</span>
            </div>
          </nav>
        </div>
        <div class="user-section" v-if="authState.isLoggedIn">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="username">{{ authState.user?.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="page-content">
      <slot></slot>
    </main>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { UserFilled, ArrowDown, SwitchButton } from '@element-plus/icons-vue';
import { useAuth } from '@/composables/useAuth';

export default {
  name: 'BaseLayout',
  components: { UserFilled, ArrowDown, SwitchButton },
  setup() {
    const router = useRouter();
    const { state, logout, hasRole } = useAuth();

    const isSuperAdmin = computed(() => hasRole('super-admin'));

    const goToAgentList = () => router.push('/agents');
    const goToModelConfig = () => router.push('/model-config');
    const goToAdminUsers = () => router.push('/admin/users');

    const isAgentPage = () => {
      const name = router.currentRoute.value.name;
      return name === 'AgentList' || name === 'AgentDetail' || name === 'AgentCreate' || name === 'AgentRun';
    };
    const isModelConfigPage = () => router.currentRoute.value.name === 'ModelConfig';
    const isAdminPage = () => {
      const name = router.currentRoute.value.name;
      return name === 'AdminUserManage' || name === 'AdminRoleManage' || name === 'AdminPermissionView';
    };

    const handleCommand = async (cmd) => {
      if (cmd === 'logout') {
        await logout();
        ElMessage.success('已退出登录');
        router.push('/login');
      }
    };

    return {
      authState: state,
      isSuperAdmin,
      goToAgentList,
      goToModelConfig,
      goToAdminUsers,
      isAgentPage,
      isModelConfigPage,
      isAdminPage,
      handleCommand,
      UserFilled,
    };
  },
};
</script>

<style scoped>
.base-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.page-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  width: 100%;
  padding: 0 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 4rem;
}

.brand-section {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
  cursor: pointer;
}

.brand-logo i {
  font-size: 1.5rem;
  color: #3b82f6;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
  font-weight: 500;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #334155;
}

.nav-item.active {
  background: #e0f2fe;
  color: #0369a1;
}

.nav-item i {
  font-size: 1rem;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f1f5f9;
}

.username {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.page-content {
  flex: 1;
  padding: 0;
}
</style>
