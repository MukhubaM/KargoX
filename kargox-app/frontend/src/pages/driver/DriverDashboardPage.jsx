import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import AppShell from '../../components/AppShell'
import Button from '../../components/Button'
import Input from '../../components/Input'
import WaybillCard from '../../components/WaybillCard'
import Timeline from '../../components/Timeline'
import { api } from '../../lib/api'
import { subscribeTopic } from '../../lib/ws'
import { useGeolocation } from '../../lib/useGeolocation'

const ACTIVE_STATUSES = new Set(['DRIVER_ASSIGNED', 'DRIVER_EN_ROUTE', 'DRIVER_ARRIVED', 'GOODS_COLLECTED', 'IN_TRANSIT', 'DELIVERED'])

const NEXT_STATUS = {
  DRIVER_ASSIGNED: 'DRIVER_EN_ROUTE',
  DRIVER_EN_ROUTE: 'DRIVER_ARRIVED',
  DRIVER_ARRIVED: 'GOODS_COLLECTED',
  GOODS_COLLECTED: 'IN_TRANSIT',
  IN_TRANSIT: 'DELIVERED',
  DELIVERED: 'COMPLETED',
}

const NEXT_LABEL = {
  DRIVER_ASSIGNED: "I'm on my way",
  DRIVER_EN_ROUTE: "I've arrived",
  DRIVER_ARRIVED: 'Confirm pickup PIN',
  GOODS_COLLECTED: 'Start delivery',
  IN_TRANSIT: 'Confirm delivery OTP',
  DELIVERED: 'Mark completed',
}

