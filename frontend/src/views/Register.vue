<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>注册</h2>
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="银行账号">
          <el-input v-model="form.bankAccount" maxlength="16" placeholder="16位数字" />
        </el-form-item>

        <el-form-item label="商家注册">
          <el-switch v-model="isMerchant" />
        </el-form-item>

        <template v-if="isMerchant">
          <el-form-item label="店铺名">
            <el-input v-model="form.shopName" />
          </el-form-item>
          <el-form-item label="营业执照">
            <el-upload :http-request="(opt) => doUpload(opt, 'businessLicense')" :show-file-list="false">
              <img v-if="form.businessLicense" :src="form.businessLicense" class="upload-img" />
              <el-button v-else>上传营业执照</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="身份证">
            <el-upload :http-request="(opt) => doUpload(opt, 'idCard')" :show-file-list="false">
              <img v-if="form.idCard" :src="form.idCard" class="upload-img" />
              <el-button v-else>上传身份证</el-button>
            </el-upload>
          </el-form-item>
        </template>

        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="验证码" />
            <img v-if="captchaImage" :src="captchaImage" class="captcha-img" @click="loadCaptcha" title="点击刷新" />
          </div>
        </el-form-item>

        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">
          注册
        </el-button>
      </el-form>
      <div class="tips">已有账号？<router-link to="/login">去登录</router-link></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, merchantRegister, getCaptcha, uploadFile } from '../api'

const router = useRouter()
const isMerchant = ref(false)
const captchaImage = ref('')
const captchaId = ref('')
const loading = ref(false)

const form = reactive({
  username: '', password: '', realName: '', phone: '', email: '', city: '',
  gender: 1, bankAccount: '', shopName: '', businessLicense: '', idCard: '',
  captchaCode: ''
})

const loadCaptcha = async () => {
  const data = await getCaptcha()
  captchaId.value = data.captchaId
  captchaImage.value = data.image
}

const doUpload = async (opt, field) => {
  const data = await uploadFile(opt.file)
  form[field] = data.url
  ElMessage.success('上传成功')
}

const submit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (!form.captchaCode) {
    ElMessage.warning('请输入验证码')
    return
  }
  const payload = { ...form, captchaId: captchaId.value }
  loading.value = true
  try {
    if (isMerchant.value) {
      await merchantRegister(payload)
    } else {
      await register(payload)
    }
    ElMessage.success('注册成功，请等待管理员审核')
    router.push('/login')
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
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
  width: 480px;
  padding: 10px 20px;
}
.auth-card h2 {
  text-align: center;
  margin-bottom: 20px;
}
.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.captcha-img {
  height: 40px;
  width: 120px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
.upload-img {
  height: 60px;
  border-radius: 4px;
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
