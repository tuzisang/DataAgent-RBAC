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
            <h1 class="content-title">角色管理</h1>
            <p class="content-subtitle">管理角色权限配置</p>
          </div>
        </div>

        <div class="section-card">
          <el-table :data="roles" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="roleName" label="角色名称" />
            <el-table-column prop="roleCode" label="角色编码" />
            <el-table-column prop="description" label="描述" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="showEditPermissions(row)">
                  配置权限
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </main>
    </div>

    <el-dialog v-model="dialogVisible" :title="`配置权限 - ${currentRole?.roleName}`" width="600px">
      <div v-loading="permissionLoading" class="permission-dialog">
        <el-checkbox-group v-model="selectedPermissionIds">
          <div v-for="(perms, resource) in groupedPermissions" :key="resource" class="permission-group">
            <h4>{{ resourceLabel(resource) }}</h4>
            <el-checkbox v-for="p in perms" :key="p.id" :label="p.id">
              {{ p.permissionName }}
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </BaseLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import BaseLayout from '@/layouts/BaseLayout.vue';
import adminService from '@/services/admin';

const loading = ref(false);
const permissionLoading = ref(false);
const submitting = ref(false);
const roles = ref([]);
const groupedPermissions = reactive({});
const dialogVisible = ref(false);
const currentRole = ref(null);
const selectedPermissionIds = ref([]);

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

const loadRoles = async () => {
  loading.value = true;
  try {
    roles.value = await adminService.listRoles();
  } catch (e) {
    ElMessage.error(e.message || '获取角色列表失败');
  } finally {
    loading.value = false;
  }
};

const loadPermissions = async () => {
  try {
    const data = await adminService.listPermissions();
    Object.assign(groupedPermissions, data);
  } catch (e) {
    console.error('获取权限列表失败', e);
  }
};

const showEditPermissions = async (row) => {
  currentRole.value = row;
  selectedPermissionIds.value = [];
  dialogVisible.value = true;
  permissionLoading.value = true;
  try {
    const perms = await adminService.getRolePermissions(row.id);
    selectedPermissionIds.value = perms.map((p) => p.id);
  } catch (e) {
    ElMessage.error(e.message || '获取角色权限失败');
  } finally {
    permissionLoading.value = false;
  }
};

const handleSave = async () => {
  submitting.value = true;
  try {
    await adminService.updateRolePermissions(currentRole.value.id, selectedPermissionIds.value);
    ElMessage.success('权限更新成功');
    dialogVisible.value = false;
  } catch (e) {
    ElMessage.error(e.message || '更新失败');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadRoles();
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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
}

.permission-dialog {
  max-height: 400px;
  overflow-y: auto;
}

.permission-group {
  margin-bottom: 16px;
}

.permission-group h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #1f2937;
  font-weight: 600;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 4px;
}

.permission-group .el-checkbox {
  margin-right: 16px;
  margin-bottom: 8px;
}
</style>
