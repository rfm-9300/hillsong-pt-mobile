<script>
    import PageHeader from '$lib/components/PageHeader.svelte';
    import Card from '$lib/components/Card.svelte';
    import EmptyState from '$lib/components/EmptyState.svelte';
    import LoadingOverlay from '$lib/components/LoadingOverlay.svelte';
    
    /** @type {import('./$types').PageData} */
    export let data;
    
    let kidsService = data.kidsService || {};
    let attendance = data.attendance || [];
    let kidsServiceId = data.kidsServiceId;
    let error = data.error || null;
    let loading = false;
    
    // Format kids service date and time for display
    $: {
        if (kidsService) {
            const startTime = kidsService.startTime || kidsService.start_time ? 
                new Date(kidsService.startTime || kidsService.start_time) : null;
            const endTime = kidsService.endTime || kidsService.end_time ? 
                new Date(kidsService.endTime || kidsService.end_time) : null;
            
            kidsService = {
                ...kidsService,
                formattedDate: startTime ? startTime.toLocaleDateString() : 'N/A',
                formattedTime: startTime ? 
                    `${startTime.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })}${endTime ? ' - ' + endTime.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' }) : ''}` : 
                    'N/A'
            };
        }
    }
</script>

<PageHeader 
    title={kidsService?.name ? `${kidsService.name} Attendance` : 'Kids Service Attendance'} 
    backLink="/admin/attendance/kids-service" 
/>

{#if loading}
    <LoadingOverlay />
{:else if error}
    <Card>
        <div class="p-6 text-center">
            <div class="text-red-500 mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            </div>
            <h3 class="text-lg font-medium mb-2">Error Loading Attendance Data</h3>
            <p class="text-gray-600 dark:text-gray-400">{error}</p>
        </div>
    </Card>
{:else}
    <div class="mb-6">
        <Card>
            <div class="p-6">
                <h2 class="text-xl font-semibold mb-4">Kids Service Details</h2>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <p class="text-gray-600 dark:text-gray-400">
                            <span class="font-medium">Name:</span> {kidsService.name || 'N/A'}
                        </p>
                    </div>
                    <div>
                        <p class="text-gray-600 dark:text-gray-400">
                            <span class="font-medium">Date:</span> {kidsService.formattedDate}
                        </p>
                    </div>
                    <div>
                        <p class="text-gray-600 dark:text-gray-400">
                            <span class="font-medium">Time:</span> {kidsService.formattedTime}
                        </p>
                    </div>
                    <div>
                        <p class="text-gray-600 dark:text-gray-400">
                            <span class="font-medium">Age Group:</span> {kidsService.ageGroup || kidsService.age_group || 'All Ages'}
                        </p>
                    </div>
                    <div>
                        <p class="text-gray-600 dark:text-gray-400">
                            <span class="font-medium">Location:</span> {kidsService.location || 'N/A'}
                        </p>
                    </div>
                    <div>
                        <p class="text-gray-600 dark:text-gray-400">
                            <span class="font-medium">Capacity:</span> {kidsService.maxCapacity || kidsService.max_capacity || 'Unlimited'}
                        </p>
                    </div>
                </div>
            </div>
        </Card>
    </div>

    <div class="mb-6">
        <div class="flex justify-between items-center mb-4">
            <div>
                <h2 class="text-xl font-semibold">Attendance Records</h2>
                {#if attendance.length > 0}
                    <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        Total: {attendance.length} kids
                        • Currently checked in: {attendance.filter(r => r.attendance?.status === 'CHECKED_IN').length}
                        • Checked out: {attendance.filter(r => r.attendance?.status === 'CHECKED_OUT').length}
                    </p>
                {/if}
            </div>
            <div>
                <button class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors">
                    Export Data
                </button>
            </div>
        </div>

        {#if attendance.length === 0}
            <EmptyState 
                title="No Attendance Records" 
                description="There are no attendance records for this kids service yet."
            >
                {#snippet icon()}
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-full h-full">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z" />
                    </svg>
                {/snippet}
            </EmptyState>
        {:else}
            <Card>
                <div class="overflow-x-auto">
                    <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                        <thead class="bg-gray-50 dark:bg-gray-800">
                            <tr>
                                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                    Kid
                                </th>
                                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                    Check-in Time
                                </th>
                                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                    Check-out Time
                                </th>
                                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                    Status
                                </th>
                                <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                    Notes
                                </th>
                                <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                    Actions
                                </th>
                            </tr>
                        </thead>
                        <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                            {#each attendance as record}
                                <tr>
                                    <td class="px-6 py-4 whitespace-nowrap">
                                        <div class="flex items-center">
                                            <div>
                                                <div class="text-sm font-medium text-gray-900 dark:text-white">
                                                    {record.attendeeName || 'N/A'}
                                                </div>
                                                <div class="text-sm text-gray-500 dark:text-gray-400">
                                                    Checked in by: {record.checkedInByName || 'Unknown'}
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap">
                                        <div class="text-sm text-gray-900 dark:text-white">
                                            {record.attendance?.checkInTime ? 
                                                new Date(record.attendance.checkInTime).toLocaleTimeString(undefined, { 
                                                    hour: '2-digit', 
                                                    minute: '2-digit' 
                                                }) : 'N/A'
                                            }
                                        </div>
                                        <div class="text-sm text-gray-500 dark:text-gray-400">
                                            {record.attendance?.checkInTime ? 
                                                new Date(record.attendance.checkInTime).toLocaleDateString() : ''
                                            }
                                        </div>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap">
                                        <div class="text-sm text-gray-900 dark:text-white">
                                            {record.attendance?.checkOutTime ? 
                                                new Date(record.attendance.checkOutTime).toLocaleTimeString(undefined, { 
                                                    hour: '2-digit', 
                                                    minute: '2-digit' 
                                                }) : '-'
                                            }
                                        </div>
                                        <div class="text-sm text-gray-500 dark:text-gray-400">
                                            {record.attendance?.checkOutTime ? 
                                                new Date(record.attendance.checkOutTime).toLocaleDateString() : ''
                                            }
                                        </div>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap">
                                        {#if record.attendance?.status === 'CHECKED_IN'}
                                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100">
                                                Checked In
                                            </span>
                                        {:else if record.attendance?.status === 'CHECKED_OUT'}
                                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-100">
                                                Checked Out
                                            </span>
                                        {:else if record.attendance?.status === 'EMERGENCY'}
                                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800 dark:bg-red-800 dark:text-red-100">
                                                Emergency
                                            </span>
                                        {:else if record.attendance?.status === 'NO_SHOW'}
                                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800 dark:bg-yellow-800 dark:text-yellow-100">
                                                No Show
                                            </span>
                                        {:else}
                                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-100">
                                                Unknown
                                            </span>
                                        {/if}
                                    </td>
                                    <td class="px-6 py-4">
                                        <div class="text-sm text-gray-900 dark:text-white max-w-xs truncate">
                                            {record.attendance?.notes || '-'}
                                        </div>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                        {#if record.attendance?.status === 'CHECKED_IN'}
                                            <button class="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300">
                                                Check Out
                                            </button>
                                        {:else}
                                            <span class="text-gray-400">-</span>
                                        {/if}
                                    </td>
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                </div>
            </Card>
        {/if}
    </div>
{/if}