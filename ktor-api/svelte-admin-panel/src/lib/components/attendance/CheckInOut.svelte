<script>
    import { createEventDispatcher } from 'svelte';
    import { attendanceService } from '../../services/attendanceService';
    import { EventType } from '../../types/attendance';
    import Button from '../Button.svelte';
    import Input from '../Input.svelte';
    import Textarea from '../Textarea.svelte';
    import Card from '../Card.svelte';
    
    // Props
    let {
        eventType = EventType.EVENT,
        eventId = null,
        onSuccess = () => {},
        class: className = '',
        ...props
    } = $props();
    
    // State
    let selectedAttendeeType = $state('USER'); // 'USER' or 'KID'
    let searchQuery = $state('');
    let searchResults = $state([]);
    let selectedAttendee = $state(null);
    let notes = $state('');
    let loading = $state(false);
    let error = $state(null);
    let successMessage = $state('');
    let searching = $state(false);
    let recentAttendees = $state([]);
    let frequentAttendees = $state([]);
    let loadingRecent = $state(false);
    let loadingFrequent = $state(false);
    let showQuickSelect = $state(false);
    let selectedTab = $state('recent'); // 'recent' or 'frequent'
    let searchHistory = $state([]); // Store recent searches
    let showSearchHistory = $state(false); // Toggle for search history dropdown
    let retryCount = $state(0); // Counter for API retry attempts
    let validationErrors = $state({}); // Object to store validation errors by field
    let isAlreadyCheckedIn = $state(false); // Flag to track if attendee is already checked in
    
    const dispatch = createEventDispatcher();
    
    // Search for attendees based on query
    async function searchAttendees() {
        if (!searchQuery.trim()) {
            searchResults = [];
            return;
        }
        
        searching = true;
        error = null;
        
        try {
            // Use enhanced API methods with attendance history
            const searchOptions = {
                includeAttendanceHistory: true,
                eventType: eventType
            };
            
            // Use the actual API methods for searching users and kids
            if (selectedAttendeeType === 'USER') {
                searchResults = await attendanceService.searchUsers(searchQuery, 10, searchOptions);
            } else {
                searchResults = await attendanceService.searchKids(searchQuery, 10, searchOptions);
            }
            
            // Sort results to prioritize those with attendance history
            searchResults.sort((a, b) => {
                // First prioritize by attendance count if available
                if (a.attendanceCount && b.attendanceCount) {
                    return b.attendanceCount - a.attendanceCount;
                }
                // Then by last attendance date if available
                if (a.lastAttendance && b.lastAttendance) {
                    return new Date(b.lastAttendance) - new Date(a.lastAttendance);
                }
                // Default to alphabetical order
                return a.name.localeCompare(b.name);
            });
        } catch (err) {
            console.error('Error searching attendees:', err);
            error = err.message || 'Failed to search attendees. Please try again.';
            searchResults = [];
        } finally {
            searching = false;
        }
    }
    
    // Load recent attendees
    async function loadRecentAttendees() {
        if (!eventType) return;
        
        loadingRecent = true;
        
        try {
            recentAttendees = await attendanceService.getRecentAttendees(eventType);
        } catch (err) {
            console.error('Error loading recent attendees:', err);
            // Don't show error to user, just log it
        } finally {
            loadingRecent = false;
        }
    }
    
    // Load frequent attendees
    async function loadFrequentAttendees() {
        if (!eventType) return;
        
        loadingFrequent = true;
        
        try {
            frequentAttendees = await attendanceService.getFrequentAttendees(eventType);
        } catch (err) {
            console.error('Error loading frequent attendees:', err);
            // Don't show error to user, just log it
        } finally {
            loadingFrequent = false;
        }
    }
    
    // Load quick select options and search history when component mounts
    $effect(() => {
        if (eventType && eventId) {
            loadRecentAttendees();
            loadFrequentAttendees();
            
            // Load search history from localStorage
            if (typeof localStorage !== 'undefined') {
                try {
                    const savedHistory = localStorage.getItem('attendeeSearchHistory');
                    if (savedHistory) {
                        searchHistory = JSON.parse(savedHistory);
                    }
                } catch (e) {
                    console.error('Failed to load search history from localStorage', e);
                }
            }
        }
    });
    
    // Handle search input changes
    function handleSearchInput() {
        // Clear selected attendee when search query changes
        if (selectedAttendee) {
            selectedAttendee = null;
        }
        
        // Debounce search
        clearTimeout(window.searchTimeout);
        window.searchTimeout = setTimeout(() => {
            searchAttendees();
        }, 300);
    }
    
    // Handle keyboard navigation in search results
    let activeSearchIndex = $state(-1);
    
    function handleSearchKeydown(e) {
        if (searchResults.length === 0) return;
        
        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                activeSearchIndex = Math.min(activeSearchIndex + 1, searchResults.length - 1);
                break;
            case 'ArrowUp':
                e.preventDefault();
                activeSearchIndex = Math.max(activeSearchIndex - 1, -1);
                break;
            case 'Enter':
                e.preventDefault();
                if (activeSearchIndex >= 0 && activeSearchIndex < searchResults.length) {
                    selectAttendee(searchResults[activeSearchIndex]);
                }
                break;
            case 'Escape':
                e.preventDefault();
                searchResults = [];
                activeSearchIndex = -1;
                break;
            default:
                break;
        }
    }
    
    // Select an attendee from search results
    function selectAttendee(attendee) {
        selectedAttendee = attendee;
        searchResults = [];
        searchQuery = attendee.name;
        
        // Add to search history if not already present
        if (searchQuery.trim() && !searchHistory.some(item => item.id === attendee.id && item.type === selectedAttendeeType)) {
            // Keep only the last 5 searches
            if (searchHistory.length >= 5) {
                searchHistory = searchHistory.slice(0, 4);
            }
            
            searchHistory = [
                {
                    id: attendee.id,
                    name: attendee.name,
                    type: selectedAttendeeType,
                    email: selectedAttendeeType === 'USER' ? attendee.email : null,
                    parentName: selectedAttendeeType === 'KID' ? attendee.parentName : null,
                    timestamp: new Date().toISOString()
                },
                ...searchHistory
            ];
            
            // Store in localStorage for persistence
            if (typeof localStorage !== 'undefined') {
                try {
                    localStorage.setItem('attendeeSearchHistory', JSON.stringify(searchHistory));
                } catch (e) {
                    console.error('Failed to save search history to localStorage', e);
                }
            }
        }
    }
    
    // Clear selected attendee
    function clearSelection() {
        selectedAttendee = null;
        searchQuery = '';
        searchResults = [];
    }
    
    // Validate check-in form with enhanced validation
    function validateCheckIn() {
        const validationResult = { isValid: true, errors: {} };
        
        // Validate attendee selection
        if (!selectedAttendee) {
            validationResult.isValid = false;
            validationResult.errors.attendee = 'required';
            error = 'Please select an attendee first';
            return validationResult.isValid;
        }
        
        // Validate event selection
        if (!eventId) {
            validationResult.isValid = false;
            validationResult.errors.event = 'required';
            error = 'No event selected';
            return validationResult.isValid;
        }
        
        // Check if attendee is already checked in
        if (isAlreadyCheckedIn) {
            validationResult.isValid = false;
            validationResult.errors.attendee = 'already-checked-in';
            error = `${selectedAttendee.name} is already checked in to this ${eventType.toLowerCase()}`;
            return validationResult.isValid;
        }
        
        // Validate notes length if provided (optional validation)
        if (notes && notes.length > 500) {
            validationResult.isValid = false;
            validationResult.errors.notes = 'too-long';
            error = 'Notes cannot exceed 500 characters';
            return validationResult.isValid;
        }
        
        validationErrors = validationResult.errors;
        return validationResult.isValid;
    }
    
    // Check in attendee with enhanced error handling and validation
    async function checkIn() {
        // Reset error and success states
        error = null;
        successMessage = '';
        
        // Validate form before proceeding
        if (!validateCheckIn()) {
            return;
        }
        
        loading = true;
        
        // Track retry attempts
        let attempts = 0;
        const maxAttempts = 3; // Increased from 2 to 3 for better reliability
        let lastError = null;
        
        while (attempts < maxAttempts) {
            try {
                // Double-check if the attendee is already checked in
                // This prevents race conditions where another admin might have checked in the same person
                let isAlreadyCheckedIn = false;
                
                try {
                    // Verify attendance status with a timeout to prevent long waits
                    const statusCheckPromise = new Promise(async (resolve, reject) => {
                        try {
                            if (selectedAttendeeType === 'USER') {
                                switch (eventType) {
                                    case EventType.EVENT:
                                        isAlreadyCheckedIn = await attendanceService.isUserCheckedInToEvent(eventId, selectedAttendee.id);
                                        break;
                                    case EventType.SERVICE:
                                        isAlreadyCheckedIn = await attendanceService.isUserCheckedInToService(eventId, selectedAttendee.id);
                                        break;
                                    default:
                                        break;
                                }
                            } else if (selectedAttendeeType === 'KID' && eventType === EventType.KIDS_SERVICE) {
                                isAlreadyCheckedIn = await attendanceService.isKidCheckedInToKidsService(eventId, selectedAttendee.id);
                            }
                            resolve(isAlreadyCheckedIn);
                        } catch (err) {
                            reject(err);
                        }
                    });
                    
                    // Add timeout to status check
                    const timeoutPromise = new Promise((_, reject) => {
                        setTimeout(() => reject(new Error('Status check timed out')), 5000);
                    });
                    
                    // Race the status check against the timeout
                    isAlreadyCheckedIn = await Promise.race([statusCheckPromise, timeoutPromise]);
                    
                    if (isAlreadyCheckedIn) {
                        throw new Error(`${selectedAttendee.name} is already checked in to this ${eventType.toLowerCase()}`);
                    }
                } catch (statusCheckErr) {
                    // If the error is specifically about already being checked in, throw it
                    if (statusCheckErr.message && statusCheckErr.message.includes('already checked in')) {
                        throw statusCheckErr;
                    }
                    
                    // If it's a timeout, log and continue with check-in attempt
                    if (statusCheckErr.message && statusCheckErr.message.includes('timed out')) {
                        console.warn('Status check timed out, proceeding with check-in attempt');
                    } else {
                        // Otherwise, log but continue with check-in attempt
                        console.warn('Error checking attendance status:', statusCheckErr);
                    }
                }
                
                // Prepare check-in request with validation
                const checkInRequest = {
                    [selectedAttendeeType === 'USER' ? 'userId' : 'kidId']: selectedAttendee.id,
                    checkedInBy: 1, // TODO: Get current user ID
                    notes: notes ? notes.trim() : ''
                };
                
                // Validate request object
                if (!checkInRequest.userId && !checkInRequest.kidId) {
                    throw new Error('Invalid attendee selection');
                }
                
                // Attempt the check-in with a timeout
                const checkInPromise = attendanceService.checkInByEventType(eventType, eventId, checkInRequest);
                const checkInTimeoutPromise = new Promise((_, reject) => {
                    setTimeout(() => reject(new Error('Check-in request timed out')), 8000);
                });
                
                await Promise.race([checkInPromise, checkInTimeoutPromise]);
                
                // If we get here, the check-in was successful
                successMessage = `${selectedAttendee.name} checked in successfully`;
                dispatch('checkin', { 
                    attendee: selectedAttendee, 
                    attendeeType: selectedAttendeeType,
                    eventType,
                    eventId,
                    notes
                });
                onSuccess();
                
                // Reset form
                clearSelection();
                notes = '';
                validationErrors = {};
                
                // Clear success message after a delay
                setTimeout(() => {
                    successMessage = '';
                }, 3000);
                
                // Break out of the retry loop
                break;
            } catch (err) {
                attempts++;
                lastError = err;
                console.error(`Error checking in attendee (attempt ${attempts}/${maxAttempts}):`, err);
                
                // Handle specific error cases
                if (err.message && err.message.includes('already checked in')) {
                    error = err.message;
                    validationErrors.attendee = 'already-checked-in';
                    // No need to retry for this error
                    break;
                } else if (
                    err.message && (
                        err.message.includes('network') || 
                        err.message.includes('timeout') || 
                        err.message.includes('timed out') ||
                        err.message.includes('connection')
                    )
                ) {
                    // For network or timeout errors, retry if we haven't reached max attempts
                    if (attempts < maxAttempts) {
                        // Wait before retrying with exponential backoff
                        const backoffTime = Math.min(1000 * Math.pow(2, attempts - 1), 5000);
                        await new Promise(resolve => setTimeout(resolve, backoffTime));
                        continue;
                    }
                    
                    error = err.message.includes('network') || err.message.includes('connection')
                        ? 'Network error. Please check your connection and try again.' 
                        : 'The request timed out. Please try again.';
                } else if (err.message && err.message.includes('permission')) {
                    error = 'You do not have permission to check in this attendee.';
                    break;
                } else if (err.message && err.message.includes('not found')) {
                    error = `The ${eventType.toLowerCase()} or attendee could not be found.`;
                    break;
                } else if (err.message && err.message.includes('Invalid')) {
                    error = err.message;
                    break;
                } else {
                    error = err.message || 'Failed to check in attendee. Please try again.';
                    break;
                }
            }
        }
        
        // If all attempts failed with network/timeout errors, show a more helpful message
        if (attempts === maxAttempts && lastError && 
            (lastError.message.includes('network') || 
             lastError.message.includes('timeout') || 
             lastError.message.includes('timed out'))) {
            error = 'Unable to complete check-in after multiple attempts. The server might be experiencing issues. Please try again later.';
        }
        
        loading = false;
    }
    
    // Check if the attendee is already checked in with enhanced error handling
    async function checkIfAlreadyCheckedIn() {
        if (!selectedAttendee || !eventId) return;
        
        try {
            isAlreadyCheckedIn = false;
            error = null;
            validationErrors = {};
            
            // Try up to 3 times in case of network issues
            let attempts = 0;
            const maxAttempts = 3;
            
            while (attempts < maxAttempts) {
                try {
                    // Add timeout to prevent hanging on slow connections
                    const statusCheckPromise = new Promise(async (resolve, reject) => {
                        try {
                            let result = false;
                            
                            if (selectedAttendeeType === 'USER') {
                                switch (eventType) {
                                    case EventType.EVENT:
                                        result = await attendanceService.isUserCheckedInToEvent(eventId, selectedAttendee.id);
                                        break;
                                    case EventType.SERVICE:
                                        result = await attendanceService.isUserCheckedInToService(eventId, selectedAttendee.id);
                                        break;
                                    default:
                                        break;
                                }
                            } else if (selectedAttendeeType === 'KID' && eventType === EventType.KIDS_SERVICE) {
                                result = await attendanceService.isKidCheckedInToKidsService(eventId, selectedAttendee.id);
                            }
                            
                            resolve(result);
                        } catch (err) {
                            reject(err);
                        }
                    });
                    
                    // Add timeout to status check
                    const timeoutPromise = new Promise((_, reject) => {
                        setTimeout(() => reject(new Error('Status check timed out')), 4000);
                    });
                    
                    // Race the status check against the timeout
                    isAlreadyCheckedIn = await Promise.race([statusCheckPromise, timeoutPromise]);
                    
                    // If we get here, the request was successful
                    break;
                } catch (retryErr) {
                    attempts++;
                    
                    // If this was our last attempt, throw the error
                    if (attempts >= maxAttempts) {
                        throw retryErr;
                    }
                    
                    // Otherwise wait a bit and try again with exponential backoff
                    const backoffTime = Math.min(1000 * Math.pow(2, attempts - 1), 4000);
                    await new Promise(resolve => setTimeout(resolve, backoffTime));
                }
            }
            
            if (isAlreadyCheckedIn) {
                error = `${selectedAttendee.name} is already checked in to this ${eventType.toLowerCase()}`;
                validationErrors.attendee = 'already-checked-in';
                
                // Show the error but don't clear the selection immediately
                // This gives the user a chance to see who was selected before clearing
                setTimeout(() => {
                    selectedAttendee = null;
                    searchQuery = '';
                    validationErrors = {};
                }, 3000);
            } else {
                error = null;
                validationErrors = {};
            }
        } catch (err) {
            console.error('Error checking attendance status:', err);
            
            // Show a more user-friendly error message based on the error type
            if (err.message && err.message.includes('network')) {
                error = 'Network error while verifying attendance status. Please check your connection.';
            } else if (err.message && err.message.includes('timeout') || err.message && err.message.includes('timed out')) {
                error = 'The request timed out while verifying attendance status. Please try again.';
            } else if (err.message && err.message.includes('not found')) {
                error = `The ${eventType.toLowerCase()} or attendee could not be found.`;
            } else if (err.message && err.message.includes('permission')) {
                error = 'You do not have permission to check attendance status.';
            } else {
                error = 'Unable to verify current attendance status. Please try again.';
            }
            
            // Add a retry button that will be shown in the error message
            // The button is rendered in the template
        }
    }
    
    // Effect to check if attendee is already checked in when selected
    $effect(() => {
        if (selectedAttendee) {
            checkIfAlreadyCheckedIn();
        }
    });
