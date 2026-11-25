<template>
  <div>
    <NavBar />
    <div class="container">
      <div class="wallet-cards">
        <div class="wallet-card">
          <div class="label">钱包余额</div>
          <div class="value">¥{{ wallet.balance }}</div>
        </div>
        <div class="wallet-card">
          <div class="label">积分</div>
          <div class="value">{{ wallet.points }}</div>
          <div class="sub">100积分 = 1元</div>
        </div>
        <div class="wallet-card">
          <div class="label">我的好评率</div>
          <div class="value">{{ user.rating ?? '暂无' }}</div>
          <div class="sub">由商家评价得出</div>
        </div>
      </div>

      <el-card class="redeem-card">
        <span>积分抵扣（每100积分抵扣1元）：</span>
        <el-input-number v-model="redeemPointsVal" :min="100" :step="100" />
        <el-button type="primary" @click="redeem">立即抵扣</el-button>
      </el-card>

      <h3 style="margin: 24px 0 12px">资金流水</h3>
      <el-table :data="transactions">
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span :class="row.amount > 0 ? 'plus' : 'minus'">
              {{ row.amount > 0 ? '+' : '' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { getWallet, walletTransactions, redeemPoints, getMe } from '../api'

const wallet = ref({ balance: 0, points: 0 })
const user = ref({})
const transactions = ref([])
const redeemPointsVal = ref(100)

const typeLabel = (t) => ({
  RECHARGE: '充值',
  PAY: '支付',
  REFUND: '退款',
  RECEIVE: '收款',
  FEE: '手续费'
}[t] || t)

const typeTag = (t) => ({
  RECHARGE: 'success',
  PAY: 'danger',
  REFUND: 'warning',
  RECEIVE: 'success',
  FEE: 'info'
}[t] || 'info')

const load = async () => {
  wallet.value = await getWallet()
  user.value = await getMe()
  transactions.value = await walletTransactions()
}

const redeem = async () => {
  await redeemPoints(redeemPointsVal.value)
  ElMessage.success('抵扣成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
.wallet-cards {
  display: flex;
  gap: 20px;
}
.wallet-card {
  flex: 1;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 8px;
  padding: 24px;
}
.label {
  font-size: 14px;
  opacity: 0.9;
}
.value {
  font-size: 32px;
  font-weight: bold;
  margin: 8px 0;
}
.sub {
  font-size: 12px;
  opacity: 0.8;
}
.redeem-card {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.plus {
  color: #67c23a;
}
.minus {
  color: #f56c6c;
}
</style>
