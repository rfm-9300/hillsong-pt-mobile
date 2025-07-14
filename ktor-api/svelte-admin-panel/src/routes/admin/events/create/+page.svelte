<script>
    import { goto } from '$app/navigation';
    import { fade, fly } from 'svelte/transition';

    let title = '';
    let description = '';
    let date = '';
    let location = '';
    let maxAttendees = 0;
    let needsApproval = false;
    let image = null;
    let imagePreview = null;
    let loading = false;
    let message = '';
    let isError = false;
    let dragActive = false;

    async function createEvent() {
        loading = true;
        message = '';

        try {
            const token = localStorage.getItem('authToken');
            const formData = new FormData();
            formData.append('title', title);
            formData.append('description', description);
            formData.append('date', new Date(date).toISOString());
            formData.append('location', location);
            formData.append('maxAttendees', maxAttendees);
            formData.append('needsApproval', needsApproval);
            
            if (image) {
                formData.append('image', image);
            }

            const response = await fetch('/api/events', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            const result = await response.json();

            if (response.ok) {
                isError = false;
                message = result.message || 'Event created successfully!';
                setTimeout(() => {
                    goto('/admin/events');
                }, 2000);
            } else {
                isError = true;
                message = result.message || 'Failed to create event';
            }
        } catch (error) {
            isError = true;
            message = `Error: ${error.message}`;
            console.error(error);
        } finally {
            loading = false;
        }
    }

    function onFileSelected(e) {
        const file = e.target.files[0];
        if (file) {
            image = file;
            createImagePreview(file);
        }
    }
    
    function createImagePreview(file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            imagePreview = e.target.result;
        };
        reader.readAsDataURL(file);
    }
    
    function handleDragEnter(e) {
        e.preventDefault();
        dragActive = true;
    }
    
    function handleDragLeave(e) {
        e.preventDefault();
        dragActive = false;
    }
    
    function handleDrop(e) {
        e.preventDefault();
        dragActive = false;
        
        if (e.dataTransfer.files.length) {
            const file = e.dataTransfer.files[0];
            if (file.type.startsWith('image/')) {
                image = file;
                createImagePreview(file);
            }
        }
    }
    
    function goBack() {
        goto('/admin/events');
    }
    
    function removeImage() {
        image = null;
        imagePreview = null;
        const fileInput = document.getElementById('image');
        if (fileInput) fileInput.value = '';
    }
</script>

<div class="p-8 w-full max-w-3xl mx-auto bg-gray-50 rounded-xl shadow-lg">
    <div class="mb-8">
        <button 
            on:click={goBack}
            class="flex items-center text-gray-600 hover:text-purple-600 transition-colors cursor-pointer"
        >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back to Events
        </button>
    </div>

    <div class="flex items-center justify-between mb-6">
        <h1 class="text-3xl font-bold text-gray-800">Create New Event</h1>
    </div>

    {#if message}
        <div 
            in:fly={{ y: -20, duration: 300 }}
            class="p-4 rounded-lg mb-6 flex items-center" 
            class:bg-green-50={!isError} 
            class:text-green-700={!isError}
            class:bg-red-50={isError} 
            class:text-red-700={isError}
        >
            {#if !isError}
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
            {:else}
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            {/if}
            <p>{message}</p>
        </div>
    {/if}

    <div in:fly={{ y: 20, duration: 300 }} class="bg-white rounded-xl shadow-lg p-6 space-y-6">
        <form on:submit|preventDefault={createEvent} class="space-y-6">
            <div>
                <label for="title" class="block text-sm font-medium text-gray-700 mb-1">Title</label>
                <input 
                    type="text" 
                    id="title" 
                    bind:value={title} 
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-purple-500 focus:border-purple-500" 
                    placeholder="Enter event title"
                    required
                    disabled={loading}
                >
            </div>
            
            <div>
                <label for="date" class="block text-sm font-medium text-gray-700 mb-1">Date & Time</label>
                <input 
                    type="datetime-local" 
                    id="date" 
                    bind:value={date} 
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-purple-500 focus:border-purple-500" 
                    required
                    disabled={loading}
                >
            </div>
            
            <div>
                <label for="location" class="block text-sm font-medium text-gray-700 mb-1">Location</label>
                <input 
                    type="text" 
                    id="location" 
                    bind:value={location} 
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-purple-500 focus:border-purple-500" 
                    placeholder="Enter event location"
                    required
                    disabled={loading}
                >
            </div>
            
            <div>
                <label for="maxAttendees" class="block text-sm font-medium text-gray-700 mb-1">Max Attendees</label>
                <input 
                    type="number" 
                    id="maxAttendees" 
                    bind:value={maxAttendees} 
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-purple-500 focus:border-purple-500" 
                    min="0"
                    placeholder="Enter maximum number of attendees"
                    required
                    disabled={loading}
                >
            </div>
            
            <div class="flex items-center">
                <input 
                    type="checkbox" 
                    id="needsApproval" 
                    bind:checked={needsApproval} 
                    class="w-5 h-5 text-purple-600 border-gray-300 rounded focus:ring-purple-500 cursor-pointer"
                    disabled={loading}
                >
                <label for="needsApproval" class="ml-2 text-sm font-medium text-gray-700 cursor-pointer">
                    Registration needs admin approval
                </label>
            </div>
            
            <!-- Image Upload with Preview -->
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Event Image</label>
                
                {#if imagePreview}
                    <div class="relative rounded-lg overflow-hidden mb-3 bg-gray-100">
                        <img src={imagePreview} alt="Image preview" class="w-full h-64 object-cover">
                        <button 
                            type="button" 
                            on:click={removeImage}
                            class="absolute top-2 right-2 bg-red-600 text-white rounded-full p-1 hover:bg-red-700 transition-colors cursor-pointer"
                            title="Remove image"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                {:else}
                    <!-- Drag and Drop Zone -->
                    <div 
                        class="border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors"
                        class:border-gray-300={!dragActive} 
                        class:border-purple-500={dragActive}
                        class:bg-gray-50={!dragActive}
                        class:bg-purple-50={dragActive}
                        on:dragenter={handleDragEnter}
                        on:dragover|preventDefault
                        on:dragleave={handleDragLeave}
                        on:drop={handleDrop}
                        on:click={() => document.getElementById('image').click()}
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12 mx-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <p class="mt-3 text-sm text-gray-600">Drag and drop image here, or click to select file</p>
                        <p class="mt-1 text-xs text-gray-500">PNG, JPG, GIF up to 5MB</p>
                    </div>
                {/if}
                
                <input 
                    type="file" 
                    id="image" 
                    on:change={onFileSelected} 
                    accept="image/*"
                    class="hidden"
                >
            </div>
            
            <div>
                <label for="description" class="block text-sm font-medium text-gray-700 mb-1">Description</label>
                <textarea 
                    id="description" 
                    bind:value={description} 
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-purple-500 focus:border-purple-500" 
                    rows="6"
                    placeholder="Write event description here..."
                    required
                    disabled={loading}
                ></textarea>
            </div>
            
            <div class="flex items-center pt-4 border-t border-gray-200">
                <button 
                    type="button" 
                    on:click={goBack} 
                    class="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg mr-4 hover:bg-gray-200 transition-colors cursor-pointer"
                    disabled={loading}
                >
                    Cancel
                </button>
                <button 
                    type="submit" 
                    class="flex items-center justify-center px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex-1 cursor-pointer"
                    disabled={loading}
                >
                    {#if loading}
                        <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Creating Event...
                    {:else}
                        Create Event
                    {/if}
                </button>
            </div>
        </form>
    </div>
</div>