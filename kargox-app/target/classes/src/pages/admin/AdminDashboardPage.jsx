import { useEffect, useState } from 'react'
import AppShell from '../../components/AppShell'
import { api } from '../../lib/api'

function StatCard({ label, value }) {
  return (
    <div className="bg-card rounded-lg border border-asphalt-600/15 p-5">
      <p className="text-xs uppercase tracking-wider text-ink-faint">{label}</p>
      <p className="font-display text-2xl font-semibold text-ink mt-1">{value}</p>
    </div>
  )
}

export default function AdminDashboardPage() {
  const [stats, setStats] = useState(null)
  useEffect(() => { api.admin.dashboard().then(setStats) }, [])

  if (!stats) return <AppShell title="Admin dashboard"><p className="text-ink-muted text-sm">Loading…</p></AppShell>

  return (
    <AppShell title="Admin dashboard">
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <StatCard label="Total users" value={stats.totalUsers} />
        <StatCard label="Customers" value={stats.totalCustomers} />
        <StatCard label="Drivers" value={stats.totalDrivers} />
        <StatCard label="Online now" value={stats.activeDrivers} />
        <StatCard label="Total bookings" value={stats.totalBookings} />
        <StatCard label="In progress" value={stats.bookingsInProgress} />
        <StatCard label="Completed" value={stats.completedBookings} />
        <StatCard label="Revenue" value={`R${stats.totalRevenue.toFixed(2)}`} />
      </div>

      <div className="bg-card rounded-lg border border-asphalt-600/15 p-6">
        <p className="text-sm font-semibold text-ink mb-4">Bookings by status</p>
        <div className="space-y-2">
          {Object.entries(stats.bookingsByStatus).filter(([, v]) => v > 0).map(([status, count]) => (
            <div key={status} className="flex items-center gap-3">
              <span className="text-xs font-mono w-40 text-ink-muted">{status}</span>
              <div className="flex-1 bg-paper-dim rounded-full h-2 overflow-hidden">
                <div className="bg-signal h-full" style={{ width: `${Math.min(100, (count / Math.max(1, stats.totalBookings)) * 100)}%` }} />
              </div>
              <span className="text-sm font-medium w-8 text-right">{count}</span>
            </div>
          ))}
        </div>
      </div>
    </AppShell>
  )
}
