'use client';

import { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '../context/AuthContext';
import { Alert, Button, Input } from '../components/ui';
import { EyeIcon, EyeOffIcon } from '../components/icons/Icons';

function BrandMark({ size = 20 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 20 20" fill="none">
      <path d="M10 1v18M6 5v10M14 5v10M1 10h18" stroke="var(--color-accent)" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

function LoginContent() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    if (searchParams.get('registered') === 'true') {
      setSuccess('Registration successful. Please sign in.');
    }
  }, [searchParams]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      const response = await res.json();
      if (response?.success && response?.data?.token) {
        login(response.data.token);
        router.push('/admin/dashboard');
      } else {
        setError(response?.message || 'Login failed: Invalid credentials.');
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'An unexpected error occurred.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-[100dvh] w-full items-center justify-center bg-[var(--color-sidebar-bg)] p-4 pb-safe pt-safe sm:p-6">
      <div className="w-full max-w-[380px]">
        <div className="mb-9 text-center">
          <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-[12px] border-[1.5px] border-[rgba(201,149,42,0.3)] bg-[var(--color-accent-sub)]">
            <BrandMark />
          </div>
          <h1 className="font-display text-[28px] leading-none text-[#F9F8F6]">Hillsong PT</h1>
          <p className="mt-1 text-[12px] uppercase tracking-[2px] text-[var(--color-sidebar-text)]">Admin Panel</p>
        </div>

        <div className="rounded-[10px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 pb-6 pt-7 sm:px-7">
          <h2 className="font-display text-[22px] leading-tight text-[var(--color-text)]">Sign in</h2>
          <p className="mb-6 mt-1 text-[13px] text-[var(--color-text-sub)]">Enter your credentials to access the admin panel.</p>

          {success && <Alert type="success" message={success} className="mb-4" />}
          {error && <Alert type="error" message={error} onClose={() => setError('')} className="mb-4" />}

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input label="Email" type="email" value={email} onChange={setEmail} required autoComplete="email" />
            <div>
              <div className="relative">
                <Input label="Password" type={showPassword ? 'text' : 'password'} value={password} onChange={setPassword} required autoComplete="current-password" className="[&_input]:pr-10" />
                <button
                  type="button"
                  className="absolute bottom-[8px] right-2 flex min-h-touch min-w-touch items-center justify-center text-[var(--color-text-muted)] hover:text-[var(--color-text-sub)] sm:bottom-[9px] sm:right-1 sm:min-h-0 sm:min-w-0"
                  onClick={() => setShowPassword((shown) => !shown)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOffIcon /> : <EyeIcon />}
                </button>
              </div>
              <div className="mt-2 text-right">
                <button type="button" className="text-[11px] font-semibold text-[var(--color-accent)]">Forgot password?</button>
              </div>
            </div>

            <Button type="submit" variant="primary" size="lg" loading={loading} className="mt-1.5 w-full">
              Sign In
            </Button>
          </form>

          <p className="mt-5 text-center text-[13px] text-[var(--color-text-sub)]">
            Don&apos;t have an account?{' '}
            <Link href="/register" className="font-semibold text-[var(--color-accent)] hover:text-[var(--color-accent-hover)]">
              Register
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[var(--color-sidebar-bg)]" />}>
      <LoginContent />
    </Suspense>
  );
}
