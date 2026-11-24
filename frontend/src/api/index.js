import request from './request'

// ===== 认证 =====
export const getCaptcha = () => request.get('/api/auth/captcha')
export const register = (data) => request.post('/api/auth/register', data)
export const merchantRegister = (data) => request.post('/api/auth/merchant/register', data)
export const login = (data) => request.post('/api/auth/login', data)
export const getMe = () => request.get('/api/auth/me')

// ===== 商品 =====
export const searchProducts = (params) => request.get('/api/products', { params })
export const getProduct = (id) => request.get(`/api/products/${id}`)
export const createProduct = (data) => request.post('/api/products', data)
export const updateProduct = (id, data) => request.put(`/api/products/${id}`, data)
export const offlineProduct = (id) => request.post(`/api/products/${id}/offline`)
export const myProducts = () => request.get('/api/products/mine')
export const merchantProfile = () => request.get('/api/merchant/profile')

// ===== 购物车 =====
export const getCart = () => request.get('/api/cart')
export const addToCart = (productId, quantity) =>
  request.post('/api/cart', null, { params: { productId, quantity } })
export const updateCart = (id, quantity) =>
  request.put(`/api/cart/${id}`, null, { params: { quantity } })
export const deleteCart = (id) => request.delete(`/api/cart/${id}`)

// ===== 订单 =====
export const createOrder = (data) => request.post('/api/orders', data)
export const myOrders = () => request.get('/api/orders')
export const merchantOrders = () => request.get('/api/orders/merchant')
export const getOrder = (id) => request.get(`/api/orders/${id}`)
export const confirmOrder = (id) => request.post(`/api/orders/${id}/confirm`)
export const requestReturn = (id) => request.post(`/api/orders/${id}/return`)
export const auditReturn = (id, approve) =>
  request.post(`/api/orders/${id}/return/audit`, { approve })

// ===== 钱包 =====
export const getWallet = () => request.get('/api/wallet')
export const walletTransactions = () => request.get('/api/wallet/transactions')
export const redeemPoints = (points) => request.post('/api/wallet/redeem', { points })

// ===== 评价 =====
export const reviewProduct = (productId, orderId, data) =>
  request.post(`/api/products/${productId}/reviews?orderId=${orderId}`, data)
export const productReviews = (productId) => request.get(`/api/products/${productId}/reviews`)
export const reviewMerchant = (merchantId, orderId, data) =>
  request.post(`/api/merchants/${merchantId}/reviews?orderId=${orderId}`, data)
export const merchantReviews = (merchantId) => request.get(`/api/merchants/${merchantId}/reviews`)
export const reviewBuyer = (buyerId, orderId, data) =>
  request.post(`/api/buyers/${buyerId}/reviews?orderId=${orderId}`, data)

// ===== 文件上传 =====
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ===== 管理端 =====
export const adminUsers = (params) => request.get('/api/admin/users', { params })
export const auditUser = (id, approve) => request.post(`/api/admin/users/${id}/audit`, { approve })
export const updateUser = (id, data) => request.put(`/api/admin/users/${id}`, data)
export const deleteUser = (id) => request.delete(`/api/admin/users/${id}`)
export const adminRecharge = (id, amount) => request.post(`/api/admin/users/${id}/recharge`, { amount })
export const adminMerchants = () => request.get('/api/admin/merchants')
export const auditMerchant = (id, approve) => request.post(`/api/admin/merchants/${id}/audit`, { approve })
export const setMerchantLevel = (id, level) => request.put(`/api/admin/merchants/${id}/level`, { level })
export const banMerchant = (id, days) => request.post(`/api/admin/merchants/${id}/ban`, { days })
export const unbanMerchant = (id) => request.post(`/api/admin/merchants/${id}/unban`)
export const offlineMerchant = (id) => request.post(`/api/admin/merchants/${id}/offline`)
export const adminProducts = (params) => request.get('/api/admin/products', { params })
export const auditProduct = (id, approve) => request.post(`/api/admin/products/${id}/audit`, { approve })
