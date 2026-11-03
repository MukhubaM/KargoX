export default function Select({ label, className = '', id, children, ...rest }) {
  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label htmlFor={id} className="text-xs font-medium uppercase tracking-wider text-ink-muted">
          {label}
        </label>
      )}
      <select
        id={id}
        className={`bg-card border border-asphalt-600/25 rounded-md px-3 py-2.5 text-sm text-ink focus:border-signal-dark outline-none transition-colors ${className}`}
        {...rest}
      >
        {children}
      </select>
    </div>
  )
}
