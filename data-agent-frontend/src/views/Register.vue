<!--
  ~ Copyright 2024-2026 the original author or authors.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      https://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
<template>
  <div class="auth-page">
    <div class="auth-card-wrapper">
      <div class="auth-card">
        <div class="auth-header">
          <i class="bi bi-robot brand-icon"></i>
          <h1>注册账号</h1>
          <p>创建您的 Data Agent 账号</p>
        </div>

        <div class="auth-form">
          <div class="form-item">
            <label>用户名</label>
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
              @keyup.enter="handleRegister"
            />
          </div>

          <div class="form-item">
            <label>密码</label>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleRegister"
            />
          </div>

          <div class="form-item">
            <label>确认密码</label>
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleRegister"
            />
          </div>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </div>

        <div class="auth-footer">
          <span>已有账号？</span>
          <el-link type="primary" @click="goLogin">立即登录</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import authService from '@/services/auth';

const router = useRouter();

const loading = ref(false);
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
});

const handleRegister = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致');
    return;
  }
  if (form.password.length < 6) {
    ElMessage.warning('密码长度至少为6位');
    return;
  }

  loading.value = true;
  try {
    await authService.register({
      username: form.username,
      password: form.password,
    });
    ElMessage.success('注册成功，请登录');
    router.push('/login');
  } catch (e) {
    ElMessage.error(e.message || '注册失败');
  } finally {
    loading.value = false;
  }
};

const goLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-card-wrapper {
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.auth-card {
  background: white;
  border-radius: 12px;
  padding: 40px 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.auth-header {
  text-align: center;
  margin-bottom: 28px;
}

.brand-icon {
  font-size: 40px;
  color: #3b82f6;
}

.auth-header h1 {
  margin: 12px 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.auth-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 14px;
  color: #374151;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.auth-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}
</style>
