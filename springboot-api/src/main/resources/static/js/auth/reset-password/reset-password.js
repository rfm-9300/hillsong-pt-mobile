function requestResetPassword() {
    const resetRequestForm = document.getElementById('reset-request-form');
    if (resetRequestForm) {
        resetRequestForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            
            const api = window.api;
            const email = document.getElementById('email').value;
            
            try {
                const response = await api.post(ApiClient.ENDPOINTS.REQUEST_PASSWORD_RESET, { email: email });
                
                if (response.success) {
                    showAlert(response.message, 'success');
                } else {
                    showAlert(response.message || 'Failed to send reset link', 'error');
                }
            } catch (error) {
                console.error('Error requesting password reset:', error);
                showAlert('An error occurred. Please try again later.', 'error');
            }
        });
    }
}
requestResetPassword();

function resetPassword() {
    // Reset Password Form Submission
    const resetPasswordForm = document.getElementById('reset-password-form');
    if (resetPasswordForm) {
        resetPasswordForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            
            const api = window.api;
            const token = document.querySelector('input[name="token"]').value;
            const newPassword = document.getElementById('new-password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            
            if (newPassword !== confirmPassword) {
                showAlert('Passwords do not match', 'error');
                return;
            }
            
            try {
                const response = await api.post(ApiClient.ENDPOINTS.RESET_PASSWORD, {
                    token: token,
                    newPassword: newPassword
                });
                
                if (response.success) {
                    showAlert('Password reset successful', 'success');
                    
                    // Redirect to login page after 2 seconds
                    setTimeout(() => {
                        const loginHref = document.querySelector('[hx-get="' + ApiClient.ENDPOINTS.LOGIN + '"]');
                        if (loginHref) {
                            loginHref.click();
                        } else {
                            setNavigateUrl(ApiClient.ENDPOINTS.LOGIN);
                            navigate();
                        }
                    }, 2000);
                } else {
                    showAlert(response.message || 'Failed to reset password', 'error');
                }
            } catch (error) {
                console.error('Error resetting password:', error);
                showAlert('An error occurred. Please try again later.', 'error');
            }
        });
    }
}
resetPassword();

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