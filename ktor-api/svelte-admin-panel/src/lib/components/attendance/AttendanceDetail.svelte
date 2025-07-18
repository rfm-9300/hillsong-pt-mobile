<script>
    import { createEventDispatcher } from 'svelte';
    import { attendanceService } from '../../services/attendanceService';
    import { AttendanceStatus } from '../../types/attendance';
    import Modal from '../Modal.svelte';
    import Button from '../Button.svelte';
    import StatusBadge from './StatusBadge.svelte';
    import StatusUpdateInterface from './StatusUpdateInterface.svelte';
    import NotesEditor from './NotesEditor.svelte';
    
    // Props
    let {
        show = false,
        attendance = null,
        onClose = () => {},
        onUpdate = () => {},
        class: className = '',
        ...props
    } = $props();
    
    // State
    let isEditing = $state(false);
    let updatedAttendance = $state(null);
    let loading = $state(false);
    let error = $state(null);
    let successMessage = $state('');
    let showConfirmation = $state(false);
    let confirmationAction = $state(null);
    let confirmationMessage = $state('');
    let newStatus = $state(null);
    
    // Create a copy of attendance for editing
    $effect(() => {
        if (attendance) {
            updatedAttendance = { ...attendance };
        }
    });
    
    const dispatch = createEventDispatcher();
    
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
    
    // Get status info based on status
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
    
    // Toggle edit mode
    function toggleEditMode() {
        isEditing = !isEditing;
        if (!isEditing) {
            // Reset to original values when canceling edit
            updatedAttendance = { ...attendance };
        }
    }
    
    // Update attendance status
    async function updateStatus(newStatus) {
        loading = true;
        error = null;
        successMessage = '';
        
        try {
            const updateRequest = {
                attendanceId: attendance.attendance.id,
                status: newStatus,
                notes: updatedAttendance.attendance.notes
            };
            
            await attendanceService.updateAttendanceStatus(updateRequest);
            
            // Update local state
            attendance.attendance.status = newStatus;
            updatedAttendance.attendance.status = newStatus;
            
            successMessage = `Status updated to ${getStatusInfo(newStatus).label}`;
            dispatch('update', { attendance });
            onUpdate(attendance);
            
            // Exit edit mode
            isEditing = false;
        } catch (err) {
            console.error('Error updating attendance status:', err);
            error = err.message || 'Failed to update status. Please try again.';
        } finally {
            loading = false;
        }
    }
    
    // Update attendance notes
    async function updateNotes() {
        loading = true;
        error = null;
        successMessage = '';
        
        try {
            const updateRequest = {
                attendanceId: attendance.attendance.id,
                notes: updatedAttendance.attendance.notes
            };
            
            await attendanceService.updateAttendanceNotes(updateRequest);
            
            // Update local state
            attendance.attendance.notes = updatedAttendance.attendance.notes;
            
            successMessage = 'Notes updated successfully';
            dispatch('update', { attendance });
            onUpdate(attendance);
            
            // Exit edit mode
            isEditing = false;
        } catch (err) {
            console.error('Error updating attendance notes:', err);
            error = err.message || 'Failed to update notes. Please try again.';
        } finally {
            loading = false;
        }
    }
    
    // Check out attendee
    async function checkOut() {
        loading = true;
        error = null;
        successMessage = '';
        
        try {
            // Validate that the attendee is currently checked in
            if (attendance.attendance.status !== AttendanceStatus.CHECKED_IN) {
                throw new Error(`Cannot check out attendee with status: ${getStatusInfo(attendance.attendance.status).label}`);
            }
            
            // Validate that the attendee hasn't already been checked out
            if (attendance.attendance.checkOutTime) {
                throw new Error('This attendee has already been checked out');
            }
            
            const checkOutRequest = {
                attendanceId: attendance.attendance.id,
                checkedOutBy: 1, // TODO: Get current user ID
                notes: updatedAttendance.attendance.notes
            };
            
            // Determine if it's a user or kid
            if (attendance.attendance.userId) {
                await attendanceService.checkOutUser(checkOutRequest);
            } else if (attendance.attendance.kidId) {
                await attendanceService.checkOutKid(checkOutRequest);
            } else {
                throw new Error('Invalid attendee type: neither user nor kid ID found');
            }
            
            // Update local state
            attendance.attendance.status = AttendanceStatus.CHECKED_OUT;
            attendance.attendance.checkOutTime = new Date().toISOString();
            updatedAttendance = { ...attendance };
            
            successMessage = 'Attendee checked out successfully';
            dispatch('checkout', { attendance });
            onUpdate(attendance);
            
            // Exit edit mode
            isEditing = false;
        } catch (err) {
            console.error('Error checking out attendee:', err);
            
            // Handle specific error cases
            if (err.message && err.message.includes('already been checked out')) {
                error = err.message;
            } else if (err.message && err.message.includes('Cannot check out')) {
                error = err.message;
            } else if (err.message && err.message.includes('network')) {
                error = 'Network error. Please check your connection and try again.';
            } else if (err.message && err.message.includes('timeout')) {
                error = 'The request timed out. Please try again.';
            } else if (err.message && err.message.includes('permission')) {
                error = 'You do not have permission to check out this attendee.';
            } else if (err.message && err.message.includes('not found')) {
                error = 'The attendance record could not be found.';
            } else if (err.message && err.message.includes('Invalid attendee type')) {
                error = err.message;
            } else {
                error = err.message || 'Failed to check out attendee. Please try again.';
            }
            
            // Provide recovery options for certain errors
            if (error.includes('already been checked out')) {
                // Refresh the attendance data to show the correct status
                onUpdate(attendance);
            }
        } finally {
            loading = false;
        }
    }
    
    // Show confirmation dialog for status change
    function confirmStatusChange(status) {
        newStatus = status;
        confirmationMessage = `Are you sure you want to change the status to ${getStatusInfo(status).label}?`;
        confirmationAction = 'updateStatus';
        showConfirmation = true;
    }
    
    // Show confirmation dialog for check out
    function confirmCheckOut() {
        confirmationMessage = 'Are you sure you want to check out this attendee?';
        confirmationAction = 'checkOut';
        showConfirmation = true;
    }
    
    // Handle confirmation dialog result
    function handleConfirmation(confirmed) {
        if (confirmed) {
            if (confirmationAction === 'updateStatus' && newStatus) {
                updateStatus(newStatus);
            } else if (confirmationAction === 'checkOut') {
                checkOut();
            }
        }
        
        // Reset confirmation state
        showConfirmation = false;
        confirmationAction = null;
        newStatus = null;
    }
    
    // Save changes
    function saveChanges() {
        if (updatedAttendance.attendance.status !== attendance.attendance.status) {
            confirmStatusChange(updatedAttendance.attendance.status);
        } else if (updatedAttendance.attendance.notes !== attendance.attendance.notes) {
            updateNotes();
        } else {
            // No changes to save
            isEditing = false;
        }
    }