export default function DriverDashboardPage() {
  const [driver, setDriver] = useState(null)
  const [notFound, setNotFound] = useState(false)
  const [license, setLicense] = useState('')
  const [offers, setOffers] = useState([])
  const [activeBooking, setActiveBooking] = useState(null)
  const [code, setCode] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const geolocation = useGeolocation()

  const loadDriver = () =>
    api.drivers.me().then((d) => { setDriver(d); setNotFound(false) }).catch(() => setNotFound(true))

  const loadActive = () =>
    api.bookings.my(0, 5).then((page) => {
      const active = page.content.find((b) => ACTIVE_STATUSES.has(b.status))
      setActiveBooking(active ?? null)
    })

  useEffect(() => { loadDriver() }, [])

  useEffect(() => {
    if (!driver) return
    loadActive()
    const interval = setInterval(loadActive, 5000)
    const unsub = subscribeTopic(`/topic/drivers/${driver.id}/requests`, (offer) => {
      setOffers((prev) => (prev.some((o) => o.bookingId === offer.bookingId) ? prev : [offer, ...prev]))
    })
    return () => { clearInterval(interval); unsub() }
  }, [driver?.id])

  const onboard = async () => {
    setBusy(true); setError('')
    try {
      const d = await api.drivers.onboard(license)
      setDriver(d); setNotFound(false)
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Could not create your driver profile.')
    } finally { setBusy(false) }
  }

  const goOnline = async () => {
    setError('')
    try {
      if (driver.status !== 'ONLINE') {
        const { latitude, longitude } = await geolocation.request()
        await api.drivers.updateLocation(latitude, longitude)
      }
      const d = await api.drivers.updateStatus(driver.status === 'ONLINE' ? 'OFFLINE' : 'ONLINE')
      setDriver(d)
    } catch (err) {
      setError(err?.response?.data?.message ?? geolocation.error ?? 'Could not update your status.')
    }
  }

  const acceptOffer = async (offer) => {
    setBusy(true); setError('')
    try {
      await api.drivers.acceptBooking(offer.bookingId)
      setOffers((prev) => prev.filter((o) => o.bookingId !== offer.bookingId))
      loadActive()
    } catch (err) {
      setError(err?.response?.data?.message ?? 'This booking may already be taken.')
    } finally { setBusy(false) }
  }

  const advance = async () => {
    if (!activeBooking) return
    const next = NEXT_STATUS[activeBooking.status]
    setBusy(true); setError('')
    try {
      const needsCode = next === 'GOODS_COLLECTED' || next === 'DELIVERED'
      const updated = await api.bookings.updateStatus(activeBooking.id, next, needsCode ? code : undefined)
      setActiveBooking(updated)
      setCode('')
      if (next === 'COMPLETED') setTimeout(loadActive, 500)
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Could not update the booking.')
    } finally { setBusy(false) }
  }

  if (notFound) {
    return (
      <AppShell title="Driver onboarding">
        <div className="bg-card rounded-lg border border-asphalt-600/15 p-6 max-w-md space-y-4">
          <p className="text-sm text-ink-muted">Complete your driver profile to start receiving delivery requests.</p>
          <Input label="Driver's license number" value={license} onChange={(e) => setLicense(e.target.value)} />
          {error && <p className="text-sm text-alert-dark">{error}</p>}
          <Button onClick={onboard} disabled={busy || !license}>Submit for verification</Button>
          <p className="text-xs text-ink-faint">An administrator verifies documents before you can go online.</p>
        </div>
      </AppShell>
    )
  }

  if (!driver) return <AppShell title="Driver dashboard"><p className="text-ink-muted text-sm">Loading…</p></AppShell>

  return (
    <AppShell title="Driver dashboard">
      <div className="space-y-6 max-w-2xl">
        <div className="bg-card rounded-lg border border-asphalt-600/15 p-5 flex items-center justify-between">
          <div>
            <p className="text-sm font-semibold text-ink">Status: {driver.status.replaceAll('_', ' ')}</p>
            <p className="text-xs text-ink-faint">{driver.completedDeliveries} completed · ★ {driver.averageRating?.toFixed(1) ?? ' '} · Wallet R{driver.walletBalance.toFixed(2)}</p>
          </div>
          <Button
            variant={driver.status === 'ONLINE' ? 'danger' : 'secondary'}
            onClick={goOnline}
            disabled={!!activeBooking || !driver.documentsVerified || (driver.status !== 'ONLINE' && !driver.profilePhotoUrl)}
          >
            {driver.status === 'ONLINE' ? 'Go offline' : 'Go online'}
          </Button>
        </div>

        {!driver.documentsVerified && (
          <div className="bg-signal/10 border border-signal/30 rounded-md p-4 text-sm text-ink">
            Your documents are pending admin verification. You'll be able to go online once approved.
          </div>
        )}

        {driver.documentsVerified && !driver.profilePhotoUrl && (
          <div className="bg-signal/10 border border-signal/30 rounded-md p-4 text-sm text-ink">
            Add a profile photo before going online, so customers can recognize you.{' '}
            <Link to="/profile" className="font-medium text-signal-dark">Add one now →</Link>
          </div>
        )}

        {error && <p className="text-sm text-alert-dark">{error}</p>}

        {activeBooking ? (
          <div className="space-y-4">
            <p className="text-sm font-semibold text-ink">Active delivery</p>
            <WaybillCard booking={activeBooking} />
            <div className="bg-card rounded-lg border border-asphalt-600/15 p-6">
              <Timeline status={activeBooking.status} />
            </div>
            <div className="bg-card rounded-lg border border-asphalt-600/15 p-5 flex items-center gap-3">
              {(activeBooking.status === 'DRIVER_ARRIVED' || activeBooking.status === 'IN_TRANSIT') && (
                <Input
                  placeholder={activeBooking.status === 'DRIVER_ARRIVED' ? 'Pickup PIN from customer' : 'Delivery OTP from customer'}
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  className="w-56"
                />
              )}
              <Button onClick={advance} disabled={busy}>{NEXT_LABEL[activeBooking.status]}</Button>
            </div>
          </div>
        ) : driver.status === 'ONLINE' ? (
          <div>
            <p className="text-sm font-semibold text-ink mb-3">Incoming requests</p>
            {offers.length === 0 ? (
              <div className="bg-card rounded-lg border border-dashed border-asphalt-600/30 p-8 text-center text-sm text-ink-faint">
                Waiting for nearby delivery requests…
              </div>
            ) : (
              <div className="space-y-3">
                {offers.map((offer) => (
                  <div key={offer.bookingId} className="bg-card rounded-lg border border-asphalt-600/15 p-5 flex items-center justify-between">
                    <div>
                      <p className="font-mono text-xs text-ink-faint">{offer.bookingReference}</p>
                      <p className="text-sm text-ink">{offer.pickupAddress} → {offer.destinationAddress}</p>
                      <p className="text-xs text-ink-faint">{offer.estimatedDistanceKm} km · {offer.recommendedVehicleType.replaceAll('_', ' ')}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-display font-semibold text-lg mb-2">R{offer.estimatedFare.toFixed(2)}</p>
                      <Button onClick={() => acceptOffer(offer)} disabled={busy}>Accept</Button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : (
          <div className="bg-card rounded-lg border border-dashed border-asphalt-600/30 p-8 text-center text-sm text-ink-faint">
            You're offline. Go online to start receiving requests.
          </div>
        )}
      </div>
    </AppShell>
  )
}
