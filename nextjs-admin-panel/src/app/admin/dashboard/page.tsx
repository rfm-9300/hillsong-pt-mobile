'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth as useSessionAuth } from '../../context/AuthContext';
import { api, ENDPOINTS } from '../../../lib/api';
import { useMultipleApiCalls, useAuth as useProfileAuth } from '../../hooks';
import { Alert, Badge, Button, Card, DashboardSkeleton, ErrorBoundary, PageHeader, StatCard } from '../../components/ui';
import { EventsIcon, GroupsIcon, PostsIcon, RefreshIcon, UsersIcon, VideosIcon } from '../../components/icons/Icons';

interface DashboardData extends Record<string, unknown> {
  posts: { data: { posts: unknown[] } } | null;
  events: { data: { events: unknown[]; content?: unknown[] } } | null;
  users: { data: unknown[] } | null;
}

const actions = [
  { label: 'New Post', href: '/admin/posts/create', icon: PostsIcon },
  { label: 'New Event', href: '/admin/events/create', icon: EventsIcon },
  { label: 'Add Video', href: '/admin/videos/create', icon: VideosIcon },
  { label: 'New Group', href: '/admin/groups/create', icon: GroupsIcon },
  { label: 'New Encounter', href: '/admin/encounters/create', icon: UsersIcon },
  { label: 'View Users', href: '/admin/users', icon: UsersIcon },
];

export default function DashboardPage() {
  const { isAuthenticated, loading: authLoading } = useSessionAuth();
  const { user } = useProfileAuth();
  const router = useRouter();
  const [hasExecuted, setHasExecuted] = useState(false);

  const apiCalls = useMemo(() => ({
    events: () => api.get<{ data: { events: unknown[]; content?: unknown[] } }>(ENDPOINTS.EVENTS),
    posts: () => api.get<{ data: { posts: unknown[] } }>(ENDPOINTS.POSTS),
    users: () => api.get<{ data: unknown[] }>(ENDPOINTS.PROFILE_ALL),
  }), []);

  const { data, loading, errors, globalLoading, globalError, executeAll, retry } = useMultipleApiCalls<DashboardData>(apiCalls, {
    batchLoadingKey: 'dashboard_data',
    context: 'Dashboard Data Loading',
    message: 'Loading dashboard statistics...',
    showToUser: false,
  });

  useEffect(() => {
    if (authLoading) return;
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }
    if (!hasExecuted) {
      setHasExecuted(true);
      executeAll();
    }
  }, [isAuthenticated, authLoading, router, hasExecuted, executeAll]);

  if (globalLoading && !data.posts && !data.events && !data.users) {
    return (
      <>
        <PageHeader title="Dashboard" subtitle="Loading dashboard data..." />
        <DashboardSkeleton />
      </>
    );
  }

  const eventList = data.events?.data?.events || data.events?.data?.content || [];
  const postsCount = data.posts?.data?.posts?.length || 0;
  const eventsCount = eventList.length;
  const usersCount = Array.isArray(data.users?.data) ? data.users.data.length : 0;
  const firstName = user?.firstName || user?.fullName?.split(' ')[0] || 'there';

  return (
    <ErrorBoundary>
      <PageHeader title="Dashboard" subtitle={`Welcome back, ${firstName}. Here's what's happening.`} />

      {globalError && (
        <Alert type="error" message={globalError.message} className="mb-4" />
      )}

      <div className="mb-4 grid grid-cols-1 gap-4 md:grid-cols-3">
        <StatCard title="Total Posts" value={postsCount} color="amber" trend={`+${postsCount} this week`} href="/admin/posts" loading={loading.posts} error={errors.posts?.message} icon={<PostsIcon />} />
        <StatCard title="Total Events" value={eventsCount} color="green" trend={`${eventsCount} upcoming`} href="/admin/events" loading={loading.events} error={errors.events?.message} icon={<EventsIcon />} />
        <StatCard title="Total Users" value={usersCount} color="blue" trend={`+${usersCount} this month`} href="/admin/users" loading={loading.users} error={errors.users?.message} icon={<UsersIcon />} />
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <Card className="p-4 sm:p-[18px_20px]">
          <h2 className="mb-4 text-[13px] font-bold text-[var(--color-text)]">Quick Actions</h2>
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
            {actions.map(({ label, href, icon: Icon }) => (
              <button
                key={href}
                type="button"
                onClick={() => router.push(href)}
                className="flex cursor-pointer flex-col items-center gap-[7px] rounded-[8px] border border-[var(--color-border)] p-[12px_8px] text-center transition-colors duration-150 hover:border-[rgba(201,149,42,0.3)] hover:bg-[var(--color-accent-sub)]"
              >
                <Icon className="text-[var(--color-accent)]" />
                <span className="text-[10px] font-medium text-[var(--color-text-sub)] sm:text-[11px]">{label}</span>
              </button>
            ))}
          </div>
        </Card>

        <Card className="p-4 sm:p-[18px_20px]">
          <div className="mb-2 flex items-center justify-between">
            <h2 className="text-[13px] font-bold text-[var(--color-text)]">System Status</h2>
            <Button variant="ghost" size="xs" icon={<RefreshIcon />} onClick={() => executeAll()}>Refresh</Button>
          </div>
          {[
            ['API Status', <Badge key="api" color={globalError ? 'yellow' : 'green'}>{globalError ? 'Degraded' : 'Operational'}</Badge>],
            ['Database', <Badge key="db" color="green">Connected</Badge>],
            ['Last Backup', <Badge key="backup" color="neutral">2 hours ago</Badge>],
          ].map(([label, value], index) => (
            <div key={label as string} className="flex items-center justify-between border-b border-[var(--color-border)] py-2.5 last:border-0">
              <span className="text-[13px] text-[var(--color-text-sub)]">{label}</span>
              {value}
            </div>
          ))}
          {(errors.posts || errors.events || errors.users) && (
            <div className="mt-3 space-y-2">
              <Alert type="warning" message="Some admin data failed to load. Retry the affected endpoint if needed." />
              {errors.posts && <Button size="xs" variant="ghost" onClick={() => retry('posts')}>Retry posts</Button>}
              {errors.events && <Button size="xs" variant="ghost" onClick={() => retry('events')}>Retry events</Button>}
              {errors.users && <Button size="xs" variant="ghost" onClick={() => retry('users')}>Retry users</Button>}
            </div>
          )}
        </Card>
      </div>
    </ErrorBoundary>
  );
}
