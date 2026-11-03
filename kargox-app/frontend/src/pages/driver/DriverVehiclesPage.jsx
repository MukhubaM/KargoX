import { useEffect, useState } from 'react'
import AppShell from '../../components/AppShell'
import Button from '../../components/Button'
import Input from '../../components/Input'
import VehicleIcon from '../../components/VehicleIcon'
import { api } from '../../lib/api'

const TYPES = ['MOTORCYCLE', 'HATCHBACK', 'SEDAN', 'PICKUP_TRUCK', 'VAN', 'TRUCK']

export default function DriverVehiclesPage() {
  const [vehicles, setVehicles] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [type, setType] = useState('PICKUP_TRUCK')
  const [make, setMake] = useState('')
  const [model, setModel] = useState('')
  const [reg, setReg] = useState('')
  const [maxWeight, setMaxWeight] = useState('500')
  const [error, setError] = useState('')

  const load = () => api.vehicles.my().then(setVehicles).catch((err) =>
    setError(err?.response?.data?.message ?? 'Could not load your vehicles.'))
  useEffect(() => { load() }, [])

  const submit = async () => {
    setError('')
    try {
      await api.vehicles.register({ type, make, model, registrationNumber: reg, maxWeightKg: parseFloat(maxWeight) })
      setShowForm(false)
      setMake(''); setModel(''); setReg('')
      load()
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Could not register vehicle.')
    }
  }

  return (
    <AppShell title="My vehicles">
      <div className="max-w-xl space-y-4">
        {error && !showForm && <p className="text-sm text-alert-dark">{error}</p>}
        {vehicles.map((v) => (
          <div key={v.id} className="bg-card rounded-lg border border-asphalt-600/15 p-5 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <VehicleIcon type={v.type} size={30} className="text-ink-muted flex-shrink-0" />
              <div>
                <p className="font-medium text-ink">{v.make} {v.model} · {v.type.replaceAll('_', ' ')}</p>
                <p className="font-mono text-xs text-ink-faint">{v.registrationNumber}</p>
              </div>
            </div>
            <span className={`text-xs px-2 py-1 rounded-sm font-medium ${v.documentsVerified ? 'bg-route/15 text-route-dark' : 'bg-signal/20 text-signal-dark'}`}>
              {v.documentsVerified ? 'Verified' : 'Pending verification'}
            </span>
          </div>
        ))}

        {showForm ? (
          <div className="bg-card rounded-lg border border-asphalt-600/15 p-5 space-y-3">
            <div>
              <p className="text-xs font-medium uppercase tracking-wider text-ink-muted mb-1.5">Vehicle type</p>
              <div className="grid grid-cols-3 gap-2">
                {TYPES.map((t) => (
                  <button
                    key={t}
                    type="button"
                    onClick={() => setType(t)}
                    className={`flex flex-col items-center gap-1 py-2.5 rounded-md border text-xs transition-colors ${
                      type === t ? 'bg-signal border-signal text-asphalt-950' : 'border-asphalt-600/25 text-ink-muted'
                    }`}
                  >
                    <VehicleIcon type={t} size={24} />
                    {t.replaceAll('_', ' ')}
                  </button>
                ))}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <Input label="Make" value={make} onChange={(e) => setMake(e.target.value)} placeholder="Toyota" />
              <Input label="Model" value={model} onChange={(e) => setModel(e.target.value)} placeholder="Hilux" />
            </div>
            <Input label="Registration number" value={reg} onChange={(e) => setReg(e.target.value)} placeholder="KX 01 GP" />
            <Input label="Max load (kg)" type="number" value={maxWeight} onChange={(e) => setMaxWeight(e.target.value)} />
            {error && <p className="text-sm text-alert-dark">{error}</p>}
            <div className="flex gap-3">
              <Button variant="ghost" onClick={() => setShowForm(false)}>Cancel</Button>
              <Button onClick={submit} disabled={!make || !model || !reg}>Register vehicle</Button>
            </div>
          </div>
        ) : (
          <button onClick={() => setShowForm(true)} className="text-sm text-signal-dark font-medium">+ Register a vehicle</button>
        )}
      </div>
    </AppShell>
  )
}
