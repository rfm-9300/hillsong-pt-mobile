// Ensure the script runs after the DOM is fully loaded
document.addEventListener('DOMContentLoaded', function() {
    const likeIcons = document.querySelectorAll('.like-icon');

    likeIcons.forEach(icon => {
        icon.addEventListener('click', function() {
            const postId = this.getAttribute('data-post-id');
            console.log('Clicked like for post:', postId);
            const url = `/post/like?postId=${postId}`;


            fetch(url, {
                method: 'GET',
                credentials: 'include'
            })
            .then(response => {
                console.log('Got response:', response);
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }

            })
            .catch(error => {
                console.error('Error:', error);
            });
        });
    });
});