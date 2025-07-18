<script>
    import { createEventDispatcher } from 'svelte';
    import { AttendanceStatus } from '../../types/attendance';
    import { attendanceService } from '../../services/attendanceService';
    import Button from '../Button.svelte';
    import StatusBadge from './StatusBadge.svelte';
    
    // Props
    let {
        attendanceId,
        currentStatus,
        notes = '',
        showConfirmation = true,
        class: className = '',
        ...props
    } = $props();
    
    // State
    let loading = false;
    let error = null;
    let success = false;
    let selectedStatus = currentStatus;
    let updatedNotes = notes;
    let confirmDialogOpen = false;
    let statusToConfirm = null;
    
    const dispatch = createEventDispatcher();
    
    // Get status info for display
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
    
    // Handle status selection
    function selectStatus(status) {
        if (status === selectedStatus) return;
        
        if (showConfirmation) {
            statusToConfirm = status;
            confirmDialogOpen = true;
        } else {
            selectedStatus = status;
        }
    }
    
    // Handle confirmation dialog result
    function handleConfirmation(confirmed) {
        if (confirmed && statusToConfirm) {
            selectedStatus = statusToConfirm;
        }
        
        confirmDialogOpen = false;
        statusToConfirm = null;
    }
    
    // Update attendance status
    async function updateStatus() {
        if (selectedStatus === currentStatus && updatedNotes === notes) {
            // No changes to save
            dispatch('cancel');
            return;
        }
        
        loading = true;
        error = null;
        success = false;
        
        try {
            const updateRequest = {
                attendanceId,
                status: selectedStatus,
                notes: updatedNotes
            };
            
            await attendanceService.updateAttendanceStatus(updateRequest);
            
            success = true;
            dispatch('update', { 
                status: selectedStatus, 
                notes: updatedNotes 
            });
            
            // Reset after successful update
            setTimeout(() => {
                if (success) {
                    dispatch('complete');
                }
            }, 1500);
        } catch (err) {
            console.error('Error updating attendance status:', err);
            error = err.message || 'Failed to update status. Please try again.';
        } finally {
            loading = false;
        }
    }
</script>

<div class="status-update-interface {className}" {...props}>
    {#if error}
        <div class="p-4 mb-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
            <p>{error}</p>
        </div>
    {:else if success}
        <div class="p-4 mb-4 bg-green-50 border border-green-200 rounded-lg text-green-700">
            <p>Status updated successfully!</p>
        </div>
    {/if}
    
    <div class="mb-4">
        <label class="block text-sm font-medium text-gray-700 mb-2">Status</label>
        <div class="flex flex-wrap gap-2">
            <button 
                class="p-2 rounded-md transition-all {selectedStatus === AttendanceStatus.CHECKED_IN ? 'ring-2 ring-indigo-500 shadow-md' : 'hover:bg-gray-100'}"
                on:click={() => selectStatus(AttendanceStatus.CHECKED_IN)}
                disabled={loading}
                aria-pressed={selectedStatus === AttendanceStatus.CHECKED_IN}
            >
                <StatusBadge status={AttendanceStatus.CHECKED_IN} />
            </button>
            <button 
                class="p-2 rounded-md transition-all {selectedStatus === AttendanceStatus.CHECKED_OUT ? 'ring-2 ring-indigo-500 shadow-md' : 'hover:bg-gray-100'}"
                on:click={() => selectStatus(AttendanceStatus.CHECKED_OUT)}
                disabled={loading}
                aria-pressed={selectedStatus === AttendanceStatus.CHECKED_OUT}
            >
                <StatusBadge status={AttendanceStatus.CHECKED_OUT} />
            </button>
            <button 
                class="p-2 rounded-md transition-all {selectedStatus === AttendanceStatus.EMERGENCY ? 'ring-2 ring-indigo-500 shadow-md' : 'hover:bg-gray-100'}"
                on:click={() => selectStatus(AttendanceStatus.EMERGENCY)}
                disabled={loading}
                aria-pressed={selectedStatus === AttendanceStatus.EMERGENCY}
            >
                <StatusBadge status={AttendanceStatus.EMERGENCY} />
            </button>
            <button 
                class="p-2 rounded-md transition-all {selectedStatus === AttendanceStatus.NO_SHOW ? 'ring-2 ring-indigo-500 shadow-md' : 'hover:bg-gray-100'}"
                on:click={() => selectStatus(AttendanceStatus.NO_SHOW)}
                disabled={loading}
                aria-pressed={selectedStatus === AttendanceStatus.NO_SHOW}
            >
                <StatusBadge status={AttendanceStatus.NO_SHOW} />
            </button>
        </div>
    </div>
    
    <div class="mb-4">
        <label for="notes" class="block text-sm font-medium text-gray-700 mb-2">Notes</label>
        <textarea
            id="notes"
            class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
            rows="3"
            bind:value={updatedNotes}
            placeholder="Add notes about this status change..."
            disabled={loading}
        ></textarea>
    </div>
    
    <div class="flex justify-end gap-2">
        <Button variant="secondary" onclick={() => dispatch('cancel')} disabled={loading}>
            Cancel
        </Button>
        <Button variant="primary" onclick={updateStatus} disabled={loading}>
            {#if loading}
                <span class="inline-block animate-spin mr-2">⟳</span>
            {/if}
            Update Status
        </Button>
    </div>
    
    <!-- Confirmation Dialog -->
    {#if confirmDialogOpen}
        <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div class="bg-white rounded-lg shadow-lg p-6 max-w-sm mx-auto">
                <h3 class="text-lg font-semibold mb-4">Confirm Status Change</h3>
                <p class="mb-6">
                    Are you sure you want to change the status from 
                    <StatusBadge status={currentStatus} size="sm" /> to 
                    <StatusBadge status={statusToConfirm} size="sm" />?
                </p>
                <div class="flex justify-end gap-2">
                    <Button variant="secondary" onclick={() => handleConfirmation(false)}>
                        Cancel
                    </Button>
                    <Button variant="primary" onclick={() => handleConfirmation(true)}>
                        Confirm
                    </Button>
                </div>
            </div>
        </div>
    {/if}
</div>