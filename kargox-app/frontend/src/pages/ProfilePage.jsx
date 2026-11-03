import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell'
import Button from '../components/Button'
import Input from '../components/Input'
import ImageUpload from '../components/ImageUpload'
import { api } from '../lib/api'
import { useAuth } from '../lib/auth'

export default function ProfilePage() {
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [fullName, setFullName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [photoUrl, setPhotoUrl] = useState(null)
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api.users.me().then((p) => {
      setProfile(p)
      setFullName(p.fullName)
      setPhoneNumber(p.phoneNumber ?? '')
      setPhotoUrl(p.profilePhotoUrl)
    })
  }, [])

  const save = async () => {
    setError('')
    setSaved(false)
    setSaving(true)
    try {
      const updated = await api.users.updateMe({ fullName, phoneNumber, profilePhotoUrl: photoUrl })
      setProfile(updated)
      setSaved(true)
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Could not save your profile.')
    } finally {
      setSaving(false)
    }
  }

  if (!profile) return <AppShell title="Profile"><p className="text-ink-muted text-sm">Loading…</p></AppShell>

  return (
    <AppShell title="Profile">
      <div className="bg-card rounded-lg border border-asphalt-600/15 p-6 space-y-5 max-w-lg">
        <div>
          <p className="text-xs uppercase tracking-wider text-ink-faint mb-2">Profile photo</p>
          <ImageUpload
            url={photoUrl}
            onUploaded={setPhotoUrl}
            uploadType="profile"
            label="Add a profile photo"
          />
          {user?.role === 'DRIVER' && !photoUrl && (
            <p className="text-xs text-signal-dark mt-2">
              Required before you can go online, customers see this so they can recognize you.
            </p>
          )}
        </div>

        <Input label="Full name" required value={fullName} onChange={(e) => setFullName(e.target.value)} />
        <Input label="Phone number" value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} placeholder="+27 82 000 0000" />
        <Input label="Email" value={profile.email} disabled className="opacity-60" />

        {profile.averageRating != null && (
          <p className="text-sm text-ink-muted">★ {profile.averageRating.toFixed(1)} average rating ({profile.totalRatings} ratings)</p>
        )}

        {error && <p className="text-sm text-alert-dark">{error}</p>}
        {saved && <p className="text-sm text-route-dark">Saved.</p>}
        <Button onClick={save} disabled={saving || !fullName}>{saving ? 'Saving…' : 'Save changes'}</Button>
      </div>
    </AppShell>
  )
}
