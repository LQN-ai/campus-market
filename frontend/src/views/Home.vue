<template>
  <div>
    <NavBar />
    <div class="container">
      <el-carousel v-if="recommendList.length" height="260px" class="carousel" :interval="4000">
        <el-carousel-item v-for="r in recommendList" :key="r.id" @click="$router.push(`/product/${r.id}`)">
          <img :src="r.images && r.images[0] ? r.images[0] : placeholder" class="carousel-img" />
          <div class="carousel-info">
            <span class="carousel-name">{{ r.name }}</span>
            <span class="carousel-price">¥{{ r.price }}</span>
          </div>
        </el-carousel-item>
      </el-carousel>

      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索商品名称" size="large" clearable @keyup.enter="load(1)">
          <template #append>
            <el-button :icon="'Search'" @click="load(1)" />
          </template>
        </el-input>
        <el-select v-model="sortBy" size="large" style="width: 160px" @change="load(1)">
          <el-option label="默认排序" value="" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
          <el-option label="销量优先" value="sales" />
          <el-option label="好评率优先" value="rating" />
        </el-select>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <el-empty v-else-if="list.length === 0" description="暂无商品" />

      <div v-else class="grid">
        <div v-for="p in list" :key="p.id" class="card" @click="$router.push(`/product/${p.id}`)">
          <div class="card-img">
            <img :src="p.images && p.images[0] ? p.images[0] : placeholder" />
          </div>
          <div class="card-body">
            <div class="name">{{ p.name }}</div>
            <div class="price-row">
              <span class="price">¥{{ p.price }}</span>
              <span class="original">¥{{ p.originalPrice }}</span>
            </div>
            <div class="meta">
              <span>{{ p.shopName }}</span>
              <span>销量 {{ p.sales }}</span>
              <span>好评 {{ p.rating }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="load"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NavBar from '../components/NavBar.vue'
import { searchProducts, getRecommendProducts } from '../api'

const keyword = ref('')
const sortBy = ref('')
const list = ref([])
const recommendList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const loading = ref(false)
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="200"%3E%3Crect width="200" height="200" fill="%23eee"/%3E%3C/svg%3E'

const load = async (p = 1) => {
  page.value = p
  loading.value = true
  try {
    const data = await searchProducts({
      keyword: keyword.value || undefined,
      sortBy: sortBy.value || undefined,
      page: p,
      size: size.value
    })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const loadRecommend = async () => {
  recommendList.value = await getRecommendProducts()
}

onMounted(() => {
  load(1)
  loadRecommend()
})
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.carousel {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}
.carousel-img {
  width: 100%;
  height: 260px;
  object-fit: cover;
}
.carousel-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 20px;
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
  color: #fff;
  display: flex;
  align-items: center;
  gap: 16px;
}
.carousel-name {
  font-size: 18px;
  font-weight: bold;
}
.carousel-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}
.card-img img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}
.card-body {
  padding: 12px;
}
.name {
  font-size: 15px;
  height: 40px;
  overflow: hidden;
}
.price-row {
  margin: 8px 0;
}
.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}
.original {
  color: #999;
  font-size: 13px;
  text-decoration: line-through;
  margin-left: 8px;
}
.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}
.loading {
  text-align: center;
  padding: 60px;
  color: #999;
}
.pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
