import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AppShell from '../../components/AppShell'
import Button from '../../components/Button'
import Input from '../../components/Input'
import Select from '../../components/Select'
import MapPicker from '../../components/MapPicker'
import ImageUpload from '../../components/ImageUpload'
import VehicleIcon from '../../components/VehicleIcon'
import { api } from '../../lib/api'

const CATEGORIES = [
  'FURNITURE', 'APPLIANCES', 'PARCELS', 'DOCUMENTS', 'BUILDING_MATERIALS',
  'OFFICE_EQUIPMENT', 'ELECTRONICS', 'GROCERIES', 'BUSINESS_INVENTORY',
  'PERSONAL_BELONGINGS', 'FRAGILE_ITEMS', 'LARGE_CARGO', 'OTHER',
]

const SIZE_TIERS = [
  { value: 'SMALL', label: 'Small', hint: 'Fits in a car boot' },
  { value: 'MEDIUM', label: 'Medium', hint: 'Needs a bakkie/pickup  truck' },
  { value: 'LARGE', label: 'Large', hint: 'Needs a van/truck' },
]

const emptyItem = () => ({
  name: '', category: 'PARCELS', sizeTier: 'SMALL', quantity: 1, fragile: false, photoUrl: null,
})

export default function NewBookingPage() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)

  const [pickup, setPickup] = useState({ address: '', latitude: null, longitude: null })
  const [destination, setDestination] = useState({ address: '', latitude: null, longitude: null })
  const [instructions, setInstructions] = useState('')

  const [items, setItems] = useState([emptyItem()])

  const [estimate, setEstimate] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const updateItem = (i, patch) =>
    setItems((prev) => prev.map((it, idx) => (idx === i ? { ...it, ...patch } : it)))

  const routeReady = pickup.latitude != null && pickup.longitude != null && pickup.address
    && destination.latitude != null && destination.longitude != null && destination.address

  const getEstimate = async () => {
    setError('')
    setLoading(true)
    try {
      const result = await api.bookings.estimateFare({
        pickupLatitude: pickup.latitude,
        pickupLongitude: pickup.longitude,
        destinationLatitude: destination.latitude,
        destinationLongitude: destination.longitude,
        goodsItems: items,
      })
      setEstimate(result)
      setStep(3)
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Could not calculate a fare estimate.')
    } finally {
      setLoading(false)
    }
  }

  const confirmBooking = async () => {
    setError('')
    setLoading(true)
    try {
      const booking = await api.bookings.create({
        pickupAddress: pickup.address, pickupLatitude: pickup.latitude, pickupLongitude: pickup.longitude,
        destinationAddress: destination.address, destinationLatitude: destination.latitude, destinationLongitude: destination.longitude,
        goodsItems: items, deliveryInstructions: instructions || undefined,
      })
      navigate(`/track/${booking.id}`)
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Could not create your booking.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AppShell title="Pick up my goods">
      <div className="flex gap-2 mb-6 text-xs font-medium uppercase tracking-wider">
        {['Route', 'Goods', 'Confirm'].map((label, i) => (
          <div key={label} className={`px-3 py-1.5 rounded-sm ${step === i + 1 ? 'bg-asphalt-950 text-paper' : 'bg-asphalt-600/10 text-ink-faint'}`}>
            {i + 1}. {label}
          </div>
        ))}
      </div>

      {step === 1 && (
        <div className="bg-card rounded-lg border border-asphalt-600/15 p-6 space-y-6 max-w-xl">
          <MapPicker
            label="Pickup location"
            address={pickup.address}
            latitude={pickup.latitude}
            longitude={pickup.longitude}
            onChange={setPickup}
            allowUseMyLocation
          />
          <MapPicker
            label="Destination"
            address={destination.address}
            latitude={destination.latitude}
            longitude={destination.longitude}
            onChange={setDestination}
          />
          <Button onClick={() => setStep(2)} disabled={!routeReady}>Next: describe your goods</Button>
        </div>
      )}

      {step === 2 && (
        <div className="bg-card rounded-lg border border-asphalt-600/15 p-6 space-y-5 max-w-xl">
          <p className="text-sm font-semibold text-ink">What are we moving?</p>
          {items.map((item, i) => (
            <div key={i} className="border border-asphalt-600/15 rounded-md p-4 space-y-3 relative">
              {items.length > 1 && (
                <button
                  onClick={() => setItems((prev) => prev.filter((_, idx) => idx !== i))}
                  className="absolute top-3 right-3 text-xs text-alert-dark"
                >
                  Remove
                </button>
              )}
              <Input label="Item name" required value={item.name} onChange={(e) => updateItem(i, { name: e.target.value })} placeholder="3-seater sofa" />

              <Select label="Category" value={item.category} onChange={(e) => updateItem(i, { category: e.target.value })}>
                {CATEGORIES.map((c) => <option key={c} value={c}>{c.replaceAll('_', ' ')}</option>)}
              </Select>

              <div>
                <p className="text-xs font-medium uppercase tracking-wider text-ink-muted mb-1.5">Size</p>
                <div className="grid grid-cols-3 gap-2">
                  {SIZE_TIERS.map((tier) => (
                    <button
                      key={tier.value}
                      type="button"
                      onClick={() => updateItem(i, { sizeTier: tier.value })}
                      className={`text-left px-3 py-2 rounded-md border text-xs transition-colors ${
                        item.sizeTier === tier.value
                          ? 'bg-signal border-signal text-asphalt-950'
                          : 'border-asphalt-600/25 text-ink-muted'
                      }`}
                    >
                      <span className="block font-semibold">{tier.label}</span>
                      <span className="block opacity-80">{tier.hint}</span>
                    </button>
                  ))}
                </div>
              </div>

              <div className="flex items-center justify-between">
                <Input label="Quantity" type="number" min={1} value={item.quantity} onChange={(e) => updateItem(i, { quantity: parseInt(e.target.value) || 1 })} className="w-24" />
                <label className="flex items-center gap-2 text-sm text-ink-muted">
                  <input type="checkbox" checked={item.fragile} onChange={(e) => updateItem(i, { fragile: e.target.checked })} />
                  Fragile
                </label>
              </div>

              <ImageUpload url={item.photoUrl} onUploaded={(url) => updateItem(i, { photoUrl: url })} />
            </div>
          ))}
          <button onClick={() => setItems((prev) => [...prev, emptyItem()])} className="text-sm text-signal-dark font-medium">
            + Add another item
          </button>
          <Input label="Delivery instructions (optional)" value={instructions} onChange={(e) => setInstructions(e.target.value)} placeholder="Ring the buzzer at gate 2" />
          {error && <p className="text-sm text-alert-dark">{error}</p>}
          <div className="flex gap-3">
            <Button variant="ghost" onClick={() => setStep(1)}>Back</Button>
            <Button onClick={getEstimate} disabled={loading || items.some((i) => !i.name)}>
              {loading ? 'Calculating…' : 'Get fare estimate'}
            </Button>
          </div>
        </div>
      )}

      {step === 3 && estimate && (
        <div className="bg-card rounded-lg border border-asphalt-600/15 p-6 space-y-5 max-w-xl">
          <p className="text-sm font-semibold text-ink">Confirm your booking</p>

          <div className="flex items-center gap-4 bg-asphalt-950 rounded-md p-4 text-paper">
            <VehicleIcon type={estimate.recommendedVehicleType} size={44} />
            <div>
              <p className="text-xs uppercase tracking-wide text-paper/60">We'll send a</p>
              <p className="font-display font-semibold">{estimate.recommendedVehicleType.replaceAll('_', ' ')}</p>
            </div>
          </div>

          <div className="bg-paper-dim rounded-md p-4 grid grid-cols-2 gap-4 text-sm">
            <div><p className="text-ink-faint text-xs uppercase tracking-wide">Distance</p><p className="font-medium">{estimate.distanceKm} km</p></div>
            <div><p className="text-ink-faint text-xs uppercase tracking-wide">ETA to destination</p><p className="font-medium">{estimate.estimatedDurationMinutes} min</p></div>
            <div className="col-span-2"><p className="text-ink-faint text-xs uppercase tracking-wide">Estimated fare</p><p className="font-display font-semibold text-xl">R{estimate.estimatedFare.toFixed(2)}</p></div>
          </div>
          <p className="text-xs text-ink-faint">{estimate.breakdown}</p>
          {error && <p className="text-sm text-alert-dark">{error}</p>}
          <div className="flex gap-3">
            <Button variant="ghost" onClick={() => setStep(2)}>Back</Button>
            <Button onClick={confirmBooking} disabled={loading}>{loading ? 'Booking…' : 'Request pickup'}</Button>
          </div>
        </div>
      )}
    </AppShell>
  )
}
