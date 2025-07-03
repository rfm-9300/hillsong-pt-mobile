function switchTab(tabName) {
    // Hide all content
    document.getElementById('upcoming-content').classList.add('hidden');
    document.getElementById('past-content').classList.add('hidden');

    // Remove active state from all tabs
    document.getElementById('upcoming-tab').classList.remove('text-blue-600', 'border-b-2', 'border-blue-600');
    document.getElementById('past-tab').classList.remove('text-blue-600', 'border-b-2', 'border-blue-600');

    // Add default style to all tabs
    document.getElementById('upcoming-tab').classList.add('text-gray-500');
    document.getElementById('past-tab').classList.add('text-gray-500');

    // Show selected content and activate tab
    document.getElementById(`${tabName}-content`).classList.remove('hidden');
    document.getElementById(`${tabName}-tab`).classList.remove('text-gray-500');
    document.getElementById(`${tabName}-tab`).classList.add('text-blue-600', 'border-b-2', 'border-blue-600');
}

// Add smooth scroll behavior for anchor links
document.addEventListener('DOMContentLoaded', function() {
    
    // Add loading state to event cards when clicked
    document.querySelectorAll('.event-card').forEach(card => {
        card.addEventListener('click', function() {
            this.classList.add('opacity-75', 'cursor-wait');
        });
    });
}); 