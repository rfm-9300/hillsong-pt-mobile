'use client';

import { useId } from 'react';
import { ministryOptions } from '@/lib/groups';
import { Ministry } from '@/lib/types';
import { cn } from '@/lib/utils';

interface MinistrySelectProps {
  value: Ministry;
  onChange: (value: Ministry) => void;
  error?: string;
  disabled?: boolean;
}

export default function MinistrySelect({ value, onChange, error, disabled = false }: MinistrySelectProps) {
  const selectId = useId();

  return (
    <div className="space-y-1">
      <label htmlFor={selectId} className="block text-sm font-medium text-gray-700">
        Ministry
        <span className="text-red-500 ml-1">*</span>
      </label>
      <select
        id={selectId}
        value={value}
        onChange={(event) => onChange(event.target.value as Ministry)}
        disabled={disabled}
        className={cn(
          'block w-full rounded-md border border-gray-300 bg-white px-3 py-3 text-sm shadow-sm',
          'focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500',
          error && 'border-red-300 focus:border-red-500 focus:ring-red-500',
          disabled && 'cursor-not-allowed bg-gray-50 text-gray-500'
        )}
      >
        {ministryOptions.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error && <p className="text-sm text-red-600">{error}</p>}
    </div>
  );
}
