import { useState } from 'react'

export function useGeolocation() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const request = () =>
    new Promise((resolve, reject) => {
      if (!('geolocation' in navigator)) {
        const message = 'Your browser does not support location services.'
        setError(message)
        reject(new Error(message))
        return
      }

      setLoading(true)
      setError('')

      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLoading(false)
          resolve({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          })
        },
        (err) => {
          setLoading(false)
          const message =
            err.code === err.PERMISSION_DENIED
              ? 'Location access was denied. You can still search for your address manually.'
              : 'Could not determine your location. Please search for your address instead.'
          setError(message)
          reject(new Error(message))
        },
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 },
      )
    })

  return { request, loading, error }
}
