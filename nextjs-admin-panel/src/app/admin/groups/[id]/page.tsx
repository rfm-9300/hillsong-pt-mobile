'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import GroupForm from '@/app/components/GroupForm';
import { Alert, LoadingOverlay, PageHeader } from '@/app/components/ui';
import { api, ENDPOINTS } from '@/lib/api';
import { Group } from '@/lib/types';

export default function EditGroupPage() {
  const params = useParams();
  const groupId = params.id as string;
  const [group, setGroup] = useState<Group | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchGroup = async () => {
      try {
        setLoading(true);
        const response = await api.get<{ data: Group }>(ENDPOINTS.ADMIN_GROUP_BY_ID(groupId));
        setGroup(response?.data ?? null);
      } catch (fetchError) {
        console.error('Error fetching group:', fetchError);
        setError(fetchError instanceof Error ? fetchError.message : 'Failed to load group');
      } finally {
        setLoading(false);
      }
    };

    if (groupId) {
      fetchGroup();
    }
  }, [groupId]);

  if (loading) {
    return (
      <div className="space-y-6">
        <PageHeader title="Edit Group" />
        <LoadingOverlay show={true} message="Loading group..." />
      </div>
    );
  }

  if (error || !group) {
    return (
      <Alert
        type="error"
        message={error ?? 'The requested group could not be found.'}
        onClose={() => setError(null)}
      />
    );
  }

  return <GroupForm mode="edit" groupId={groupId} initialGroup={group} />;
}
