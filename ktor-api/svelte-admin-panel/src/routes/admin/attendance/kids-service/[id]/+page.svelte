<script>
    import PageHeader from '$lib/components/PageHeader.svelte';
    import Card from '$lib/components/Card.svelte';
    import Button from '$lib/components/Button.svelte';
    import AttendanceList from '$lib/components/attendance/AttendanceList.svelte';
    import CheckInOut from '$lib/components/attendance/CheckInOut.svelte';
    import { api } from '$lib/api';
    import { EventType } from '$lib/types/attendance';
    
    /** @type {import('./$types').PageData} */
    export let data;
    
    let showCheckInModal = false;
    let error = data.error || null;
    let kidsService = data.kidsService;
    let kidsServiceId = data.kidsServiceId;
    
    // Handle check-in
    function openCheckInModal() {
        showCheckInModal = true;
    }
    
    // Handle check-out
    async function handleCheckOut(event) {
        const { attendanceId, onSuccess, onError } = event.detail;
        
        try {
            // Call API to check out kid
            const response = await api.post(api.endpoints.ATTENDANCE_KID_CHECK_OUT, {
                attendanceId,
                checkedOutBy: 1, // Replace with actual user ID
                notes: ''
            });
            
            if (response && response.success) {
                // Call the success callback from the optimistic UI update
                if (onSuccess) onSuccess();
            } else {
                throw new Error('Failed to check out kid');
            }
        } catch (err) {
            console.error('Error checking out kid:', err);
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
    
    // Format time display
    function formatTime(timeString) {
        if (!timeString) return 'N/A';
        try {
            return new Date(timeString).toLocaleTimeString(undefined, { 
                hour: '2-digit', 
                minute: '2-digit' 
            });
        } catch {
            return 'N/A';
        }
    }
</script>

<PageHeader 
    title={kidsService ? `${kidsService.name} Attendance` : 'Kids Service Attendance'} 
    backLink="/admin/attendance/kids-service" 
/>

{#if error}
    <Card>
        <div class="p-6 text-center">
            <div class="text-red-500 mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            </div>
            <h3 class="text-lg font-medium mb-2">Error Loading Kids Service</h3>
            <p class="text-gray-600 dark:text-gray-400">{error}</p>
            <Button variant="secondary" class="mt-4" onclick={() => window.history.back()}>
                Go Back
            </Button>
        </div>
    </Card>
{:else if kidsService}
    <div class="mb-6">
        <Card>
            <div class="p-6">
                <div class="flex flex-col md:flex-row md:justify-between md:items-center">
                    <div>
                        <h2 class="text-xl font-semibold">{kidsService.name}</h2>
                        
                        {#if kidsService.description}
                            <p class="text-gray-600 dark:text-gray-400 mt-1">
                                {kidsService.description}
                            </p>
                        {/if}
                        
                        <div class="mt-2 space-y-1">
                            <p class="text-gray-600 dark:text-gray-400">
                                <span class="font-medium">Age Group:</span> 
                                {kidsService.ageGroup || kidsService.age_group || 'All Ages'}
                            </p>
                            
                            <p class="text-gray-600 dark:text-gray-400">
                                <span class="font-medium">Time:</span> 
                                {formatTime(kidsService.startTime || kidsService.start_time)}
                                {#if kidsService.endTime || kidsService.end_time}
                                    - {formatTime(kidsService.endTime || kidsService.end_time)}
                                {/if}
                            </p>
                            
                            {#if kidsService.location}
                                <p class="text-gray-600 dark:text-gray-400">
                                    <span class="font-medium">Location:</span> {kidsService.location}
                                </p>
                            {/if}
                            
                            <div class="flex items-center mt-2">
                                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {kidsService.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                                    {kidsService.isActive ? 'Active' : 'Inactive'}
                                </span>
                            </div>
                        </div>
                    </div>
                    <div class="mt-4 md:mt-0">
                        <Button 
                            variant="primary" 
                            onclick={openCheckInModal}
                            disabled={!kidsService.isActive}
                        >
                            Check In Kid
                        </Button>
                    </div>
                </div>
            </div>
        </Card>
    </div>
    
    <AttendanceList 
        eventType={EventType.KIDS_SERVICE}
        eventId={kidsServiceId}
        on:checkin={openCheckInModal}
        on:checkout={handleCheckOut}
        on:updatestatus={handleStatusUpdate}
    />
    
    {#if showCheckInModal}
        <div class="fixed inset-0 bg-black bg-opacity-50 z-40 flex items-center justify-center p-4">
            <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                <div class="p-6">
                    <div class="flex justify-between items-center mb-4">
                        <h3 class="text-lg font-semibold">Check In Kid</h3>
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
                        eventType={EventType.KIDS_SERVICE}
                        eventId={kidsServiceId}
                        onSuccess={() => {
                            showCheckInModal = false;
                            // Refresh attendance list
                        }}
                    />
                </div>
            </div>
        </div>
    {/if}
{/if}