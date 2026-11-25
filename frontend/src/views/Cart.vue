<template>
  <div>
    <NavBar />
    <div class="container">
      <h2>购物车</h2>
      <el-empty v-if="items.length === 0" description="购物车为空" />
      <template v-else>
        <el-table :data="items" @selection-change="onSelect" style="margin-top: 20px">
          <el-table-column type="selection" width="50" />
          <el-table-column label="商品" min-width="220">
            <template #default="{ row }">
              <div class="product-cell">
                <img :src="row.image || placeholder" class="thumb" />
                <div>
                  <div>{{ row.productName }}</div>
                  <div class="shop">{{ row.shopName }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="数量" width="180">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" :max="Math.max(row.stock, 1)" size="small" @change="changeQty(row)" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="footer">
          <span>已选 {{ selected.length }} 件</span>
          <el-button type="danger" size="large" @click="checkout">一键下单</el-button>
        </div>
      </template>
    </div>
  </div>

  <el-dialog v-model="tradeDialog" title="约定线下交易时间和地点" width="420px" append-to-body>
    <el-form label-width="80px">
      <el-form-item label="交易时间">
        <el-input v-model="tradeForm.tradeTime" placeholder="如：12月3日下午2点" />
      </el-form-item>
      <el-form-item label="交易地点">
        <el-input v-model="tradeForm.tradeLocation" placeholder="如：图书馆门口" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="tradeDialog = false">取消</el-button>
      <el-button type="primary" @click="confirmCheckout">确定下单</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { getCart, updateCart, deleteCart, createOrder } from '../api'

const router = useRouter()
const items = ref([])
const selected = ref([])
const tradeDialog = ref(false)
const tradeForm = ref({ tradeTime: '', tradeLocation: '' })
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="60"%3E%3Crect width="60" height="60" fill="%23eee"/%3E%3C/svg%3E'

const load = async () => {
  items.value = await getCart()
}

const onSelect = (rows) => {
  selected.value = rows
}

const changeQty = async (row) => {
  await updateCart(row.cartItemId, row.quantity)
}

const remove = async (row) => {
  await deleteCart(row.cartItemId)
  ElMessage.success('已删除')
  load()
}

const checkout = async () => {
  if (selected.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  tradeForm.value = { tradeTime: '', tradeLocation: '' }
  tradeDialog.value = true
}

const confirmCheckout = async () => {
  await createOrder({ cartItemIds: selected.value.map(i => i.cartItemId), ...tradeForm.value })
  ElMessage.success('下单成功')
  tradeDialog.value = false
  router.push('/orders')
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}
.product-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}
.thumb {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}
.shop {
  color: #999;
  font-size: 12px;
}
.footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}
</style>
