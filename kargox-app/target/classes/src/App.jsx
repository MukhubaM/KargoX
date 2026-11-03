import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './lib/auth'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import NewBookingPage from './pages/customer/NewBookingPage'
import BookingHistoryPage from './pages/customer/BookingHistoryPage'
import TrackBookingPage from './pages/customer/TrackBookingPage'
import DriverDashboardPage from './pages/driver/DriverDashboardPage'
import DriverVehiclesPage from './pages/driver/DriverVehiclesPage'
import DriverHistoryPage from './pages/driver/DriverHistoryPage'
import AdminDashboardPage from './pages/admin/AdminDashboardPage'
import AdminUsersPage from './pages/admin/AdminUsersPage'
import AdminBookingsPage from './pages/admin/AdminBookingsPage'
import ProfilePage from './pages/ProfilePage'

const HOME = { CUSTOMER: '/book', DRIVER: '/driver', ADMIN: '/admin' }

function RootRedirect() {
  const { user, loading } = useAuth()
  if (loading) return null
  if (!user) return <Navigate to="/login" replace />
  return <Navigate to={HOME[user.role] ?? '/login'} replace />
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<RootRedirect />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route path="/book" element={<ProtectedRoute roles={['CUSTOMER']}><NewBookingPage /></ProtectedRoute>} />
          <Route path="/history" element={<ProtectedRoute roles={['CUSTOMER']}><BookingHistoryPage /></ProtectedRoute>} />
          <Route path="/track/:id" element={<ProtectedRoute roles={['CUSTOMER']}><TrackBookingPage /></ProtectedRoute>} />

          <Route path="/driver" element={<ProtectedRoute roles={['DRIVER']}><DriverDashboardPage /></ProtectedRoute>} />
          <Route path="/driver/vehicles" element={<ProtectedRoute roles={['DRIVER']}><DriverVehiclesPage /></ProtectedRoute>} />
          <Route path="/driver/history" element={<ProtectedRoute roles={['DRIVER']}><DriverHistoryPage /></ProtectedRoute>} />

          <Route path="/admin" element={<ProtectedRoute roles={['ADMIN']}><AdminDashboardPage /></ProtectedRoute>} />
          <Route path="/admin/users" element={<ProtectedRoute roles={['ADMIN']}><AdminUsersPage /></ProtectedRoute>} />
          <Route path="/admin/bookings" element={<ProtectedRoute roles={['ADMIN']}><AdminBookingsPage /></ProtectedRoute>} />

          <Route path="/profile" element={<ProtectedRoute roles={['CUSTOMER', 'DRIVER', 'ADMIN']}><ProfilePage /></ProtectedRoute>} />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
