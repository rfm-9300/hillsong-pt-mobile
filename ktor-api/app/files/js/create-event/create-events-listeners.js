console.log('main LOADED');

let cropper;

function loadcropper() {
    console.log('cropper LOADED');
    const imageInput = document.getElementById('image');
    const imagePreview = document.getElementById('image-preview');
    const cropperContainer = document.getElementById('image-cropper-container');
    const uploadStatus = document.getElementById('upload-status');

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
                    aspectRatio: 16 / 9, // Set aspect ratio for event images
                    viewMode: 1, // Restrict the crop box to the image size
                    autoCropArea: 1, // Automatically crop the entire image
                });
            };
            reader.readAsDataURL(file);
        } else {
            // Clear the text if no file is selected
            uploadStatus.textContent = "";
            cropperContainer.classList.add('hidden');
        }
    });
}

// Get the current date and time
const now = new Date();

// Format the date as YYYY-MM-DD
const year = now.getFullYear();
const month = String(now.getMonth() + 1).padStart(2, '0');
const day = String(now.getDate()).padStart(2, '0');
const formattedDate = `${year}-${month}-${day}`;

// Format the time as HH:MM (rounded to nearest 30 minutes)
const hours = now.getHours();
const minutes = now.getMinutes();
const roundedMinutes = Math.ceil(minutes / 30) * 30;

// Set the values of the inputs
document.getElementById("date").value = formattedDate;
document.getElementById("hour").value = String(hours).padStart(2, '0');
document.getElementById("minute").value = String(roundedMinutes).padStart(2, '0');

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

// Initialize cropper
loadcropper();