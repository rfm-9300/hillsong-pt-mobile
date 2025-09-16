'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button, NavigationHeader } from '@/app/components/ui';
import EventsList from '@/app/components/EventsList';

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
      <NavigationHeader 
        title="Events Management" 
        subtitle="Manage community events and gatherings"
        breadcrumbs={[
          { label: 'Dashboard', href: '/admin/dashboard' },
          { label: 'Events', current: true },
        ]}
      >
        <Button
          variant="primary"
          onClick={handleCreateEvent}
        >
          Create Event
        </Button>
      </NavigationHeader>
      
      <EventsList 
        key={refreshKey}
        onEventDeleted={handleEventDeleted}
      />
    </div>
  );
}