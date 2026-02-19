async function joinEvent(eventId) {
    
    const data = await window.api.post(ApiClient.ENDPOINTS.JOIN_EVENT, { eventId: eventId });
    console.log('Event joined:', data.success);
    // Optionally update the page content (example)
    if (data.success) {
        const message = data.message;
        // Example: Show a success message
        const contentDiv = document.getElementById('main-content');
        const eventDetailUrl = `${ApiClient.ENDPOINTS.EVENT_DETAIL.replace("{eventId}", eventId)}`;
        const html = await window.api.getHtml(eventDetailUrl);
        contentDiv.innerHTML = html;
        showAlert(message, "success");
    } else {
        const message = data.message;
        // Example: Show an error message
        showAlert(message, "error");
    }
}

async function approveUser(eventId, userId) {
    const data = await window.api.post(ApiClient.ENDPOINTS.APPROVE_USER, { eventId: eventId, userId: userId });
    console.log('User approved:', data.success);
    // Optionally update the page content (example)
    if (data.success) {
        const message = data.message;
        // Example: Show a success message
        const contentDiv = document.getElementById('main-content');
        const eventDetailUrl = `${ApiClient.ENDPOINTS.EVENT_DETAIL.replace("{eventId}", eventId)}`;
        const html = await window.api.getHtml(eventDetailUrl);
        contentDiv.innerHTML = html;
        showAlert(message, "success");
    } else {
        const message = data.message;
        // Example: Show an error message
        showAlert(message, "error");
    }
}

async function removeUser(eventId, userId) {
    // Show confirmation dialog
    if (!confirm("Are you sure you want to remove this user from the event?")) {
        return;
    }
    
    const data = await window.api.post(ApiClient.ENDPOINTS.REMOVE_USER, { eventId: eventId, userId: userId });
    console.log('User removed:', data.success);
    
    if (data.success) {
        // Refresh the event details page to show updated attendee list
        const contentDiv = document.getElementById('main-content');
        const eventDetailUrl = `${ApiClient.ENDPOINTS.EVENT_DETAIL.replace("{eventId}", eventId)}`;
        const html = await window.api.getHtml(eventDetailUrl);
        contentDiv.innerHTML = html;
        showAlert("User removed successfully", "success");
    } else {
        showAlert(data.message || "Failed to remove user", "error");
    }
}

function updateCountdown() {
    // Check if countdown element exists before trying to update it
    const countdownElement = document.getElementById("countdown");
    if (!countdownElement) return;
    
    const now = new Date();
    const eventTime = new Date(eventDate);
    const difference = eventTime - now;
    if (difference <= 0) {
        countdownElement.innerHTML = "Event has started!";
    } else {
        const days = Math.floor(difference / (1000 * 60 * 60 * 24));
        const hours = Math.floor((difference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((difference % (1000 * 60)) / 1000);
        countdownElement.innerHTML = `${days} days, ${hours} hours, ${minutes} minutes, ${seconds} seconds`;
    }
}

function toggleWaitingList() {
    const content = document.getElementById('waiting-list');
    content.classList.toggle('hidden');
}

function toggleParticipants() {
    console.log('Toggling participants');
    const content = document.getElementById('participants-list');
    const icon = document.querySelector('.participants-toggle-icon path');
    console.log('Content:', content);
    console.log('Icon before toggle:', icon.getAttribute('d'));
    content.classList.toggle('hidden');
    if (content.classList.contains('hidden')) {
        icon.setAttribute('d', 'M5 8l7 7 7-7'); // Down arrow
    } else {
        icon.setAttribute('d', 'M5 16l7-7 7 7'); // Up arrow
    }
    console.log('Icon after toggle:', icon.getAttribute('d'));
}

setInterval(updateCountdown, 1000);
updateCountdown(); // Initial call
setNavigateUrl(ApiClient.ENDPOINTS.EVENTS_UPCOMING);
