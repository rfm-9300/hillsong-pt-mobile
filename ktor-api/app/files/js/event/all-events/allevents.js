function deleteEvent(eventId) {
    window.api.post(ApiClient.ENDPOINTS.DELETE_EVENT, { eventId })
        .then(data => {
            console.log('Event deleted:', eventId);
            if (data.success) {
                // Optionally update the page content (example)
                // Example: Show a success message
                showAlert('Event deleted', 'success');
            } else {
                // Example: Show an error message
                const message = data.message;
                showAlert(message, 'error');
            }
        })
        .catch(error => console.error('Error deleting event:', error));
}

function updateEvent(eventId) {
    window.api.post(ApiClient.ENDPOINTS.UPDATE_EVENT, { eventId })
        .then(data => {
            console.log('Event updated:', eventId);
            
        })
        .catch(error => console.error('Error updating event:', error));
}

