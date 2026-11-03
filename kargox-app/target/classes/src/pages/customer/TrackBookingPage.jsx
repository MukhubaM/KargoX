
import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import AppShell from '../../components/AppShell'
import WaybillCard from '../../components/WaybillCard'
import Timeline from '../../components/Timeline'
import Button from '../../components/Button'
import { api } from '../../lib/api'

const CANCELLABLE = new Set(['REQUESTED', 'SEARCHING_DRIVER', 'DRIVER_ASSIGNED', 'DRIVER_EN_ROUTE', 'DRIVER_ARRIVED', 'NO_DRIVER_FOUND'])

export default function TrackBookingPage() {
  const { id } = useParams()
  const [booking, setBooking] = useState(null)
  const [loadError, setLoadError] = useState('')
  const [rated, setRated] = useState(false)
  const [score, setScore] = useState(5)

  const refresh = () => {
    if (!id) return
    api.bookings.getById(parseInt(id))
      .then((b) => { setBooking(b); setLoadError('') })
      .catch((err) => setLoadError(err?.response?.data?.message ?? 'Could not load this booking.'))
  }

  useEffect(() => {
    refresh()
    const interval = setInterval(refresh, 4000)
    return () => clearInterval(interval)
  }, [id])

  const cancel = async () => {
    if (!booking) return
    const reason = window.prompt('Why are you cancelling this booking?')
    if (!reason) return
    const updated = await api.bookings.cancel(booking.id, reason)
    setBooking(updated)
  }

  const submitRating = async () => {
    if (!booking) return
    await api.ratings.rate(booking.id, score)
    setRated(true)
  }

  if (!booking) {
    return (
      <AppShell title="Track delivery">
        <p className="text-ink-muted text-sm">{loadError || 'Loading…'}</p>
      </AppShell>
    )
  }

  return (
    <AppShell title={`Tracking ${booking.bookingReference}`}>
      <div className="space-y-6 max-w-2xl">
        <WaybillCard booking={booking} />

        <div className="bg-card rounded-lg border border-asphalt-600/15 p-6">
          <Timeline status={booking.status} />
        </div>

        {booking.status === 'SEARCHING_DRIVER' && (
          <div className="bg-signal/10 border border-signal/30 rounded-md p-4 text-sm text-ink">
            Looking for a nearby driver with a suitable vehicle, this updates automatically.
          </div>
        )}

        {booking.status === 'NO_DRIVER_FOUND' && (
          <div className="bg-alert/10 border border-alert/30 rounded-md p-4 text-sm text-ink">
            No drivers were available nearby. You can cancel and try again shortly.
          </div>
        )}

        {booking.driverName && (
          <div className="bg-card rounded-lg border border-asphalt-600/15 p-5 flex items-center justify-between">
            <div>
              <p className="text-xs uppercase tracking-wider text-ink-faint">Driver</p>
              <p className="font-medium text-ink">{booking.driverName}{booking.vehicleRegistration ? ` · ${booking.vehicleRegistration}` : ''}</p>
              {booking.driverRating != null && <p className="text-sm text-ink-muted">★ {booking.driverRating.toFixed(1)}</p>}
            </div>
            {booking.driverPhone && <a href={`tel:${booking.driverPhone}`} className="text-signal-dark text-sm font-medium">Call</a>}
          </div>
        )}

        <div className="grid grid-cols-2 gap-4">
          <div className="bg-card rounded-lg border border-asphalt-600/15 p-5">
            <p className="text-xs uppercase tracking-wider text-ink-faint">Pickup PIN</p>
            <p className="font-mono text-2xl font-semibold text-ink">{booking.pickupPin}</p>
            <p className="text-xs text-ink-faint mt-1">Give this to your driver at pickup</p>
          </div>
          <div className="bg-card rounded-lg border border-asphalt-600/15 p-5">
            <p className="text-xs uppercase tracking-wider text-ink-faint">Delivery OTP</p>
            <p className="font-mono text-2xl font-semibold text-ink">{booking.deliveryOtp}</p>
            <p className="text-xs text-ink-faint mt-1">Share this once your goods arrive</p>
          </div>
        </div>

        {booking.status === 'CANCELLED' && (
          <div className="bg-alert/10 border border-alert/30 rounded-md p-4 text-sm text-ink">
            Cancelled: {booking.cancellationReason}
          </div>
        )}

        {booking.status === 'COMPLETED' && !rated && (
          <div className="bg-card rounded-lg border border-asphalt-600/15 p-5">
            <p className="text-sm font-semibold text-ink mb-3">Rate your driver</p>
            <div className="flex items-center gap-3">
              <select value={score} onChange={(e) => setScore(parseInt(e.target.value))} className="border border-asphalt-600/25 rounded-md px-3 py-2 text-sm">
                {[5, 4, 3, 2, 1].map((n) => <option key={n} value={n}>{n} star{n > 1 ? 's' : ''}</option>)}
              </select>
              <Button onClick={submitRating}>Submit rating</Button>
            </div>
          </div>
        )}
        {rated && <p className="text-sm text-route-dark">Thanks for your feedback.</p>}

        {CANCELLABLE.has(booking.status) && (
          <Button variant="danger" onClick={cancel}>Cancel booking</Button>
        )}
      </div>
    </AppShell>
  )
}
