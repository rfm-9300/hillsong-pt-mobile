'use client';

import { useEffect, useState } from 'react';
import { Card, EmptyState, LoadingSkeleton, Alert } from './ui';
import GroupCard from './GroupCard';
import { api, ENDPOINTS } from '@/lib/api';
import { GroupSummary } from '@/lib/types';

interface GroupsListProps {
  refreshKey?: number;
}

export default function GroupsList({ refreshKey = 0 }: GroupsListProps) {
  const [groups, setGroups] = useState<GroupSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchGroups = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await api.get<{ data: { content: GroupSummary[] } }>(
          `${ENDPOINTS.ADMIN_GROUPS}?includeInactive=true&size=100&sortBy=updatedAt&sortDir=desc`
        );

        setGroups(response?.data?.content ?? []);
      } catch (fetchError) {
        console.error('Error fetching groups:', fetchError);
        setError(fetchError instanceof Error ? fetchError.message : 'Failed to load groups');
      } finally {
        setLoading(false);
      }
    };

    fetchGroups();
  }, [refreshKey]);

  const handleDeleted = (groupId: string) => {
    setGroups((current) =>
      current.map((group) =>
        group.id === groupId
          ? {
              ...group,
              isActive: false,
            }
          : group
      )
    );
  };

  if (loading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 6 }).map((_, index) => (
          <LoadingSkeleton key={index} className="h-[86px] rounded-[10px]" />
        ))}
      </div>
    );
  }

  if (error) {
    return <Alert type="error" message={error} onClose={() => setError(null)} />;
  }

  if (groups.length === 0) {
    return (
      <Card>
        <EmptyState
          title="No groups found"
          description="Create your first connection group to make it available in the app."
          actionText="Create Group"
          onAction={() => {
            window.location.href = '/admin/groups/create';
          }}
        />
      </Card>
    );
  }

  return (
    <div className="space-y-3">
      {groups.map((group) => (
        <GroupCard key={group.id} group={group} onDelete={handleDeleted} />
      ))}
    </div>
  );
}
