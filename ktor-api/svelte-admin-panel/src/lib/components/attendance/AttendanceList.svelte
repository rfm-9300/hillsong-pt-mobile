<script>
	import { onMount, createEventDispatcher } from 'svelte';
	import { attendanceService } from '../../services/attendanceService';
	import { EventType, AttendanceStatus } from '../../types/attendance';
	import Card from '../Card.svelte';
	import Button from '../Button.svelte';
	import EmptyState from '../EmptyState.svelte';
	import StatusBadge from './StatusBadge.svelte';
	import Modal from '../Modal.svelte';
	import StatusUpdateInterface from './StatusUpdateInterface.svelte';
	import LoadingOverlay from '../LoadingOverlay.svelte';

	// Props
	let {
		eventType = EventType.EVENT,
		eventId = null,
		onCheckIn = () => {},
		onCheckOut = () => {},
		onUpdateStatus = () => {},
		class: className = '',
		...props
	} = $props();

	// State
	let attendanceRecords = $state([]);
	let filteredRecords = $state([]);
	let loading = $state(true);
	let error = $state(null);
	let sortField = $state('checkInTime');
	let sortDirection = $state('desc');
	let filterStatus = $state('ALL');
	let searchQuery = $state('');

	// Optimistic UI update tracking
	let pendingOperations = $state({});
	let operationsInProgress = $state(new Set());

	// Modal states
	let showStatusUpdateModal = $state(false);
	let showNotesModal = $state(false);
	let selectedRecord = $state(null);

	// Pagination
	let currentPage = $state(1);
	let itemsPerPage = $state(10);
	let totalPages = $state(1);
	let totalItems = $state(0);

	const dispatch = createEventDispatcher();

	// Load attendance data with server-side pagination
	async function loadAttendanceData() {
		loading = true;
		error = null;

		try {
			if (!eventId) {
				attendanceRecords = [];
				filteredRecords = [];
				totalPages = 0;
				totalItems = 0;
				return;
			}

			// Prepare options for API call
			const options = {
				page: currentPage,
				limit: itemsPerPage,
				search: searchQuery || undefined,
				status: filterStatus !== 'ALL' ? filterStatus : undefined
			};

			// Get data with pagination from the server
			const result = await attendanceService.getAttendanceByEventType(eventType, eventId, options);

			// Update state with server response
			attendanceRecords = result.attendances;
			filteredRecords = result.attendances;
			totalItems = result.total;
			totalPages = Math.ceil(result.total / itemsPerPage);

			// If server doesn't handle sorting, sort locally
			if (sortField) {
				sortRecords();
			}
		} catch (err) {
			console.error('Error loading attendance data:', err);
			error = err.message || 'Failed to load attendance data';
			attendanceRecords = [];
			filteredRecords = [];
		} finally {
			loading = false;
		}
	}

	// Sort records locally
	function sortRecords() {
		filteredRecords.sort((a, b) => {
			let fieldA, fieldB;

			switch (sortField) {
				case 'attendeeName':
					fieldA = a.attendeeName;
					fieldB = b.attendeeName;
					break;
				case 'checkInTime':
					fieldA = new Date(a.attendance.checkInTime);
					fieldB = new Date(b.attendance.checkInTime);
					break;
				case 'checkOutTime':
					fieldA = a.attendance.checkOutTime ? new Date(a.attendance.checkOutTime) : new Date(0);
					fieldB = b.attendance.checkOutTime ? new Date(b.attendance.checkOutTime) : new Date(0);
					break;
				case 'status':
					fieldA = a.attendance.status;
					fieldB = b.attendance.status;
					break;
				default:
					fieldA = a.attendance.checkInTime;
					fieldB = b.attendance.checkInTime;
			}

			if (sortDirection === 'asc') {
				return fieldA > fieldB ? 1 : -1;
			} else {
				return fieldA < fieldB ? 1 : -1;
			}
		});
	}

	// Sort by field
	function sortBy(field) {
		if (sortField === field) {
			// Toggle direction if already sorting by this field
			sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
		} else {
			sortField = field;
			sortDirection = 'asc';
		}

		// Apply sorting locally since we're not sending sort parameters to the server
		sortRecords();
	}

	// Handle status filter change
	function handleStatusFilterChange(event) {
		filterStatus = event.target.value;
		currentPage = 1; // Reset to first page
		loadAttendanceData(); // Reload data with new filter
	}

	// Handle search input
	function handleSearch(event) {
		searchQuery = event.target.value;
		currentPage = 1; // Reset to first page
		loadAttendanceData(); // Reload data with new search query
	}

	// Handle page change
	function goToPage(page) {
		if (page >= 1 && page <= totalPages) {
			currentPage = page;
			loadAttendanceData(); // Reload data with new page
		}
	}

	// Format date
	function formatDate(dateString) {
		if (!dateString) return '—';
		const date = new Date(dateString);
		return new Intl.DateTimeFormat('en-US', {
			year: 'numeric',
			month: 'short',
			day: 'numeric',
			hour: 'numeric',
			minute: 'numeric'
		}).format(date);
	}

	// Get status badge class and icon
	function getStatusInfo(status) {
		switch (status) {
			case AttendanceStatus.CHECKED_IN:
				return {
					badgeClass: 'bg-green-100 text-green-800',
					iconClass: 'text-green-500',
					icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z', // Check circle
					label: 'Checked In'
				};
			case AttendanceStatus.CHECKED_OUT:
				return {
					badgeClass: 'bg-blue-100 text-blue-800',
					iconClass: 'text-blue-500',
					icon: 'M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4', // Archive box
					label: 'Checked Out'
				};
			case AttendanceStatus.EMERGENCY:
				return {
					badgeClass: 'bg-red-100 text-red-800',
					iconClass: 'text-red-500',
					icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z', // Exclamation triangle
					label: 'Emergency'
				};
			case AttendanceStatus.NO_SHOW:
				return {
					badgeClass: 'bg-gray-100 text-gray-800',
					iconClass: 'text-gray-500',
					icon: 'M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z', // X circle
					label: 'No Show'
				};
			default:
				return {
					badgeClass: 'bg-gray-100 text-gray-800',
					iconClass: 'text-gray-500',
					icon: 'M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z', // Question mark circle
					label: 'Unknown'
				};
		}
	}

	// Handle check-out action with optimistic UI update
	function handleCheckOut(record) {
		const attendanceId = record.attendance.id;
		const operationId = `checkout-${attendanceId}`;

		// Add to operations in progress
		operationsInProgress.add(operationId);

		// Store original state in case we need to revert
		pendingOperations[operationId] = {
			type: 'checkout',
			recordId: attendanceId,
			originalStatus: record.attendance.status,
			originalCheckOutTime: record.attendance.checkOutTime
		};

		// Optimistically update the UI
		const index = filteredRecords.findIndex((r) => r.attendance.id === attendanceId);
		if (index !== -1) {
			// Update with optimistic values
			filteredRecords[index].attendance.status = AttendanceStatus.CHECKED_OUT;
			filteredRecords[index].attendance.checkOutTime = new Date().toISOString();
		}

		// Dispatch event to parent component
		dispatch('checkout', {
			attendanceId,
			optimistic: true,
			onSuccess: () => {
				// Operation succeeded, clean up
				operationsInProgress.delete(operationId);
				delete pendingOperations[operationId];
			},
			onError: (error) => {
				// Revert optimistic update
				const index = filteredRecords.findIndex((r) => r.attendance.id === attendanceId);
				if (index !== -1) {
					const original = pendingOperations[operationId];
					filteredRecords[index].attendance.status = original.originalStatus;
					filteredRecords[index].attendance.checkOutTime = original.originalCheckOutTime;
				}
				operationsInProgress.delete(operationId);
				delete pendingOperations[operationId];
			}
		});

		// Call the provided callback
		onCheckOut(record);
	}

	// Open status update modal
	function openStatusUpdateModal(record) {
		selectedRecord = record;
		showStatusUpdateModal = true;
	}

	// Handle status update from modal with optimistic UI update
	function handleStatusUpdateFromModal(event) {
		const { status, notes } = event.detail;
		const attendanceId = selectedRecord.attendance.id;
		const operationId = `status-${attendanceId}`;

		// Add to operations in progress
		operationsInProgress.add(operationId);

		// Store original state in case we need to revert
		pendingOperations[operationId] = {
			type: 'status',
			recordId: attendanceId,
			originalStatus: selectedRecord.attendance.status,
			originalNotes: selectedRecord.attendance.notes
		};

		// Update the record in the local state (optimistic update)
		const index = filteredRecords.findIndex((r) => r.attendance.id === attendanceId);
		if (index !== -1) {
			filteredRecords[index].attendance.status = status;
			filteredRecords[index].attendance.notes = notes;
		}

		// Notify parent component
		dispatch('updatestatus', {
			attendanceId,
			status,
			notes,
			optimistic: true,
			onSuccess: () => {
				// Operation succeeded, clean up
				operationsInProgress.delete(operationId);
				delete pendingOperations[operationId];
			},
			onError: (error) => {
				// Revert optimistic update
				const index = filteredRecords.findIndex((r) => r.attendance.id === attendanceId);
				if (index !== -1) {
					const original = pendingOperations[operationId];
					filteredRecords[index].attendance.status = original.originalStatus;
					filteredRecords[index].attendance.notes = original.originalNotes;
				}
				operationsInProgress.delete(operationId);
				delete pendingOperations[operationId];
			}
		});

		onUpdateStatus(selectedRecord, status);

		// Close modal
		showStatusUpdateModal = false;
		selectedRecord = null;
	}

	// Handle status update (legacy method - will be replaced by modal)
	function handleStatusUpdate(record, newStatus) {
		// Use the new modal approach instead
		selectedRecord = record;
		openStatusUpdateModal(record);
	}

	// Watch for changes in props
	$effect(() => {
		loadAttendanceData();
	});

	// Helper function to check if any operations are in progress
	function hasActiveOperations() {
		return operationsInProgress.size > 0;
	}
