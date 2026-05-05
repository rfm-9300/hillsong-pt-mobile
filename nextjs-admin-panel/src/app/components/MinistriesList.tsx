'use client';

import { useEffect, useState } from 'react';
import { Card, EmptyState, LoadingSkeleton, Alert } from './ui';
import MinistryCard from './MinistryCard';
import { api, ENDPOINTS } from '@/lib/api';
import { MinistrySummary } from '@/lib/types';

interface MinistriesListProps {
  refreshKey?: number;
}

export default function MinistriesList({ refreshKey = 0 }: MinistriesListProps) {
  const [ministries, setMinistries] = useState<MinistrySummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchMinistries = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await api.get<{ data: { content: MinistrySummary[] } }>(
          `${ENDPOINTS.ADMIN_MINISTRIES}?includeInactive=true&size=100&sortBy=updatedAt&sortDir=desc`
        );
        setMinistries(response?.data?.content ?? []);
      } catch (fetchError) {
        console.error('Error fetching ministries:', fetchError);
        setError(fetchError instanceof Error ? fetchError.message : 'Failed to load ministries');
      } finally {
        setLoading(false);
      }
    };
    fetchMinistries();
  }, [refreshKey]);

  const handleDeleted = (ministryId: string) => {
    setMinistries((current) =>
      current.map((m) => (m.id === ministryId ? { ...m, isActive: false } : m))
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

  if (error) return <Alert type="error" message={error} onClose={() => setError(null)} />;

  if (ministries.length === 0) {
    return (
      <Card>
        <EmptyState
          title="No ministries yet"
          description="Create your first ministry — service team or connection-group umbrella — to start registering volunteers."
          actionText="Create Ministry"
          onAction={() => {
            window.location.href = '/admin/ministries/create';
          }}
        />
      </Card>
    );
  }

  return (
    <div className="space-y-3">
      {ministries.map((ministry) => (
        <MinistryCard key={ministry.id} ministry={ministry} onDelete={handleDeleted} />
      ))}
    </div>
  );
}
