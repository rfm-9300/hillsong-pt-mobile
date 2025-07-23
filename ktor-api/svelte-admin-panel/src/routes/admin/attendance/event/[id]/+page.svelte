<script>
    import { onMount } from 'svelte';
    import { page } from '$app/stores';
    import PageHeader from '$lib/components/PageHeader.svelte';
    import Card from '$lib/components/Card.svelte';
    import Button from '$lib/components/Button.svelte';
    import AttendanceList from '$lib/components/attendance/AttendanceList.svelte';
    import CheckInOut from '$lib/components/attendance/CheckInOut.svelte';
    import { api } from '$lib/api';
    import { EventType } from '$lib/types/attendance';
    import LoadingOverlay from '$lib/components/LoadingOverlay.svelte';
    
    // Get event ID from URL params
    const eventId = $page.params.id;
    
    let event = null;
    let loading = true;
    let error = null;
    let showCheckInModal = false;
    
    onMount(async () => {
        try {
            // Fetch event details
            const response = await api.get(api.endpoints.EVENT_BY_ID(eventId));
            
            if (response && response.data && response.data.event) {
                event = response.data.event;
            } else if (response && response.event) {
                event = response.event;
            } else {
                throw new Error('Event not found');
            }
            
            loading = false;
        } catch (err) {
            console.error('Error loading event:', err);
            error = err.message || 'Failed to load event';
            loading = false;
        }
    });
    
    // Handle check-in
    function openCheckInModal() {
        showCheckInModal = true;
    }
    
    // Handle check-out
    async function handleCheckOut(event) {
        const { attendanceId, onSuccess, onError } = event.detail;
        
        try {
            // Call API to check out attendee
            const response = await api.post(api.endpoints.ATTENDANCE_CHECK_OUT, {
                attendanceId,
                checkedOutBy: 1, // Replace with actual user ID
                notes: ''
            });
            
            if (response && response.success) {
                // Call the success callback from the optimistic UI update
                if (onSuccess) onSuccess();
            } else {
                throw new Error('Failed to check out attendee');
            }
        } catch (err) {
            console.error('Error checking out attendee:', err);
            // Call the error callback from the optimistic UI update
            if (onError) onError(err);
        }
    }
    
    // Handle status update
    async function handleStatusUpdate(event) {
        const { attendanceId, status, notes, onSuccess, onError } = event.detail;
        
        try {
            // Call API to update status
            const response = await api.put(api.endpoints.ATTENDANCE_UPDATE_STATUS, {
                attendanceId,
                status,
                notes,
                updatedBy: 1 // Replace with actual user ID
            });
            
            if (response && response.success) {
                // Call the success callback from the optimistic UI update
                if (onSuccess) onSuccess();
            } else {
                throw new Error('Failed to update attendance status');
            }
        } catch (err) {
            console.error('Error updating attendance status:', err);
            // Call the error callback from the optimistic UI update
            if (onError) onError(err);
        }
    }
</script>

<PageHeader title={event ? `${event.title} Attendance` : 'Event Attendance'} backLink="/admin/attendance/event" />

{#if loading}
    <div class="flex justify-center items-center h-64">
        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
    </div>
{:else if error}
    <Card>
        <div class="p-6 text-center">
            <div class="text-red-500 mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            </div>
            <h3 class="text-lg font-medium mb-2">Error Loading Event</h3>
            <p class="text-gray-600 dark:text-gray-400">{error}</p>
            <Button variant="secondary" class="mt-4" onclick={() => window.history.back()}>
                Go Back
            </Button>
        </div>
    </Card>
{:else if event}
    <div class="mb-6">
        <Card>
            <div class="p-6">
                <div class="flex flex-col md:flex-row md:justify-between md:items-center">
                    <div>
                        <h2 class="text-xl font-semibold">{event.title}</h2>
                        <p class="text-gray-600 dark:text-gray-400 mt-1">
                            <span class="font-medium">Date:</span> {new Date(event.date).toLocaleDateString()}
                        </p>
                        <p class="text-gray-600 dark:text-gray-400 mt-1">
                            <span class="font-medium">Location:</span> {event.location || 'N/A'}
                        </p>
                    </div>
                    <div class="mt-4 md:mt-0">
                        <Button variant="primary" onclick={openCheckInModal}>
                            Check In Attendee
                        </Button>
                    </div>
                </div>
            </div>
        </Card>
    </div>
    
    <AttendanceList 
        eventType={EventType.EVENT}
        eventId={eventId}
        on:checkin={openCheckInModal}
        on:checkout={handleCheckOut}
        on:updatestatus={handleStatusUpdate}
    />
    
    {#if showCheckInModal}
        <div class="fixed inset-0 bg-black bg-opacity-50 z-40 flex items-center justify-center p-4">
            <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                <div class="p-6">
                    <div class="flex justify-between items-center mb-4">
                        <h3 class="text-lg font-semibold">Check In Attendee</h3>
                        <button 
                            class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
                            onclick={() => showCheckInModal = false}
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                    
                    <CheckInOut 
                        eventType={EventType.EVENT}
                        eventId={eventId}
                        on:close={() => showCheckInModal = false}
                        on:success={() => {
                            showCheckInModal = false;
                            // Refresh attendance list
                        }}
                    />
                </div>
            </div>
        </div>
    {/if}
{/if}