const PATHS = {
  MOTORCYCLE: (
    <g>
      <circle cx="6" cy="17" r="2.5" />
      <circle cx="18" cy="17" r="2.5" />
      <path d="M6 17 L9 10 H14 L18 17 M9 10 L11 6 H14" fill="none" strokeLinecap="round" strokeLinejoin="round" />
    </g>
  ),
  HATCHBACK: (
    <g>
      <path d="M3 16 h1 l1.5-4.5 A2 2 0 0 1 7.4 10 h9.2 a2 2 0 0 1 1.9 1.5 L20 16 h1"
        fill="none" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M6 11.5 L7.5 8 h8 l2 3.5" fill="none" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="7.5" cy="16.5" r="1.8" />
      <circle cx="16.5" cy="16.5" r="1.8" />
    </g>
  ),
  SEDAN: (
    <g>
      <path d="M2 16 h1.5 l1-4.5 A2 2 0 0 1 6.4 10 h4 l2-3 h2.5 l1 3 h1.7 a2 2 0 0 1 1.9 1.5 L21.5 16 H23"
        fill="none" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M6.5 11.5 L8 8.3 h3.5 l2-3" fill="none" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="7" cy="16.5" r="1.8" />
      <circle cx="18" cy="16.5" r="1.8" />
    </g>
  ),
  PICKUP_TRUCK: (
    <g>
      <path d="M2 16.5 h1 v-4 l2-4 h4 l2 2 h4.5 v-2 h5 a1.5 1.5 0 0 1 1.5 1.5 v6.5 h1"
        fill="none" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M5 12.5 h3.5" fill="none" strokeLinecap="round" />
      <circle cx="6.5" cy="17" r="1.8" />
      <circle cx="18.5" cy="17" r="1.8" />
    </g>
  ),
  VAN: (
    <g>
      <path d="M2 16.5 h1 v-6 a1.5 1.5 0 0 1 1.5-1.5 h11 a2 2 0 0 1 1.8 1.2 l1.7 3.8 h1 a1 1 0 0 1 1 1 v1.5 h1"
        fill="none" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M9 9 v6.5 M15.3 10.2 L16.8 13.5 H9" fill="none" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="7" cy="17" r="1.8" />
      <circle cx="17.5" cy="17" r="1.8" />
    </g>
  ),
  TRUCK: (
    <g>
      <path d="M1 16.5 h1.5 v-7.5 h9 v7.5 h1 v-4.5 h4 l2.5 2.5 v2 h1.5"
        fill="none" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M11.5 12 h4" fill="none" strokeLinecap="round" />
      <circle cx="6.5" cy="17" r="1.8" />
      <circle cx="19" cy="17" r="1.8" />
    </g>
  ),
}

const LABELS = {
  MOTORCYCLE: 'Motorcycle', HATCHBACK: 'Hatchback', SEDAN: 'Sedan',
  PICKUP_TRUCK: 'Pickup truck', VAN: 'Van', TRUCK: 'Truck',
}

export default function VehicleIcon({ type, size = 32, className = '' }) {
  return (
    <svg
      width={size} height={size} viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="1.4"
      className={className}
      role="img"
      aria-label={LABELS[type] ?? type}
    >
      {PATHS[type] ?? PATHS.HATCHBACK}
    </svg>
  )
}
