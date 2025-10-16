'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button, NavigationHeader } from '@/app/components/ui';
import EncountersList from '@/app/components/EncountersList';

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
      <NavigationHeader 
        title="Encounters Management" 
        subtitle="Manage community encounters and gatherings"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Encounters', current: true },
        ]}
      >
        <Button
          variant="primary"
          onClick={handleCreateEncounter}
        >
          Create Encounter
        </Button>
      </NavigationHeader>
      
      <EncountersList 
        key={refreshKey}
        onEncounterDeleted={handleEncounterDeleted}
      />
    </div>
  );
}
