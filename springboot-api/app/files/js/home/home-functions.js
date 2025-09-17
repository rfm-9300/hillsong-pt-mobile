async function submitPost() {
    try {
        const title = document.getElementById('post-title').value;
        const content = document.getElementById('post-content').value;

        const api = window.api;
        if (!api.token) {
            showAlert('Please log in to create a post.', 'error');
            return;
        }
        const payload = {
            title: title,
            content: content
        };
        console.log('payload', payload);

        // Send data as JSON
        const response = await api.post(ApiClient.ENDPOINTS.CREATE_POST, payload);

        if (!response.success) {
            showAlert(response.message, 'error');
            return;
        }

        showAlert('Post created successfully!', 'success');
    } catch (error) {
        showAlert('An error occurred while creating the post. Please try again later.', 'error');
    }
}
window.navigateUrl = "/events/upcoming";

function setNavigateUrl(url) {
    console.log('setting navigate url to', url);
    window.navigateUrl = url;
}

async function navigate() {
    const contentDiv = document.getElementById('main-content');
    const html = await window.api.getHtml(window.navigateUrl);
    contentDiv.innerHTML = html;
    htmx.process(contentDiv);
}