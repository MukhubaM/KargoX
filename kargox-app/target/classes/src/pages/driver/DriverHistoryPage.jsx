import { useEffect, useState } from 'react'
import AppShell from '../../components/AppShell'
import WaybillCard from '../../components/WaybillCard'
import { api } from '../../lib/api'

export default function DriverHistoryPage() {
  const [bookings, setBookings] = useState([])
  useEffect(() => { api.bookings.my(0, 20).then((r) => setBookings(r.content)) }, [])

  return (
    <AppShell title="Delivery history">
      {bookings.length === 0 ? (
        <p className="text-sm text-ink-muted">No completed deliveries yet.</p>
      ) : (
        <div className="space-y-4 max-w-2xl">
          {bookings.map((b) => <WaybillCard key={b.id} booking={b} />)}
        </div>
      )}
    </AppShell>
  )
}
