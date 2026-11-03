import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../lib/auth'
import Button from '../components/Button'
import Input from '../components/Input'

const HOME = { CUSTOMER: '/book', DRIVER: '/driver', ADMIN: '/admin' }

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await login(email, password)
      navigate(HOME[user.role] ?? '/')
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Could not sign in. Check your email and password.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-asphalt-950 flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="flex items-center gap-2 justify-center mb-8">
          <span className="w-9 h-9 rounded-md bg-signal flex items-center justify-center text-asphalt-950 font-display font-bold">K</span>
          <div>
            <p className="font-display font-semibold text-paper leading-none">KargoX</p>
            <p className="text-[11px] text-paper/50 leading-none mt-1">Move Anything. Anywhere. Anytime.</p>
          </div>
        </div>

        <form onSubmit={submit} className="bg-card rounded-lg p-6 space-y-4 shadow-xl">
          <h1 className="font-display text-lg font-semibold text-ink">Sign in</h1>
          <Input label="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" />
          <Input label="Password" type="password" required value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />
          {error && <p className="text-sm text-alert-dark">{error}</p>}
          <Button type="submit" fullWidth disabled={loading}>{loading ? 'Signing in…' : 'Sign in'}</Button>
          <p className="text-center text-sm text-ink-muted">
            No account? <Link to="/register" className="text-signal-dark font-medium">Create one</Link>
          </p>
        </form>
      </div>
    </div>
  )
}
