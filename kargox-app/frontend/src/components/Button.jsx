const variants = {
  primary: 'bg-asphalt-950 text-paper hover:bg-asphalt-800 disabled:bg-asphalt-600',
  secondary: 'bg-signal text-asphalt-950 hover:bg-signal-dark disabled:opacity-50',
  ghost: 'bg-transparent text-ink border border-asphalt-600/40 hover:bg-asphalt-950/5',
  danger: 'bg-alert text-white hover:bg-alert-dark disabled:opacity-50',
}

export default function Button({ variant = 'primary', fullWidth, className = '', ...rest }) {
  return (
    <button
      className={`${variants[variant]} ${fullWidth ? 'w-full' : ''} px-4 py-2.5 rounded-md font-medium text-sm tracking-wide transition-colors disabled:cursor-not-allowed ${className}`}
      {...rest}
    />
  )
}
