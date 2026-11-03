import { createContext, useContext, useEffect, useState } from 'react'
import { api } from './api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const stored = localStorage.getItem('kargox_user')
    if (stored) setUser(JSON.parse(stored))
    setLoading(false)
  }, [])

  const persist = (res) => {
    const current = { userId: res.userId, fullName: res.fullName, email: res.email, role: res.role }
    localStorage.setItem('kargox_access_token', res.accessToken)
    localStorage.setItem('kargox_refresh_token', res.refreshToken)
    localStorage.setItem('kargox_user', JSON.stringify(current))
    setUser(current)
    return current
  }

  const login = async (email, password) => persist(await api.auth.login(email, password))

  const register = async (payload) => persist(await api.auth.register(payload))

  const logout = () => {
    localStorage.removeItem('kargox_access_token')
    localStorage.removeItem('kargox_refresh_token')
    localStorage.removeItem('kargox_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
