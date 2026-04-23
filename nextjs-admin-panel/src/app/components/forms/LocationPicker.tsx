'use client';

import type { ReactNode } from 'react';
import { cityOptions } from '@/lib/groups';
import { cn } from '@/lib/utils';

interface LocationValue {
  addressLine: string;
  city: string;
  region?: string;
  postalCode?: string;
  country: string;
  latitude: number;
  longitude: number;
}

interface LocationPickerProps {
  value: LocationValue;
  onChange: (value: LocationValue) => void;
  errors?: Record<string, string>;
  disabled?: boolean;
}

export default function LocationPicker({
  value,
  onChange,
  errors = {},
  disabled = false,
}: LocationPickerProps) {
  const update = <K extends keyof LocationValue>(field: K, nextValue: LocationValue[K]) => {
    onChange({ ...value, [field]: nextValue });
  };

  return (
    <div className="space-y-4 rounded-2xl border border-gray-200 bg-gray-50/70 p-5">
      <div>
        <h3 className="text-sm font-semibold text-gray-900">Location</h3>
        <p className="mt-1 text-sm text-gray-500">
          Use the public meeting address and the exact map coordinates shown in the mobile app.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Field label="Address" error={errors.addressLine}>
          <input
            type="text"
            value={value.addressLine}
            onChange={(event) => update('addressLine', event.target.value)}
            disabled={disabled}
            className={inputClass(errors.addressLine, disabled)}
            placeholder="Rua Garrett 50"
          />
        </Field>

        <Field label="City" error={errors.city}>
          <select
            value={value.city}
            onChange={(event) => update('city', event.target.value)}
            disabled={disabled}
            className={inputClass(errors.city, disabled)}
          >
            {cityOptions.map((city) => (
              <option key={city} value={city}>
                {city}
              </option>
            ))}
          </select>
        </Field>

        <Field label="Region" error={errors.region}>
          <input
            type="text"
            value={value.region ?? ''}
            onChange={(event) => update('region', event.target.value)}
            disabled={disabled}
            className={inputClass(errors.region, disabled)}
            placeholder="Lisboa"
          />
        </Field>

        <Field label="Postal code" error={errors.postalCode}>
          <input
            type="text"
            value={value.postalCode ?? ''}
            onChange={(event) => update('postalCode', event.target.value)}
            disabled={disabled}
            className={inputClass(errors.postalCode, disabled)}
            placeholder="1200-203"
          />
        </Field>

        <Field label="Country" error={errors.country}>
          <input
            type="text"
            value={value.country}
            onChange={(event) => update('country', event.target.value.toUpperCase())}
            disabled={disabled}
            className={inputClass(errors.country, disabled)}
            placeholder="PT"
            maxLength={2}
          />
        </Field>

        <div />

        <Field label="Latitude" error={errors.latitude}>
          <input
            type="number"
            value={String(value.latitude)}
            onChange={(event) => update('latitude', Number(event.target.value))}
            disabled={disabled}
            className={inputClass(errors.latitude, disabled)}
            step="any"
          />
        </Field>

        <Field label="Longitude" error={errors.longitude}>
          <input
            type="number"
            value={String(value.longitude)}
            onChange={(event) => update('longitude', Number(event.target.value))}
            disabled={disabled}
            className={inputClass(errors.longitude, disabled)}
            step="any"
          />
        </Field>
      </div>
    </div>
  );
}

function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: ReactNode;
}) {
  return (
    <label className="block space-y-1">
      <span className="text-sm font-medium text-gray-700">{label}</span>
      {children}
      {error && <span className="text-sm text-red-600">{error}</span>}
    </label>
  );
}

function inputClass(error?: string, disabled?: boolean) {
  return cn(
    'block w-full rounded-md border border-gray-300 bg-white px-3 py-3 text-sm shadow-sm',
    'focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500',
    error && 'border-red-300 focus:border-red-500 focus:ring-red-500',
    disabled && 'cursor-not-allowed bg-gray-100 text-gray-500'
  );
}
