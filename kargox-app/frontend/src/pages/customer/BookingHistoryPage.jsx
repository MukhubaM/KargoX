import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import AppShell from '../../components/AppShell'
import WaybillCard from '../../components/WaybillCard'
import { api } from '../../lib/api'

export default function BookingHistoryPage() {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.bookings.my(0, 20).then((r) => setBookings(r.content)).finally(() => setLoading(false))
  }, [])

  if (loading) return <AppShell title="Booking history"><p className="text-ink-muted text-sm">Loading…</p></AppShell>

  return (
    <AppShell title="Booking history">
      {bookings.length === 0 ? (
        <div className="bg-card rounded-lg border border-asphalt-600/15 p-10 text-center">
          <p className="text-ink font-medium mb-1">No deliveries yet</p>
          <p className="text-sm text-ink-muted mb-4">Once you request a pickup, it'll show up here.</p>
          <Link to="/book" className="text-signal-dark text-sm font-medium">Book your first delivery →</Link>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map((b) => (
            <Link key={b.id} to={`/track/${b.id}`} className="block">
              <WaybillCard booking={b} />
            </Link>
          ))}
        </div>
      )}
    </AppShell>
  )
}
