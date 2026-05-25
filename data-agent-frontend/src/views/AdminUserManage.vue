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
            <h1 class="content-title">用户管理</h1>
            <p class="content-subtitle">管理系统用户账号、状态和角色分配</p>
          </div>
          <el-button type="primary" :icon="Plus" @click="showCreateDialog" size="large">
            新建用户
          </el-button>
        </div>

        <div class="section-card">
          <el-table :data="users" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="角色" min-width="150">
              <template #default="{ row }">
                <template v-if="row.roles && row.roles.length > 0">
                  <el-tag v-for="role in row.roles" :key="role.id" size="small" type="info" style="margin-right: 4px;">
                    {{ role.roleName }}
                  </el-tag>
                </template>
                <span v-else style="color: #c0c4cc;">无角色</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" />
            <el-table-column label="操作" width="280">
              <template #default="{ row }">
                <el-button
                  :type="row.status === 1 ? 'danger' : 'success'"
                  size="small"
                  @click="toggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button size="small" @click="showResetPassword(row)">重置密码</el-button>
                <el-button size="small" @click="showAssignRole(row)">分配角色</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </main>
    </div>

    <!-- 创建用户弹窗 -->
    <el-dialog v-model="createDialogVisible" title="新建用户" width="420px">
      <div class="dialog-form">
        <div class="form-item">
          <label>用户名</label>
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </div>
        <div class="form-item">
          <label>密码</label>
          <el-input v-model="createForm.password" type="password" placeholder="请输入密码" show-password />
        </div>
        <div class="form-item">
          <label>分配角色</label>
          <el-select v-model="createForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </div>
      </div>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetDialogVisible" title="重置密码" width="420px">
      <div class="dialog-form">
        <div class="form-item">
          <label>新密码</label>
          <el-input v-model="resetForm.password" type="password" placeholder="请输入新密码" show-password />
        </div>
      </div>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleResetPassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="420px">
      <div class="dialog-form">
        <div class="form-item">
          <label>选择角色</label>
          <el-select v-model="roleForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </div>
      </div>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAssignRole">确定</el-button>
      </template>
    </el-dialog>
  </BaseLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import BaseLayout from '@/layouts/BaseLayout.vue';
import adminService from '@/services/admin';

const loading = ref(false);
const submitting = ref(false);
const users = ref([]);
const roles = ref([]);

const createDialogVisible = ref(false);
const createForm = reactive({ username: '', password: '', roleIds: [] });

const resetDialogVisible = ref(false);
const resetForm = reactive({ userId: null, password: '' });

const roleDialogVisible = ref(false);
const roleForm = reactive({ userId: null, roleIds: [] });

const loadUsers = async () => {
  loading.value = true;
  try {
    users.value = await adminService.listUsers();
  } catch (e) {
    ElMessage.error(e.message || '获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

const loadRoles = async () => {
  try {
    roles.value = await adminService.listRoles();
  } catch (e) {
    console.error('获取角色列表失败', e);
  }
};

const showCreateDialog = () => {
  createForm.username = '';
  createForm.password = '';
  createForm.roleIds = [];
  createDialogVisible.value = true;
};

const handleCreate = async () => {
  if (!createForm.username.trim() || !createForm.password.trim()) {
    ElMessage.warning('请填写用户名和密码');
    return;
  }
  submitting.value = true;
  try {
    await adminService.createUser({
      username: createForm.username,
      password: createForm.password,
      roleIds: createForm.roleIds,
    });
    ElMessage.success('创建用户成功');
    createDialogVisible.value = false;
    loadUsers();
  } catch (e) {
    ElMessage.error(e.message || '创建失败');
  } finally {
    submitting.value = false;
  }
};

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1;
  try {
    await adminService.updateUserStatus(row.id, newStatus);
    ElMessage.success(newStatus === 1 ? '用户已启用' : '用户已禁用');
    loadUsers();
  } catch (e) {
    ElMessage.error(e.message || '操作失败');
  }
};

const showResetPassword = (row) => {
  resetForm.userId = row.id;
  resetForm.password = '';
  resetDialogVisible.value = true;
};

const handleResetPassword = async () => {
  if (!resetForm.password.trim()) {
    ElMessage.warning('请输入新密码');
    return;
  }
  submitting.value = true;
  try {
    await adminService.resetUserPassword(resetForm.userId, resetForm.password);
    ElMessage.success('密码重置成功');
    resetDialogVisible.value = false;
  } catch (e) {
    ElMessage.error(e.message || '重置失败');
  } finally {
    submitting.value = false;
  }
};

const showAssignRole = (row) => {
  roleForm.userId = row.id;
  roleForm.roleIds = [];
  roleDialogVisible.value = true;
};

const handleAssignRole = async () => {
  submitting.value = true;
  try {
    await adminService.assignUserRoles(roleForm.userId, roleForm.roleIds);
    ElMessage.success('角色分配成功');
    roleDialogVisible.value = false;
  } catch (e) {
    ElMessage.error(e.message || '分配失败');
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadUsers();
  loadRoles();
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

.dialog-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dialog-form .form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 14px;
  color: #374151;
}
</style>
