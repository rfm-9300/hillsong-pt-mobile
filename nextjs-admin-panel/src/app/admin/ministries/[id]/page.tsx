'use client';

import { useEffect, useState } from 'react';
import { useParams, useSearchParams, useRouter } from 'next/navigation';
import MinistryForm from '@/app/components/MinistryForm';
import MinistryRosterTable from '@/app/components/MinistryRosterTable';
import { Alert, Card, LoadingOverlay, NavigationHeader } from '@/app/components/ui';
import { api, ENDPOINTS } from '@/lib/api';
import { MinistryDoc } from '@/lib/types';
import { formatMinistryType } from '@/lib/ministries';

type Tab = 'overview' | 'roster' | 'activities' | 'attendance' | 'stats' | 'settings';

const TABS: Array<{ key: Tab; label: string }> = [
  { key: 'overview', label: 'Overview' },
  { key: 'roster', label: 'Roster' },
  { key: 'activities', label: 'Activities' },
  { key: 'attendance', label: 'Attendance' },
  { key: 'stats', label: 'Stats' },
  { key: 'settings', label: 'Settings' },
];

export default function MinistryDetailPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const router = useRouter();
  const ministryId = params.id as string;
  const tab = (searchParams.get('tab') as Tab) ?? 'overview';

  const [ministry, setMinistry] = useState<MinistryDoc | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const response = await api.get<{ data: MinistryDoc }>(ENDPOINTS.ADMIN_MINISTRY_BY_ID(ministryId));
        setMinistry(response?.data ?? null);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load ministry');
      } finally {
        setLoading(false);
      }
    };
    if (ministryId) void load();
  }, [ministryId]);

  if (loading) {
    return (
      <div className="space-y-6">
        <NavigationHeader title="Ministry" />
        <LoadingOverlay show message="Loading ministry..." />
      </div>
    );
  }

  if (error || !ministry) {
    return <Alert type="error" message={error ?? 'Ministry not found.'} onClose={() => setError(null)} />;
  }

  const setTab = (next: Tab) => router.replace(`/admin/ministries/${ministryId}?tab=${next}`);

  return (
    <div className="space-y-6">
      <NavigationHeader
        title={ministry.name}
        subtitle={`${formatMinistryType(ministry.type)} · ${ministry.memberCount} members · ${ministry.pendingCount} pending`}
        showBackButton
        backButtonText="Back to Ministries"
        backButtonHref="/admin/ministries"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Ministries', href: '/admin/ministries' },
          { label: ministry.name, current: true },
        ]}
      />

      <div className="flex flex-wrap gap-1 border-b border-[var(--color-border)]">
        {TABS.map((t) => (
          <button
            key={t.key}
            type="button"
            onClick={() => setTab(t.key)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
              tab === t.key
                ? 'border-[var(--color-accent)] text-[var(--color-accent)]'
                : 'border-transparent text-[var(--color-text-sub)] hover:text-[var(--color-text)]'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'overview' && <OverviewTab ministry={ministry} />}
      {tab === 'roster' && <MinistryRosterTable ministryId={ministry.id} />}
      {tab === 'activities' && (
        <Card><div className="p-6 text-sm text-[var(--color-text-sub)]">Activities scheduling — coming in Phase 3.</div></Card>
      )}
      {tab === 'attendance' && (
        <Card><div className="p-6 text-sm text-[var(--color-text-sub)]">Attendance tracking — coming in Phase 3.</div></Card>
      )}
      {tab === 'stats' && (
        <Card><div className="p-6 text-sm text-[var(--color-text-sub)]">Statistics — coming in Phase 4.</div></Card>
      )}
      {tab === 'settings' && <MinistryForm mode="edit" ministryId={ministry.id} initialMinistry={ministry} />}
    </div>
  );
}

function OverviewTab({ ministry }: { ministry: MinistryDoc }) {
  return (
    <div className="grid gap-4 md:grid-cols-3">
      <Card className="p-5 md:col-span-2">
        <div className="text-[11px] font-semibold uppercase tracking-[0.5px] text-[var(--color-text-muted)] mb-2">Description</div>
        <p className="text-sm text-[var(--color-text)] whitespace-pre-wrap">{ministry.description}</p>
        {ministry.tags.length > 0 && (
          <div className="mt-4 flex flex-wrap gap-1.5">
            {ministry.tags.map((tag) => (
              <span key={tag} className="rounded-full bg-[var(--color-surface)] px-2 py-0.5 text-[11px] text-[var(--color-text-sub)]">#{tag}</span>
            ))}
          </div>
        )}
      </Card>
      <Card className="p-5">
        <div className="space-y-3 text-sm">
          <Row label="Type" value={formatMinistryType(ministry.type)} />
          <Row label="Active members" value={ministry.memberCount.toString()} />
          <Row label="Pending requests" value={ministry.pendingCount.toString()} />
          <Row label="Leaders" value={ministry.leaderUserIds.length.toString()} />
          <Row label="Coordinators" value={ministry.coordinatorUserIds.length.toString()} />
          <Row label="Status" value={ministry.isActive ? 'Active' : 'Hidden'} />
          <Row label="Joinable" value={ministry.isJoinable ? 'Yes' : 'No'} />
          <Row label="Approval required" value={ministry.requiresApproval ? 'Yes' : 'No'} />
        </div>
      </Card>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-[var(--color-text-sub)]">{label}</span>
      <span className="font-medium text-[var(--color-text)]">{value}</span>
    </div>
  );
}
