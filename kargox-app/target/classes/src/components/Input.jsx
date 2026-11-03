export default function Input({ label, hint, className = '', id, ...rest }) {
  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label htmlFor={id} className="text-xs font-medium uppercase tracking-wider text-ink-muted">
          {label}
        </label>
      )}
      <input
        id={id}
        className={`bg-card border border-asphalt-600/25 rounded-md px-3 py-2.5 text-sm text-ink placeholder:text-ink-faint focus:border-signal-dark outline-none transition-colors ${className}`}
        {...rest}
      />
      {hint && <span className="text-xs text-ink-faint">{hint}</span>}
    </div>
  )
}
