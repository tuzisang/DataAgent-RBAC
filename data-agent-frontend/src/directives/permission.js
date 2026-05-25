/*
 * Copyright 2024-2026 the original author or authors.
 */

function hasPermission(perm) {
  try {
    const raw = localStorage.getItem('dataagent_auth');
    if (!raw) return false;
    const parsed = JSON.parse(raw);
    return (parsed.permissions || []).includes(perm);
  } catch {
    return false;
  }
}

function checkPermission(el, binding) {
  const perm = binding.value;
  if (!perm) return;
  if (!hasPermission(perm)) {
    el.style.display = 'none';
  } else {
    el.style.display = '';
  }
}

export default {
  mounted(el, binding) {
    checkPermission(el, binding);
  },
  updated(el, binding) {
    checkPermission(el, binding);
  },
};
