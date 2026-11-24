<template>
  <div>
    <NavBar />
    <div class="container" v-if="product">
      <el-page-header class="back" content="商品详情" @back="router.back()" />
      <div class="detail">
        <div class="images">
          <el-carousel v-if="product.images && product.images.length" height="400px">
            <el-carousel-item v-for="img in product.images" :key="img">
              <img :src="img" class="main-img" />
            </el-carousel-item>
          </el-carousel>
          <img v-else :src="placeholder" class="main-img" />
        </div>
        <div class="info">
          <h2>{{ product.name }}</h2>
          <div class="price-box">
            <span class="price">¥{{ product.price }}</span>
            <span class="original">原价 ¥{{ product.originalPrice }}</span>
          </div>
          <div class="info-row">
            <span>店铺：{{ product.shopName }}</span>
            <span>商家好评率：{{ product.merchantRating ?? '暂无' }}</span>
          </div>
          <div class="info-row">
            <span>销量：{{ product.sales }}</span>
            <span>商品好评率：{{ product.rating }}</span>
          </div>
          <div class="info-row">
            <span>新旧程度：{{ product.conditionLevel || '未填写' }}</span>
            <span>库存：{{ product.stock }}</span>
            <span>是否议价：{{ product.negotiable === 1 ? '是' : '否' }}</span>
          </div>
          <div class="desc">
            <h4>使用说明</h4>
            <p>{{ product.description || '暂无说明' }}</p>
          </div>
          <div class="actions">
            <el-button type="warning" size="large" @click="addCart">加入购物车</el-button>
            <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
          </div>
        </div>
      </div>

      <el-divider content-position="left">商品评价</el-divider>
      <el-empty v-if="reviews.length === 0" description="暂无评价" />
      <div v-else class="reviews">
        <div v-for="r in reviews" :key="r.id" class="review">
          <div class="review-head">
            <span class="user">{{ r.userName || '匿名用户' }}</span>
            <el-rate :model-value="r.rating" disabled />
            <span class="time">{{ r.createdAt }}</span>
          </div>
          <div class="review-content">{{ r.content }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { getProduct, productReviews, addToCart, createOrder } from '../api'
import { useAuthStore } from '../store/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const product = ref(null)
const reviews = ref([])
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="400"%3E%3Crect width="400" height="400" fill="%23eee"/%3E%3C/svg%3E'

const load = async () => {
  product.value = await getProduct(route.params.id)
  reviews.value = await productReviews(route.params.id)
}

const addCart = async () => {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await addToCart(product.value.id, 1)
  ElMessage.success('已加入购物车')
}

const buyNow = async () => {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  await createOrder({ productId: product.value.id, quantity: 1 })
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
.back {
  margin-bottom: 16px;
}
.detail {
  display: flex;
  gap: 30px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}
.images {
  flex: 1;
}
.main-img {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 8px;
}
.info {
  flex: 1;
}
.price-box {
  background: #fef0f0;
  padding: 16px;
  margin: 16px 0;
  border-radius: 8px;
}
.price {
  color: #f56c6c;
  font-size: 28px;
  font-weight: bold;
}
.original {
  color: #999;
  text-decoration: line-through;
  margin-left: 12px;
}
.info-row {
  display: flex;
  gap: 24px;
  color: #666;
  margin: 8px 0;
}
.desc {
  margin: 16px 0;
  color: #666;
}
.actions {
  margin-top: 24px;
}
.reviews {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}
.review {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user {
  font-weight: bold;
}
.time {
  color: #999;
  font-size: 12px;
}
.review-content {
  margin-top: 6px;
  color: #555;
}
</style>
