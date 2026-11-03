import { useEffect, useRef, useState } from 'react'
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'
import { api } from '../lib/api'
import { useGeolocation } from '../lib/useGeolocation'
import Input from './Input'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({ iconRetinaUrl: markerIcon2x, iconUrl: markerIcon, shadowUrl: markerShadow })

const DEFAULT_CENTER = [-26.2041, 28.0473] // Johannesburg

function ClickHandler({ onPick }) {
  useMapEvents({
    click(e) {
      onPick(e.latlng.lat, e.latlng.lng)
    },
  })
  return null
}

function RecenterOnChange({ position }) {
  const map = useMap()
  useEffect(() => {
    if (position) map.setView(position, Math.max(map.getZoom(), 14))
  }, [position[0], position[1]])
  return null
}

export default function MapPicker({ label, address, latitude, longitude, onChange, allowUseMyLocation }) {
  const [query, setQuery] = useState(address || '')
  const [suggestions, setSuggestions] = useState([])
  const [showSuggestions, setShowSuggestions] = useState(false)
  const debounceRef = useRef(null)
  const geolocation = useGeolocation()

  const hasPosition = latitude != null && longitude != null
  const position = hasPosition ? [latitude, longitude] : null

  useEffect(() => setQuery(address || ''), [address])

  const runSearch = (text) => {
    clearTimeout(debounceRef.current)
    if (!text || text.trim().length < 3) {
      setSuggestions([])
      return
    }
    debounceRef.current = setTimeout(async () => {
      try {
        const results = await api.geocode.search(text)
        setSuggestions(results)
        setShowSuggestions(true)
      } catch {
        setSuggestions([])
      }
    }, 450)
  }

  const pickSuggestion = (s) => {
    setQuery(s.displayName)
    setShowSuggestions(false)
    setSuggestions([])
    onChange({ address: s.displayName, latitude: s.latitude, longitude: s.longitude })
  }

  const pickOnMap = async (lat, lng) => {
    onChange({ address: query, latitude: lat, longitude: lng })
    try {
      const result = await api.geocode.reverse(lat, lng)
      setQuery(result.displayName)
      onChange({ address: result.displayName, latitude: lat, longitude: lng })
    } catch {

    }
  }

  const useMyLocation = async () => {
    try {
      const { latitude: lat, longitude: lng } = await geolocation.request()
      await pickOnMap(lat, lng)
    } catch {

    }
  }

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <p className="text-sm font-semibold text-ink">{label}</p>
        {allowUseMyLocation && (
          <button
            type="button"
            onClick={useMyLocation}
            disabled={geolocation.loading}
            className="text-xs font-medium text-signal-dark disabled:opacity-50"
          >
            {geolocation.loading ? 'Finding you…' : '📍 Use my location'}
          </button>
        )}
      </div>

      <div className="relative">
        <Input
          value={query}
          onChange={(e) => { setQuery(e.target.value); runSearch(e.target.value) }}
          onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
          onBlur={() => setTimeout(() => setShowSuggestions(false), 150)}
          placeholder="Search for an address…"
        />
        {showSuggestions && suggestions.length > 0 && (
          <div className="absolute z-[1000] mt-1 w-full bg-card border border-asphalt-600/20 rounded-md shadow-lg max-h-56 overflow-y-auto">
            {suggestions.map((s, i) => (
              <button
                key={i}
                type="button"
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => pickSuggestion(s)}
                className="block w-full text-left px-3 py-2 text-sm hover:bg-paper-dim border-b border-asphalt-600/10 last:border-b-0"
              >
                {s.displayName}
              </button>
            ))}
          </div>
        )}
      </div>

      {geolocation.error && <p className="text-xs text-alert-dark">{geolocation.error}</p>}

      <div className="h-56 rounded-md overflow-hidden border border-asphalt-600/15">
        <MapContainer center={position ?? DEFAULT_CENTER} zoom={position ? 14 : 11} style={{ height: '100%', width: '100%' }}>
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <ClickHandler onPick={pickOnMap} />
          {position && <RecenterOnChange position={position} />}
          {position && (
            <Marker
              position={position}
              draggable
              eventHandlers={{
                dragend: (e) => {
                  const { lat, lng } = e.target.getLatLng()
                  pickOnMap(lat, lng)
                },
              }}
            />
          )}
        </MapContainer>
      </div>
      <p className="text-xs text-ink-faint">Search an address, tap "Use my location" or click/drag the pin on the map.</p>
    </div>
  )
}
