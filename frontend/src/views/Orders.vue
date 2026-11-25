<template>
  <div>
    <NavBar />
    <div class="container">
      <el-tabs v-model="tab">
        <el-tab-pane label="我的订单" name="buyer">
          <div class="filter-bar">
            <el-radio-group v-model="orderFilter" size="small">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="PENDING">待收货</el-radio-button>
              <el-radio-button label="CONFIRMED">已确认</el-radio-button>
              <el-radio-button label="RETURN_REQUESTED">在途退货</el-radio-button>
              <el-radio-button label="RETURNED">已退货</el-radio-button>
            </el-radio-group>
          </div>
          <el-empty v-if="filteredBuyerOrders.length === 0" description="暂无订单" />
          <div v-else>
            <el-card v-for="o in filteredBuyerOrders" :key="o.order.id" class="order-card">
              <div class="order-head">
                <span>订单号：{{ o.order.orderNo }}</span>
                <span class="shop">{{ o.shopName }}</span>
                <el-tag>{{ statusLabel(o.order.status) }}</el-tag>
              </div>
              <div v-if="o.order.tradeTime || o.order.tradeLocation" class="trade-info">
                线下交易：{{ o.order.tradeTime || '时间待定' }} · {{ o.order.tradeLocation || '地点待定' }}
              </div>
              <div v-for="it in o.items" :key="it.productId" class="order-item">
                <img :src="it.image || placeholder" class="thumb" />
                <span class="pname">{{ it.productName }}</span>
                <span>¥{{ it.price }} × {{ it.quantity }}</span>
                <el-button v-if="['CONFIRMED', 'AUTO_CONFIRMED'].includes(o.order.status)" link type="primary" size="small" @click="openProductReview(o, it)">评价</el-button>
              </div>
              <div class="order-foot">
                <span>合计：¥{{ o.order.totalAmount }}</span>
                <div class="btns">
                  <el-button v-if="o.order.status === 'PENDING'" type="primary" size="small" @click="confirm(o.order.id)">确认收货</el-button>
                  <el-button v-if="['CONFIRMED', 'AUTO_CONFIRMED'].includes(o.order.status)" type="warning" size="small" @click="doReturn(o.order.id)">申请退货</el-button>
                  <el-button v-if="['CONFIRMED', 'AUTO_CONFIRMED'].includes(o.order.status)" size="small" @click="openMerchantReview(o)">评价商家</el-button>
                </div>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="auth.role === 'MERCHANT'" label="销售订单" name="seller">
          <el-empty v-if="sellerOrders.length === 0" description="暂无订单" />
          <div v-else>
            <el-card v-for="o in sellerOrders" :key="o.order.id" class="order-card">
              <div class="order-head">
                <span>订单号：{{ o.order.orderNo }}</span>
                <span class="shop">买家：{{ o.buyerName || '未知' }}</span>
                <el-tag type="warning">买家好评率 {{ o.buyerRating ?? '暂无' }}</el-tag>
                <el-tag>{{ statusLabel(o.order.status) }}</el-tag>
              </div>
              <div v-if="o.order.tradeTime || o.order.tradeLocation" class="trade-info">
                线下交易：{{ o.order.tradeTime || '时间待定' }} · {{ o.order.tradeLocation || '地点待定' }}
              </div>
              <div v-for="it in o.items" :key="it.productId" class="order-item">
                <img :src="it.image || placeholder" class="thumb" />
                <span class="pname">{{ it.productName }}</span>
                <span>¥{{ it.price }} × {{ it.quantity }}</span>
              </div>
              <div class="order-foot">
                <span>合计：¥{{ o.order.totalAmount }}</span>
                <div class="btns">
                  <template v-if="o.order.status === 'RETURN_REQUESTED'">
                    <el-button type="success" size="small" @click="audit(o.order.id, true)">同意退货</el-button>
                    <el-button type="danger" size="small" @click="audit(o.order.id, false)">拒绝退货</el-button>
                  </template>
                  <el-button v-if="['CONFIRMED', 'AUTO_CONFIRMED', 'RETURNED'].includes(o.order.status)" size="small" @click="openBuyerReview(o)">评价买家</el-button>
                  <el-button v-if="!o.buyerBlacklisted && ['CONFIRMED', 'AUTO_CONFIRMED', 'RETURNED'].includes(o.order.status)" link type="danger" size="small" @click="blacklistBuyer(o)">拉黑买家</el-button>
                  <el-button v-if="o.buyerBlacklisted" link type="info" size="small" @click="unblacklistBuyer(o)">解除拉黑</el-button>
                </div>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="reviewDialog" :title="reviewTitle" width="400px">
      <el-rate v-model="reviewForm.rating" />
      <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="写下您的评价" style="margin-top: 12px" />
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { myOrders, merchantOrders, confirmOrder, requestReturn, auditReturn, reviewProduct, reviewMerchant, reviewBuyer, merchantBlacklist, merchantUnblacklist } from '../api'
import { useAuthStore } from '../store/auth'

