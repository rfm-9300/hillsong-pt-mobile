<div class="p-8">
    <h1 class="text-2xl font-bold">Create Event</h1>

    <form on:submit|preventDefault={createEvent} class="mt-8 space-y-6">
        <div>
            <label for="title" class="text-sm font-medium">Title</label>
            <input type="text" id="title" bind:value={title} class="w-full px-3 py-2 mt-1 border rounded-md" required>
        </div>
        <div>
            <label for="description" class="text-sm font-medium">Description</label>
            <textarea id="description" bind:value={description} class="w-full px-3 py-2 mt-1 border rounded-md" required></textarea>
        </div>
        <div>
            <label for="date" class="text-sm font-medium">Date</label>
            <input type="datetime-local" id="date" bind:value={date} class="w-full px-3 py-2 mt-1 border rounded-md" required>
        </div>
        <div>
            <label for="location" class="text-sm font-medium">Location</label>
            <input type="text" id="location" bind:value={location} class="w-full px-3 py-2 mt-1 border rounded-md" required>
        </div>
        <div>
            <label for="maxAttendees" class="text-sm font-medium">Max Attendees</label>
            <input type="number" id="maxAttendees" bind:value={maxAttendees} class="w-full px-3 py-2 mt-1 border rounded-md" required>
        </div>
        <div class="flex items-center">
            <input type="checkbox" id="needsApproval" bind:checked={needsApproval} class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500">
            <label for="needsApproval" class="ml-2 text-sm font-medium">Needs Approval</label>
        </div>
        <div>
            <label for="image" class="text-sm font-medium">Image</label>
            <input type="file" id="image" bind:files={image} class="w-full px-3 py-2 mt-1 border rounded-md">
        </div>
        <button type="submit" class="w-full px-4 py-2 mt-4 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
            Create Event
        </button>
    </form>
</div>