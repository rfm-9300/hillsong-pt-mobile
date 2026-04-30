'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { AttendanceRecord, AttendanceStatus } from '@/lib/types';
import { Alert, Badge, Button, Card, EmptyState, PageHeader, TableHeader, TableRow } from '@/app/components/ui';
import { useMultipleApiCalls } from '@/app/hooks';
import { api } from '@/lib/api';
import { AttendanceIcon, EventsIcon, KidsIcon, RefreshIcon, ReportsIcon, ServiceIcon } from '@/app/components/icons/Icons';
import type { BadgeColor } from '@/app/components/ui/Badge';

type ApiResponse<T = unknown> = { success: boolean; data?: T };

interface AttendanceStats {
  totalEvents: number;
  totalServices: number;
  totalKidsServices: number;
  totalAttendees: number;
  recentActivity: AttendanceRecord[];
}

interface AttendanceData extends Record<string, unknown> {
  stats: ApiResponse<AttendanceStats> | null;
  recentActivity: ApiResponse<AttendanceRecord[]> | null;
}

const cols = [
  { label: 'Name', width: '200px' },
  { label: 'Event', width: '1fr' },
  { label: 'Type', width: '140px' },
  { label: 'Status', width: '130px' },
  { label: 'Time', width: '140px' },
];

const statusColors: Record<AttendanceStatus, BadgeColor> = {
  [AttendanceStatus.CHECKED_IN]: 'green',
  [AttendanceStatus.CHECKED_OUT]: 'neutral',
  [AttendanceStatus.NO_SHOW]: 'red',
  [AttendanceStatus.EMERGENCY]: 'red',
};

export default function AttendanceOverviewPage() {
  const router = useRouter();
  const [stats, setStats] = useState<AttendanceStats | null>(null);
  const [recentActivity, setRecentActivity] = useState<AttendanceRecord[]>([]);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [hasExecuted, setHasExecuted] = useState(false);

  const apiCalls = useMemo(() => ({
    stats: () => api.attendance.getStats() as Promise<ApiResponse<AttendanceStats>>,
    recentActivity: () => api.attendance.getRecent(10) as Promise<ApiResponse<AttendanceRecord[]>>,
  }), []);

  const { data, globalLoading, executeAll } = useMultipleApiCalls<AttendanceData>(apiCalls, {
    batchLoadingKey: 'attendance_overview',
    showToUser: false,
  });

  useEffect(() => {
    if (data.stats?.success && data.stats.data) setStats(data.stats.data);
    if (data.recentActivity?.success && data.recentActivity.data) setRecentActivity(data.recentActivity.data);
  }, [data]);

  useEffect(() => {
    if (!hasExecuted) {
      setHasExecuted(true);
      executeAll().catch(() => setAlert({ type: 'error', message: 'Failed to load attendance data' }));
    }
  }, [hasExecuted, executeAll]);

  const loadData = async () => {
    try {
      await executeAll();
    } catch {
      setAlert({ type: 'error', message: 'Failed to load attendance data' });
    }
  };

  const cards = [
    { label: 'Event Attendance', value: stats?.totalEvents || 0, sub: 'Regular events', href: '/admin/attendance/event', icon: EventsIcon },
    { label: 'Service Attendance', value: stats?.totalServices || 0, sub: 'Church services', href: '/admin/attendance/service', icon: ServiceIcon },
    { label: 'Kids Services', value: stats?.totalKidsServices || 0, sub: 'Children programs', href: '/admin/attendance/kids-service', icon: KidsIcon },
    { label: 'Reports', value: '→', sub: `${stats?.totalAttendees || 0} total attendees`, href: '/admin/attendance/reports', icon: ReportsIcon },
  ];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Attendance"
        subtitle="Overview of attendance tracking across all event types"
        actions={<Button variant="ghost" size="sm" icon={<RefreshIcon />} onClick={loadData} loading={globalLoading}>Refresh</Button>}
      />

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        {cards.map(({ label, value, sub, href, icon: Icon }) => (
          <Card key={href} className="cursor-pointer p-4 transition-all hover:border-[var(--color-border-med)] hover:shadow-[0_4px_16px_rgba(0,0,0,0.08)] sm:p-[20px_22px]" onClick={() => router.push(href)}>
            <div className="mb-5 flex items-start justify-between">
              <div className="text-[12px] font-bold uppercase tracking-[0.3px] text-[var(--color-text-sub)]">{label}</div>
              <Icon className="text-[var(--color-accent)]" />
            </div>
            <div className="font-display text-[32px] leading-none text-[var(--color-text)] sm:text-[40px]">{value}</div>
            <div className="mt-1 text-[12px] text-[var(--color-text-sub)]">{sub}</div>
          </Card>
        ))}
      </div>

      <PageHeader title="Recent Records" subtitle="Latest check-ins across all event types" />

      {recentActivity.length === 0 ? (
        <Card>
          <EmptyState title="No recent activity" description="No attendance records have been created recently." icon={<AttendanceIcon />} />
        </Card>
      ) : (
        <Card className="overflow-hidden">
          <TableHeader cols={cols} />
          {recentActivity.map((record, index) => (
            <TableRow
              key={record.id}
              cols={cols}
              last={index === recentActivity.length - 1}
              cells={[
                <span key="name" className="font-semibold text-[var(--color-text)]">{record.attendeeName}</span>,
                <span key="event" className="truncate">{record.eventName}</span>,
                <Badge key="type" color="neutral">{record.eventType.replace(/_/g, ' ')}</Badge>,
                <Badge key="status" color={statusColors[record.status] || 'neutral'}>{record.status.replace(/_/g, ' ')}</Badge>,
                <span key="time">{formatTime(record.timestamp)}</span>,
              ]}
            />
          ))}
        </Card>
      )}
    </div>
  );
}

function formatTime(timestamp: string) {
  return new Date(timestamp).toLocaleString('en-GB', { hour: '2-digit', minute: '2-digit' });
}
