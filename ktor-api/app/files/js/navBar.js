function setActiveTab(tabElement) {
    // Remove active class from all tabs
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.remove('text-blue-600', 'font-bold');
        tab.classList.add('text-gray-600');
    });

    // Add active class to clicked tab
    tabElement.classList.remove('text-gray-600');
    tabElement.classList.add('text-blue-600', 'font-bold');
}

setActiveTab(document.getElementById('home-tab'));