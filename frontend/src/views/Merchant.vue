<template>
  <div>
    <NavBar />
    <div class="container">
      <div class="shop-card" v-if="profile">
        <div class="shop-info">
          <span class="shop-name">{{ profile.shopName }}</span>
          <el-tag type="warning">等级 {{ profile.level }}</el-tag>
          <span>好评率：{{ profile.rating }}</span>
          <span>累计交易额：¥{{ profile.totalSales }}</span>
        </div>
      </div>
      <el-tabs v-model="tab">
        <el-tab-pane label="我的商品" name="list">
          <div class="filter-bar">
            <el-radio-group v-model="statusFilter" size="small">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="PENDING">待审核</el-radio-button>
              <el-radio-button label="PUBLISHED">在售</el-radio-button>
              <el-radio-button label="LOCKED">已售罄</el-radio-button>
              <el-radio-button label="SOLD">已出售</el-radio-button>
              <el-radio-button label="OFFLINE">已下架</el-radio-button>
            </el-radio-group>
          </div>
          <el-table :data="filteredProducts">
            <el-table-column label="图片" width="90">
              <template #default="{ row }">
                <img :src="row.images && row.images[0] || placeholder" class="thumb" />
              </template>
            </el-table-column>
            <el-table-column prop="name" label="名称" min-width="140" />
            <el-table-column prop="price" label="价格" width="90" />
            <el-table-column prop="stock" label="库存" width="70" />
            <el-table-column prop="sales" label="销量" width="70" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="{ row }">
                <el-button link type="primary" @click="edit(row)">编辑</el-button>
                <el-button link type="info" @click="viewReviews(row)">评价</el-button>
                <el-button v-if="row.status === 'PUBLISHED'" link type="danger" @click="offline(row)">下架</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="发布商品" name="publish">
          <el-form :model="form" label-width="90px" style="max-width: 560px">
            <el-form-item label="商品名称">
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item label="类别">
              <el-input v-model="form.category" placeholder="如：书籍、数码、生活用品" />
            </el-form-item>
            <el-form-item label="原价">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="售价">
              <el-input-number v-model="form.price" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="尺寸">
              <el-input v-model="form.size" />
            </el-form-item>
            <el-form-item label="新旧程度">
              <el-select v-model="form.conditionLevel">
                <el-option label="全新" value="全新" />
                <el-option label="九成新" value="九成新" />
                <el-option label="八成新" value="八成新" />
                <el-option label="七成新" value="七成新" />
              </el-select>
            </el-form-item>
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="1" />
            </el-form-item>
            <el-form-item label="是否议价">
              <el-switch v-model="form.negotiable" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="使用说明">
              <el-input v-model="form.description" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="商品图片">
              <el-upload
                list-type="picture-card"
                :http-request="doUpload"
                :file-list="fileList"
                :on-remove="onRemove"
                multiple
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-button type="primary" size="large" @click="submit">
              {{ editingId ? '保存修改' : '发布商品' }}
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="reviewsVisible" title="商品评价" width="560px" append-to-body>
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
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { myProducts, createProduct, updateProduct, offlineProduct, uploadFile, merchantProfile, productReviews } from '../api'

const tab = ref('list')
const products = ref([])
const profile = ref(null)
const statusFilter = ref('')
const editingId = ref(null)
const fileList = ref([])
const reviewsVisible = ref(false)
const reviews = ref([])
const filteredProducts = computed(() =>
  statusFilter.value ? products.value.filter(p => p.status === statusFilter.value) : products.value
)
const placeholder = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="60"%3E%3Crect width="60" height="60" fill="%23eee"/%3E%3C/svg%3E'

const form = reactive({
  name: '', category: '', originalPrice: 0, price: 0, size: '',
  conditionLevel: '全新', stock: 1, negotiable: 0, description: '', images: []
})

const statusLabel = (s) => ({
  PENDING: '待审核', PUBLISHED: '在售', OFFLINE: '已下架', LOCKED: '已售罄', SOLD: '已售出'
}[s] || s)

const statusTag = (s) => ({
  PENDING: 'warning', PUBLISHED: 'success', OFFLINE: 'info', LOCKED: 'info', SOLD: 'info'
}[s] || 'info')

const load = async () => {
  products.value = await myProducts()
  profile.value = await merchantProfile()
}

const viewReviews = async (row) => {
  reviews.value = await productReviews(row.id)
  reviewsVisible.value = true
}

const doUpload = async (opt) => {
  const data = await uploadFile(opt.file)
  form.images.push(data.url)
  ElMessage.success('上传成功')
}

const onRemove = (file) => {
  const idx = form.images.indexOf(file.url || file.response?.url)
  if (idx >= 0) form.images.splice(idx, 1)
}

const resetForm = () => {
  Object.assign(form, {
    name: '', category: '', originalPrice: 0, price: 0, size: '',
    conditionLevel: '全新', stock: 1, negotiable: 0, description: '', images: []
  })
  fileList.value = []
  editingId.value = null
}

const edit = (row) => {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name, category: row.category, originalPrice: Number(row.originalPrice),
    price: Number(row.price), size: row.size, conditionLevel: row.conditionLevel,
    stock: row.stock, negotiable: row.negotiable, description: row.description,
    images: [...(row.images || [])]
  })
  fileList.value = (row.images || []).map((u, i) => ({ name: `img${i}`, url: u }))
  tab.value = 'publish'
}

const offline = async (row) => {
  await offlineProduct(row.id)
  ElMessage.success('已下架')
  load()
}

const submit = async () => {
  if (!form.name || !form.price) {
    ElMessage.warning('请填写商品名称和价格')
    return
  }
  const payload = { ...form }
  if (editingId.value) {
    await updateProduct(editingId.value, payload)
    ElMessage.success('已提交修改，等待审核')
  } else {
    await createProduct(payload)
    ElMessage.success('发布成功，等待审核')
  }
  resetForm()
  tab.value = 'list'
  load()
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}
.filter-bar {
  margin-bottom: 16px;
}
.shop-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.shop-info {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #666;
}
.shop-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}
.reviews {
  max-height: 400px;
  overflow-y: auto;
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
