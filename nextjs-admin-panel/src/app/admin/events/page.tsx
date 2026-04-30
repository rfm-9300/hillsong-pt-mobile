'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button, PageHeader } from '@/app/components/ui';
import EventsList from '@/app/components/EventsList';
import { PlusIcon } from '@/app/components/icons/Icons';

export default function EventsPage() {
  const router = useRouter();
  const [refreshKey, setRefreshKey] = useState(0);

  const handleCreateEvent = () => {
    router.push('/admin/events/create');
  };

  const handleEventDeleted = () => {
    // Trigger a refresh of the events list
    setRefreshKey(prev => prev + 1);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Events"
        subtitle="Manage community events and gatherings"
        breadcrumbs={['Admin', 'Events']}
        actions={<Button variant="primary" size="sm" icon={<PlusIcon />} onClick={handleCreateEvent}>New Event</Button>}
      />
      
      <EventsList 
        key={refreshKey}
        onEventDeleted={handleEventDeleted}
      />
    </div>
  );
}