</script>

<Modal 
    show={show} 
    title="Attendance Details" 
    onClose={onClose}
    size="lg"
>
    {#if !attendance}
        <div class="text-center py-4">
            <p>No attendance record selected</p>
        </div>
    {:else}
        <div class="attendance-detail {className}" {...props}>
            {#if loading}
                <div class="flex justify-center items-center p-4">
                    <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-indigo-500"></div>
                </div>
            {:else if error}
                <div class="p-4 mb-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
                    <p>{error}</p>
                </div>
            {:else if successMessage}
                <div class="p-4 mb-4 bg-green-50 border border-green-200 rounded-lg text-green-700">
                    <p>{successMessage}</p>
                </div>
            {/if}
            
            <!-- Attendee Information -->
            <div class="mb-6">
                <h3 class="text-lg font-semibold mb-2">Attendee Information</h3>
                <div class="bg-gray-50 p-4 rounded-lg">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <p class="text-sm text-gray-500">Name</p>
                            <p class="font-medium">{attendance.attendeeName}</p>
                        </div>
                        <div>
                            <p class="text-sm text-gray-500">Type</p>
                            <p class="font-medium">{attendance.attendance.userId ? 'User' : 'Kid'}</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Event Information -->
            <div class="mb-6">
                <h3 class="text-lg font-semibold mb-2">Event Information</h3>
                <div class="bg-gray-50 p-4 rounded-lg">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <p class="text-sm text-gray-500">Event Name</p>
                            <p class="font-medium">{attendance.eventName}</p>
                        </div>
                        <div>
                            <p class="text-sm text-gray-500">Event Type</p>
                            <p class="font-medium">{attendance.attendance.eventType}</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Attendance Details -->
            <div class="mb-6">
                <h3 class="text-lg font-semibold mb-2">Attendance Details</h3>
                <div class="bg-gray-50 p-4 rounded-lg">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <p class="text-sm text-gray-500">Check-in Time</p>
                            <p class="font-medium">{formatDate(attendance.attendance.checkInTime)}</p>
                            <p class="text-xs text-gray-500">By {attendance.checkedInByName}</p>
                        </div>
                        <div>
                            <p class="text-sm text-gray-500">Check-out Time</p>
                            {#if attendance.attendance.checkOutTime}
                                <p class="font-medium">{formatDate(attendance.attendance.checkOutTime)}</p>
                                <p class="text-xs text-gray-500">By {attendance.checkedOutByName}</p>
                            {:else}
                                <p class="font-medium text-gray-500">—</p>
                            {/if}
                        </div>
                        <div>
                            <p class="text-sm text-gray-500">Status</p>
                            <div class="mt-1">
                                {#if isEditing}
                                    <div class="flex flex-wrap gap-2">
                                        <button 
                                            class="p-1 rounded-md {updatedAttendance.attendance.status === AttendanceStatus.CHECKED_IN ? 'ring-2 ring-indigo-500' : ''}"
                                            onclick={() => updatedAttendance.attendance.status = AttendanceStatus.CHECKED_IN}
                                        >
                                            <StatusBadge status={AttendanceStatus.CHECKED_IN} />
                                        </button>
                                        <button 
                                            class="p-1 rounded-md {updatedAttendance.attendance.status === AttendanceStatus.CHECKED_OUT ? 'ring-2 ring-indigo-500' : ''}"
                                            onclick={() => updatedAttendance.attendance.status = AttendanceStatus.CHECKED_OUT}
                                        >
                                            <StatusBadge status={AttendanceStatus.CHECKED_OUT} />
                                        </button>
                                        <button 
                                            class="p-1 rounded-md {updatedAttendance.attendance.status === AttendanceStatus.EMERGENCY ? 'ring-2 ring-indigo-500' : ''}"
                                            onclick={() => updatedAttendance.attendance.status = AttendanceStatus.EMERGENCY}
                                        >
                                            <StatusBadge status={AttendanceStatus.EMERGENCY} />
                                        </button>
                                        <button 
                                            class="p-1 rounded-md {updatedAttendance.attendance.status === AttendanceStatus.NO_SHOW ? 'ring-2 ring-indigo-500' : ''}"
                                            onclick={() => updatedAttendance.attendance.status = AttendanceStatus.NO_SHOW}
                                        >
                                            <StatusBadge status={AttendanceStatus.NO_SHOW} />
                                        </button>
                                    </div>
                                {:else}
                                    <StatusBadge status={attendance.attendance.status} />
                                {/if}
                            </div>
                        </div>
                        <div>
                            <p class="text-sm text-gray-500">Created At</p>
                            <p class="font-medium">{formatDate(attendance.attendance.createdAt)}</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Notes -->
            <div class="mb-6">
                <h3 class="text-lg font-semibold mb-2">Notes</h3>
                <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                        <span class="text-sm text-gray-500">Add important information about this attendance record</span>
                        {#if !isEditing && attendance.attendance.notes}
                            <span class="bg-blue-100 text-blue-800 text-xs font-medium px-2.5 py-0.5 rounded">
                                Has notes
                            </span>
                        {/if}
                    </div>
                    {#if !isEditing && !attendance.attendance.notes}
                        <button 
                            class="text-sm text-indigo-600 hover:text-indigo-800 flex items-center gap-1"
                            onclick={toggleEditMode}
                        >
                            <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                            </svg>
                            Add Notes
                        </button>
                    {/if}
                </div>
                <NotesEditor
                    attendanceId={attendance.attendance.id}
                    notes={attendance.attendance.notes}
                    readOnly={!isEditing}
                    autoSave={isEditing}
                    showButtons={!isEditing}
                    saveDelay={800}
                    onupdate={(e) => {
                        const updatedNotes = e.detail.notes;
                        updatedAttendance.attendance.notes = updatedNotes;
                        attendance.attendance.notes = updatedNotes;
                        
                        // If we're in edit mode, the NotesEditor will auto-save
                        // If not in edit mode, this event shouldn't fire
                        if (!isEditing) {
                            successMessage = 'Notes updated successfully';
                            setTimeout(() => {
                                successMessage = '';
                            }, 2000);
                        }
                        
                        // Notify parent component of the update
                        dispatch('update', { attendance });
                        onUpdate(attendance);
                    }}
                />
            </div>
            
            <!-- Action Buttons -->
            <div class="flex flex-wrap justify-end gap-2">
                {#if isEditing}
                    <Button variant="secondary" onclick={toggleEditMode}>
                        Cancel
                    </Button>
                    <Button variant="primary" onclick={saveChanges} disabled={loading}>
                        {#if loading}
                            <span class="inline-block animate-spin mr-2">⟳</span>
                        {/if}
                        Save Changes
                    </Button>
                {:else}
                    <Button variant="secondary" onclick={onClose}>
                        Close
                    </Button>
                    {#if attendance.attendance.status === AttendanceStatus.CHECKED_IN}
                        <Button variant="secondary" onclick={confirmCheckOut} disabled={loading}>
                            Check Out
                        </Button>
                    {/if}
                    <Button variant="primary" onclick={toggleEditMode}>
                        Edit
                    </Button>
                {/if}
            </div>
        </div>
        
        <!-- Confirmation Dialog -->
        {#if showConfirmation}
            <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                <div class="bg-white rounded-lg shadow-lg p-6 max-w-sm mx-auto">
                    <h3 class="text-lg font-semibold mb-4">Confirm Action</h3>
                    <p class="mb-6">{confirmationMessage}</p>
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
    {/if}
</Modal>