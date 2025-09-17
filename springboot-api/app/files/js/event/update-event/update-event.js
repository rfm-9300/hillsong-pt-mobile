let cropper;

function loadcropper() {
    console.log('cropper LOADED');
    const imageInput = document.getElementById('image');
    const imagePreview = document.getElementById('image-preview');
    const cropperContainer = document.getElementById('image-cropper-container');
    const uploadStatus = document.getElementById('upload-status');

    // Initialize cropper with existing image
    if (imagePreview.src) {
        cropper = new Cropper(imagePreview, {
            aspectRatio: 16 / 9,
            viewMode: 1,
            autoCropArea: 1,
        });
    }

    imageInput.addEventListener('change', function (e) {
        const file = e.target.files[0];
        if (file) {
            // Update the text to show the filename
            const fileName = file.name.length > 10 ? file.name.substring(0, 10) + "..." : file.name;
            uploadStatus.textContent = `"${fileName}" selected!`;

            const reader = new FileReader();
            reader.onload = function (event) {
                // Show the image preview and Cropper.js container
                imagePreview.src = event.target.result;
                cropperContainer.classList.remove('hidden');

                // Initialize Cropper.js
                if (cropper) {
                    cropper.destroy(); // Destroy existing Cropper instance
                }
                cropper = new Cropper(imagePreview, {
                    aspectRatio: 16 / 9,
                    viewMode: 1,
                    autoCropArea: 1,
                });
            };
            reader.readAsDataURL(file);
        } else {
            // Clear the text if no file is selected
            uploadStatus.textContent = "";
        }
    });
}

// Function to set time from quick select buttons
function setTime(timeString) {
    const [hours, minutes] = timeString.split(':');
    document.getElementById("hour").value = hours;
    document.getElementById("minute").value = minutes;
}

// Add input validation for hour and minute
document.getElementById("hour").addEventListener("input", function() {
    let value = parseInt(this.value);
    if (value < 0) this.value = 0;
    if (value > 23) this.value = 23;
});

document.getElementById("minute").addEventListener("input", function() {
    let value = parseInt(this.value);
    if (value < 0) this.value = 0;
    if (value > 59) this.value = 59;
});

document.getElementById('submit-btn').addEventListener('click', async function(event) {
    console.log('submit-btn clicked');
    event.preventDefault();

    const api = window.api;
    const contentDiv = document.getElementById('main-content');

    if (!api.token) {
        showAlert('Please log in to update the event.', 'error');
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
        formData.append('eventId', document.getElementById('eventId').value);
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

        const data = await api.post(ApiClient.ENDPOINTS.UPDATE_EVENT, formData, {}, false);

        if (!data.success) {
            showAlert(data.message, 'error');
            return;
        }

        console.log('Response:', data);

        if (data.success) {
            showAlert('Event updated successfully!', 'success');
            setNavigateUrl(ApiClient.ENDPOINTS.EVENTS_UPCOMING);
            navigate();
        } else {
            showAlert(data.message, 'error');
        }

    } catch (error) {
        console.error('Error during the event update process:', error);
        showAlert('An error occurred while updating the event. Please try again later.', 'error');
    }
});

// Initialize cropper
loadcropper();

