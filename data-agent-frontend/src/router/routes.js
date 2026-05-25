/*
 * Copyright 2024-2026 the original author or authors.
 */

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true, title: '注册' },
  },
  {
    path: '/',
    redirect: '/agents',
  },
  {
    path: '/agents',
    name: 'AgentList',
    component: () => import('@/views/AgentList.vue'),
    meta: { title: '智能体列表', module: 'agent' },
  },
  {
    path: '/agent/create',
    name: 'AgentCreate',
    component: () => import('@/views/AgentCreate.vue'),
    meta: { title: '创建智能体', module: 'agent' },
  },
  {
    path: '/agent/:id',
    name: 'AgentDetail',
    component: () => import('@/views/AgentDetail.vue'),
    meta: { title: '智能体详情', module: 'agent' },
  },
  {
    path: '/agent/:id/run',
    name: 'AgentRun',
    component: () => import('@/views/AgentRun.vue'),
    meta: { title: '运行智能体', module: 'agent' },
  },
  {
    path: '/model-config',
    name: 'ModelConfig',
    component: () => import('@/views/ModelConfig.vue'),
    meta: { title: '模型配置', module: 'config' },
  },
  {
    path: '/admin/users',
    name: 'AdminUserManage',
    component: () => import('@/views/AdminUserManage.vue'),
    meta: { title: '用户管理', module: 'admin', requireRole: 'super-admin' },
  },
  {
    path: '/admin/roles',
    name: 'AdminRoleManage',
    component: () => import('@/views/AdminRoleManage.vue'),
    meta: { title: '角色管理', module: 'admin', requireRole: 'super-admin' },
  },
  {
    path: '/admin/permissions',
    name: 'AdminPermissionView',
    component: () => import('@/views/AdminPermissionView.vue'),
    meta: { title: '权限查看', module: 'admin', requireRole: 'super-admin' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面未找到', module: 'error' },
  },
];

export default routes;