const auth = useAuthStore()
const tab = ref('buyer')
const buyerOrders = ref([])
const sellerOrders = ref([])
const orderFilter = ref('')
const filteredBuyerOrders = computed(() => {
  if (!orderFilter.value) return buyerOrders.value
  if (orderFilter.value === 'CONFIRMED') {
    return buyerOrders.value.filter(o => ['CONFIRMED', 'AUTO_CONFIRMED'].includes(o.order.status))
  }
  return buyerOrders.value.filter(o => o.order.status === orderFilter.value)
})
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="60"%3E%3Crect width="60" height="60" fill="%23eee"/%3E%3C/svg%3E'

const reviewDialog = ref(false)
const reviewTarget = ref(null)
const reviewType = ref('product')
const reviewProductId = ref(null)
const reviewProductName = ref('')
const reviewTitle = ref('')
const reviewForm = ref({ rating: 5, content: '' })

const statusLabel = (s) => ({
  PENDING: '待收货',
  CONFIRMED: '已确认收货',
  AUTO_CONFIRMED: '已自动确认',
  RETURN_REQUESTED: '退货申请中',
  RETURNED: '已退货'
}[s] || s)

const load = async () => {
  buyerOrders.value = await myOrders()
  if (auth.role === 'MERCHANT') {
    sellerOrders.value = await merchantOrders()
  }
}

const confirm = async (id) => {
  await confirmOrder(id)
  ElMessage.success('已确认收货，货款已结算给商家')
  load()
}

const doReturn = async (id) => {
  await requestReturn(id)
  ElMessage.success('退货申请已提交，等待商家审核')
  load()
}

const audit = async (id, approve) => {
  await auditReturn(id, approve)
  ElMessage.success(approve ? '已同意退货' : '已拒绝退货')
  load()
}

const blacklistBuyer = async (order) => {
  await ElMessageBox.confirm(`确认拉黑买家「${order.buyerName || '未知'}」？拉黑后该买家将无法购买你的商品。`, '提示', { type: 'warning' })
  await merchantBlacklist(order.order.userId)
  ElMessage.success('已拉黑该买家')
  load()
}

const unblacklistBuyer = async (order) => {
  await merchantUnblacklist(order.order.userId)
  ElMessage.success('已解除拉黑')
  load()
}

const openProductReview = (order, item) => {
  reviewTarget.value = order
  reviewType.value = 'product'
  reviewProductId.value = item.productId
  reviewProductName.value = item.productName
  reviewTitle.value = `评价商品：${item.productName}`
  reviewForm.value = { rating: 5, content: '' }
  reviewDialog.value = true
}

const openMerchantReview = (order) => {
  reviewTarget.value = order
  reviewType.value = 'merchant'
  reviewTitle.value = '评价商家'
  reviewForm.value = { rating: 5, content: '' }
  reviewDialog.value = true
}

const openBuyerReview = (order) => {
  reviewTarget.value = order
  reviewType.value = 'buyer'
  reviewTitle.value = '评价买家'
  reviewForm.value = { rating: 5, content: '' }
  reviewDialog.value = true
}

const submitReview = async () => {
  const order = reviewTarget.value
  if (reviewType.value === 'product') {
    await reviewProduct(reviewProductId.value, order.order.id, reviewForm.value)
  } else if (reviewType.value === 'merchant') {
    await reviewMerchant(order.order.merchantId, order.order.id, reviewForm.value)
  } else {
    await reviewBuyer(order.order.userId, order.order.id, reviewForm.value)
  }
  ElMessage.success('评价成功')
  reviewDialog.value = false
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}
.filter-bar {
  margin-bottom: 16px;
}
.order-card {
  margin-bottom: 16px;
}
.trade-info {
  background: #fef0f0;
  color: #e6a23c;
  font-size: 13px;
  padding: 8px 12px;
  border-radius: 4px;
  margin-top: 10px;
}
.order-head {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}
.shop {
  color: #409eff;
}
.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
}
.thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}
.pname {
  flex: 1;
}
.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
}
.btns {
  display: flex;
  gap: 8px;
}
</style>
