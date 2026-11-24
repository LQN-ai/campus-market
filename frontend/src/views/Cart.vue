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
              <el-input-number v-model="row.quantity" :min="1" :max="row.stock" size="small" @change="changeQty(row)" />
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
  await createOrder({ cartItemIds: selected.value.map(i => i.cartItemId) })
  ElMessage.success('下单成功')
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
