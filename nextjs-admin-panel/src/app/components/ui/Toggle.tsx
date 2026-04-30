'use client';

import { cn } from '@/lib/utils';

export default function Toggle({
  checked,
  onChange,
  label,
  className,
}: {
  checked: boolean;
  onChange: (checked: boolean) => void;
  label?: string;
  className?: string;
}) {
  return (
    <label className={cn('flex cursor-pointer items-center gap-2.5 text-[13px] font-medium text-[var(--color-text)]', className)}>
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        onClick={() => onChange(!checked)}
        className={cn(
          'relative h-5 w-9 rounded-full border transition-colors duration-150',
          checked ? 'border-[var(--color-accent)] bg-[var(--color-accent)]' : 'border-[var(--color-border-med)] bg-[var(--color-surface-alt)]'
        )}
      >
        <span className={cn('absolute top-0.5 h-4 w-4 rounded-full bg-white shadow transition-transform duration-150', checked ? 'translate-x-[18px]' : 'translate-x-0.5')} />
      </button>
      {label && <span>{label}</span>}
    </label>
  );
}
