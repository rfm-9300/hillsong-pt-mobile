'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import GroupsList from '@/app/components/GroupsList';
import { Button, PageHeader } from '@/app/components/ui';
import { PlusIcon } from '@/app/components/icons/Icons';

export default function GroupsPage() {
  const router = useRouter();
  const [refreshKey] = useState(0);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Groups"
        subtitle="Manage connection groups shown in the mobile app"
        breadcrumbs={['Admin', 'Groups']}
        actions={<Button variant="primary" size="sm" icon={<PlusIcon />} onClick={() => router.push('/admin/groups/create')}>New Group</Button>}
      />

      <GroupsList refreshKey={refreshKey} />
    </div>
  );
}
