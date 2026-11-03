import { useEffect, useState } from 'react'
import AppShell from '../../components/AppShell'
import Button from '../../components/Button'
import { api } from '../../lib/api'

export default function AdminUsersPage() {
  const [users, setUsers] = useState([])

  const load = () => api.admin.users(0, 50).then((r) => setUsers(r.content))
  useEffect(() => { load() }, [])

  const toggle = async (u) => {
    await api.admin.setUserActive(u.id, !u.active)
    load()
  }

  return (
    <AppShell title="Users">
      <div className="bg-card rounded-lg border border-asphalt-600/15 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-paper-dim text-ink-faint text-xs uppercase tracking-wider">
            <tr>
              <th className="text-left px-4 py-3">Name</th>
              <th className="text-left px-4 py-3">Email</th>
              <th className="text-left px-4 py-3">Role</th>
              <th className="text-left px-4 py-3">Status</th>
              <th className="text-left px-4 py-3"></th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id} className="border-t border-asphalt-600/10">
                <td className="px-4 py-3 font-medium text-ink">{u.fullName}</td>
                <td className="px-4 py-3 text-ink-muted">{u.email}</td>
                <td className="px-4 py-3 text-ink-muted">{u.role}</td>
                <td className="px-4 py-3">
                  <span className={`text-xs px-2 py-1 rounded-sm font-medium ${u.active ? 'bg-route/15 text-route-dark' : 'bg-alert/15 text-alert-dark'}`}>
                    {u.active ? 'Active' : 'Suspended'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <Button variant={u.active ? 'danger' : 'secondary'} onClick={() => toggle(u)} className="!py-1.5 !px-3 text-xs">
                    {u.active ? 'Suspend' : 'Reactivate'}
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </AppShell>
  )
}
