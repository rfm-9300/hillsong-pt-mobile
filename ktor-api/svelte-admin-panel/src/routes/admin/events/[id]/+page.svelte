<script>
    import { page } from '$app/stores';
    import { goto } from '$app/navigation';
    import { api } from '$lib/api';

    export let data;
    let event = data.event;
    let title = event.title;
    let description = event.description;
    let date = new Date(event.date).toISOString().slice(0, 16);
    let location = event.location;
    let maxAttendees = event.maxAttendees;
    let image = null;

    async function updateEvent() {
        const eventId = $page.params.id;
        const formData = new FormData();
        formData.append('eventId', eventId);
        formData.append('title', title);
        formData.append('description', description);
        formData.append('date', new Date(date).toISOString());
        formData.append('location', location);
        formData.append('maxAttendees', maxAttendees);
        if (image) {
            formData.append('image', image[0]);
        }

        try {
            await api.updateEvent(formData);
            goto('/admin/events');
        } catch (error) {
            console.error('Failed to update event:', error);
        }
    }
</script>

<div class="p-8">
    <h1 class="text-2xl font-bold">Edit Event</h1>

    {#if event}
        <form on:submit|preventDefault={updateEvent} class="mt-8 space-y-6">
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
            <div>
                <label for="image" class="text-sm font-medium">Image</label>
                <input type="file" id="image" bind:files={image} class="w-full px-3 py-2 mt-1 border rounded-md">
            </div>
            <button type="submit" class="w-full px-4 py-2 mt-4 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
                Update Event
            </button>
        </form>
    {/if}
</div>