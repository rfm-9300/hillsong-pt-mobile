<script>
    import { fade, fly } from 'svelte/transition';
    import { api } from '$lib/api';
    import PageHeader from '$lib/components/PageHeader.svelte';
    import Button from '$lib/components/Button.svelte';
    import Card from '$lib/components/Card.svelte';
    import EmptyState from '$lib/components/EmptyState.svelte';
    import Modal from '$lib/components/Modal.svelte';
    
    export let data;
    let events = data.events;
    let deleteConfirmation = { show: false, eventId: null };

    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'short', 
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
    
    function navigateToEdit(eventId) {
        window.location.href = `/admin/events/${eventId}`;
    }
    
    function confirmDelete(eventId) {
        deleteConfirmation = { show: true, eventId };
    }
    
    async function deleteEvent(eventId) {
        try {
            await api.post(api.endpoints.EVENT_DELETE, { eventId });
            events = events.filter(event => event.id !== eventId);
        } catch (error) {
            console.error('Failed to delete event:', error);
        } finally {
            deleteConfirmation = { show: false, eventId: null };
        }
    }
</script>

<div in:fade={{ duration: 300 }}>
    <PageHeader title="Manage Events" subtitle="Create and manage your events">
        <Button onclick={() => window.location.href = '/admin/events/create'}>
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd" />
            </svg>
            Create Event
        </Button>
    </PageHeader>

    {#if events.length === 0}
        <EmptyState 
            title="No Events Yet"
            description="Create your first event to get started"
            actionText="Create Event"
            onAction={() => window.location.href = '/admin/events/create'}
        >
            {#snippet icon()}
                <svg xmlns="http://www.w3.org/2000/svg" class="h-full w-full" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
            {/snippet}
        </EmptyState>
    {:else}
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {#each events as event, i (event.id)}
                <Card 
                    hover={true}
                    padding="p-0"
                    class="h-64 cursor-pointer overflow-hidden"
                    onclick={() => navigateToEdit(event.id)}
                >
                    <div 
                        in:fly={{ y: 20, delay: i * 75, duration: 300 }} 
                        class="relative h-full"
                        role="button"
                        tabindex="0"
                        on:keydown={(e) => e.key === 'Enter' && navigateToEdit(event.id)}
                        aria-label="Edit event: {event.title}"
                    >
                        <!-- Background gradient -->
                        <div class="absolute inset-0 bg-gradient-to-r from-indigo-500 to-purple-600"></div>
                        
                        <!-- Calendar day indication -->
                        <div class="absolute top-4 right-4 bg-white rounded-lg shadow-md overflow-hidden z-20">
                            <div class="px-3 py-1 bg-indigo-600 text-white text-xs font-bold text-center">
                                {new Date(event.date).toLocaleString('en-US', { month: 'short' })}
                            </div>
                            <div class="px-3 py-2 text-center text-indigo-800 font-bold">
                                {new Date(event.date).getDate()}
                            </div>
                        </div>
                        
                        <!-- Content overlay -->
                        <div class="relative h-full flex flex-col justify-between p-5 text-white z-10">
                            <div>
                                <h3 class="text-xl font-bold mb-2 text-white line-clamp-2">{event.title}</h3>
                                <p class="text-white/80 mb-4 line-clamp-2">{event.description}</p>
                            </div>
                            
                            <div class="mt-auto">
                                <div class="flex justify-between text-sm text-white/70 mb-4">
                                    <div class="flex items-center">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                        </svg>
                                        {formatDate(event.date)}
                                    </div>
                                    <div class="flex items-center">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                                        </svg>
                                        {event.location}
                                    </div>
                                </div>
                                
                                <div class="flex justify-between pt-3 border-t border-white/20">
                                    <Button 
                                        variant="ghost"
                                        size="sm"
                                        onclick={(e) => { e.stopPropagation(); navigateToEdit(event.id); }}
                                        class="!text-white hover:!text-indigo-200 !p-1"
                                    >
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                        </svg>
                                        Edit
                                    </Button>
                                    <Button 
                                        variant="ghost"
                                        size="sm"
                                        onclick={(e) => { e.stopPropagation(); confirmDelete(event.id); }}
                                        class="!text-white hover:!text-red-200 !p-1"
                                    >
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                        Delete
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>
                </Card>
            {/each}
        </div>
    {/if}
</div>

<Modal 
    show={deleteConfirmation.show}
    title="Confirm Deletion"
    onClose={() => deleteConfirmation = { show: false, eventId: null }}
    size="sm"
>
    <p class="text-gray-700 mb-6">Are you sure you want to delete this event? This action cannot be undone.</p>
    
    <div class="flex justify-end gap-3">
        <Button 
            variant="secondary"
            onclick={() => deleteConfirmation = { show: false, eventId: null }}
        >
            Cancel
        </Button>
        <Button 
            variant="danger"
            onclick={() => deleteEvent(deleteConfirmation.eventId)}
        >
            Delete
        </Button>
    </div>
</Modal>