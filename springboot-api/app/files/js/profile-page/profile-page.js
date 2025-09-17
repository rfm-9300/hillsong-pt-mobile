let cropper;

function showEditProfile() {
  const editProfile = document.getElementById('profile-edit-box');

    if (editProfile.classList.contains('hidden')) {
        editProfile.classList.remove('hidden');
    } else {
        editProfile.classList.add('hidden');
    }
}

function hideEditProfile() {
    document.getElementById('profile-edit-box').classList.add('hidden');
    // Destroy cropper when closing to prevent issues
    if (cropper) {
        cropper.destroy();
        cropper = null;
    }
}

async function submitProfileEdit() {
    try {
        const api = window.api;
        if (!api.token) {
            showAlert('Please log in to edit your profile.', 'error');
            return;
        }
        // Create FormData object
        const formData = new FormData();

        // add cropped image
        if(cropper) {
            // Get the cropped image as BLOB
            const blob = await new Promise(resolve => {
                cropper.getCroppedCanvas({
                    width: 300, // Output fixed size for consistent results
                    height: 300,
                    fillColor: '#fff',
                    imageSmoothingEnabled: true,
                    imageSmoothingQuality: 'high'
                }).toBlob(resolve, 'image/jpeg', 0.9);
            });
            
            // Append as file with filename
            formData.append('image', blob, 'profile.jpg');
        }

        const data = await api.post(ApiClient.ENDPOINTS.UPDATE_PROFILE, formData, {}, false)
        console.log('Response:', data);

        if (!data.success){
            showAlert(data.message, 'error');
            return;
        }
        hideEditProfile();
        showAlert('Profile updated successfully!', 'success');
        
        // Refresh the profile image without reloading the page
        setTimeout(() => {
            const randomParam = new Date().getTime();
            document.querySelectorAll('img[src*="profile-picture"]').forEach(img => {
                const src = img.src.split('?')[0];
                img.src = `${src}?v=${randomParam}`;
            });
        }, 500);

    } catch (error) {
        console.error('Error during profile update:', error);
        showAlert('An error occurred while updating your profile.', 'error');
    }
    
}

function loadcropper() {
    const imageInput = document.getElementById('profile-picture');
    const imagePreview = document.getElementById('image-preview');
    const cropperContainer = document.getElementById('image-preview-container');
    
    // Enhance the preview container's style
    if (cropperContainer) {
        cropperContainer.classList.remove('hidden');
        cropperContainer.classList.add('w-full', 'flex', 'flex-col', 'items-center', 'mb-4');
        
        // Set better dimensions for the cropper preview and make it circular
        const previewContainer = cropperContainer.querySelector('div');
        if (previewContainer) {
            previewContainer.classList.remove('w-32', 'h-32');
            previewContainer.classList.add('w-64', 'h-64', 'border-2', 'shadow-md', 'rounded-full', 'overflow-hidden');
        }
    }

    imageInput.addEventListener('change', function (e) {
        const file = e.target.files[0];
        if (file) {
            // Validate file type and size
            if (!file.type.match('image.*')) {
                showAlert('Please select an image file.', 'error');
                return;
            }
            
            if (file.size > 5 * 1024 * 1024) { // 5MB limit
                showAlert('Image is too large. Please select an image under 5MB.', 'error');
                return;
            }
            
            const reader = new FileReader();
            reader.onload = function (event) {
                // Show the image preview and Cropper.js container
                imagePreview.src = event.target.result;
                cropperContainer.classList.remove('hidden');
                
                // Ensure the container is visible and has proper size
                const previewContainer = cropperContainer.querySelector('div');
                if (previewContainer) {
                    previewContainer.classList.remove('w-32', 'h-32');
                    previewContainer.classList.add('w-64', 'h-64', 'rounded-full');
                }

                // Initialize Cropper.js
                if (cropper) {
                    cropper.destroy(); // Destroy existing Cropper instance
                }
                
                // Add circular mask style
                const style = document.createElement('style');
                style.id = 'circular-cropper-style';
                style.innerHTML = `
                    .cropper-view-box,
                    .cropper-face {
                        border-radius: 50%;
                    }
                `;
                // Remove existing style if it exists
                if (document.getElementById('circular-cropper-style')) {
                    document.getElementById('circular-cropper-style').remove();
                }
                document.head.appendChild(style);
                
                // Better cropper configuration
                cropper = new Cropper(imagePreview, {
                    aspectRatio: 1, // 1:1 for profile picture
                    viewMode: 1,    // Restrict the crop box to the image size
                    dragMode: 'move', // Let users move the image by default
                    guides: false,  // Hide guides for circular crop
                    center: true,
                    highlight: false, // Hide highlight for circular crop
                    background: false,
                    autoCropArea: 1, // Show full circular area
                    responsive: true,
                    restore: false,
                    minContainerWidth: 250,
                    minContainerHeight: 250,
                    ready: function() {
                        // Add cropper controls
                        addCropperControls();
                    }
                });
            };
            reader.readAsDataURL(file);
        }
    });
}

