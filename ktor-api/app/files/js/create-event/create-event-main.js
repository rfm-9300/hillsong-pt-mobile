document.getElementById('submit-btn').addEventListener('click', async function(event) {
console.log('submit-btn clicked');
    event.preventDefault();

    const api = new ApiClient(); // Instantiate the ApiClient
    const contentDiv = document.getElementById('event-content');

    if (!api.token) {
        showAlert('Please log in to create an event.', 'error');
        return;
    }

    try {
        // Create FormData object
        const formData = new FormData();

        // Combine date and time into a single datetime string
        const date = document.getElementById('date').value;
        const hour = document.getElementById('hour').value.padStart(2, '0');
        const minute = document.getElementById('minute').value.padStart(2, '0');
        const dateTime = `${date}T${hour}:${minute}`;

        // Add all form fields
        formData.append('source', 'web');
        formData.append('title', document.getElementById('title').value);
        formData.append('description', document.getElementById('description').value);
        formData.append('date', dateTime);
        formData.append('location', document.getElementById('location').value);
        formData.append('maxAttendees', document.getElementById('maxAttendees').value);
        formData.append('needsApproval', document.getElementById('needsApproval').checked);

        // Add cropped image if it exists
        if (cropper) {
            // Get the cropped image as BLOB
            const blob = await new Promise(resolve => {
                cropper.getCroppedCanvas().toBlob(resolve, 'image/jpeg', 0.9);
            });
            
            // Append as file with filename
            formData.append('image', blob, 'event.jpg');
        }

        const data = await api.post(ApiClient.ENDPOINTS.CREATE_EVENT, formData, {}, false)

        if (!data.success){
            showAlert(data.message, 'error');
            return;
        }

        console.log('Response:', data);

        const eventContent = document.getElementById('event-content');

        if (data.success) {
            showAlert('Event created successfully!', 'success');
            setNavigateUrl(ApiClient.ENDPOINTS.EVENTS_UPCOMING);
            navigate();
        } else {
            showAlert(data.message, 'error');
        }

    } catch (error) {
        console.error('Error during the event creation process:', error);
        document.getElementById('event-content').innerHTML = `<p class="text-red-500 font-bold">An error occurred while creating the event. Please try again later.</p>`;
    }
});