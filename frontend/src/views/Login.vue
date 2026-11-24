<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>登录</h2>
      <el-form :model="form" @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>
      <div class="tips">
        还没有账号？<router-link to="/register">去注册</router-link>
      </div>
      <div class="tips">管理员默认账号：admin / admin123</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()
const form = ref({ username: '', password: '' })
const loading = ref(false)

const submit = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const data = await login(form.value)
    auth.setLogin(data.token, data.user)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.auth-card {
  width: 380px;
  padding: 10px 20px;
}
.auth-card h2 {
  text-align: center;
  margin-bottom: 20px;
}
.tips {
  text-align: center;
  margin-top: 12px;
  font-size: 13px;
  color: #999;
}
.tips a {
  color: #409eff;
}
</style>
