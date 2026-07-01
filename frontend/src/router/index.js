import { createRouter, createWebHistory } from 'vue-router';
import Edite from '../views/Edite.vue';
import LoginForm from '../views/LoginForm.vue';
import RegisterForm from '../views/RegisterForm.vue';
import Home from '../views/Home.vue';
import Admin from '../views/Admin.vue';

const routes = [
  { path: '/',         name: 'login',    component: LoginForm    },
  { path: '/edite',    name: 'edite',    component: Edite        },
  { path: '/register', name: 'register', component: RegisterForm },
  { path: '/home',     name: 'home',     component: Home         },
  { path: '/admin',    name: 'admin',    component: Admin        },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

router.beforeEach((to, from) => {
  const userStr = localStorage.getItem('user');
  let user = null;
  try {
    user = userStr ? JSON.parse(userStr) : null;
  } catch (e) {
    localStorage.removeItem('user');
  }

  const requiresAuth = ['home', 'edite', 'admin'];

  if (requiresAuth.includes(to.name) && !user) {
    // 未登录，跳回登录页
    return { name: 'login' };
  } else if (to.name === 'admin' && user?.role !== 'admin') {
    // 非管理员访问 /admin，跳到普通主页
    return { name: 'home' };
  } else if (to.name === 'login' && user) {
    // 已登录时访问登录页，按角色分流
    return { name: user.role === 'admin' ? 'admin' : 'home' };
  }
});

export default router;