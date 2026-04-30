'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Alert, Button, Input } from '../components/ui';

export default function RegisterPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const passwordMismatch = Boolean(formData.confirmPassword && formData.password !== formData.confirmPassword);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setLoading(true);
    try {
      const res = await fetch('/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data?.message || 'Registration failed');
      router.push('/login?registered=true');
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message || 'Registration failed' : 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const update = (field: keyof typeof formData) => (value: string) => {
    setFormData((current) => ({ ...current, [field]: value }));
  };

  return (
    <div className="flex min-h-[100dvh] w-full items-center justify-center bg-[var(--color-sidebar-bg)] p-4 pb-safe pt-safe sm:p-6">
      <div className="w-full max-w-[420px]">
        <div className="mb-9 text-center">
          <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-[12px] border-[1.5px] border-[rgba(201,149,42,0.3)] bg-[var(--color-accent-sub)]">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 1v18M6 5v10M14 5v10M1 10h18" stroke="var(--color-accent)" strokeWidth="2" strokeLinecap="round" />
            </svg>
          </div>
          <h1 className="font-display text-[28px] leading-none text-[#F9F8F6]">Hillsong PT</h1>
          <p className="mt-1 text-[12px] uppercase tracking-[2px] text-[var(--color-sidebar-text)]">Admin Panel</p>
        </div>

        <div className="rounded-[10px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 pb-6 pt-7 sm:px-7">
          <h2 className="font-display text-[22px] leading-tight text-[var(--color-text)]">Create account</h2>
          <p className="mb-6 mt-1 text-[13px] text-[var(--color-text-sub)]">Register a new admin panel account.</p>

          {error && <Alert type="error" message={error} onClose={() => setError('')} className="mb-4" />}

          <form className="space-y-4" onSubmit={handleSubmit}>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <Input label="First Name" value={formData.firstName} onChange={update('firstName')} required autoComplete="given-name" />
              <Input label="Last Name" value={formData.lastName} onChange={update('lastName')} required autoComplete="family-name" />
            </div>
            <Input label="Email" type="email" value={formData.email} onChange={update('email')} required autoComplete="email" />
            <Input label="Password" type="password" minLength={8} value={formData.password} onChange={update('password')} required autoComplete="new-password" />
            <Input
              label="Confirm Password"
              type="password"
              minLength={8}
              value={formData.confirmPassword}
              onChange={update('confirmPassword')}
              error={passwordMismatch ? 'Passwords do not match' : undefined}
              required
              autoComplete="new-password"
            />
            <Button type="submit" variant="primary" size="lg" loading={loading} disabled={passwordMismatch} className="w-full">
              Create Account
            </Button>
          </form>

          <p className="mt-5 text-center text-[13px] text-[var(--color-text-sub)]">
            Already have an account?{' '}
            <Link href="/login" className="font-semibold text-[var(--color-accent)] hover:text-[var(--color-accent-hover)]">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
