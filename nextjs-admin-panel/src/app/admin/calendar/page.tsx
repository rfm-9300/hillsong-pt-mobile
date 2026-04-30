'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Alert, Badge, Button, Card, DeleteConfirmationModal, EmptyState, LoadingOverlay, PageHeader, TableHeader, TableRow } from '@/app/components/ui';
import { CalendarEvent, CalendarEventType } from '@/lib/types';
import { api } from '@/lib/api';
import { PlusIcon, TrashIcon } from '@/app/components/icons/Icons';
import type { BadgeColor } from '@/app/components/ui/Badge';

const cols = [
  { label: 'Date', width: '140px' },
  { label: 'Title', width: '1fr' },
  { label: 'Type', width: '160px' },
  { label: 'Time', width: '140px' },
  { label: 'Location', width: '130px' },
  { label: '', width: '70px' },
];

const typeColors: Partial<Record<CalendarEventType, BadgeColor>> = {
  [CalendarEventType.PRAYER_MEETING]: 'blue',
  [CalendarEventType.WORSHIP_SERVICE]: 'amber',
  [CalendarEventType.YOUTH_EVENT]: 'green',
  [CalendarEventType.SPECIAL_EVENT]: 'red',
  [CalendarEventType.GENERAL]: 'neutral',
};

export default function CalendarPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [events, setEvents] = useState<CalendarEvent[]>([]);
  const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [eventToDelete, setEventToDelete] = useState<CalendarEvent | null>(null);
  const [deleting, setDeleting] = useState(false);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const response = await api.calendar.getUpcoming(0, 50) as any;
      if (response?.success) {
        setEvents(response.data.content || response.data || []);
      }
    } catch {
      setAlert({ type: 'error', message: 'Failed to load calendar events. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchEvents(); }, []);

  const handleDelete = async () => {
    if (!eventToDelete) return;
    try {
      setDeleting(true);
      const response = await api.calendar.delete(eventToDelete.id) as any;
      if (response?.success) {
        setAlert({ type: 'success', message: 'Event deleted successfully' });
        setEventToDelete(null);
        fetchEvents();
      }
    } catch {
      setAlert({ type: 'error', message: 'Failed to delete event' });
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Calendar"
        subtitle="Manage church calendar events"
        breadcrumbs={['Admin', 'Calendar']}
        actions={<Button size="sm" icon={<PlusIcon />} onClick={() => router.push('/admin/calendar/create')}>New Calendar Event</Button>}
      />

      {alert && <Alert type={alert.type} message={alert.message} onClose={() => setAlert(null)} />}

      {loading && events.length === 0 ? (
        <LoadingOverlay show={true} message="Loading calendar..." />
      ) : events.length === 0 ? (
        <Card>
          <EmptyState title="No upcoming events found" description="Create a calendar event to show it here." actionText="Create First Event" onAction={() => router.push('/admin/calendar/create')} />
        </Card>
      ) : (
        <Card className="overflow-hidden">
          <TableHeader cols={cols} />
          {events.map((event, index) => (
            <TableRow
              key={event.id}
              cols={cols}
              last={index === events.length - 1}
              cells={[
                <span key="date" className="font-semibold text-[var(--color-text)]">{formatDate(event.date)}</span>,
                <span key="title" className="truncate text-[var(--color-text)]">{event.title}</span>,
                <Badge key="type" color={typeColors[event.eventType] || 'neutral'}>{event.eventType.replace(/_/g, ' ')}</Badge>,
                <span key="time">{event.isAllDay ? 'All day' : `${event.startTime || '-'} - ${event.endTime || '-'}`}</span>,
                <span key="location" className="truncate">{event.location || 'No location'}</span>,
              ]}
              actions={<Button size="xs" variant="danger" icon={<TrashIcon />} onClick={() => setEventToDelete(event)}>Delete</Button>}
            />
          ))}
        </Card>
      )}

      <DeleteConfirmationModal
        show={!!eventToDelete}
        title="Delete Calendar Event"
        message={`Delete "${eventToDelete?.title}" from the calendar?`}
        onConfirm={handleDelete}
        onCancel={() => setEventToDelete(null)}
        loading={deleting}
      />
    </div>
  );
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}
