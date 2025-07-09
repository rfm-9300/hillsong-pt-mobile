
    import { goto } from '$app/navigation';

    let title = '';
    let description = '';
    let date = '';
    let location = '';
    let maxAttendees = 0;
    let needsApproval = false;
    let image = null;

    async function createEvent() {
        const token = localStorage.getItem('authToken');
        const formData = new FormData();
        formData.append('title', title);
        formData.append('description', description);
        formData.append('date', new Date(date).toISOString());
        formData.append('location', location);
        formData.append('maxAttendees', maxAttendees);
        formData.append('needsApproval', needsApproval);
        if (image) {
            formData.append('image', image[0]);
        }

        const response = await fetch('/api/events', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (response.ok) {
            goto('/admin/events');
        }
    }
