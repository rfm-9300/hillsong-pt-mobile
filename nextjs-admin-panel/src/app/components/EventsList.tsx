'use client';

import { useState, useEffect } from 'react';
import { EmptyState, LoadingSkeleton, Alert, EventsGrid, AnimatedGrid } from './ui';
import EventCard from './EventCard';
import { Event } from '@/lib/types';
import { api, ENDPOINTS } from '@/lib/api';

interface EventsListProps {
  onEventDeleted?: () => void;
}

export default function EventsList({ onEventDeleted }: EventsListProps) {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get<{ data: { content: Event[] } }>(ENDPOINTS.EVENTS);
      if (response?.data?.content) {
        // Sort events by date (upcoming first, then by date)
        const sortedEvents = response.data.content.sort((a, b) => {
          const dateA = new Date(a.date);
          const dateB = new Date(b.date);
          const now = new Date();
          
          const aIsUpcoming = dateA > now;
          const bIsUpcoming = dateB > now;
          
          // If one is upcoming and other is past, upcoming comes first
          if (aIsUpcoming && !bIsUpcoming) return -1;
          if (!aIsUpcoming && bIsUpcoming) return 1;
          
          // If both are upcoming or both are past, sort by date
          return dateA.getTime() - dateB.getTime();
        });
        
        setEvents(sortedEvents);
      }
    } catch (error) {
      console.error('Error fetching events:', error);
      setError(error instanceof Error ? error.message : 'Failed to load events');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const handleEventDeleted = (eventId: string) => {
    setEvents(prev => prev.filter(event => event.id !== eventId));
    onEventDeleted?.();
  };

  if (loading) {
    return (
      <EventsGrid>
        {Array.from({ length: 6 }).map((_, index) => (
          <LoadingSkeleton 
            key={index} 
            className="h-80 rounded-lg"
            style={{ animationDelay: `${index * 100}ms` }}
          />
        ))}
      </EventsGrid>
    );
  }

  if (error) {
    return (
      <Alert
        type="error"
        message={error}
        onClose={() => setError(null)}
      />
    );
  }

  if (events.length === 0) {
    return (
      <EmptyState
        title="No events found"
        description="Get started by creating your first event. Events help you organize and manage community gatherings."
        actionText="Create Event"
        onAction={() => window.location.href = '/admin/events/create'}
        icon={
          <svg className="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
        }
      />
    );
  }

  return (
    <AnimatedGrid cols="grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
      {events.map((event) => (
        <EventCard
          key={event.id}
          event={event}
          onDelete={handleEventDeleted}
        />
      ))}
    </AnimatedGrid>
  );
}