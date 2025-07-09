<script>
    export let data;
    let events = data.events;

    async function deleteEvent(eventId) {
        const token = localStorage.getItem('authToken');
        const response = await fetch('/api/events/delete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ eventId })
        });

        if (response.ok) {
            events = events.filter(event => event.id !== eventId);
        }
    }
</script>

<div class="p-8">
    <div class="flex items-center justify-between">
        <h1 class="text-2xl font-bold">Manage Events</h1>
        <a href="/admin/events/create" class="px-4 py-2 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
            Create Event
        </a>
    </div>

    <div class="mt-8">
        <table class="w-full text-left bg-white rounded-lg shadow-md">
            <thead>
                <tr>
                    <th class="p-4">Title</th>
                    <th class="p-4">Date</th>
                    <th class="p-4">Location</th>
                    <th class="p-4">Actions</th>
                </tr>
            </thead>
            <tbody>
                {#each events as event}
                    <tr class="border-t">
                        <td class="p-4">{event.title}</td>
                        <td class="p-4">{new Date(event.date).toLocaleString()}</td>
                        <td class="p-4">{event.location}</td>
                        <td class="p-4">
                            <a href="/admin/events/{event.id}" class="text-blue-600 hover:underline">Edit</a>
                            <button on:click={() => deleteEvent(event.id)} class="ml-4 text-red-600 hover:underline">Delete</button>
                        </td>
                    </tr>
                {/each}
            </tbody>
        </table>
    </div>
</div>