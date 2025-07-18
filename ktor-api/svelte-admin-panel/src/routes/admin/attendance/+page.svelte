<script>
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';
    import PageHeader from '$lib/components/PageHeader.svelte';
    import Card from '$lib/components/Card.svelte';
    import { EventType, AttendanceStatus } from '$lib/types/attendance';
    import { api } from '$lib/api';
    
    // Recent activity data
    let recentActivity = [];
    let loading = true;
    let error = null;
    
    // Filter states
    let selectedEventType = 'ALL';
    let dateRange = {
        start: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // Last 7 days
        end: new Date().toISOString().split('T')[0] // Today
    };
    
    // Quick stats
    let stats = {
        totalEvents: 0,
        totalServices: 0,
        totalKidsServices: 0,
        totalAttendees: 0
    };
    
    onMount(async () => {
        try {
            // In a real implementation, you would fetch this data from the API
            // For now, we'll use mock data
            await loadMockData();
            loading = false;
        } catch (err) {
            error = err.message || 'Failed to load attendance data';
            loading = false;
        }
    });
    
    async function loadMockData() {
        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // Mock stats
        stats = {
            totalEvents: 12,
            totalServices: 24,
            totalKidsServices: 18,
            totalAttendees: 356
        };
        
        // Mock recent activity
        recentActivity = [
            {
                id: 1,
                eventType: EventType.EVENT,
                eventName: 'Community Gathering',
                attendeeName: 'John Smith',
                status: AttendanceStatus.CHECKED_IN,
                timestamp: '2025-07-16T14:30:00Z'
            },
            {
                id: 2,
                eventType: EventType.SERVICE,
                eventName: 'Sunday Service',
                attendeeName: 'Jane Doe',
                status: AttendanceStatus.CHECKED_OUT,
                timestamp: '2025-07-16T12:45:00Z'
            },
            {
                id: 3,
                eventType: EventType.KIDS_SERVICE,
                eventName: 'Kids Club',
                attendeeName: 'Tommy Johnson',
                status: AttendanceStatus.CHECKED_IN,
                timestamp: '2025-07-16T10:15:00Z'
            },
            {
                id: 4,
                eventType: EventType.EVENT,
                eventName: 'Youth Night',
                attendeeName: 'Sarah Williams',
                status: AttendanceStatus.NO_SHOW,
                timestamp: '2025-07-15T18:30:00Z'
            },
            {
                id: 5,
                eventType: EventType.SERVICE,
                eventName: 'Prayer Meeting',
                attendeeName: 'Robert Brown',
                status: AttendanceStatus.EMERGENCY,
                timestamp: '2025-07-15T09:00:00Z'
            }
        ];
    }
    
    // Function to navigate to specific attendance type
    function navigateToEventType(type) {
        goto(`/admin/attendance/${type.toLowerCase()}`);
    }
    
    // Function to filter recent activity
    function filterActivity() {
        // In a real implementation, you would fetch filtered data from the API
        // For this demo, we'll just show a message
        alert(`Filtering: Event Type=${selectedEventType}, Date Range=${dateRange.start} to ${dateRange.end}`);
    }
    
    // Function to format date for display
    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleString();
    }
    
    // Function to get status badge class
    function getStatusBadgeClass(status) {
        switch(status) {
            case AttendanceStatus.CHECKED_IN:
                return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
            case AttendanceStatus.CHECKED_OUT:
                return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
            case AttendanceStatus.EMERGENCY:
                return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
            case AttendanceStatus.NO_SHOW:
                return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
            default:
                return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200';
        }
    }
    
    // Function to get event type badge class
    function getEventTypeBadgeClass(eventType) {
        switch(eventType) {
            case EventType.EVENT:
                return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
            case EventType.SERVICE:
                return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200';
            case EventType.KIDS_SERVICE:
                return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
            default:
                return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200';
        }
    }
</script>

<PageHeader title="Attendance Management" />

<!-- Quick Stats -->
<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
    <Card>
        <div class="p-4">
            <div class="flex items-center">
                <div class="flex-shrink-0 bg-blue-100 dark:bg-blue-900 rounded-full p-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-blue-600 dark:text-blue-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                </div>
                <div class="ml-4">
                    <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400">Events</h3>
                    <p class="text-lg font-semibold">{stats.totalEvents}</p>
                </div>
            </div>
        </div>
    </Card>
    
    <Card>
        <div class="p-4">
            <div class="flex items-center">
                <div class="flex-shrink-0 bg-purple-100 dark:bg-purple-900 rounded-full p-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-purple-600 dark:text-purple-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                </div>
                <div class="ml-4">
                    <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400">Services</h3>
                    <p class="text-lg font-semibold">{stats.totalServices}</p>
                </div>
            </div>
        </div>
    </Card>
    
    <Card>
        <div class="p-4">
            <div class="flex items-center">
                <div class="flex-shrink-0 bg-green-100 dark:bg-green-900 rounded-full p-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-green-600 dark:text-green-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                </div>
                <div class="ml-4">
                    <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400">Kids Services</h3>
                    <p class="text-lg font-semibold">{stats.totalKidsServices}</p>
                </div>
            </div>
        </div>
    </Card>
    
    <Card>
        <div class="p-4">
            <div class="flex items-center">
                <div class="flex-shrink-0 bg-gray-100 dark:bg-gray-700 rounded-full p-3">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-600 dark:text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
                </div>
                <div class="ml-4">
                    <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400">Total Attendees</h3>
                    <p class="text-lg font-semibold">{stats.totalAttendees}</p>
                </div>
            </div>
        </div>
    </Card>
</div>