</script>

<div class="attendance-list {className}" {...props}>
	<!-- Filters and search -->
	<div class="mb-4 flex flex-col md:flex-row gap-4 justify-between">
		<div class="flex flex-col sm:flex-row gap-2">
			<div class="relative">
				<input
					type="text"
					placeholder="Search attendees..."
					class="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 w-full"
					value={searchQuery}
					oninput={handleSearch}
				/>
				<div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
					<svg
						class="h-5 w-5 text-gray-400"
						xmlns="http://www.w3.org/2000/svg"
						viewBox="0 0 20 20"
						fill="currentColor"
					>
						<path
							fill-rule="evenodd"
							d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
							clip-rule="evenodd"
						/>
					</svg>
				</div>
			</div>

			<div class="relative">
				<div class="relative group">
					<select
						class="border border-gray-300 rounded-lg pl-9 pr-4 py-2 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 appearance-none w-full"
						value={filterStatus}
						onchange={handleStatusFilterChange}
					>
						<option value="ALL">All Statuses</option>
						<option value={AttendanceStatus.CHECKED_IN}>Checked In</option>
						<option value={AttendanceStatus.CHECKED_OUT}>Checked Out</option>
						<option value={AttendanceStatus.EMERGENCY}>Emergency</option>
						<option value={AttendanceStatus.NO_SHOW}>No Show</option>
					</select>
					<div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
						{#if filterStatus !== 'ALL'}
							{@const statusInfo = getStatusInfo(filterStatus)}
							<svg
								class="h-4 w-4 {statusInfo.iconClass}"
								xmlns="http://www.w3.org/2000/svg"
								fill="none"
								viewBox="0 0 24 24"
								stroke="currentColor"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d={statusInfo.icon}
								/>
							</svg>
						{:else}
							<svg
								class="h-4 w-4 text-gray-500"
								xmlns="http://www.w3.org/2000/svg"
								fill="none"
								viewBox="0 0 24 24"
								stroke="currentColor"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
								/>
							</svg>
						{/if}
					</div>
					<!-- Custom dropdown with status indicators (shown on hover) -->
					<div
						class="absolute hidden group-hover:block mt-1 w-full bg-white border border-gray-300 rounded-md shadow-lg z-10"
					>
						<div class="py-1">
							<button
								class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center gap-2"
								onclick={() => {
									filterStatus = 'ALL';
									handleStatusFilterChange({ target: { value: 'ALL' } });
								}}
							>
								<svg
									class="h-4 w-4 text-gray-500"
									xmlns="http://www.w3.org/2000/svg"
									fill="none"
									viewBox="0 0 24 24"
									stroke="currentColor"
								>
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"
									/>
								</svg>
								All Statuses
							</button>
							<button
								class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center gap-2"
								onclick={() => {
									filterStatus = AttendanceStatus.CHECKED_IN;
									handleStatusFilterChange({ target: { value: AttendanceStatus.CHECKED_IN } });
								}}
							>
								<StatusBadge status={AttendanceStatus.CHECKED_IN} size="sm" />
							</button>
							<button
								class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center gap-2"
								onclick={() => {
									filterStatus = AttendanceStatus.CHECKED_OUT;
									handleStatusFilterChange({ target: { value: AttendanceStatus.CHECKED_OUT } });
								}}
							>
								<StatusBadge status={AttendanceStatus.CHECKED_OUT} size="sm" />
							</button>
							<button
								class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center gap-2"
								onclick={() => {
									filterStatus = AttendanceStatus.EMERGENCY;
									handleStatusFilterChange({ target: { value: AttendanceStatus.EMERGENCY } });
								}}
							>
								<StatusBadge status={AttendanceStatus.EMERGENCY} size="sm" />
							</button>
							<button
								class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center gap-2"
								onclick={() => {
									filterStatus = AttendanceStatus.NO_SHOW;
									handleStatusFilterChange({ target: { value: AttendanceStatus.NO_SHOW } });
								}}
							>
								<StatusBadge status={AttendanceStatus.NO_SHOW} size="sm" />
							</button>
						</div>
					</div>
				</div>
				<div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
					<svg
						class="h-4 w-4 text-gray-500"
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke="currentColor"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M19 9l-7 7-7-7"
						/>
					</svg>
				</div>
			</div>
		</div>

		<div>
			<Button variant="primary" onclick={() => dispatch('checkin')} disabled={loading}>
				{#if loading}
					<span class="inline-block animate-spin mr-2">⟳</span>
				{/if}
				Check In Attendee
			</Button>
		</div>
	</div>

	<!-- Active filters display -->
	{#if filterStatus !== 'ALL'}
		<div class="mb-4 flex flex-wrap gap-2">
			<div class="text-sm text-gray-500 flex items-center">
				<span class="mr-2">Active filters:</span>
				<div class="flex items-center gap-1 px-2 py-1 bg-gray-100 rounded-full">
					<StatusBadge status={filterStatus} size="sm" />
					<button
						class="ml-1 text-gray-400 hover:text-gray-600"
						onclick={() => {
							filterStatus = 'ALL';
							handleStatusFilterChange({ target: { value: 'ALL' } });
						}}
						aria-label="Clear status filter"
					>
						<svg
							class="h-4 w-4"
							xmlns="http://www.w3.org/2000/svg"
							viewBox="0 0 20 20"
							fill="currentColor"
						>
							<path
								fill-rule="evenodd"
								d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
								clip-rule="evenodd"
							/>
						</svg>
					</button>
				</div>
			</div>
		</div>
	{/if}

	<!-- Attendance table -->
	<Card class="overflow-hidden relative">
		<LoadingOverlay show={loading} message="Loading attendance data..." transparent={true} />
		{#if loading && !filteredRecords.length}
			<div class="flex justify-center items-center p-8">
				<div class="h-12 w-12"></div>
			</div>
		{:else if error}
			<div class="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
				<p>{error}</p>
				<Button variant="secondary" size="sm" class="mt-2" onclick={loadAttendanceData}>
					Retry
				</Button>
			</div>
		{:else if filteredRecords.length === 0}
			<EmptyState
				title="No attendance records found"
				description="There are no attendance records matching your filters."
			/>
		{:else}
			<div class="overflow-x-auto">
				<table class="min-w-full divide-y divide-gray-200">
					<thead class="bg-gray-50">
						<tr>
							<th
								class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
								onclick={() => sortBy('attendeeName')}
							>
								<div class="flex items-center">
									Attendee
									{#if sortField === 'attendeeName'}
										<svg
											class="ml-1 w-4 h-4"
											fill="currentColor"
											viewBox="0 0 20 20"
											xmlns="http://www.w3.org/2000/svg"
										>
											{#if sortDirection === 'asc'}
												<path
													fill-rule="evenodd"
													d="M5.293 7.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L10 4.414l-3.293 3.293a1 1 0 01-1.414 0z"
													clip-rule="evenodd"
												></path>
											{:else}
												<path
													fill-rule="evenodd"
													d="M14.707 12.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L10 15.586l3.293-3.293a1 1 0 011.414 0z"
													clip-rule="evenodd"
												></path>
											{/if}
										</svg>
									{/if}
								</div>
							</th>
							<th
								class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
								onclick={() => sortBy('checkInTime')}
							>
								<div class="flex items-center">
									Check-in Time
									{#if sortField === 'checkInTime'}
										<svg
											class="ml-1 w-4 h-4"
											fill="currentColor"
											viewBox="0 0 20 20"
											xmlns="http://www.w3.org/2000/svg"
										>
											{#if sortDirection === 'asc'}
												<path
													fill-rule="evenodd"
													d="M5.293 7.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L10 4.414l-3.293 3.293a1 1 0 01-1.414 0z"
													clip-rule="evenodd"
												></path>
											{:else}
												<path
													fill-rule="evenodd"
													d="M14.707 12.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L10 15.586l3.293-3.293a1 1 0 011.414 0z"
													clip-rule="evenodd"
												></path>
											{/if}
										</svg>
									{/if}
								</div>
							</th>
							<th
								class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
								onclick={() => sortBy('checkOutTime')}
							>
								<div class="flex items-center">
									Check-out Time
									{#if sortField === 'checkOutTime'}
										<svg
											class="ml-1 w-4 h-4"
											fill="currentColor"
											viewBox="0 0 20 20"
											xmlns="http://www.w3.org/2000/svg"
										>
											{#if sortDirection === 'asc'}
												<path
													fill-rule="evenodd"
													d="M5.293 7.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L10 4.414l-3.293 3.293a1 1 0 01-1.414 0z"
													clip-rule="evenodd"
												></path>
											{:else}
												<path
													fill-rule="evenodd"
													d="M14.707 12.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L10 15.586l3.293-3.293a1 1 0 011.414 0z"
													clip-rule="evenodd"
												></path>
											{/if}
										</svg>
									{/if}
								</div>
							</th>
							<th
								class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
								onclick={() => sortBy('status')}
							>
								<div class="flex items-center">
									Status
									{#if sortField === 'status'}
										<svg
											class="ml-1 w-4 h-4"
											fill="currentColor"
											viewBox="0 0 20 20"
											xmlns="http://www.w3.org/2000/svg"
										>
											{#if sortDirection === 'asc'}
												<path
													fill-rule="evenodd"
													d="M5.293 7.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L10 4.414l-3.293 3.293a1 1 0 01-1.414 0z"
													clip-rule="evenodd"
												></path>
											{:else}
												<path
													fill-rule="evenodd"
													d="M14.707 12.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L10 15.586l3.293-3.293a1 1 0 011.414 0z"
													clip-rule="evenodd"
												></path>
											{/if}
										</svg>
									{/if}
								</div>
							</th>
							<th
								class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
							>
								Notes
							</th>
							<th
								class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider"
							>
								Actions
							</th>
						</tr>
					</thead>
					<tbody class="bg-white divide-y divide-gray-200">
						{#each filteredRecords as record (record.attendance.id)}
							<tr class="hover:bg-gray-50 relative">
								{#if operationsInProgress.has(`checkout-${record.attendance.id}`) || operationsInProgress.has(`status-${record.attendance.id}`)}
									<td colspan="7" class="absolute inset-0 z-10 p-0">
										<LoadingOverlay show={true} transparent={true} message="" />
									</td>
								{/if}
								<td class="px-6 py-4 whitespace-nowrap" data-label="Attendee">
									<div class="text-sm font-medium text-gray-900">{record.attendeeName}</div>
									<div class="text-xs text-gray-500">
										{record.attendance.userId ? 'User' : 'Kid'}
									</div>
								</td>
								<td class="px-6 py-4 whitespace-nowrap" data-label="Check-in Time">
									<div class="text-sm text-gray-900">
										{formatDate(record.attendance.checkInTime)}
									</div>
									<div class="text-xs text-gray-500">By {record.checkedInByName}</div>
								</td>
								<td class="px-6 py-4 whitespace-nowrap" data-label="Check-out Time">
									{#if record.attendance.checkOutTime}
										<div class="text-sm text-gray-900">
											{formatDate(record.attendance.checkOutTime)}
										</div>
										<div class="text-xs text-gray-500">By {record.checkedOutByName}</div>
									{:else}
										<span class="text-sm text-gray-500">—</span>
									{/if}
								</td>
								<td class="px-6 py-4 whitespace-nowrap" data-label="Status">
									{#if record.attendance.status}
										<StatusBadge status={record.attendance.status} />
									{:else}
										<StatusBadge status="UNKNOWN" />
									{/if}
								</td>
								<td class="px-6 py-4" data-label="Notes">
									<div class="text-sm text-gray-900 truncate max-w-xs">
										{record.attendance.notes || '—'}
									</div>
								</td>
								<td
									class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium relative"
									data-label="Actions"
								>
									<div class="flex justify-end space-x-2">
										{#if record.attendance.status === AttendanceStatus.CHECKED_IN}
											<Button
												variant="secondary"
												size="sm"
												onclick={() => handleCheckOut(record)}
												loading={operationsInProgress.has(`checkout-${record.attendance.id}`)}
												disabled={operationsInProgress.has(`checkout-${record.attendance.id}`) ||
													operationsInProgress.has(`status-${record.attendance.id}`)}
											>
												Check Out
											</Button>
										{/if}
										<div class="relative group">
											<Button variant="outline" size="sm" class="flex items-center gap-1">
												Status
												<svg
													class="h-4 w-4"
													xmlns="http://www.w3.org/2000/svg"
													viewBox="0 0 20 20"
													fill="currentColor"
												>
													<path
														fill-rule="evenodd"
														d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
														clip-rule="evenodd"
													/>
												</svg>
											</Button>
											<div
												class="absolute hidden group-hover:block right-0 mt-1 w-48 bg-white border border-gray-300 rounded-md shadow-lg z-10"
											>
												<div class="py-1">
													<button
														class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center gap-2 {operationsInProgress.has(
															`checkout-${record.attendance.id}`
														) || operationsInProgress.has(`status-${record.attendance.id}`)
															? 'opacity-50 cursor-not-allowed'
															: ''}"
														onclick={() => openStatusUpdateModal(record)}
														disabled={operationsInProgress.has(
															`checkout-${record.attendance.id}`
														) || operationsInProgress.has(`status-${record.attendance.id}`)}
													>
														{#if operationsInProgress.has(`status-${record.attendance.id}`)}
															<span class="inline-block animate-spin mr-2">⟳</span>
														{/if}
														<span class="text-indigo-600">Update Status</span>
													</button>
													<div class="border-t border-gray-100 my-1"></div>
													<div class="px-4 py-2 text-xs text-gray-500">
														{#if operationsInProgress.has(`checkout-${record.attendance.id}`)}
															<div class="flex items-center">
																<span class="inline-block animate-spin mr-2">⟳</span>
																Checking out...
															</div>
														{:else if operationsInProgress.has(`status-${record.attendance.id}`)}
															<div class="flex items-center">
																<span class="inline-block animate-spin mr-2">⟳</span>
																Updating status...
															</div>
														{/if}
													</div>
													<div class="border-t border-gray-100 my-1"></div>
													<button
														class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center gap-2 opacity-75"
														disabled={true}
													>
														<span class="text-xs text-gray-500">Current Status:</span>
													</button>
													<button
														class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center gap-2"
														disabled={true}
													>
														<StatusBadge status={record.attendance.status} size="sm" />
													</button>
													<button
														class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center gap-2"
														onclick={() => handleStatusUpdate(record, AttendanceStatus.EMERGENCY)}
														disabled={record.attendance.status === AttendanceStatus.EMERGENCY}
													>
														<StatusBadge status={AttendanceStatus.EMERGENCY} size="sm" />
													</button>
													<button
														class="w-full text-left px-4 py-2 text-sm hover:bg-gray-100 flex items-center gap-2"
														onclick={() => handleStatusUpdate(record, AttendanceStatus.NO_SHOW)}
														disabled={record.attendance.status === AttendanceStatus.NO_SHOW}
													>
														<StatusBadge status={AttendanceStatus.NO_SHOW} size="sm" />
													</button>
												</div>
											</div>
										</div>
										<Button variant="ghost" size="sm" onclick={() => dispatch('view', record)}>
											View
										</Button>
									</div>
								</td>
							</tr>
						{/each}
					</tbody>
				</table>
			</div>

			<!-- Pagination -->
			{#if totalPages > 1}
				<div class="px-6 py-3 flex items-center justify-between border-t border-gray-200">
					<div class="flex-1 flex justify-between sm:hidden">
						<Button
							variant="secondary"
							size="sm"
							disabled={currentPage === 1}
							onclick={() => goToPage(currentPage - 1)}
						>
							Previous
						</Button>
						<Button
							variant="secondary"
							size="sm"
							disabled={currentPage === totalPages}
							onclick={() => goToPage(currentPage + 1)}
						>
							Next
						</Button>
					</div>
					<div class="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
						<div>
							<p class="text-sm text-gray-700">
								Showing <span class="font-medium">{(currentPage - 1) * itemsPerPage + 1}</span> to
								<span class="font-medium"
									>{Math.min(currentPage * itemsPerPage, attendanceRecords.length)}</span
								>
								of <span class="font-medium">{attendanceRecords.length}</span> results
							</p>
						</div>
						<div>
							<nav
								class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px"
								aria-label="Pagination"
							>
								<button
									class="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
									disabled={currentPage === 1}
									onclick={() => goToPage(currentPage - 1)}
								>
									<span class="sr-only">Previous</span>
									<svg
										class="h-5 w-5"
										xmlns="http://www.w3.org/2000/svg"
										viewBox="0 0 20 20"
										fill="currentColor"
										aria-hidden="true"
									>
										<path
											fill-rule="evenodd"
											d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
											clip-rule="evenodd"
										/>
									</svg>
								</button>

								{#each Array(totalPages) as _, i}
									<button
										class="relative inline-flex items-center px-4 py-2 border border-gray-300 bg-white text-sm font-medium {currentPage ===
										i + 1
											? 'text-indigo-600 bg-indigo-50'
											: 'text-gray-700 hover:bg-gray-50'}"
										onclick={() => goToPage(i + 1)}
									>
										{i + 1}
									</button>
								{/each}

								<button
									class="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
									disabled={currentPage === totalPages}
									onclick={() => goToPage(currentPage + 1)}
								>
									<span class="sr-only">Next</span>
									<svg
										class="h-5 w-5"
										xmlns="http://www.w3.org/2000/svg"
										viewBox="0 0 20 20"
										fill="currentColor"
										aria-hidden="true"
									>
										<path
											fill-rule="evenodd"
											d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
											clip-rule="evenodd"
										/>
									</svg>
								</button>
							</nav>
						</div>
					</div>
				</div>
			{/if}
		{/if}
	</Card>

	<!-- Status Update Modal -->
	<Modal
		show={showStatusUpdateModal}
		title="Update Attendance Status"
		onClose={() => (showStatusUpdateModal = false)}
		size="md"
	>
		{#if selectedRecord}
			<div class="mb-4">
				<h3 class="font-medium text-gray-900">Attendee: {selectedRecord.attendeeName}</h3>
				<p class="text-sm text-gray-500">
					Current Status: <StatusBadge status={selectedRecord.attendance.status} size="sm" />
				</p>
			</div>

			<StatusUpdateInterface
				attendanceId={selectedRecord.attendance.id}
				currentStatus={selectedRecord.attendance.status}
				notes={selectedRecord.attendance.notes}
				onupdate={handleStatusUpdateFromModal}
				oncancel={() => (showStatusUpdateModal = false)}
				oncomplete={() => (showStatusUpdateModal = false)}
			/>
		{/if}
	</Modal>
</div>

<style>
	/* Responsive styles for mobile */
	@media (max-width: 640px) {
		table {
			display: block;
		}

		thead {
			display: none;
		}

		tbody {
			display: block;
		}

		tr {
			display: block;
			margin-bottom: 1rem;
			border-bottom: 1px solid #e5e7eb;
		}

		td {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 0.5rem 1rem;
			text-align: right;
			border-bottom: none;
		}

		td::before {
			content: attr(data-label);
			font-weight: 500;
			text-align: left;
			color: #6b7280;
		}
	}
</style>
