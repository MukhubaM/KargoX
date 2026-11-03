const STYLES = {
  REQUESTED: 'bg-asphalt-600/15 text-asphalt-700',
  SEARCHING_DRIVER: 'bg-signal/20 text-signal-dark',
  DRIVER_ASSIGNED: 'bg-signal/20 text-signal-dark',
  DRIVER_EN_ROUTE: 'bg-signal/20 text-signal-dark',
  DRIVER_ARRIVED: 'bg-signal/20 text-signal-dark',
  GOODS_COLLECTED: 'bg-route/15 text-route-dark',
  IN_TRANSIT: 'bg-route/15 text-route-dark',
  DELIVERED: 'bg-route/15 text-route-dark',
  COMPLETED: 'bg-route text-white',
  CANCELLED: 'bg-alert/15 text-alert-dark',
  NO_DRIVER_FOUND: 'bg-alert/15 text-alert-dark',
}

const LABELS = {
  REQUESTED: 'Requested',
  SEARCHING_DRIVER: 'Finding a driver',
  DRIVER_ASSIGNED: 'Driver assigned',
  DRIVER_EN_ROUTE: 'Driver en route',
  DRIVER_ARRIVED: 'Driver arrived',
  GOODS_COLLECTED: 'Goods collected',
  IN_TRANSIT: 'In transit',
  DELIVERED: 'Delivered',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  NO_DRIVER_FOUND: 'No driver found',
}

export default function StatusBadge({ status }) {
  return (
    <span
      className={`inline-flex items-center px-2.5 py-1 rounded-sm text-xs font-semibold uppercase tracking-wide -rotate-1 ${STYLES[status]}`}
    >
      {LABELS[status]}
    </span>
  )
}
