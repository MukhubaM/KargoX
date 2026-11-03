import axios from 'axios'

const client = axios.create({ baseURL: '/api/v1' })

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('kargox_access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('kargox_access_token')
      localStorage.removeItem('kargox_user')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  },
)

const unwrap = (p) => p.then((r) => r.data.data)

export const api = {
  auth: {
    login: (email, password) =>
      unwrap(client.post('/auth/login', { email, password })),
    register: (payload) =>
      unwrap(client.post('/auth/register', payload)),
  },
  bookings: {
    estimateFare: (payload) => unwrap(client.post('/bookings/estimate-fare', payload)),
    create: (payload) => unwrap(client.post('/bookings', payload)),
    getById: (id) => unwrap(client.get(`/bookings/${id}`)),
    my: (page = 0, size = 10) => unwrap(client.get('/bookings/my', { params: { page, size } })),
    updateStatus: (id, status, verificationCode) =>
      unwrap(client.patch(`/bookings/${id}/status`, { status, verificationCode })),
    cancel: (id, reason) => unwrap(client.post(`/bookings/${id}/cancel`, { reason })),
  },
  drivers: {
    onboard: (licenseNumber) => unwrap(client.post('/drivers/onboard', { licenseNumber })),
    me: () => unwrap(client.get('/drivers/me')),
    updateStatus: (status) => unwrap(client.patch('/drivers/me/status', { status })),
    updateLocation: (latitude, longitude) => unwrap(client.post('/drivers/me/location', { latitude, longitude })),
    acceptBooking: (bookingId) => unwrap(client.post(`/drivers/bookings/${bookingId}/accept`)),
  },
  vehicles: {
    register: (payload) => unwrap(client.post('/vehicles', payload)),
    my: () => unwrap(client.get('/vehicles/my')),
  },
  notifications: {
    my: (page = 0, size = 20) => unwrap(client.get('/notifications', { params: { page, size } })),
    unreadCount: () => unwrap(client.get('/notifications/unread-count')),
  },
  admin: {
    users: (page = 0, size = 20, role, search) =>
      unwrap(client.get('/admin/users', { params: { page, size, role, search } })),
    setUserActive: (id, active) => unwrap(client.patch(`/admin/users/${id}/status`, { active })),
    bookings: (page = 0, size = 20, status) =>
      unwrap(client.get('/admin/bookings', { params: { page, size, status } })),
    dashboard: () => unwrap(client.get('/admin/dashboard')),
    refund: (bookingId, reason) => unwrap(client.post(`/admin/bookings/${bookingId}/refund`, { reason })),
  },
  uploads: {
    image: (file, type = 'goods') => {
      const form = new FormData()
      form.append('file', file)
      return unwrap(client.post(`/uploads?type=${type}`, form, {
        headers: { 'Content-Type': 'multipart/form-data' },
      }))
    },
  },
  users: {
    me: () => unwrap(client.get('/users/me')),
    updateMe: (payload) => unwrap(client.patch('/users/me', payload)),
  },
  geocode: {
    search: (query) => unwrap(client.get('/geocode/search', { params: { q: query } })),
    reverse: (lat, lon) => unwrap(client.get('/geocode/reverse', { params: { lat, lon } })),
  },
  ratings: {
    rate: (bookingId, score, comment) =>
      unwrap(client.post(`/bookings/${bookingId}/ratings`, { score, comment })),
  },
}

export default client
