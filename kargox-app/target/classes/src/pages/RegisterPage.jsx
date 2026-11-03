import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../lib/auth'
import Button from '../components/Button'
import Input from '../components/Input'

const HOME = { CUSTOMER: '/book', DRIVER: '/driver', ADMIN: '/admin' }

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState('CUSTOMER')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const user = await register({ fullName, email, password, phoneNumber, role })
      navigate(HOME[user.role] ?? '/')
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Could not create your account.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-asphalt-950 flex items-center justify-center px-4 py-10">
      <div className="w-full max-w-sm">
        <div className="flex items-center gap-2 justify-center mb-8">
          <span className="w-9 h-9 rounded-md bg-signal flex items-center justify-center text-asphalt-950 font-display font-bold">K</span>
          <div>
            <p className="font-display font-semibold text-paper leading-none">KargoX</p>
            <p className="text-[11px] text-paper/50 leading-none mt-1">Move Anything. Anywhere. Anytime.</p>
          </div>
        </div>

        <form onSubmit={submit} className="bg-card rounded-lg p-6 space-y-4 shadow-xl">
          <h1 className="font-display text-lg font-semibold text-ink">Create your account</h1>

          <div className="grid grid-cols-2 gap-2">
            <button type="button" onClick={() => setRole('CUSTOMER')}
              className={`py-2 rounded-md text-sm font-medium border transition-colors ${role === 'CUSTOMER' ? 'bg-signal border-signal text-asphalt-950' : 'border-asphalt-600/25 text-ink-muted'}`}>
              I need deliveries
            </button>
            <button type="button" onClick={() => setRole('DRIVER')}
              className={`py-2 rounded-md text-sm font-medium border transition-colors ${role === 'DRIVER' ? 'bg-signal border-signal text-asphalt-950' : 'border-asphalt-600/25 text-ink-muted'}`}>
              I want to drive
            </button>
          </div>

          <Input label="Full name" required value={fullName} onChange={(e) => setFullName(e.target.value)} />
          <Input label="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          <Input label="Phone number" value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} placeholder="+27 82 000 0000" />
          <Input label="Password" type="password" required minLength={8} value={password} onChange={(e) => setPassword(e.target.value)} hint="At least 8 characters" />
          {error && <p className="text-sm text-alert-dark">{error}</p>}
          <Button type="submit" fullWidth disabled={loading}>{loading ? 'Creating account…' : 'Create account'}</Button>
          <p className="text-center text-sm text-ink-muted">
            Already have an account? <Link to="/login" className="text-signal-dark font-medium">Sign in</Link>
          </p>
        </form>
      </div>
    </div>
  )
}
