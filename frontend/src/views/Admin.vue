<template>
  <div>
    <NavBar />
    <div class="container">
      <el-tabs v-model="tab">
        <el-tab-pane label="用户管理" name="users">
          <el-table :data="users">
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="role" label="角色" width="100">
              <template #default="{ row }">
                <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'MERCHANT' ? 'warning' : 'info'">
                  {{ row.role }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">{{ userStatus(row.status) }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="300">
              <template #default="{ row }">
                <template v-if="row.status === 0">
                  <el-button link type="success" @click="auditUser(row, true)">通过</el-button>
                  <el-button link type="danger" @click="auditUser(row, false)">拒绝</el-button>
                </template>
                <el-button link type="primary" @click="openRecharge(row)">充值</el-button>
                <el-button link type="primary" @click="openEditUser(row)">修改</el-button>
                <el-button link type="danger" @click="removeUser(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="商家管理" name="merchants">
          <el-table :data="merchants">
            <el-table-column prop="shopName" label="店铺名" width="140" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="level" label="等级" width="70" />
            <el-table-column prop="totalSales" label="累计交易额" width="110" />
            <el-table-column label="营业执照" width="90">
              <template #default="{ row }">
                <el-image v-if="row.businessLicense" :src="row.businessLicense" fit="cover" style="width: 60px; height: 60px; cursor: pointer" @click="preview(row.businessLicense)" />
                <span v-else>无</span>
              </template>
            </el-table-column>
            <el-table-column label="身份证" width="90">
              <template #default="{ row }">
                <el-image v-if="row.idCard" :src="row.idCard" fit="cover" style="width: 60px; height: 60px; cursor: pointer" @click="preview(row.idCard)" />
                <span v-else>无</span>
              </template>
            </el-table-column>
            <el-table-column label="用户状态" width="100">
              <template #default="{ row }">{{ userStatus(row.userStatus) }}</template>
            </el-table-column>
            <el-table-column label="店铺状态" width="100">
              <template #default="{ row }">
                <el-tag :type="shopStatusTag(row.shopStatus)">{{ shopStatusLabel(row.shopStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="280">
              <template #default="{ row }">
                <template v-if="row.userStatus === 0">
                  <el-button link type="success" @click="auditMerchant(row, true)">通过</el-button>
                  <el-button link type="danger" @click="auditMerchant(row, false)">拒绝</el-button>
                </template>
                <el-button link type="primary" @click="openLevel(row)">设置等级</el-button>
                <template v-if="row.shopStatus === 2">
                  <el-button link type="success" @click="unban(row)">解除封禁</el-button>
                </template>
                <template v-else>
                  <el-button link type="warning" @click="ban(row)">封禁</el-button>
                  <el-button link type="danger" @click="offline(row)">下架店铺</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="商品审核" name="products">
          <el-table :data="products">
            <el-table-column label="图片" width="80">
              <template #default="{ row }">
                <el-image v-if="row.images && row.images.length" :src="row.images[0]" fit="cover" style="width: 60px; height: 60px; cursor: pointer" @click="preview(row.images[0])" />
                <span v-else>无</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="名称" min-width="140" />
            <el-table-column prop="shopName" label="店铺" width="120" />
            <el-table-column prop="price" label="价格" width="90" />
            <el-table-column prop="conditionLevel" label="成色" width="80" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">{{ row.status }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template #default="{ row }">
                <template v-if="row.status === 'PENDING'">
                  <el-button link type="success" @click="auditProduct(row, true)">通过</el-button>
                  <el-button link type="danger" @click="auditProduct(row, false)">拒绝</el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="editUserDialog" title="修改用户" width="420px">
      <el-form label-width="70px">
        <el-form-item label="姓名"><el-input v-model="editUserForm.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editUserForm.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editUserForm.email" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="editUserForm.city" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editUserForm.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editUserDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEditUser">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rechargeDialog" title="充值" width="320px">
      <el-input-number v-model="rechargeAmount" :min="1" :precision="2" style="width: 100%" />
      <template #footer>
        <el-button @click="rechargeDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRecharge">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="levelDialog" title="设置商家等级" width="320px">
      <el-select v-model="levelVal" style="width: 100%">
        <el-option v-for="i in 5" :key="i" :label="`等级 ${i}（费率 ${rate(i)}）`" :value="i" />
      </el-select>
      <template #footer>
        <el-button @click="levelDialog = false">取消</el-button>
        <el-button type="primary" @click="submitLevel">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="查看图片" width="600px" append-to-body>
      <div style="text-align: center">
        <img :src="previewUrl" style="max-width: 100%; max-height: 70vh" />
      </div>
      <template #footer>
        <el-button type="primary" @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import {
  adminUsers, auditUser as apiAuditUser, updateUser, deleteUser, adminRecharge, adminMerchants,
  auditMerchant as apiAuditMerchant, setMerchantLevel, banMerchant, unbanMerchant, offlineMerchant,
  adminProducts, auditProduct as apiAuditProduct
} from '../api'

const tab = ref('users')
const users = ref([])
const merchants = ref([])
const products = ref([])

const rechargeDialog = ref(false)
const rechargeAmount = ref(100)
const rechargeTarget = ref(null)

const editUserDialog = ref(false)
const editUserTarget = ref(null)
const editUserForm = ref({ realName: '', phone: '', email: '', city: '', gender: 1 })

const levelDialog = ref(false)
const levelVal = ref(1)
const levelTarget = ref(null)

const previewVisible = ref(false)
const previewUrl = ref('')
const preview = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

const userStatus = (s) => ({ 0: '待审核', 1: '正常', 2: '已拒绝', 3: '已封禁' }[s] || s)
const shopStatusLabel = (s) => ({ 0: '正常', 1: '已下架', 2: '已封禁' }[s] || s)
const shopStatusTag = (s) => ({ 0: 'success', 1: 'info', 2: 'danger' }[s] || 'info')
const rate = (i) => ({ 1: '0.1%', 2: '0.2%', 3: '0.5%', 4: '0.75%', 5: '1%' }[i])

const load = async () => {
  const u = await adminUsers({ page: 1, size: 100 })
  users.value = u.records
  merchants.value = await adminMerchants()
  const p = await adminProducts({ page: 1, size: 100, status: 'PENDING' })
  products.value = p.records
}

const auditUser = async (row, approve) => {
  await apiAuditUser(row.id, approve)
  ElMessage.success('操作成功')
  load()
}

const auditMerchant = async (row, approve) => {
  await apiAuditMerchant(row.userId, approve)
  ElMessage.success('操作成功')
  load()
}

const auditProduct = async (row, approve) => {
  await apiAuditProduct(row.id, approve)
  ElMessage.success('操作成功')
  load()
}

const openRecharge = (row) => {
  rechargeTarget.value = row
  rechargeAmount.value = 100
  rechargeDialog.value = true
}

const submitRecharge = async () => {
  await adminRecharge(rechargeTarget.value.id, rechargeAmount.value)
  ElMessage.success('充值成功')
  rechargeDialog.value = false
}

const openEditUser = (row) => {
  editUserTarget.value = row
  editUserForm.value = {
    realName: row.realName || '',
    phone: row.phone || '',
    email: row.email || '',
    city: row.city || '',
    gender: row.gender ?? 1
  }
  editUserDialog.value = true
}

const submitEditUser = async () => {
  await updateUser(editUserTarget.value.id, editUserForm.value)
  ElMessage.success('修改成功')
  editUserDialog.value = false
  load()
}

const removeUser = async (row) => {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('已删除')
  load()
}

const openLevel = (row) => {
  levelTarget.value = row
  levelVal.value = row.level
  levelDialog.value = true
}

const submitLevel = async () => {
  await setMerchantLevel(levelTarget.value.userId, levelVal.value)
  ElMessage.success('等级已更新')
  levelDialog.value = false
  load()
}

const ban = async (row) => {
  await ElMessageBox.confirm('确认封禁该商家1天（封禁期间禁止发布新商品）？', '提示', { type: 'warning' })
  await banMerchant(row.userId, 1)
  ElMessage.success('已封禁1天')
  load()
}

const unban = async (row) => {
  await unbanMerchant(row.userId)
  ElMessage.success('已解除封禁')
  load()
}

const offline = async (row) => {
  await ElMessageBox.confirm('确认下架该店铺全部商品？', '提示', { type: 'warning' })
  await offlineMerchant(row.userId)
  ElMessage.success('店铺已下架')
  load()
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}
</style>
