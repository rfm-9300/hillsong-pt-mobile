'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { AttendanceRecord, AttendanceStatus, EventType } from '@/lib/types';

type ApiResponse<T = unknown> = { success: boolean; data?: T };
import { 
  Card, 
  Button, 
  EmptyState, 
  LoadingOverlay,
  Alert,
  StatCard,
  NavigationHeader
} from '@/app/components/ui';
import { useApiCall } from '@/app/hooks';
import { api } from '@/lib/api';
import Link from 'next/link';
import { cn } from '@/lib/utils';

interface AttendanceStats {
  totalEvents: number;
  totalServices: number;
  totalKidsServices: number;
  totalAttendees: number;
  recentActivity: AttendanceRecord[];
}

const AttendanceOverviewPage: React.FC = () => {
  const [stats, setStats] = useState<AttendanceStats | null>(null);
  const [recentActivity, setRecentActivity] = useState<AttendanceRecord[]>([]);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const { 
    execute: fetchStats, 
    loading: loadingStats 
  } = useApiCall(api.attendance.getStats);

  const { 
    execute: fetchRecentActivity, 
    loading: loadingActivity 
  } = useApiCall(api.attendance.getRecent);

  const loadData = useCallback(async () => {
    try {
      const [statsResponse, activityResponse] = await Promise.all([
        fetchStats() as Promise<ApiResponse<AttendanceStats>>,
        fetchRecentActivity(10) as Promise<ApiResponse<AttendanceRecord[]>>
      ]);

      if (statsResponse && statsResponse.success && statsResponse.data) {
        setStats(statsResponse.data);
      }

      if (activityResponse && activityResponse.success && activityResponse.data) {
        setRecentActivity(activityResponse.data);
      }
    } catch (error) {
      console.error('Failed to load attendance data:', error);
      setAlert({ type: 'error', message: 'Failed to load attendance data' });
    }
  }, [fetchStats, fetchRecentActivity]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const formatTimestamp = (timestamp: string) => {
    return new Date(timestamp).toLocaleString();
  };

  const getStatusColor = (status: AttendanceStatus) => {
    switch (status) {
      case AttendanceStatus.CHECKED_IN:
        return 'text-green-600';
      case AttendanceStatus.CHECKED_OUT:
        return 'text-blue-600';
      case AttendanceStatus.NO_SHOW:
        return 'text-red-600';
      case AttendanceStatus.EMERGENCY:
        return 'text-orange-600';
      default:
        return 'text-gray-600';
    }
  };

  const getEventTypeIcon = (eventType: EventType) => {
    switch (eventType) {
      case EventType.EVENT:
        return '🎉';
      case EventType.SERVICE:
        return '⛪';
      case EventType.KIDS_SERVICE:
        return '👶';
      default:
        return '📅';
    }
  };

  const navigationCards = [
    {
      title: 'Event Attendance',
      description: 'Manage attendance for regular events',
      href: '/admin/attendance/event',
      icon: '🎉',
      color: 'bg-blue-50 border-blue-200 hover:bg-blue-100',
    },
    {
      title: 'Service Attendance',
      description: 'Track attendance for regular services',
      href: '/admin/attendance/service',
      icon: '⛪',
      color: 'bg-green-50 border-green-200 hover:bg-green-100',
    },
    {
      title: 'Kids Service Attendance',
      description: 'Manage children\'s program attendance',
      href: '/admin/attendance/kids-service',
      icon: '👶',
      color: 'bg-purple-50 border-purple-200 hover:bg-purple-100',
    },
    {
      title: 'Reports & Analytics',
      description: 'Generate attendance reports and analytics',
      href: '/admin/attendance/reports',
      icon: '📊',
      color: 'bg-orange-50 border-orange-200 hover:bg-orange-100',
    },
  ];

  return (
    <div className="space-y-6">
      <NavigationHeader
        title="Attendance Management"
        subtitle="Overview of attendance tracking across all events and services"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Attendance', current: true },
        ]}
      >
        <Button
          variant="primary"
          onClick={loadData}
          loading={loadingStats || loadingActivity}
        >
          Refresh
        </Button>
      </NavigationHeader>

      {alert && (
        <Alert
          type={alert.type}
          message={alert.message}
          onClose={() => setAlert(null)}
        />
      )}

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <StatCard
          title="Total Events"
          value={stats?.totalEvents || 0}
          icon="🎉"
          loading={loadingStats}
          onClick={() => window.location.href = '/admin/attendance/event'}
        />
        <StatCard
          title="Total Services"
          value={stats?.totalServices || 0}
          icon="⛪"
          loading={loadingStats}
          onClick={() => window.location.href = '/admin/attendance/service'}
        />
        <StatCard
          title="Kids Services"
          value={stats?.totalKidsServices || 0}
          icon="👶"
          loading={loadingStats}
          onClick={() => window.location.href = '/admin/attendance/kids-service'}
        />
        <StatCard
          title="Total Attendees"
          value={stats?.totalAttendees || 0}
          icon="👥"
          loading={loadingStats}
          onClick={() => window.location.href = '/admin/attendance/reports'}
        />
      </div>

      {/* Navigation Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {navigationCards.map((card) => (
          <Link key={card.href} href={card.href}>
            <Card className={cn(
              'p-6 transition-all duration-200 cursor-pointer',
              card.color
            )}>
              <div className="flex items-start space-x-4">
                <div className="text-3xl">{card.icon}</div>
                <div className="flex-1">
                  <h3 className="text-lg font-medium text-gray-900 mb-2">
                    {card.title}
                  </h3>
                  <p className="text-sm text-gray-600">
                    {card.description}
                  </p>
                </div>
                <div className="text-gray-400">
                  →
                </div>
              </div>
            </Card>
          </Link>
        ))}
      </div>

      {/* Recent Activity */}
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-medium text-gray-900">Recent Activity</h3>
          <Link href="/admin/attendance/reports">
            <Button variant="ghost" size="sm">
              View All Reports
            </Button>
          </Link>
        </div>

        <div className="relative">
          <LoadingOverlay show={loadingActivity} />
          
          {recentActivity.length === 0 ? (
            <EmptyState
              title="No recent activity"
              description="No attendance records have been created recently"
              icon={<span className="text-4xl">📋</span>}
            />
          ) : (
            <div className="space-y-3">
              {recentActivity.map((record) => (
                <div
                  key={record.id}
                  className="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <div className="flex items-center space-x-3">
                    <span className="text-lg">
                      {getEventTypeIcon(record.eventType)}
                    </span>
                    <div>
                      <div className="font-medium text-gray-900">
                        {record.attendeeName}
                      </div>
                      <div className="text-sm text-gray-500">
                        {record.eventName}
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-3">
                    <span className={cn(
                      'text-sm font-medium',
                      getStatusColor(record.status)
                    )}>
                      {record.status === AttendanceStatus.CHECKED_IN && 'Checked In'}
                      {record.status === AttendanceStatus.CHECKED_OUT && 'Checked Out'}
                      {record.status === AttendanceStatus.NO_SHOW && 'No Show'}
                      {record.status === AttendanceStatus.EMERGENCY && 'Emergency'}
                    </span>
                    <span className="text-xs text-gray-400">
                      {formatTimestamp(record.timestamp)}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </Card>

      {/* Quick Actions */}
      <Card className="p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          <Link href="/admin/attendance/event">
            <Button variant="secondary" className="w-full justify-start">
              <span className="mr-2">🎉</span>
              Manage Events
            </Button>
          </Link>
          <Link href="/admin/attendance/service">
            <Button variant="secondary" className="w-full justify-start">
              <span className="mr-2">⛪</span>
              Manage Services
            </Button>
          </Link>
          <Link href="/admin/attendance/kids-service">
            <Button variant="secondary" className="w-full justify-start">
              <span className="mr-2">👶</span>
              Kids Programs
            </Button>
          </Link>
          <Link href="/admin/attendance/reports">
            <Button variant="secondary" className="w-full justify-start">
              <span className="mr-2">📊</span>
              View Reports
            </Button>
          </Link>
        </div>
      </Card>
    </div>
  );
};

export default AttendanceOverviewPage;