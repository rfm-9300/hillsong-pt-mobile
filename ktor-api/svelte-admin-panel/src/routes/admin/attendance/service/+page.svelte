<script>
    import PageHeader from '$lib/components/PageHeader.svelte';
    import Card from '$lib/components/Card.svelte';
    import { EventType } from '$lib/types/attendance';
    import { goto } from '$app/navigation';
    
    /** @type {import('./$types').PageData} */
    export let data;
    
    let services = [];
    let loading = false;
    let error = data.error || null;
    
    $: {
        if (data.services) {
            // Format service data for display
            services = data.services.map(service => {
                // Extract time information - handle both camelCase and snake_case field names
                const startTime = service.startTime || service.start_time ? 
                    new Date(service.startTime || service.start_time) : null;
                const endTime = service.endTime || service.end_time ? 
                    new Date(service.endTime || service.end_time) : null;
                
                // Format day and time
                const day = startTime ? startTime.toLocaleDateString(undefined, { weekday: 'long' }) : 'N/A';
                const time = startTime ? 
                    `${startTime.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })}${endTime ? ' - ' + endTime.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' }) : ''}` : 
                    'N/A';
                
                console.log('Formatted service:', { 
                    id: service.id,
                    name: service.name,
                    startTime: startTime?.toISOString(),
                    endTime: endTime?.toISOString(),
                    day,
                    time
                });
                
                return {
                    ...service,
                    day,
                    time
                };
            });
        }
    }
    
    function viewAttendance(serviceId) {
        goto(`/admin/attendance/service/${serviceId}`);
    }
</script>

<PageHeader title="Services Attendance" backLink="/admin/attendance" />

<div class="mb-6">
    <div class="flex justify-between items-center">
        <h2 class="text-xl font-semibold">Select a Service</h2>
    </div>
</div>

{#if loading}
    <div class="flex justify-center items-center h-64">
        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-purple-500"></div>
    </div>
{:else if error}
    <Card>
        <div class="p-6 text-center">
            <div class="text-red-500 mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            </div>
            <h3 class="text-lg font-medium mb-2">Error Loading Services</h3>
            <p class="text-gray-600 dark:text-gray-400">{error}</p>
        </div>
    </Card>
{:else if services.length === 0}
    <Card>
        <div class="p-6 text-center">
            <div class="text-gray-400 mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            </div>
            <h3 class="text-lg font-medium mb-2">No Services Found</h3>
            <p class="text-gray-600 dark:text-gray-400">There are no services available to manage attendance.</p>
        </div>
    </Card>
{:else}
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {#each services as service}
            <Card>
                <div class="p-6">
                    <h3 class="text-lg font-semibold mb-2">{service.name}</h3>
                    <p class="text-gray-600 dark:text-gray-400 mb-2">
                        <span class="font-medium">Day:</span> {service.day || 'N/A'}
                    </p>
                    <p class="text-gray-600 dark:text-gray-400 mb-4">
                        <span class="font-medium">Time:</span> {service.time || 'N/A'}
                    </p>
                    <button 
                        on:click={() => viewAttendance(service.id)}
                        class="w-full px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                    >
                        View Attendance
                    </button>
                </div>
            </Card>
        {/each}
    </div>
{/if}