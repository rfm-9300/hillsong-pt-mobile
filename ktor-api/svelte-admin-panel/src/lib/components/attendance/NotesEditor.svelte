<script>
    import { createEventDispatcher } from 'svelte';
    import { attendanceService } from '../../services/attendanceService';
    import Button from '../Button.svelte';
    import { fade } from 'svelte/transition';
    
    // Props
    let {
        attendanceId,
        notes = '',
        readOnly = false,
        showButtons = true,
        autoSave = false,
        saveDelay = 1000,
        class: className = '',
        ...props
    } = $props();
    
    // State
    let editedNotes = $state(notes);
    let isEditing = $state(false);
    let loading = $state(false);
    let error = $state(null);
    let successMessage = $state('');
    let characterCount = $derived(editedNotes ? editedNotes.length : 0);
    let saveTimeout = $state(null);
    let lastSavedNotes = $state(notes);
    let hasUnsavedChanges = $derived(editedNotes !== lastSavedNotes);
    const MAX_CHARS = 500; // Maximum character limit
    
    const dispatch = createEventDispatcher();
    
    // Start editing
    function startEditing() {
        isEditing = true;
    }
    
    // Cancel editing
    function cancelEditing() {
        editedNotes = notes;
        isEditing = false;
        error = null;
        successMessage = '';
        clearTimeout(saveTimeout);
    }
    
    // Auto-save notes after delay
    function debouncedSave() {
        if (autoSave && hasUnsavedChanges && !loading && characterCount <= MAX_CHARS) {
            clearTimeout(saveTimeout);
            saveTimeout = setTimeout(() => {
                saveNotes(true);
            }, saveDelay);
        }
    }
    
    // Save notes
    async function saveNotes(isAutoSave = false) {
        if (editedNotes === lastSavedNotes) {
            if (!isAutoSave) isEditing = false;
            return;
        }
        
        if (characterCount > MAX_CHARS) {
            error = `Notes exceed maximum length of ${MAX_CHARS} characters`;
            return;
        }
        
        loading = true;
        error = null;
        successMessage = '';
        
        try {
            const updateRequest = {
                attendanceId,
                notes: editedNotes
            };
            
            await attendanceService.updateAttendanceNotes(updateRequest);
            
            successMessage = 'Notes updated successfully';
            notes = editedNotes;
            lastSavedNotes = editedNotes;
            dispatch('update', { notes: editedNotes });
            
            // Exit edit mode after a short delay if not auto-save
            if (!isAutoSave) {
                setTimeout(() => {
                    if (!error) {
                        isEditing = false;
                        successMessage = '';
                    }
                }, 1500);
            } else {
                // Clear success message after a short delay for auto-save
                setTimeout(() => {
                    successMessage = '';
                }, 1500);
            }
        } catch (err) {
            console.error('Error updating notes:', err);
            error = err.message || 'Failed to update notes. Please try again.';
        } finally {
            loading = false;
        }
    }
    
    // Handle keyboard shortcuts
    function handleKeydown(event) {
        // Save on Ctrl+Enter or Cmd+Enter
        if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
            event.preventDefault();
            saveNotes();
        }
        // Cancel on Escape
        else if (event.key === 'Escape') {
            event.preventDefault();
            cancelEditing();
        }
    }
    
    // Handle input changes for auto-save
    function handleInput() {
        if (autoSave) {
            debouncedSave();
        }
    }
    
    // Watch for changes in props
    $effect(() => {
        editedNotes = notes;
        lastSavedNotes = notes;
        
        // If readOnly is false and autoSave is true, automatically enter edit mode
        if (!readOnly && autoSave && !isEditing) {
            isEditing = true;
        }
    });
</script>

<div class="notes-editor {className}" {...props}>
    {#if error}
        <div class="p-2 mb-2 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm" transition:fade={{ duration: 200 }}>
            <p>{error}</p>
        </div>
    {:else if successMessage}
        <div class="p-2 mb-2 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm" transition:fade={{ duration: 200 }}>
            <p>{successMessage}</p>
        </div>
    {/if}
    
    <div class="flex items-center justify-between mb-2">
        {#if !isEditing && !readOnly}
            <button 
                class="text-xs text-indigo-600 hover:text-indigo-800 flex items-center gap-1"
                onclick={startEditing}
                aria-label="Edit notes"
            >
                <svg class="h-3 w-3" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                </svg>
                Edit Notes
            </button>
        {/if}
        
        {#if notes && !isEditing}
            <span class="text-xs text-gray-500">Last updated: {new Date().toLocaleDateString()}</span>
        {/if}
    </div>
    
    {#if isEditing && !readOnly}
        <div transition:fade={{ duration: 150 }}>
            <div class="relative">
                <textarea
                    class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                    rows="4"
                    bind:value={editedNotes}
                    placeholder="Add notes here..."
                    disabled={loading}
                    onkeydown={handleKeydown}
                    oninput={handleInput}
                    aria-label="Attendance notes"
                ></textarea>
                <div class="absolute bottom-2 right-2 text-xs text-gray-500 {characterCount > MAX_CHARS ? 'text-red-500 font-semibold' : ''}">
                    {characterCount}/{MAX_CHARS}
                </div>
            </div>
            
            <div class="mt-1 text-xs text-gray-500 flex justify-between">
                <span>Tip: Press <kbd class="px-1 py-0.5 bg-gray-100 border border-gray-300 rounded">Ctrl+Enter</kbd> to save, <kbd class="px-1 py-0.5 bg-gray-100 border border-gray-300 rounded">Esc</kbd> to cancel</span>
                {#if characterCount > MAX_CHARS}
                    <span class="text-red-500">Note is too long ({characterCount - MAX_CHARS} characters over limit)</span>
                {/if}
            </div>
            
            {#if showButtons}
                <div class="flex justify-end gap-2 mt-2">
                    <Button variant="secondary" size="sm" onclick={cancelEditing} disabled={loading}>
                        Cancel
                    </Button>
                    <Button 
                        variant="primary" 
                        size="sm" 
                        onclick={() => saveNotes(false)} 
                        disabled={loading || characterCount > MAX_CHARS || !hasUnsavedChanges}
                    >
                        {#if loading}
                            <span class="inline-block animate-spin mr-2">⟳</span>
                        {/if}
                        Save Notes
                    </Button>
                </div>
            {/if}
        </div>
    {:else}
        <div class="bg-gray-50 p-4 rounded-lg min-h-[80px] relative border border-gray-200 hover:border-gray-300 transition-colors">
            {#if notes}
                <p class="whitespace-pre-wrap text-sm">{notes}</p>
            {:else}
                <p class="text-gray-500 italic text-sm">No notes available</p>
            {/if}
            
            {#if !readOnly && showButtons && !isEditing}
                <button 
                    class="absolute bottom-2 right-2 p-1 rounded-full hover:bg-gray-200 text-gray-500"
                    onclick={startEditing}
                    aria-label="Edit notes"
                >
                    <svg class="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                    </svg>
                </button>
            {/if}
        </div>
    {/if}
    
    {#if autoSave && hasUnsavedChanges && !loading && !error}
        <div class="mt-1 flex items-center justify-end">
            <span class="text-xs text-indigo-600">
                <svg class="inline-block animate-spin h-3 w-3 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                Auto-saving...
            </span>
        </div>
    {/if}
</div>