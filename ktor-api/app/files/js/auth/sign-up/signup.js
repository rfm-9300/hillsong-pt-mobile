document.getElementById('signup-form').addEventListener('submit', async function(event) {
    console.log('signup-form-form submitted');
        event.preventDefault(); // Prevent default form submission

        const api = window.api
        const contentDiv = document.getElementById('main-content');

        // Get form data
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirm-password').value;
        const firstName = document.getElementById('first-name').value;
        const lastName = document.getElementById('last-name').value;

        const payload = {
            email,
            password,
            confirmPassword,
            firstName,
            lastName
        }

        try {
            // Send POST request
            const data = await api.post(ApiClient.ENDPOINTS.SIGNUP, payload, {})

            // Check if the request was successful
            if (!data.success) {
                console.log('HTTP error:', data.message);
                showAlert(data.message, 'error');
                return;
            }

            console.log('Response:', data);
            showAlert('Signup successful. Please login.', 'success');

            tryLogin(email, password);

        } catch (error) {
            console.error('Error during the login process:', error);
            showAlert('Signup Failed. Please try again.', 'error');
        }
    });

    async function tryLogin(email, password){
        const api = window.api
        const payload = {
            email: email,
            password: password
        };
        try {
            // Send POST request
            const data = await api.post(ApiClient.ENDPOINTS.LOGIN, payload);
            console.log('Response:', data);

            const token = data.data.token;
            // Store the token
            if (token) {
                localStorage.setItem('authToken', token);
                //store cookie
                //Store the token in a cookie (valid for 1 day)
                 const expirationDate = new Date();
                 expirationDate.setDate(expirationDate.getDate() + 10); // 1 day
                 document.cookie = `authToken=${token}; expires=${expirationDate.toUTCString()}; path=/`;
                 console.log('Token stored in a cookie as well:', document.cookie);
            } else {
                console.error('No token found in response');
            }

            // Redirect to the home page
            window.location.href = '/';

        } catch (error) {
            console.error('Error during the login process:', error);
            document.getElementById('main-content').innerHTML = 'Login Failed. Please try again.';
        }
    }