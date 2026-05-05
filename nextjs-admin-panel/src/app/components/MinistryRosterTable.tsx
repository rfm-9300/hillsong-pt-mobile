'use client';

import { useEffect, useState } from 'react';
import { Alert, Badge, Button, Card, EmptyState, LoadingSkeleton } from './ui';
import { api, ENDPOINTS } from '@/lib/api';
import { MembershipStatus, MinistryMember, MinistryRole } from '@/lib/types';

interface MinistryRosterTableProps {
  ministryId: string;
  refreshKey?: number;
}

export default function MinistryRosterTable({ ministryId, refreshKey = 0 }: MinistryRosterTableProps) {
  const [members, setMembers] = useState<MinistryMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<MembershipStatus>(MembershipStatus.ACTIVE);
  const [actingId, setActingId] = useState<string | null>(null);

  const load = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get<{ data: { content: MinistryMember[] } }>(
        `${ENDPOINTS.MINISTRY_MEMBERS(ministryId)}?status=${statusFilter}&size=200`
      );
      setMembers(response?.data?.content ?? []);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load members');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ministryId, statusFilter, refreshKey]);

  const updateRole = async (userId: string, role: MinistryRole) => {
    setActingId(userId);
    try {
      await api.put(ENDPOINTS.MINISTRY_MEMBER(ministryId, userId), { role });
      await load();
    } finally {
      setActingId(null);
    }
  };

  const remove = async (userId: string) => {
    setActingId(userId);
    try {
      await api.delete(ENDPOINTS.MINISTRY_MEMBER(ministryId, userId));
      await load();
    } finally {
      setActingId(null);
    }
  };

  const approve = async (userId: string) => {
    setActingId(userId);
    try {
      await api.post(ENDPOINTS.MINISTRY_MEMBER_APPROVE(ministryId, userId), {});
      await load();
    } finally {
      setActingId(null);
    }
  };

  const reject = async (userId: string) => {
    setActingId(userId);
    try {
      await api.post(ENDPOINTS.MINISTRY_MEMBER_REJECT(ministryId, userId), {});
      await load();
    } finally {
      setActingId(null);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center gap-2">
        {(Object.values(MembershipStatus) as MembershipStatus[]).map((s) => (
          <button
            key={s}
            type="button"
            onClick={() => setStatusFilter(s)}
            className={`rounded-full px-3 py-1 text-xs font-medium ${
              statusFilter === s
                ? 'bg-[var(--color-accent)] text-white'
                : 'bg-[var(--color-surface)] text-[var(--color-text-sub)] hover:bg-[var(--color-surface-hover)]'
            }`}
          >
            {s}
          </button>
        ))}
      </div>

      {error && <Alert type="error" message={error} onClose={() => setError(null)} />}

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <LoadingSkeleton key={i} className="h-[64px] rounded-[10px]" />
          ))}
        </div>
      ) : members.length === 0 ? (
        <Card>
          <EmptyState
            title="No members in this status"
            description="Switch the filter or add new members from the actions above."
          />
        </Card>
      ) : (
        <div className="space-y-2">
          {members.map((m) => (
            <Card key={m.id} className="p-3">
              <div className="grid grid-cols-1 items-center gap-3 md:grid-cols-[1fr_180px_180px_220px]">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-[14px] font-bold text-[var(--color-text)] truncate">{m.fullName || m.email}</span>
                    <Badge color={roleColor(m.role)} size="xs">{m.role}</Badge>
                    <Badge color={statusColor(m.status)} size="xs">{m.status}</Badge>
                  </div>
                  <div className="text-[12px] text-[var(--color-text-sub)] truncate">{m.email}</div>
                </div>
                <div className="text-[12px] text-[var(--color-text-sub)] truncate">{m.phone ?? '—'}</div>
                <div className="text-[12px] text-[var(--color-text-sub)]">
                  Joined {new Date(m.joinedAt).toLocaleDateString()}
                </div>
                <div className="flex flex-wrap gap-1.5 justify-end">
                  {m.status === MembershipStatus.PENDING && (
                    <>
                      <Button size="xs" variant="primary" onClick={() => approve(m.userId)} loading={actingId === m.userId}>
                        Approve
                      </Button>
                      <Button size="xs" variant="danger" onClick={() => reject(m.userId)} loading={actingId === m.userId}>
                        Reject
                      </Button>
                    </>
                  )}
                  {m.status === MembershipStatus.ACTIVE && (
                    <>
                      {m.role === MinistryRole.VOLUNTEER ? (
                        <Button size="xs" variant="ghost" onClick={() => updateRole(m.userId, MinistryRole.LEADER)} loading={actingId === m.userId}>
                          Make leader
                        </Button>
                      ) : (
                        <Button size="xs" variant="ghost" onClick={() => updateRole(m.userId, MinistryRole.VOLUNTEER)} loading={actingId === m.userId}>
                          Demote
                        </Button>
                      )}
                      <Button size="xs" variant="danger" onClick={() => remove(m.userId)} loading={actingId === m.userId}>
                        Remove
                      </Button>
                    </>
                  )}
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

function roleColor(role: MinistryRole): 'green' | 'blue' | 'amber' | 'neutral' {
  switch (role) {
    case MinistryRole.COORDINATOR: return 'amber';
    case MinistryRole.LEADER: return 'blue';
    case MinistryRole.VOLUNTEER: return 'neutral';
    default: return 'neutral';
  }
}

function statusColor(status: MembershipStatus): 'green' | 'amber' | 'red' | 'neutral' {
  switch (status) {
    case MembershipStatus.ACTIVE: return 'green';
    case MembershipStatus.PENDING: return 'amber';
    case MembershipStatus.REJECTED: return 'red';
    case MembershipStatus.INACTIVE: return 'neutral';
    default: return 'neutral';
  }
}
