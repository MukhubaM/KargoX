import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../lib/auth'

const NAV = {
  CUSTOMER: [
    { to: '/book', label: 'Book a delivery' },
    { to: '/history', label: 'History' },
    { to: '/profile', label: 'Profile' },
  ],
  DRIVER: [
    { to: '/driver', label: 'Dashboard' },
    { to: '/driver/vehicles', label: 'Vehicles' },
    { to: '/driver/history', label: 'History' },
    { to: '/profile', label: 'Profile' },
  ],
  ADMIN: [
    { to: '/admin', label: 'Dashboard' },
    { to: '/admin/users', label: 'Users' },
    { to: '/admin/bookings', label: 'Bookings' },
    { to: '/profile', label: 'Profile' },
  ],
}

export default function AppShell({ title, children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const items = user ? NAV[user.role] : []

  return (
    <div className="min-h-screen flex bg-paper">
      <aside className="w-60 bg-asphalt-950 text-paper flex flex-col flex-shrink-0">
        <div className="px-5 py-6 flex items-center gap-2">
          <span className="w-7 h-7 rounded-md bg-signal flex items-center justify-center text-asphalt-950 font-display font-bold text-sm">K</span>
          <div>
            <p className="font-display font-semibold text-sm leading-none">KargoX</p>
            <p className="text-[10px] text-paper/50 leading-none mt-1">Move Anything. Anywhere. Anytime.</p>
          </div>
        </div>
        <nav className="flex-1 px-3 space-y-1">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end
              className={({ isActive }) =>
                `block px-3 py-2.5 rounded-md text-sm transition-colors ${
                  isActive ? 'bg-signal text-asphalt-950 font-medium' : 'text-paper/70 hover:bg-asphalt-800 hover:text-paper'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="p-3 border-t border-paper/10">
          <div className="px-3 py-2">
            <p className="text-sm font-medium truncate">{user?.fullName}</p>
            <p className="text-[11px] text-paper/50 uppercase tracking-wider">{user?.role}</p>
          </div>
          <button
            onClick={() => {
              logout()
              navigate('/login')
            }}
            className="w-full text-left px-3 py-2 rounded-md text-sm text-paper/70 hover:bg-asphalt-800 hover:text-paper transition-colors"
          >
            Sign out
          </button>
        </div>
      </aside>

      <main className="flex-1 min-w-0">
        <header className="border-b border-asphalt-600/15 bg-card px-8 py-5">
          <h1 className="font-display text-xl font-semibold text-ink">{title}</h1>
        </header>
        <div className="p-8 max-w-5xl">{children}</div>
      </main>
    </div>
  )
}
