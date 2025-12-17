'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button, Alert, LoadingOverlay, NavigationHeader } from '@/app/components/ui';
import { FormContainer, Input, Textarea, ImageUpload, Checkbox } from '@/app/components/forms';
import { api, ENDPOINTS } from '@/lib/api';
import { CalendarEventType } from '@/lib/types';

interface EntityOption {
    id: number;
    title: string;
    date: string;
    location: string;
    description: string;
    type: 'EVENT' | 'ENCOUNTER';
}

export default function CreateCalendarEventPage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [alert, setAlert] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

    // Entity Import State
    const [entities, setEntities] = useState<EntityOption[]>([]);
    const [loadingEntities, setLoadingEntities] = useState(false);
    const [selectedEntityId, setSelectedEntityId] = useState<string>('');

    const [formData, setFormData] = useState({
        title: '',
        description: '',
        eventDate: '',
        startTime: '',
        endTime: '',
        location: '',
        isAllDay: false,
        eventType: CalendarEventType.GENERAL,
        imageFile: null as File | null
    });

    const [errors, setErrors] = useState<Record<string, string>>({});

    useEffect(() => {
        fetchEntities();
    }, []);

    const fetchEntities = async () => {
        try {
            setLoadingEntities(true);
            // Fetch both Events and Encounters to offer as import options
            // Note: This relies on existing API methods.
            // Adjust if you need specific "upcoming" endpoints for imports.

            const [eventsRes, encountersRes] = await Promise.all([
                api.get<any>(ENDPOINTS.EVENT_UPCOMING),
                api.get<any>(ENDPOINTS.ENCOUNTER_UPCOMING)
            ]);

            const options: EntityOption[] = [];

            if (eventsRes && eventsRes.success) {
                // Handle different response structures (pagination vs list)
                const list = eventsRes.data.content || eventsRes.data || [];
                list.forEach((e: any) => {
                    options.push({
                        id: e.id,
                        title: e.title,
                        date: e.date,
                        location: e.location,
                        description: e.description,
                        type: 'EVENT'
                    });
                });
            }

            if (encountersRes && encountersRes.success) {
                const list = encountersRes.data || [];
                list.forEach((e: any) => {
                    options.push({
                        id: e.id,
                        title: e.title,
                        date: e.date,
                        location: e.location,
                        description: e.description,
                        type: 'ENCOUNTER'
                    });
                });
            }

            setEntities(options);

        } catch (error) {
            console.error('Failed to load importable entities', error);
            // Don't block the page, just log
        } finally {
            setLoadingEntities(false);
        }
    };

    const handleEntitySelect = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const value = e.target.value;
        setSelectedEntityId(value);

        if (!value) return;

        const [type, idStr] = value.split('_');
        const id = parseInt(idStr);

        const entity = entities.find(item => item.id === id && item.type === type);

        if (entity) {
            // Pre-fill form
            // Parse date and time from entity.date string if possible
            // Assuming entity.date is "YYYY-MM-DD HH:mm:ss" or similar
            const dateObj = new Date(entity.date);
            const dateStr = dateObj.toISOString().split('T')[0];
            const timeStr = dateObj.toTimeString().slice(0, 5);

            let eventType = CalendarEventType.GENERAL;
            if (type === 'ENCOUNTER') eventType = CalendarEventType.SPECIAL_EVENT;
            if (type === 'EVENT') eventType = CalendarEventType.GENERAL; // Could refine based on title

            setFormData(prev => ({
                ...prev,
                title: entity.title,
                description: entity.description || '',
                eventDate: dateStr,
                startTime: timeStr,
                location: entity.location || '',
                eventType: eventType
            }));
        }
    };

    const validateForm = () => {
        const newErrors: Record<string, string> = {};

        if (!formData.title.trim()) newErrors.title = 'Title is required';
        if (!formData.eventDate) newErrors.eventDate = 'Date is required';
        if (!formData.isAllDay && !formData.startTime) newErrors.startTime = 'Start time is required for timed events';

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) return;

        setLoading(true);
        setAlert(null);

        try {
            const submitData = new FormData();

            const eventData = {
                title: formData.title,
                description: formData.description,
                eventDate: formData.eventDate,
                startTime: formData.startTime || null,
                endTime: formData.endTime || null,
                location: formData.location,
                isAllDay: formData.isAllDay,
                eventType: formData.eventType
            };

            submitData.append('event', new Blob([JSON.stringify(eventData)], {
                type: 'application/json'
            }));

            if (formData.imageFile) {
                submitData.append('image', formData.imageFile);
            }

            const response = await api.calendar.create(submitData) as any;

            if (response && response.success) {
                setAlert({ type: 'success', message: 'Calendar event created successfully!' });
                setTimeout(() => {
                    router.push('/admin/calendar');
                }, 1500);
            } else {
                throw new Error(response?.message || 'Failed to create event');
            }

        } catch (error) {
            console.error('Error creating calendar event:', error);
            setAlert({
                type: 'error',
                message: error instanceof Error ? error.message : 'Failed to create calendar event'
            });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-6 pb-12">
            <NavigationHeader
                title="Create Calendar Event"
                subtitle="Add a new event to the church calendar"
                showBackButton={true}
                backButtonText="Back to Calendar"
                backButtonHref="/admin/calendar"
                breadcrumbs={[
                    { label: 'Dashboard', href: '/admin/dashboard' },
                    { label: 'Calendar', href: '/admin/calendar' },
                    { label: 'Create', current: true },
                ]}
            />

            {alert && (
                <Alert
                    type={alert.type}
                    message={alert.message}
                    onClose={() => setAlert(null)}
                />
            )}

            <div className="max-w-2xl bg-white p-6 rounded-lg shadow-sm border border-gray-200">

                {/* Import Section */}
                <div className="mb-8 p-4 bg-gray-50 rounded-md border border-gray-200">
                    <h3 className="text-sm font-medium text-gray-700 mb-2">Import from existing</h3>
                    <div className="flex gap-4 items-center">
                        <select
                            className="flex-1 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                            value={selectedEntityId}
                            onChange={handleEntitySelect}
                            disabled={loadingEntities}
                        >
                            <option value="">-- Select an Event or Encounter to pre-fill --</option>
                            {entities.map(entity => (
                                <option key={`${entity.type}_${entity.id}`} value={`${entity.type}_${entity.id}`}>
                                    [{entity.type}] {entity.title} - {new Date(entity.date).toLocaleDateString()}
                                </option>
                            ))}
                        </select>
                        {loadingEntities && <span className="text-xs text-gray-500">Loading options...</span>}
                    </div>
                    <p className="text-xs text-gray-500 mt-2">
                        Selecting an item will verify details into the form below. You can still edit them before saving.
                    </p>
                </div>

                <FormContainer onSubmit={handleSubmit}>
                    <div className="space-y-6">
                        <Input
                            label="Event Title"
                            value={formData.title}
                            onChange={(value) => setFormData(prev => ({ ...prev, title: value }))}
                            error={errors.title}
                            required
                            disabled={loading}
                        />

                        <Textarea
                            label="Description"
                            value={formData.description}
                            onChange={(value) => setFormData(prev => ({ ...prev, description: value }))}
                            placeholder="Details about the event..."
                            rows={4}
                            disabled={loading}
                        />

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Event Type
                                </label>
                                <select
                                    className="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm p-2 border"
                                    value={formData.eventType}
                                    onChange={(e) => setFormData(prev => ({ ...prev, eventType: e.target.value as CalendarEventType }))}
                                    disabled={loading}
                                >
                                    {Object.values(CalendarEventType).map(type => (
                                        <option key={type} value={type}>
                                            {type.replace(/_/g, ' ')}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <Input
                                label="Date"
                                type="date"
                                value={formData.eventDate}
                                onChange={(value) => setFormData(prev => ({ ...prev, eventDate: value }))}
                                error={errors.eventDate}
                                required
                                disabled={loading}
                            />
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <Input
                                label="Start Time"
                                type="time"
                                value={formData.startTime}
                                onChange={(value) => setFormData(prev => ({ ...prev, startTime: value }))}
                                error={errors.startTime}
                                disabled={loading || formData.isAllDay}
                                required={!formData.isAllDay}
                            />

                            <Input
                                label="End Time"
                                type="time"
                                value={formData.endTime}
                                onChange={(value) => setFormData(prev => ({ ...prev, endTime: value }))}
                                disabled={loading || formData.isAllDay}
                            />
                        </div>

                        <Checkbox
                            label="All Day Event"
                            checked={formData.isAllDay}
                            onChange={(checked) => setFormData(prev => ({ ...prev, isAllDay: checked }))}
                            disabled={loading}
                        />

                        <Input
                            label="Location"
                            value={formData.location}
                            onChange={(value) => setFormData(prev => ({ ...prev, location: value }))}
                            placeholder="e.g. Main Auditorium"
                            disabled={loading}
                        />

                        <ImageUpload
                            label="Event Image (Optional)"
                            value={formData.imageFile ? URL.createObjectURL(formData.imageFile) : undefined}
                            onChange={(file) => setFormData(prev => ({ ...prev, imageFile: file }))}
                            accept="image/*"
                            disabled={loading}
                        />

                        <div className="flex gap-4 pt-4">
                            <Button
                                type="submit"
                                variant="primary"
                                loading={loading}
                                disabled={loading}
                            >
                                Create Calendar Event
                            </Button>

                            <Button
                                type="button"
                                variant="secondary"
                                onClick={() => router.back()}
                                disabled={loading}
                            >
                                Cancel
                            </Button>
                        </div>
                    </div>
                </FormContainer>
            </div>

            <LoadingOverlay show={loading} message="Creating calendar event..." />
        </div>
    );
}
