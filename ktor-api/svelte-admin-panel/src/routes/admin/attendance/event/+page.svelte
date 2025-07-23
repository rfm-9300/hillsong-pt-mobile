<script>
	import { onMount } from 'svelte';
	import PageHeader from '$lib/components/PageHeader.svelte';
	import Card from '$lib/components/Card.svelte';
	import { api } from '$lib/api';
	import { EventType } from '$lib/types/attendance';
	import { goto } from '$app/navigation';

	let events = [];
	let loading = true;
	let error = null;

	onMount(async () => {
		try {
			const response = await api.get(api.endpoints.EVENTS);
			console.log('API response:', response); // Debug log to see the response structure

			// Check different possible response structures
			if (response && response.data && response.data.events) {
				events = response.data.events;
			} else if (response && response.events) {
				events = response.events;
			} else if (Array.isArray(response)) {
				events = response;
			} else {
				console.error('Unexpected API response structure:', response);
				events = [];
			}

			loading = false;
		} catch (err) {
			console.error('Error loading events:', err);
			error = err.message || 'Failed to load events';
			loading = false;
		}
	});

	function viewAttendance(eventId) {
		goto(`/admin/attendance/event/${eventId}`);
	}
</script>

<PageHeader title="Events Attendance" backLink="/admin/attendance" />

<div class="mb-6">
	<div class="flex justify-between items-center">
		<h2 class="text-xl font-semibold">Select an Event</h2>
	</div>
</div>

{#if loading}
	<div class="flex justify-center items-center h-64">
		<div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
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
			<h3 class="text-lg font-medium mb-2">Error Loading Events</h3>
			<p class="text-gray-600 dark:text-gray-400">{error}</p>
		</div>
	</Card>
{:else if events.length === 0}
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
						d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
					/>
				</svg>
			</div>
			<h3 class="text-lg font-medium mb-2">No Events Found</h3>
			<p class="text-gray-600 dark:text-gray-400">
				There are no events available to manage attendance.
			</p>
		</div>
	</Card>
{:else}
	<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
		{#each events as event}
			<Card>
				<div class="p-6">
					<h3 class="text-lg font-semibold mb-2">{event.title}</h3>
					<p class="text-gray-600 dark:text-gray-400 mb-2">
						<span class="font-medium">Date:</span>
						{new Date(event.date).toLocaleDateString()}
					</p>
					<p class="text-gray-600 dark:text-gray-400 mb-4">
						<span class="font-medium">Location:</span>
						{event.location || 'N/A'}
					</p>
					<button
						on:click={() => viewAttendance(event.id)}
						class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
					>
						View Attendance
					</button>
				</div>
			</Card>
		{/each}
	</div>
{/if}
