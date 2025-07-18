<script>
    import { onMount } from 'svelte';
    import PageHeader from '$lib/components/PageHeader.svelte';
    import Card from '$lib/components/Card.svelte';
    import { EventType } from '$lib/types/attendance';
    
    let selectedEventType = EventType.EVENT;
    let dateRange = {
        start: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // Last 30 days
        end: new Date().toISOString().split('T')[0] // Today
    };
    
    function updateEventType(type) {
        selectedEventType = type;
        // Here you would fetch reports for the selected event type
    }
    
    function updateDateRange() {
        // Here you would fetch reports for the updated date range
    }
    
    function exportReport(format) {
        // Here you would implement export functionality
        alert(`Exporting ${selectedEventType} report in ${format} format`);
    }
</script>

<PageHeader title="Attendance Reports" backLink="/admin/attendance" />

<div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
    <div class="lg:col-span-1">
        <Card>
            <div class="p-6">
                <h3 class="text-lg font-semibold mb-4">Report Filters</h3>
                
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Event Type</label>
                    <div class="flex flex-col space-y-2">
                        <label class="inline-flex items-center">
                            <input 
                                type="radio" 
                                class="form-radio" 
                                name="eventType" 
                                value={EventType.EVENT} 
                                checked={selectedEventType === EventType.EVENT}
                                on:change={() => updateEventType(EventType.EVENT)}
                            />
                            <span class="ml-2">Events</span>
                        </label>
                        <label class="inline-flex items-center">
                            <input 
                                type="radio" 
                                class="form-radio" 
                                name="eventType" 
                                value={EventType.SERVICE} 
                                checked={selectedEventType === EventType.SERVICE}
                                on:change={() => updateEventType(EventType.SERVICE)}
                            />
                            <span class="ml-2">Services</span>
                        </label>
                        <label class="inline-flex items-center">
                            <input 
                                type="radio" 
                                class="form-radio" 
                                name="eventType" 
                                value={EventType.KIDS_SERVICE} 
                                checked={selectedEventType === EventType.KIDS_SERVICE}
                                on:change={() => updateEventType(EventType.KIDS_SERVICE)}
                            />
                            <span class="ml-2">Kids Services</span>
                        </label>
                    </div>
                </div>
                
                <div class="mb-4">
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Date Range</label>
                    <div class="space-y-2">
                        <div>
                            <label class="block text-xs text-gray-500 dark:text-gray-400 mb-1">Start Date</label>
                            <input 
                                type="date" 
                                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800"
                                bind:value={dateRange.start}
                                on:change={updateDateRange}
                            />
                        </div>
                        <div>
                            <label class="block text-xs text-gray-500 dark:text-gray-400 mb-1">End Date</label>
                            <input 
                                type="date" 
                                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-800"
                                bind:value={dateRange.end}
                                on:change={updateDateRange}
                            />
                        </div>
                    </div>
                </div>
                
                <div>
                    <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Export Report</h4>
                    <div class="flex space-x-2">
                        <button 
                            on:click={() => exportReport('csv')}
                            class="px-3 py-2 bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-md hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
                        >
                            CSV
                        </button>
                        <button 
                            on:click={() => exportReport('pdf')}
                            class="px-3 py-2 bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-md hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
                        >
                            PDF
                        </button>
                    </div>
                </div>
            </div>
        </Card>
    </div>
    
    <div class="lg:col-span-2">
        <Card>
            <div class="p-6">
                <h3 class="text-lg font-semibold mb-4">Attendance Summary</h3>
                
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
                    <div class="bg-blue-50 dark:bg-blue-900 p-4 rounded-lg">
                        <div class="text-sm text-blue-600 dark:text-blue-300 mb-1">Total Attendees</div>
                        <div class="text-2xl font-bold">0</div>
                    </div>
                    <div class="bg-green-50 dark:bg-green-900 p-4 rounded-lg">
                        <div class="text-sm text-green-600 dark:text-green-300 mb-1">Average Attendance</div>
                        <div class="text-2xl font-bold">0</div>
                    </div>
                    <div class="bg-purple-50 dark:bg-purple-900 p-4 rounded-lg">
                        <div class="text-sm text-purple-600 dark:text-purple-300 mb-1">Check-in Rate</div>
                        <div class="text-2xl font-bold">0%</div>
                    </div>
                    <div class="bg-yellow-50 dark:bg-yellow-900 p-4 rounded-lg">
                        <div class="text-sm text-yellow-600 dark:text-yellow-300 mb-1">No-Show Rate</div>
                        <div class="text-2xl font-bold">0%</div>
                    </div>
                </div>
                
                <div class="mb-6">
                    <h4 class="text-md font-medium mb-2">Attendance Trend</h4>
                    <div class="bg-gray-100 dark:bg-gray-800 h-64 rounded-lg flex items-center justify-center">
                        <p class="text-gray-500 dark:text-gray-400">Chart will be displayed here</p>
                    </div>
                </div>
                
                <div>
                    <h4 class="text-md font-medium mb-2">Top Events by Attendance</h4>
                    <div class="overflow-x-auto">
                        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                            <thead>
                                <tr>
                                    <th class="px-6 py-3 bg-gray-50 dark:bg-gray-800 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                        Name
                                    </th>
                                    <th class="px-6 py-3 bg-gray-50 dark:bg-gray-800 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                        Date
                                    </th>
                                    <th class="px-6 py-3 bg-gray-50 dark:bg-gray-800 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                                        Attendees
                                    </th>
                                </tr>
                            </thead>
                            <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-800">
                                <tr>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">
                                        No data available
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                                        -
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                                        -
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </Card>
    </div>
</div>