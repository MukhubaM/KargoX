import { useEffect, useState } from 'react'
import AppShell from '../../components/AppShell'
import Button from '../../components/Button'
import WaybillCard from '../../components/WaybillCard'
import { api } from '../../lib/api'

export default function AdminBookingsPage() {
  const [bookings, setBookings] = useState([])
  const [busyId, setBusyId] = useState(null)
  const [message, setMessage] = useState('')

  const load = () => api.admin.bookings(0, 30).then((r) => setBookings(r.content))
  useEffect(() => { load() }, [])

  const refund = async (booking) => {
    const reason = window.prompt(`Refund booking ${booking.bookingReference}. Reason?`)
    if (!reason) return
    setBusyId(booking.id)
    setMessage('')
    try {
      await api.admin.refund(booking.id, reason)
      setMessage(`Refunded ${booking.bookingReference}.`)
      load()
    } catch (err) {
      setMessage(err?.response?.data?.message ?? 'Could not process the refund.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <AppShell title="All bookings">
      {message && <p className="text-sm text-ink-muted mb-4">{message}</p>}
      <div className="space-y-4 max-w-2xl">
        {bookings.map((b) => (
          <WaybillCard
            key={b.id}
            booking={b}
            footer={
              b.status === 'COMPLETED' ? (
                <Button variant="ghost" onClick={() => refund(b)} disabled={busyId === b.id} className="!py-1.5 !px-3 text-xs">
                  {busyId === b.id ? 'Processing…' : 'Issue refund'}
                </Button>
              ) : null
            }
          />
        ))}
      </div>
    </AppShell>
  )
}
