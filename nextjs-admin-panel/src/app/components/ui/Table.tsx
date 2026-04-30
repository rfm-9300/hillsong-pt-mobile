import React from 'react';
import { cn } from '@/lib/utils';

export interface TableColumn {
  label: string;
  width: string;
}

export function TableHeader({ cols, className }: { cols: TableColumn[]; className?: string }) {
  return (
    <div
      className={cn('hidden border-b border-[var(--color-border-med)] py-2 md:grid', className)}
      style={{ gridTemplateColumns: cols.map((col) => col.width).join(' ') }}
    >
      {cols.map((col) => (
        <div key={col.label} className="px-3 text-[11px] font-bold uppercase tracking-[0.6px] text-[var(--color-text-muted)]">
          {col.label}
        </div>
      ))}
    </div>
  );
}

export function TableRow({
  cols,
  cells,
  actions,
  last,
  className,
}: {
  cols: TableColumn[];
  cells: React.ReactNode[];
  actions?: React.ReactNode;
  last?: boolean;
  className?: string;
}) {
  return (
    <>
      <div className={cn('mb-2 rounded-[10px] border border-[var(--color-border)] bg-[var(--color-surface)] p-3 md:hidden', className)}>
        {cells.map((cell, index) => {
          const label = cols[index]?.label;
          if (!label) return null;
          return (
            <div key={index} className="flex items-start justify-between gap-3 py-1.5 text-[13px]">
              <span className="shrink-0 text-[11px] font-bold uppercase tracking-[0.4px] text-[var(--color-text-muted)]">{label}</span>
              <span className="min-w-0 text-right text-[var(--color-text-sub)]">{cell}</span>
            </div>
          );
        })}
        {actions && <div className="mt-2 flex flex-wrap gap-2 border-t border-[var(--color-border)] pt-2">{actions}</div>}
      </div>
      <div
        className={cn('group hidden items-center py-[11px] text-[13px] transition-colors hover:bg-[var(--color-surface-alt)] md:grid', !last && 'border-b border-[var(--color-border)]', className)}
        style={{ gridTemplateColumns: cols.map((col) => col.width).join(' ') }}
      >
        {cells.map((cell, index) => (
          <div key={index} className="min-w-0 px-3 text-[var(--color-text-sub)]">
            {cell}
          </div>
        ))}
        {actions && <div className="px-3">{actions}</div>}
      </div>
    </>
  );
}

export function FilterBar({ children, className }: { children: React.ReactNode; className?: string }) {
  return <div className={cn('mb-4 flex flex-wrap items-center gap-2', className)}>{children}</div>;
}
