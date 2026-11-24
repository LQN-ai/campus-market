<template>
  <div class="navbar">
    <div class="navbar-inner">
      <div class="logo" @click="$router.push('/')">校园二手市场</div>
      <div class="links">
        <router-link to="/">首页</router-link>
        <router-link to="/cart">购物车</router-link>
        <router-link to="/orders">我的订单</router-link>
        <router-link to="/wallet">钱包</router-link>
        <router-link v-if="auth.role === 'MERCHANT'" to="/merchant">商家中心</router-link>
        <router-link v-if="auth.role === 'ADMIN'" to="/admin">后台管理</router-link>
      </div>
      <div class="user">
        <template v-if="auth.isLoggedIn">
          <span class="username">{{ auth.user?.realName || auth.user?.username }}</span>
          <el-button link type="primary" @click="logout">退出</el-button>
        </template>
        <template v-else>
          <el-button link type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button link type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '../store/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

const logout = () => {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.navbar {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}
.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 0 20px;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
  white-space: nowrap;
}
.links {
  display: flex;
  gap: 20px;
  flex: 1;
}
.links a {
  color: #333;
  text-decoration: none;
  font-size: 15px;
}
.links a.router-link-active {
  color: #409eff;
}
.user {
  display: flex;
  align-items: center;
  gap: 8px;
}
.username {
  font-size: 14px;
  color: #666;
}
</style>
