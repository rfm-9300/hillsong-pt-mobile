'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../context/AuthContext';
import { api, ENDPOINTS } from '../../../lib/api';
import { useEnhancedMultipleApiCalls } from '../../hooks/useApiCall';
import { useErrorContext } from '../../context/ErrorContext';
import {
  PageHeader,
  StatCard,
  QuickActions,
  ErrorBoundary,
  DashboardSkeleton,
  Card,
  RetryButton,

  AnimatedRow,
} from '../../components/ui';

interface DashboardData extends Record<string, unknown> {
  posts: { data: { postList: unknown[] } } | null;
  events: { data: { events: unknown[] } } | null;
  users: { data: { users: unknown[] } } | null;
}

export default function DashboardPage() {
  const { isAuthenticated } = useAuth();
  const router = useRouter();
  const { showSuccess } = useErrorContext();

  // API calls configuration with enhanced error handling
  const apiCalls = {
    posts: () => api.get<{ data: { postList: unknown[] } }>(ENDPOINTS.POSTS),
    events: () => api.get<{ data: { events: unknown[] } }>(ENDPOINTS.EVENTS),
    users: () => api.get<{ data: { users: unknown[] } }>(ENDPOINTS.USERS),
  };

  const {
    data,
    loading,
    errors,
    globalLoading,
    globalError,
    executeAll,
    retry,
    progress,
  } = useEnhancedMultipleApiCalls<DashboardData>(apiCalls, {
    batchLoadingKey: 'dashboard_data',
    context: 'Dashboard Data Loading',
    message: 'Loading dashboard statistics...',
    showProgress: true,
    onSuccess: () => {
      showSuccess('Dashboard data loaded successfully');
    },
  });

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    executeAll();
  }, [isAuthenticated, router, executeAll]);

  // Show loading skeleton while data is being fetched
  if (globalLoading && !data.posts && !data.events && !data.users) {
    return (
      <div className="p-4">
        <PageHeader
          title="Admin Dashboard"
          subtitle="Loading dashboard data..."
        />
        {progress !== undefined && (
          <div className="mb-6">
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
            <p className="text-sm text-gray-600 mt-2">{progress}% complete</p>
          </div>
        )}
        <DashboardSkeleton />
      </div>
    );
  }

  const stats = {
    posts: {
      count: data.posts?.data?.postList?.length || 0,
      loading: loading.posts || false,
      error: errors.posts?.message,
    },
    events: {
      count: data.events?.data?.events?.length || 0,
      loading: loading.events || false,
      error: errors.events?.message,
    },
    users: {
      count: data.users?.data?.users?.length || 0,
      loading: loading.users || false,
      error: errors.users?.message,
    },
  };

  return (
    <ErrorBoundary>
      <div className="p-4 sm:p-6 space-y-6 page-transition">
        <div className="animate-in fade-in" style={{ animationDuration: '300ms' }}>
          <PageHeader
            title="Admin Dashboard"
            subtitle="Welcome back! Here's what's happening with your community."
          />
        </div>

        {/* Global Error Handling with Retry */}
        {globalError && (
          <Card className="p-4 border-red-200 bg-red-50">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <h3 className="text-sm font-medium text-red-800 mb-1">
                  Failed to load some dashboard data
                </h3>
                <p className="text-sm text-red-700">
                  {globalError.message}
                </p>
              </div>
              <RetryButton
                onRetry={() => { executeAll(); }}
                variant="danger"
                size="sm"
                className="ml-4"
              >
                Retry All
              </RetryButton>
            </div>
          </Card>
        )}

        {/* Statistics Cards */}
        <AnimatedRow className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6" spacing="">
          <StatCard
            title="Total Posts"
            value={stats.posts.count}
            loading={stats.posts.loading}
            error={stats.posts.error}
            color="blue"
            href="/admin/posts"
            icon={
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                />
              </svg>
            }
          />
          <StatCard
            title="Total Events"
            value={stats.events.count}
            loading={stats.events.loading}
            error={stats.events.error}
            color="green"
            href="/admin/events"
            icon={
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            }
          />
          <StatCard
            title="Total Users"
            value={stats.users.count}
            loading={stats.users.loading}
            error={stats.users.error}
            color="purple"
            href="/admin/users"
            icon={
              <svg fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"
                />
              </svg>
            }
          />
        </AnimatedRow>

        {/* Quick Actions and Recent Activity */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="animate-in fade-in slide-in-from-left" style={{ animationDelay: '400ms', animationDuration: '400ms' }}>
            <QuickActions />
          </div>
          
          <div className="animate-in fade-in slide-in-from-right" style={{ animationDelay: '500ms', animationDuration: '400ms' }}>
            <Card className="p-4 sm:p-6">
              <h3 className="text-base sm:text-lg font-semibold text-gray-900 mb-4">System Status</h3>
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">API Status</span>
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  Operational
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">Database</span>
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                  Connected
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-600">Last Backup</span>
                <span className="text-sm text-gray-900">2 hours ago</span>
              </div>
              {(errors.posts || errors.events || errors.users) && (
                <div className="pt-4 border-t border-gray-200">
                  <div className="space-y-2">
                    {errors.posts && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-red-600">Posts failed to load</span>
                        <RetryButton
                          onRetry={() => retry('posts')}
                          size="sm"
                          variant="ghost"
                          className="text-xs"
                        >
                          Retry
                        </RetryButton>
                      </div>
                    )}
                    {errors.events && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-red-600">Events failed to load</span>
                        <RetryButton
                          onRetry={() => retry('events')}
                          size="sm"
                          variant="ghost"
                          className="text-xs"
                        >
                          Retry
                        </RetryButton>
                      </div>
                    )}
                    {errors.users && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-red-600">Users failed to load</span>
                        <RetryButton
                          onRetry={() => retry('users')}
                          size="sm"
                          variant="ghost"
                          className="text-xs"
                        >
                          Retry
                        </RetryButton>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          </Card>
          </div>
        </div>
      </div>
    </ErrorBoundary>
  );
}