</script>

<div class="check-in-out-component {className}" {...props}>
    <Card>
        <div slot="header" class="flex justify-between items-center">
            <h2 class="text-lg font-semibold">Check-In</h2>
        </div>
        
        {#if error}
            <div class="p-4 mb-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
                <div class="flex justify-between items-start">
                    <div>
                        <p class="font-medium">{error}</p>
                        {#if validationErrors.attendee === 'already-checked-in'}
                            <p class="text-sm mt-1">This attendee is already checked in. Please select a different attendee or check their current status.</p>
                        {/if}
                        {#if validationErrors.notes === 'too-long'}
                            <p class="text-sm mt-1">Please shorten your notes to 500 characters or less.</p>
                        {/if}
                    </div>
                    {#if error && (error.includes('network') || error.includes('timeout') || error.includes('timed out') || error.includes('verify'))}
                        <button 
                            class="ml-4 px-2 py-1 text-xs bg-red-100 hover:bg-red-200 text-red-800 rounded-md"
                            onclick={() => {
                                if (selectedAttendee) {
                                    checkIfAlreadyCheckedIn();
                                } else {
                                    error = null;
                                }
                            }}
                        >
                            Retry
                        </button>
                    {/if}
                </div>
            </div>
        {/if}
        
        {#if successMessage}
            <div class="p-4 mb-4 bg-green-50 border border-green-200 rounded-lg text-green-700">
                <p>{successMessage}</p>
            </div>
        {/if}
        
        <!-- Attendee Type Selection -->
        <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">Attendee Type</label>
            <div class="flex space-x-4">
                <label class="inline-flex items-center">
                    <input 
                        type="radio" 
                        class="form-radio h-4 w-4 text-indigo-600" 
                        name="attendeeType" 
                        value="USER" 
                        checked={selectedAttendeeType === 'USER'}
                        onclick={() => {
                            selectedAttendeeType = 'USER';
                            clearSelection();
                        }}
                    />
                    <span class="ml-2">User</span>
                </label>
                
                <label class="inline-flex items-center">
                    <input 
                        type="radio" 
                        class="form-radio h-4 w-4 text-indigo-600" 
                        name="attendeeType" 
                        value="KID" 
                        checked={selectedAttendeeType === 'KID'}
                        onclick={() => {
                            selectedAttendeeType = 'KID';
                            clearSelection();
                        }}
                        disabled={eventType !== EventType.KIDS_SERVICE}
                    />
                    <span class="ml-2">Kid</span>
                    {#if eventType !== EventType.KIDS_SERVICE}
                        <span class="ml-1 text-xs text-gray-500">(Kids only available for Kids Services)</span>
                    {/if}
                </label>
            </div>
        </div>
        
        <!-- Quick Select Toggle -->
        <div class="mb-4">
            <button 
                type="button"
                class="text-sm text-indigo-600 hover:text-indigo-800 flex items-center"
                onclick={() => showQuickSelect = !showQuickSelect}
            >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" viewBox="0 0 20 20" fill="currentColor">
                    {#if showQuickSelect}
                        <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
                    {:else}
                        <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                    {/if}
                </svg>
                {showQuickSelect ? 'Hide quick select options' : 'Show quick select options'}
            </button>
        </div>

        <!-- Quick Select Options -->
        {#if showQuickSelect}
            <div class="mb-4 bg-gray-50 p-3 rounded-lg border border-gray-200">
                <!-- Tabs for Recent/Frequent -->
                <div class="flex border-b border-gray-200 mb-3">
                    <button 
                        class="py-2 px-4 text-sm font-medium {selectedTab === 'recent' ? 'text-indigo-600 border-b-2 border-indigo-600' : 'text-gray-500 hover:text-gray-700'}"
                        onclick={() => selectedTab = 'recent'}
                    >
                        Recent Attendees
                    </button>
                    <button 
                        class="py-2 px-4 text-sm font-medium {selectedTab === 'frequent' ? 'text-indigo-600 border-b-2 border-indigo-600' : 'text-gray-500 hover:text-gray-700'}"
                        onclick={() => selectedTab = 'frequent'}
                    >
                        Frequent Attendees
                    </button>
                </div>
                
                <!-- Recent Attendees Tab -->
                {#if selectedTab === 'recent'}
                    {#if loadingRecent}
                        <div class="flex justify-center py-4">
                            <div class="animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-indigo-500"></div>
                        </div>
                    {:else if recentAttendees.length === 0}
                        <div class="py-4 text-center">
                            <p class="text-sm text-gray-500">No recent attendees found</p>
                            <button 
                                class="mt-2 text-xs text-indigo-600 hover:text-indigo-800"
                                onclick={loadRecentAttendees}
                            >
                                Refresh
                            </button>
                        </div>
                    {:else}
                        <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                            {#each recentAttendees as attendee}
                                <button
                                    type="button"
                                    class="flex items-center justify-between p-2 border border-gray-200 rounded-md bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-indigo-500 transition-colors"
                                    onclick={() => selectAttendee(attendee)}
                                >
                                    <div class="flex flex-col items-start">
                                        <span class="font-medium text-sm">{attendee.name}</span>
                                        <span class="text-xs text-gray-500">
                                            {new Date(attendee.lastAttendance).toLocaleDateString()}
                                        </span>
                                    </div>
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                                        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-8.707l-3-3a1 1 0 00-1.414 0l-3 3a1 1 0 001.414 1.414L9 9.414V13a1 1 0 102 0V9.414l1.293 1.293a1 1 0 001.414-1.414z" clip-rule="evenodd" />
                                    </svg>
                                </button>
                            {/each}
                        </div>
                    {/if}
                {/if}
                
                <!-- Frequent Attendees Tab -->
                {#if selectedTab === 'frequent'}
                    {#if loadingFrequent}
                        <div class="flex justify-center py-4">
                            <div class="animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-indigo-500"></div>
                        </div>
                    {:else if frequentAttendees.length === 0}
                        <div class="py-4 text-center">
                            <p class="text-sm text-gray-500">No frequent attendees found</p>
                            <button 
                                class="mt-2 text-xs text-indigo-600 hover:text-indigo-800"
                                onclick={loadFrequentAttendees}
                            >
                                Refresh
                            </button>
                        </div>
                    {:else}
                        <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                            {#each frequentAttendees as attendee}
                                <button
                                    type="button"
                                    class="flex items-center justify-between p-2 border border-gray-200 rounded-md bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-indigo-500 transition-colors"
                                    onclick={() => selectAttendee(attendee)}
                                >
                                    <div class="flex flex-col items-start">
                                        <span class="font-medium text-sm">{attendee.name}</span>
                                        <div class="flex items-center">
                                            <span class="text-xs bg-indigo-100 text-indigo-800 px-1.5 py-0.5 rounded-full">
                                                {attendee.attendanceCount} visits
                                            </span>
                                        </div>
                                    </div>
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                                        <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
                                    </svg>
                                </button>
                            {/each}
                        </div>
                    {/if}
                {/if}
            </div>
        {/if}

        <!-- Attendee Search -->
        <div class="mb-4 relative">
            <div class="flex items-end gap-2">
                <div class="flex-grow relative">
                    <Input
                        label={`Search for ${selectedAttendeeType === 'USER' ? 'User' : 'Kid'}`}
                        placeholder={`Enter name or ${selectedAttendeeType === 'USER' ? 'email' : 'parent name'}`}
                        value={searchQuery}
                        oninput={(e) => {
                            searchQuery = e.target.value;
                            handleSearchInput();
                        }}
                        onkeydown={handleSearchKeydown}
                        onfocus={() => {
                            if (searchHistory.length > 0 && !searchQuery) {
                                showSearchHistory = true;
                            }
                        }}
                    />
                    
                    {#if searchQuery}
                        <button 
                            class="absolute right-2 top-9 text-gray-400 hover:text-gray-600"
                            onclick={() => {
                                searchQuery = '';
                                searchResults = [];
                            }}
                            aria-label="Clear search"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                            </svg>
                        </button>
                    {/if}
                    
                    {#if searching}
                        <div class="absolute right-8 top-9">
                            <div class="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-indigo-500"></div>
                        </div>
                    {/if}
                </div>
                
                {#if searchHistory.length > 0}
                    <button 
                        class="mb-1 px-2 py-1 text-sm text-indigo-600 hover:text-indigo-800 border border-indigo-200 rounded-md hover:bg-indigo-50"
                        onclick={() => showSearchHistory = !showSearchHistory}
                        aria-label="Show search history"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clip-rule="evenodd" />
                        </svg>
                    </button>
                {/if}
            </div>
            
            <!-- Search History Dropdown -->
            {#if showSearchHistory && searchHistory.length > 0 && !searchResults.length}
                <div class="absolute z-10 mt-1 w-full bg-white shadow-lg rounded-md border border-gray-200 max-h-60 overflow-auto">
                    <div class="flex justify-between items-center px-3 py-2 bg-gray-50 border-b border-gray-200">
                        <h4 class="text-sm font-medium text-gray-700">Recent Searches</h4>
                        <button 
                            class="text-xs text-gray-500 hover:text-gray-700"
                            onclick={() => {
                                searchHistory = [];
                                showSearchHistory = false;
                                if (typeof localStorage !== 'undefined') {
                                    localStorage.removeItem('attendeeSearchHistory');
                                }
                            }}
                        >
                            Clear All
                        </button>
                    </div>
                    <ul class="py-1">
                        {#each searchHistory as item}
                            <li>
                                <button 
                                    class="w-full text-left px-4 py-2 hover:bg-gray-100 focus:bg-gray-100 focus:outline-none"
                                    onclick={() => {
                                        // Only select if type matches current selection
                                        if (item.type === selectedAttendeeType) {
                                            selectAttendee({
                                                id: item.id,
                                                name: item.name,
                                                email: item.email,
                                                parentName: item.parentName
                                            });
                                        } else {
                                            // Just set the search query if types don't match
                                            searchQuery = item.name;
                                            showSearchHistory = false;
                                            searchAttendees();
                                        }
                                    }}
                                >
                                    <div class="flex justify-between items-center">
                                        <div class="font-medium">{item.name}</div>
                                        <span class="text-xs px-2 py-0.5 rounded-full {item.type === 'USER' ? 'bg-blue-100 text-blue-800' : 'bg-purple-100 text-purple-800'}">
                                            {item.type}
                                        </span>
                                    </div>
                                    <div class="text-sm text-gray-500">
                                        {item.type === 'USER' ? item.email : `Parent: ${item.parentName}`}
                                    </div>
                                </button>
                            </li>
                        {/each}
                    </ul>
                </div>
            {/if}
            
            {#if searchResults.length > 0}
                <div class="absolute z-10 mt-1 w-full bg-white shadow-lg rounded-md border border-gray-200 max-h-60 overflow-auto">
                    <ul class="py-1">
                        {#each searchResults as result, index}
                            <li>
                                <button 
                                    class="w-full text-left px-4 py-2 {index === activeSearchIndex ? 'bg-indigo-50' : 'hover:bg-gray-100'} focus:bg-gray-100 focus:outline-none"
                                    onclick={() => selectAttendee(result)}
                                    onmouseover={() => activeSearchIndex = index}
                                    id={`search-result-${index}`}
                                >
                                    <div class="flex justify-between items-center">
                                        <div class="font-medium">{result.name}</div>
                                        {#if result.attendanceCount}
                                            <span class="text-xs bg-indigo-100 text-indigo-800 px-2 py-0.5 rounded-full">
                                                {result.attendanceCount} visits
                                            </span>
                                        {/if}
                                    </div>
                                    <div class="text-sm text-gray-500 flex justify-between">
                                        <span>{selectedAttendeeType === 'USER' ? result.email : `Parent: ${result.parentName}`}</span>
                                        {#if result.lastAttendance}
                                            <span class="text-xs text-gray-500">
                                                Last visit: {new Date(result.lastAttendance).toLocaleDateString()}
                                            </span>
                                        {/if}
                                    </div>
                                </button>
                            </li>
                        {/each}
                    </ul>
                </div>
            {/if}
        </div>
        
        <!-- Selected Attendee -->
        {#if selectedAttendee}
            <div class="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <div class="flex justify-between items-center">
                    <div>
                        <h3 class="font-medium">{selectedAttendee.name}</h3>
                        <p class="text-sm text-gray-600">
                            {selectedAttendeeType === 'USER' ? selectedAttendee.email : `Parent: ${selectedAttendee.parentName}`}
                        </p>
                    </div>
                    <button 
                        class="text-gray-500 hover:text-gray-700"
                        onclick={clearSelection}
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
                        </svg>
                    </button>
                </div>
            </div>
        {/if}
        
        <!-- Notes with validation -->
        <div class="mb-4">
            <Textarea
                label="Notes"
                placeholder="Add any important information about this check-in"
                value={notes}
                oninput={(e) => {
                    notes = e.target.value;
                    // Clear validation error when user starts typing
                    if (validationErrors.notes) {
                        validationErrors = { ...validationErrors, notes: null };
                        if (Object.values(validationErrors).every(v => !v)) {
                            error = null;
                        }
                    }
                }}
                error={validationErrors.notes === 'too-long' ? 'Notes cannot exceed 500 characters' : null}
                rows={3}
                maxLength={500}
                showCharCount={true}
            />
            {#if notes && notes.length > 450}
                <div class="mt-1 text-xs {notes.length > 500 ? 'text-red-600' : 'text-amber-600'}">
                    {notes.length}/500 characters {notes.length > 500 ? '(limit exceeded)' : '(approaching limit)'}
                </div>
            {/if}
        </div>
        </div>
        
        <!-- Action Buttons with enhanced validation -->
        <div class="flex justify-end space-x-3">
            {#if error && (error.includes('network') || error.includes('timeout') || error.includes('timed out'))}
                <Button 
                    variant="secondary" 
                    onclick={() => {
                        error = null;
                        validationErrors = {};
                    }}
                    disabled={loading}
                >
                    Cancel
                </Button>
            {/if}
            <Button 
                variant="primary" 
                onclick={checkIn}
                disabled={loading || !selectedAttendee || isAlreadyCheckedIn || (notes && notes.length > 500)}
                loading={loading}
            >
                {#if loading}
                    <span class="inline-block animate-spin mr-2">⟳</span>
                    Checking In...
                {:else if isAlreadyCheckedIn}
                    Already Checked In
                {:else if !selectedAttendee}
                    Select Attendee
                {:else}
                    Check In
                {/if}
            </Button>
        </div>
    </Card>
</div>