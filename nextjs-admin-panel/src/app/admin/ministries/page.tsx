'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import MinistriesList from '@/app/components/MinistriesList';
import { Button, PageHeader } from '@/app/components/ui';
import { PlusIcon } from '@/app/components/icons/Icons';

export default function MinistriesPage() {
  const router = useRouter();
  const [refreshKey] = useState(0);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Ministries"
        subtitle="Service teams and connection-group umbrellas. Leaders register volunteers; staff see statistics."
        breadcrumbs={['Admin', 'Ministries']}
        actions={
          <Button variant="primary" size="sm" icon={<PlusIcon />} onClick={() => router.push('/admin/ministries/create')}>
            New Ministry
          </Button>
        }
      />
      <MinistriesList refreshKey={refreshKey} />
    </div>
  );
}
