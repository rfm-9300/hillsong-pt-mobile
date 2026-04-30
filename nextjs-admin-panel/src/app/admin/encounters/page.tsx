'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button, PageHeader } from '@/app/components/ui';
import EncountersList from '@/app/components/EncountersList';
import { PlusIcon } from '@/app/components/icons/Icons';

export default function EncountersPage() {
  const router = useRouter();
  const [refreshKey, setRefreshKey] = useState(0);

  const handleCreateEncounter = () => {
    router.push('/admin/encounters/create');
  };

  const handleEncounterDeleted = () => {
    // Trigger a refresh of the encounters list
    setRefreshKey(prev => prev + 1);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Encounters"
        subtitle="Manage community encounters and gatherings"
        breadcrumbs={['Admin', 'Encounters']}
        actions={<Button variant="primary" size="sm" icon={<PlusIcon />} onClick={handleCreateEncounter}>New Encounter</Button>}
      />
      
      <EncountersList 
        key={refreshKey}
        onEncounterDeleted={handleEncounterDeleted}
      />
    </div>
  );
}
