'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button, NavigationHeader, Alert, LoadingOverlay } from '@/app/components/ui';
import { CardProps } from '@/lib/types';
import { api } from '@/lib/api';
import { CalendarEvent, ApiResponse } from '@/lib/types';

// Simple Card Component since we can't import it directly if it's not exported
const Card = ({ children, className = '', onClick }: CardProps) => (
    <div
        className={`bg-white rounded-lg shadow-sm border border-gray-200 p-6 ${className}`}
        onClick={onClick}
    >
        {children}
    </div>
);

export default function CalendarPage() {
    const router = useRouter();
    const [loading, setLoading] = useState(true);
    const [events, setEvents] = useState<CalendarEvent[]>([]);
    const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

    const fetchEvents = async () => {
        try {
            setLoading(true);
            // Fetch upcoming events
            const response = await api.calendar.getUpcoming(0, 50) as any; // Type assertion needed due to ApiResponse wrapper

            if (response && response.success) {
                // Handle pagination response structure
                const eventData = response.data.content || response.data || [];
                setEvents(eventData);
            }
        } catch (error) {
            console.error('Error fetching calendar events:', error);
            setAlert({
                type: 'error',
                message: 'Failed to load calendar events. Please try again.'
            });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchEvents();
    }, []);

    const handleDelete = async (id: number) => {
        if (!window.confirm('Are you sure you want to delete this event?')) {
            return;
        }

        try {
            const response = await api.calendar.delete(id) as any;
            if (response && response.success) {
                setAlert({ type: 'success', message: 'Event deleted successfully' });
                fetchEvents(); // Refresh list
            }
        } catch (error) {
            console.error('Error deleting event:', error);
            setAlert({ type: 'error', message: 'Failed to delete event' });
        }
    };

    const handleCreateClick = () => {
        router.push('/admin/calendar/create');
    };

    return (
        <div className="space-y-6">
            <NavigationHeader
                title="Calendar"
                subtitle="Manage church calendar events"
                breadcrumbs={[
                    { label: 'Dashboard', href: '/admin/dashboard' },
                    { label: 'Calendar', current: true },
                ]}
            >
                <Button
                    variant="primary"
                    onClick={handleCreateClick}
                >
                    Add Calendar Event
                </Button>
            </NavigationHeader>

            {alert && (
                <Alert
                    type={alert.type}
                    message={alert.message}
                    onClose={() => setAlert(null)}
                />
            )}

            {loading && events.length === 0 ? (
                <LoadingOverlay show={true} message="Loading calendar..." />
            ) : events.length === 0 ? (
                <div className="text-center py-12 bg-white rounded-lg border border-gray-200">
                    <p className="text-gray-500 mb-4">No upcoming events found</p>
                    <Button variant="primary" onClick={handleCreateClick}>
                        Create First Event
                    </Button>
                </div>
            ) : (
                <div className="grid gap-4">
                    {events.map((event) => (
                        <Card key={event.id} className="flex justify-between items-center group hover:border-blue-300 transition-colors">
                            <div className="flex gap-4">
                                <div className="flex-shrink-0 w-16 h-16 bg-blue-50 rounded-lg flex flex-col items-center justify-center text-blue-700">
                                    <span className="text-xs font-bold uppercase">{new Date(event.date).toLocaleString('default', { month: 'short' })}</span>
                                    <span className="text-xl font-bold">{new Date(event.date).getDate()}</span>
                                </div>
                                <div>
                                    <h3 className="font-semibold text-lg text-gray-900">{event.title}</h3>
                                    <div className="text-sm text-gray-500 flex flex-wrap gap-x-4 gap-y-1 mt-1">
                                        <span>{event.startTime ? `${event.startTime} - ${event.endTime}` : 'All Day'}</span>
                                        <span>•</span>
                                        <span>{event.location || 'No location'}</span>
                                        <span>•</span>
                                        <span className="capitalize">{event.eventType.replace(/_/g, ' ').toLowerCase()}</span>
                                    </div>
                                </div>
                            </div>

                            <div className="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                <Button
                                    variant="danger"
                                    size="sm"
                                    onClick={(e) => {
                                        e?.stopPropagation();
                                        handleDelete(event.id);
                                    }}
                                >
                                    Delete
                                </Button>
                            </div>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}
