<template>
  <div class="container">
    <div class="dynamic-background"></div>
    <div class="content-box">
      <h2>登录</h2>
      <div class="login-methods">
        <button @click="selectLoginMethod('username')">账号密码登录</button>
        <button @click="selectLoginMethod('email')">邮箱密码登录</button>
      </div>
      <div v-if="loginMethod">
        <input
            v-model="loginInfo.userId"
            :placeholder="loginMethod === 'username' ? '账号' : '邮箱'"
        />
        <input v-model="loginInfo.password" type="password" placeholder="密码" />
        <div class="role-selection">
          <label><input type="radio" value="user" v-model="loginInfo.role" />用户</label>
          <label><input type="radio" value="admin" v-model="loginInfo.role" />管理员</label>
        </div>
        <button @click="login">登录</button>
      </div>
      <p v-if="errorMessage">{{ errorMessage }}</p>
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { userAPI } from '@/utils/api';

export default {
  data() {
    return {
      loginMethod: '',
      loginInfo: {
        userId: '',
        password: '',
        role: 'user'
      },
      errorMessage: ''
    };
  },
  methods: {
    selectLoginMethod(method) {
      this.loginMethod = method;
      this.loginInfo.userId = '';
      this.loginInfo.password = '';
      this.loginInfo.role = 'user';
      this.errorMessage = '';
    },
    async login() {
      if (!this.loginInfo.userId || !this.loginInfo.password) {
        this.errorMessage = '请输入账号和密码';
        return;
      }

      this.errorMessage = '';

      try {
        const response = await userAPI.login({
          userId: this.loginInfo.userId,
          password: this.loginInfo.password,
          role: this.loginInfo.role
        });

        if (response.data.success === true) {
          const { user, token } = response.data.data;

          localStorage.setItem('user', JSON.stringify({
            userId: user.userId,
            name: user.name,
            role: user.role,
            id: user.Id
          }));

          if (token) {
            localStorage.setItem('token', token);
          }

          // 根据角色分流：管理员进后台，普通用户进主页
          if (user.role === 'admin') {
            this.$router.push({ name: 'admin' });
          } else {
            this.$router.push({ name: 'home' });
          }
        } else {
          this.errorMessage = response.data.message || '用户名或密码错误';
        }
      } catch (error) {
        if (error.response?.data?.message) {
          this.errorMessage = error.response.data.message;
        } else {
          this.errorMessage = '网络错误，请重试';
        }
      }
    }
  }
};
</script>

<style scoped>
.container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.dynamic-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(238, 174, 202, 1) 0%, rgba(148, 187, 233, 1) 100%);
  animation: gradientAnimation 15s ease infinite;
  z-index: -1;
}

@keyframes gradientAnimation {
  0%, 100% { filter: hue-rotate(0deg); }
  50% { filter: hue-rotate(360deg); }
}

.content-box {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  max-width: 400px;
  width: 100%;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.login-methods button {
  margin: 10px;
  padding: 10px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.login-methods button:hover { background-color: #2563eb; }

input {
  display: block;
  width: calc(100% - 20px);
  margin: 10px 0;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
}

.role-selection {
  margin: 10px 0;
  text-align: left;
}

.role-selection label {
  display: inline-block;
  margin-right: 10px;
}

button {
  margin-top: 10px;
  padding: 10px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover { background-color: #2563eb; }

.login-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #6b7280;
}

.link {
  color: #3b82f6;
  text-decoration: none;
  margin-left: 4px;
  font-weight: 500;
}

.link:hover { text-decoration: underline; }

p {
  color: #ef4444;
  margin-top: 10px;
  text-align: center;
}
</style>