import StatusBadge from './StatusBadge'
import VehicleIcon from './VehicleIcon'

// The KargoX signature component, this is used anywhere a booking needs summarizing of history, tracking, driver job offers
export default function WaybillCard({ booking, footer }) {
  const vehicleType = booking.vehicleType ?? booking.recommendedVehicleType
  return (
    <div className="bg-card rounded-lg border border-asphalt-600/15 shadow-sm overflow-hidden">
      <div className="flex flex-col sm:flex-row">
        <div className="flex-1 p-5">
          <div className="flex items-start gap-3">
            <div className="flex flex-col items-center pt-1">
              <span className="w-2 h-2 rounded-full bg-ink" />
              <span className="w-px flex-1 min-h-[24px] bg-asphalt-600/30 my-1" />
              <span className="w-2 h-2 rounded-full bg-signal-dark" />
            </div>
            <div className="flex-1 space-y-4">
              <div>
                <p className="text-xs uppercase tracking-wider text-ink-faint">Pickup</p>
                <p className="text-sm font-medium text-ink">{booking.pickupAddress}</p>
              </div>
              <div>
                <p className="text-xs uppercase tracking-wider text-ink-faint">Destination</p>
                <p className="text-sm font-medium text-ink">{booking.destinationAddress}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="perforated sm:w-56 bg-paper-dim p-5 flex flex-row sm:flex-col justify-between gap-3">
          <div className="flex items-center gap-2">
            {vehicleType && <VehicleIcon type={vehicleType} size={22} className="text-ink-muted flex-shrink-0" />}
            <div>
              <p className="text-xs uppercase tracking-wider text-ink-faint">Request ID</p>
              <p className="font-mono text-sm text-ink">{booking.bookingReference}</p>
            </div>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-ink-faint">Fare</p>
            <p className="font-display font-semibold text-lg text-ink">
              R{(booking.finalFare ?? booking.estimatedFare ?? 0).toFixed(2)}
            </p>
          </div>
          <StatusBadge status={booking.status} />
        </div>
      </div>
      {footer && <div className="border-t border-asphalt-600/15 p-4 bg-card">{footer}</div>}
    </div>
  )
}
