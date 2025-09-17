document.getElementById('login-form').addEventListener('submit', async function(event) {
        event.preventDefault(); // Prevent default form submission

        const api = window.api

        // Get form data
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        // Prepare the payload (make sure it matches your required structure)
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
                 showAlert('Login successful!', 'success');
                 //delay 1 second
                 setTimeout(() => {
                    window.location.href = '/';
                 }, 1000);
            } else {
                showAlert(data.message, 'error');
            }


        } catch (error) {
            showAlert('Login Failed. Please try again.');
        }
    });


// Google Sign-In initialization
function initGoogleSignIn() {
    console.log('Initializing Google Sign-In');
    const googleClientId = document.getElementById('google-client-id')?.value;
    
    if (!googleClientId) {
        console.warn('Google client ID not found');
        return;
    }
    
    // Load the Google Sign-In API script
    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    document.head.appendChild(script);
    
    script.onload = function() {
        // Initialize Google Sign-In client
        google.accounts.id.initialize({
            client_id: googleClientId,
            callback: handleGoogleSignIn,
            auto_select: false,
            cancel_on_tap_outside: true,
        });
        
        // Handle Google Sign-In button click
        document.getElementById('google-login-btn').addEventListener('click', function() {
            google.accounts.id.prompt();
        });
    };
}

// Facebook initialization
function initFacebookSignIn() {
    console.log('Initializing Facebook Sign-In');
    
    const facebookAppId = document.getElementById('facebook-app-id')?.value;
    
    if (!facebookAppId) {
        console.warn('Facebook App ID not found');
        return;
    }
    
    // Load the Facebook SDK
    window.fbAsyncInit = function() {
        FB.init({
            appId: facebookAppId,
            cookie: true,
            xfbml: true,
            version: 'v18.0'
        });
        
        // Handle Facebook login button click
        document.getElementById('facebook-login-btn').addEventListener('click', function() {
            FB.login(function(response) {
                if (response.authResponse) {
                    console.log('Facebook auth response:', response);
                    // Pass the access token directly to the handler instead of using global state
                    handleFacebookSignIn(response.authResponse.accessToken);
                } else {
                    showAlert('Facebook login was cancelled or failed', 'error');
                }
            }, {scope: 'email,public_profile'});
        });
    };
    
    // Load Facebook SDK script
    (function(d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id)) return;
        js = d.createElement(s); js.id = id;
        js.src = "https://connect.facebook.net/en_US/sdk.js";
        fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk'));
}

// Handle Google Sign-In response
async function handleGoogleSignIn(response) {
    if (!response.credential) {
        showAlert('Google Sign-In failed. Please try again.');
        return;
    }
    
    try {
        // Send the ID token to your backend for verification
        const data = await window.api.post(ApiClient.ENDPOINTS.GOOGLE_LOGIN, {
            idToken: response.credential
        });
        
        if (data && data.data && data.data.token) {
            const token = data.data.token;
            
            // Store the token in localStorage and cookie
            localStorage.setItem('authToken', token);
            
            // Store the token in a cookie (valid for 10 days)
            const expirationDate = new Date();
            expirationDate.setDate(expirationDate.getDate() + 10);
            document.cookie = `authToken=${token}; expires=${expirationDate.toUTCString()}; path=/`;
            
            showAlert('Google Sign-In successful!', 'success');
            
            // Redirect to home page after a short delay
            setTimeout(() => {
                window.location.href = '/';
            }, 1000);
        } else {
            showAlert('Google Sign-In failed. Please try again.');
        }
    } catch (error) {
        console.error('Google Sign-In error:', error);
        showAlert('Google Sign-In failed. Please try again.');
    }
}

// Handle Facebook login response
async function handleFacebookSignIn(accessToken) {
    try {
        // Send token to backend
        const data = await window.api.post(ApiClient.ENDPOINTS.FACEBOOK_LOGIN, {
            accessToken: accessToken
        });
        
        if (data.success && data.data.token) {
            const token = data.data.token;
            
            // Store token in localStorage and cookie
            localStorage.setItem('authToken', token);
            
            // Store cookie valid for 10 days
            const expirationDate = new Date();
            expirationDate.setDate(expirationDate.getDate() + 10);
            document.cookie = `authToken=${token}; expires=${expirationDate.toUTCString()}; path=/`;
            
            showAlert('Login successful!', 'success');
            
            // Redirect after successful login
            setTimeout(() => {
                window.location.href = '/';
            }, 1000);
        } else {
            showAlert('Facebook login failed', 'error');
        }
    } catch (error) {
        console.error('Error during Facebook authentication:', error);
        showAlert('Authentication failed. Please try again.', 'error');
    }
}

// Initialize Google Sign-In when the page loads
initGoogleSignIn();
initFacebookSignIn();