<!-- Event Type Selection -->
<div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
    <Card>
        <div class="p-6 flex flex-col items-center text-center">
            <div class="w-16 h-16 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-600 dark:text-blue-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
            </div>
            <h3 class="text-xl font-semibold mb-2">Events Attendance</h3>
            <p class="text-gray-600 dark:text-gray-300 mb-4">Manage attendance for community events and gatherings</p>
            <button 
                on:click={() => navigateToEventType(EventType.EVENT)}
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
                View Events
            </button>
        </div>
    </Card>
    
    <Card>
        <div class="p-6 flex flex-col items-center text-center">
            <div class="w-16 h-16 bg-purple-100 dark:bg-purple-900 rounded-full flex items-center justify-center mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-purple-600 dark:text-purple-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            </div>
            <h3 class="text-xl font-semibold mb-2">Services Attendance</h3>
            <p class="text-gray-600 dark:text-gray-300 mb-4">Track attendance for regular services and meetings</p>
            <button 
                on:click={() => navigateToEventType(EventType.SERVICE)}
                class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
            >
                View Services
            </button>
        </div>
    </Card>
    
    <Card>
        <div class="p-6 flex flex-col items-center text-center">
            <div class="w-16 h-16 bg-green-100 dark:bg-green-900 rounded-full flex items-center justify-center mb-4">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-green-600 dark:text-green-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            </div>
            <h3 class="text-xl font-semibold mb-2">Kids Services Attendance</h3>
            <p class="text-gray-600 dark:text-gray-300 mb-4">Manage attendance for children's programs and activities</p>
            <button 
                on:click={() => navigateToEventType(EventType.KIDS_SERVICE)}
                class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
            >
                View Kids Services
            </button>
        </div>
    </Card>
</div>

<!-- Recent Activity with Filtering -->
<div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
    <!-- Filters -->
    <div class="lg:col-span-1">
        <Card>
            <div class="p-6">
                <h3 class="text-lg font-semibold mb-4">Filter Activity</h3>
                
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Event Type</label>
                    <select 
                        bind:value={selectedEventType}
                        class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800"
                    >
                        <option value="ALL">All Types</option>
                        <option value={EventType.EVENT}>Events</option>
                        <option value={EventType.SERVICE}>Services</option>
                        <option value={EventType.KIDS_SERVICE}>Kids Services</option>
                    </select>
                </div>
                
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Date Range</label>
                    <div class="space-y-2">
                        <div>
                            <label class="block text-xs text-gray-500 dark:text-gray-400 mb-1">Start Date</label>
                            <input 
                                type="date" 
                                bind:value={dateRange.start}
                                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800"
                            />
                        </div>
                        <div>
                            <label class="block text-xs text-gray-500 dark:text-gray-400 mb-1">End Date</label>
                            <input 
                                type="date" 
                                bind:value={dateRange.end}
                                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800"
                            />
                        </div>
                    </div>
                </div>
                
                <button 
                    on:click={filterActivity}
                    class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                    Apply Filters
                </button>
            </div>
        </Card>
        
        <div class="mt-6">
            <Card>
                <div class="p-6">
                    <h3 class="text-lg font-semibold mb-4">Attendance Reports</h3>
                    <p class="text-gray-600 dark:text-gray-300 mb-4">
                        View comprehensive attendance statistics and generate reports
                    </p>
                    <a 
                        href="/admin/attendance/reports"
                        class="w-full px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors inline-block text-center"
                    >
                        View Reports
                    </a>
                </div>
            </Card>
        </div>
    </div>
    
    <!-- Recent Activity -->
    <div class="lg:col-span-3">
        <Card>
            <div class="p-6">
                <h3 class="text-lg font-semibold mb-4">Recent Activity</h3>
                
                {#if loading}
                    <div class="flex justify-center items-center h-64">
                        <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
                    </div>
                {:else if error}
                    <div class="text-center py-8">
                        <div class="text-red-500 mb-4">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                        </div>
                        <h3 class="text-lg font-medium mb-2">Error Loading Activity</h3>
                        <p class="text-gray-600 dark:text-gray-400">{error}</p>
                    </div>
                {:else if recentActivity.length === 0}
                    <div class="text-center py-8">
                        <div class="text-gray-400 mb-4">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                            </svg>
                        </div>
                        <h3 class="text-lg font-medium mb-2">No Activity Found</h3>
                        <p class="text-gray-600 dark:text-gray-400">There is no recent attendance activity to display.</p>
                    </div>
                {:else}
                    <div class="overflow-x-auto">
                        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                            <thead>
                                <tr>
                                    <th class="px-6 py-3 bg-gray-50 dark:bg-gray-800 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                        Event Type
                                    </th>
                                    <th class="px-6 py-3 bg-gray-50 dark:bg-gray-800 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                        Event
                                    </th>
                                    <th class="px-6 py-3 bg-gray-50 dark:bg-gray-800 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                        Attendee
                                    </th>
                                    <th class="px-6 py-3 bg-gray-50 dark:bg-gray-800 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                        Status
                                    </th>
                                    <th class="px-6 py-3 bg-gray-50 dark:bg-gray-800 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                        Time
                                    </th>
                                </tr>
                            </thead>
                            <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-800">
                                {#each recentActivity as activity}
                                    <tr>
                                        <td class="px-6 py-4 whitespace-nowrap">
                                            <span class="px-2 py-1 text-xs rounded-full {getEventTypeBadgeClass(activity.eventType)}">
                                                {activity.eventType}
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">
                                            {activity.eventName}
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">
                                            {activity.attendeeName}
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap">
                                            <span class="px-2 py-1 text-xs rounded-full {getStatusBadgeClass(activity.status)}">
                                                {activity.status}
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                                            {formatDate(activity.timestamp)}
                                        </td>
                                    </tr>
                                {/each}
                            </tbody>
                        </table>
                    </div>
                {/if}
            </div>
        </Card>
    </div>
</div>