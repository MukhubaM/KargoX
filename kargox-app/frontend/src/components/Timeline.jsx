const STEPS = [
  { key: 'DRIVER_ASSIGNED', label: 'Assigned' },
  { key: 'DRIVER_EN_ROUTE', label: 'En route' },
  { key: 'DRIVER_ARRIVED', label: 'Arrived' },
  { key: 'GOODS_COLLECTED', label: 'Collected' },
  { key: 'IN_TRANSIT', label: 'In transit' },
  { key: 'DELIVERED', label: 'Delivered' },
  { key: 'COMPLETED', label: 'Completed' },
]

export default function Timeline({ status }) {
  if (status === 'CANCELLED' || status === 'NO_DRIVER_FOUND' || status === 'REQUESTED' || status === 'SEARCHING_DRIVER') {
    return null
  }
  const currentIndex = STEPS.findIndex((s) => s.key === status)

  return (
    <div className="flex items-center overflow-x-auto py-2">
      {STEPS.map((step, i) => {
        const done = i <= currentIndex
        return (
          <div key={step.key} className="flex items-center flex-shrink-0">
            <div className="flex flex-col items-center gap-1.5 w-20">
              <span
                className={`w-3 h-3 rounded-full border-2 ${
                  done ? 'bg-route border-route' : 'bg-card border-asphalt-600/40'
                }`}
              />
              <span className={`text-[11px] text-center leading-tight ${done ? 'text-ink font-medium' : 'text-ink-faint'}`}>
                {step.label}
              </span>
            </div>
            {i < STEPS.length - 1 && (
              <span
                className={`h-0.5 w-8 -mt-4 ${i < currentIndex ? 'bg-route' : 'border-t-2 border-dashed border-asphalt-600/30'}`}
              />
            )}
          </div>
        )
      })}
    </div>
  )
}
