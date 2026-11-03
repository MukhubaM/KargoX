import { useRef, useState } from 'react'
import { api } from '../lib/api'

export default function ImageUpload({ url, onUploaded, uploadType = 'goods', label = 'Add a photo (optional)' }) {
  const inputRef = useRef(null)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')

  const handleFile = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    setError('')
    setUploading(true)
    try {
      const result = await api.uploads.image(file, uploadType)
      onUploaded(result.url)
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Could not upload that image.')
    } finally {
      setUploading(false)
      if (inputRef.current) inputRef.current.value = ''
    }
  }

  return (
    <div className="space-y-2">
      <div className="flex items-center gap-3">
        {url ? (
          <img src={url} alt="" className="w-14 h-14 rounded-md object-cover border border-asphalt-600/20" />
        ) : (
          <div className="w-14 h-14 rounded-md border border-dashed border-asphalt-600/30 flex items-center justify-center text-ink-faint text-xs">
            none
          </div>
        )}
        <div>
          <button
            type="button"
            onClick={() => inputRef.current?.click()}
            disabled={uploading}
            className="text-xs font-medium text-signal-dark disabled:opacity-50"
          >
            {uploading ? 'Uploading…' : url ? 'Change photo' : label}
          </button>
          {url && (
            <button
              type="button"
              onClick={() => onUploaded(null)}
              className="block text-xs text-alert-dark mt-1"
            >
              Remove
            </button>
          )}
        </div>
      </div>
      <input ref={inputRef} type="file" accept="image/*" onChange={handleFile} className="hidden" />
      {error && <p className="text-xs text-alert-dark">{error}</p>}
    </div>
  )
}
