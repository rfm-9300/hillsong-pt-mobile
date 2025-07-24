<script>
	import PageHeader from '$lib/components/PageHeader.svelte';
	import Card from '$lib/components/Card.svelte';
	import { EventType } from '$lib/types/attendance';
	import { goto } from '$app/navigation';

	/** @type {import('./$types').PageData} */
	export let data;

	let kidsServices = [];
	let loading = false;
	let error = data.error || null;

	$: {
		if (data.kidsServices) {
			// Format kids service data for display
			kidsServices = data.kidsServices.map(service => {
				// Extract time information - handle both camelCase and snake_case field names
				const startTime = service.startTime || service.start_time ? 
					new Date(service.startTime || service.start_time) : null;
				const endTime = service.endTime || service.end_time ? 
					new Date(service.endTime || service.end_time) : null;

				// Format time
				const time = startTime ? 
					`${startTime.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })}${endTime ? ' - ' + endTime.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' }) : ''}` : 
					'N/A';

				console.log('Formatted kids service:', { 
					id: service.id,
					name: service.name,
					ageGroup: service.ageGroup || service.age_group,
					startTime: startTime?.toISOString(),
					endTime: endTime?.toISOString(),
					time
				});

				return {
					...service,
					ageGroup: service.ageGroup || service.age_group || 'All Ages',
					time
				};
			});
		}
	}

	function viewAttendance(kidsServiceId) {
		goto(`/admin/attendance/kids-service/${kidsServiceId}`);
	}
</script>

<PageHeader title="Kids Services Attendance" backLink="/admin/attendance" />

<div class="mb-6">
	<div class="flex justify-between items-center">
		<h2 class="text-xl font-semibold">Select a Kids Service</h2>
	</div>
</div>

{#if loading}
	<div class="flex justify-center items-center h-64">
		<div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-green-500"></div>
	</div>
{:else if error}
	<Card>
		<div class="p-6 text-center">
			<div class="text-red-500 mb-4">
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="h-12 w-12 mx-auto"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
					/>
				</svg>
			</div>
			<h3 class="text-lg font-medium mb-2">Error Loading Kids Services</h3>
			<p class="text-gray-600 dark:text-gray-400">{error}</p>
		</div>
	</Card>
{:else if kidsServices.length === 0}
	<Card>
		<div class="p-6 text-center">
			<div class="text-gray-400 mb-4">
				<svg
					xmlns="http://www.w3.org/2000/svg"
					class="h-12 w-12 mx-auto"
					fill="none"
					viewBox="0 0 24 24"
					stroke="currentColor"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
					/>
				</svg>
			</div>
			<h3 class="text-lg font-medium mb-2">No Kids Services Found</h3>
			<p class="text-gray-600 dark:text-gray-400">
				There are no kids services available to manage attendance.
			</p>
		</div>
	</Card>
{:else}
	<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
		{#each kidsServices as service}
			<Card>
				<div class="p-6">
					<h3 class="text-lg font-semibold mb-2">{service.name}</h3>
					
					{#if service.description}
						<p class="text-gray-600 dark:text-gray-400 mb-2 text-sm">
							{service.description}
						</p>
					{/if}
					
					<p class="text-gray-600 dark:text-gray-400 mb-2">
						<span class="font-medium">Age Group:</span>
						{service.ageGroup}
					</p>
					
					<p class="text-gray-600 dark:text-gray-400 mb-2">
						<span class="font-medium">Time:</span>
						{service.time}
					</p>
					
					{#if service.location}
						<p class="text-gray-600 dark:text-gray-400 mb-4">
							<span class="font-medium">Location:</span>
							{service.location}
						</p>
					{/if}
					
					<div class="flex items-center justify-between mb-4">
						<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {service.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
							{service.isActive ? 'Active' : 'Inactive'}
						</span>
					</div>
					
					<button
						on:click={() => viewAttendance(service.id)}
						class="w-full px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
						disabled={!service.isActive}
					>
						View Attendance
					</button>
				</div>
			</Card>
		{/each}
	</div>
{/if}