function addCropperControls() {
    // Check if controls already exist
    if (document.getElementById('cropper-controls')) return;
    
    const container = document.getElementById('image-preview-container');
    const controlsDiv = document.createElement('div');
    controlsDiv.id = 'cropper-controls';
    controlsDiv.className = 'flex justify-center space-x-2 mt-4';
    
    // Zoom in button
    const zoomInBtn = document.createElement('button');
    zoomInBtn.type = 'button';
    zoomInBtn.className = 'p-2 bg-yellow-100 hover:bg-yellow-200 rounded-full';
    zoomInBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>';
    zoomInBtn.addEventListener('click', () => cropper.zoom(0.1));
    
    // Zoom out button
    const zoomOutBtn = document.createElement('button');
    zoomOutBtn.type = 'button';
    zoomOutBtn.className = 'p-2 bg-yellow-100 hover:bg-yellow-200 rounded-full';
    zoomOutBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 12H6" /></svg>';
    zoomOutBtn.addEventListener('click', () => cropper.zoom(-0.1));
    
    // Rotate button
    const rotateBtn = document.createElement('button');
    rotateBtn.type = 'button';
    rotateBtn.className = 'p-2 bg-yellow-100 hover:bg-yellow-200 rounded-full';
    rotateBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>';
    rotateBtn.addEventListener('click', () => cropper.rotate(90));
    
    // Reset button
    const resetBtn = document.createElement('button');
    resetBtn.type = 'button';
    resetBtn.className = 'p-2 bg-yellow-100 hover:bg-yellow-200 rounded-full';
    resetBtn.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>';
    resetBtn.addEventListener('click', () => cropper.reset());
    
    // Append buttons to controls div
    controlsDiv.appendChild(zoomInBtn);
    controlsDiv.appendChild(zoomOutBtn);
    controlsDiv.appendChild(rotateBtn);
    controlsDiv.appendChild(resetBtn);
    
    // Append controls to container
    container.appendChild(controlsDiv);
}

function switchTab(tabName) {
    // Hide all content
    document.getElementById('hosted-content').classList.add('hidden');
    document.getElementById('attended-content').classList.add('hidden');
    document.getElementById('waiting-content').classList.add('hidden');
    document.getElementById('attending-content').classList.add('hidden');

    // Remove active state from all tabs
    document.getElementById('hosted-tab').classList.remove('text-yellow-600', 'border-b-2', 'border-yellow-500');
    document.getElementById('attended-tab').classList.remove('text-yellow-600', 'border-b-2', 'border-yellow-500');
    document.getElementById('waiting-tab').classList.remove('text-yellow-600', 'border-b-2', 'border-yellow-500');
    document.getElementById('attending-tab').classList.remove('text-yellow-600', 'border-b-2', 'border-yellow-500');

    // Add default style to all tabs
    document.getElementById('hosted-tab').classList.add('text-gray-500');
    document.getElementById('attended-tab').classList.add('text-gray-500');
    document.getElementById('waiting-tab').classList.add('text-gray-500');
    document.getElementById('attending-tab').classList.add('text-gray-500');

    // Show selected content and activate tab
    document.getElementById(`${tabName}-content`).classList.remove('hidden');
    document.getElementById(`${tabName}-tab`).classList.remove('text-gray-500');
    document.getElementById(`${tabName}-tab`).classList.add('text-yellow-600', 'border-b-2', 'border-yellow-500');
}

// Call loadcropper when the page loads
document.addEventListener('DOMContentLoaded', loadcropper);
// Also call it immediately in case DOM is already loaded
loadcropper();