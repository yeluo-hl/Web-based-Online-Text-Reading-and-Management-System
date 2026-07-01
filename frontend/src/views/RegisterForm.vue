<template>
  <div class="register-container">
    <div class="dynamic-background"></div>
    <div class="register-box">
      <h2>注册</h2>
      <input v-model="email" placeholder="邮箱" />
      <input v-model="name" placeholder="姓名" />
      <input v-model="userId" placeholder="账号" />
      <input v-model="password" type="password" placeholder="密码" />
      <button @click="register">注册</button>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      <p v-if="successMessage" class="success">{{ successMessage }}</p>
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/" class="link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { userAPI } from '@/utils/api';

export default {
  data() {
    return {
      email: '',
      name: '',
      userId: '',       
      password: '',
      errorMessage: '',
      successMessage: ''
    };
  },
  methods: {
    async register() {
      if (!this.userId || !this.password) {
        this.errorMessage = '请输入账号和密码';
        return;
      }

      if (!this.email) {
        this.errorMessage = '请输入邮箱';
        return;
      }

      if (!this.name) {
        this.errorMessage = '请输入姓名';
        return;
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.email)) {
        this.errorMessage = '请输入有效的邮箱地址';
        return;
      }

      if (this.password.length < 6) {
        this.errorMessage = '密码长度至少6位';
        return;
      }

      this.errorMessage = '';
      this.successMessage = '';

      try {
        const response = await userAPI.register({
          email: this.email,
          name: this.name,
          userId: this.userId, 
          password: this.password,
          role: 'user'
        });

        if (response && response.data) {
          this.successMessage = response.data.message || '注册成功！正在跳转到登录页面...';
          setTimeout(() => {
            try {
              this.$router.push('/');
            } catch (navError) {
              console.error('导航失败:', navError);
              this.errorMessage = '跳转失败，请手动点击登录链接';
            }
          }, 2000);
        } else {
          this.errorMessage = '注册失败，请重试';
        }
      } catch (error) {
        if (error.response && error.response.data && error.response.data.message) {
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
.register-container {
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

.register-box {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.9);
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  max-width: 400px;
  width: 100%;
  margin: 0 20px;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #374151;
  font-size: 24px;
}

input {
  display: block;
  width: calc(100% - 20px);
  margin: 10px 0;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  font-size: 14px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

input::placeholder { color: #9ca3af; }

button {
  width: 100%;
  margin-top: 16px;
  padding: 12px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.2s ease;
}

button:hover {
  background-color: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

button:active { transform: translateY(0); }

.register-footer {
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

.error {
  color: #ef4444;
  margin-top: 8px;
  font-size: 14px;
  text-align: center;
}

.success {
  color: #10b981;
  margin-top: 8px;
  font-size: 14px;
  text-align: center;
}

@media (max-width: 480px) {
  .register-box { padding: 20px; margin: 0 16px; }
  h2 { font-size: 20px; }
  input { padding: 10px; }
  button { padding: 10px; font-size: 14px; }
}
</style>