// alert.js

/**
 * Displays an alert message in the alert box.
 * @param {string} message - The message to display.
 * @param {string} type - The type of alert (e.g., "success", "error", "info"). Defaults to "info".
 */
function showAlert(message, type = "info") {
    console.log('alert LOADED');
    const alertBox = document.getElementById("alert-box");
    const alertMessage = document.getElementById("alert-message");

    if (!alertBox || !alertMessage) {
        console.error("Alert box or message element not found!");
        return;
    }

    // Set the message
    alertMessage.textContent = message;

    switch (type) {
        case "success":
            alertBox.classList.add("bg-green-100", "border-green-400", "text-green-700");
            break;
        case "error":
            alertBox.classList.add("bg-red-100", "border-red-400", "text-red-700");
            break;
        case "info":
            alertBox.classList.add("bg-blue-100", "border-blue-400", "text-blue-700");
            break;
        default:
            alertBox.classList.add("bg-gray-100", "border-gray-400", "text-gray-700");
    }

    // Show the alert box
    alertBox.classList.remove("hidden");

    // Automatically hide the alert after 5 seconds
    setTimeout(() => {
        closeAlert();
    }, 3000);
}

/**
 * Hides the alert box.
 */
function closeAlert() {
    const alertBox = document.getElementById("alert-box");
    if (alertBox) {
        alertBox.classList.add("hidden");
    }
}

// Make the functions globally available
window.showAlert = showAlert;
window.closeAlert = closeAlert;