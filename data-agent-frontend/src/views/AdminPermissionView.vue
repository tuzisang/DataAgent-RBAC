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
  <BaseLayout>
    <div class="admin-page">
      <main class="main-content">
        <div class="content-header">
          <div class="header-info">
            <h1 class="content-title">权限查看</h1>
            <p class="content-subtitle">查看系统中所有权限配置</p>
          </div>
        </div>

        <div v-for="(perms, resource) in groupedPermissions" :key="resource" class="section-card permission-card">
          <div class="card-header">
            <h3>{{ resourceLabel(resource) }}</h3>
            <el-tag size="small">{{ perms.length }} 项</el-tag>
          </div>
          <el-table :data="perms" size="small" stripe>
            <el-table-column prop="permissionCode" label="权限编码" />
            <el-table-column prop="permissionName" label="权限名称" />
            <el-table-column prop="action" label="操作类型" />
          </el-table>
        </div>
      </main>
    </div>
  </BaseLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import BaseLayout from '@/layouts/BaseLayout.vue';
import adminService from '@/services/admin';

const groupedPermissions = reactive({});

const resourceLabelMap = {
  agent: '智能体管理',
  datasource: '数据源管理',
  'model-config': '模型配置',
  'prompt-config': '提示词配置',
  'agent-knowledge': 'Agent知识库',
  'business-knowledge': '业务知识库',
  'semantic-model': '语义模型',
  'preset-question': '预设问题',
  'agent-datasource': 'Agent数据源',
  chat: '对话',
  file: '文件',
  system: '系统管理',
};

const resourceLabel = (key) => resourceLabelMap[key] || key;

const loadPermissions = async () => {
  try {
    const data = await adminService.listPermissions();
    Object.assign(groupedPermissions, data);
  } catch (e) {
    ElMessage.error(e.message || '获取权限列表失败');
  }
};

onMounted(() => {
  loadPermissions();
});
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: #f8fafc;
}

.main-content {
  padding: 2rem;
}

.content-header {
  margin-bottom: 2rem;
}

.header-info h1 {
  font-size: 2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.header-info p {
  color: #6b7280;
  margin: 0;
  font-size: 1.1rem;
}

.section-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  margin-bottom: 1.5rem;
}

.permission-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.permission-card h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}
</style>
