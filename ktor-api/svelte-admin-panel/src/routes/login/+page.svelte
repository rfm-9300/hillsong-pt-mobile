<script>
    import { goto } from '$app/navigation';

    let email = '';
    let password = '';
    let errorMessage = '';

    async function login() {
        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            if (response.ok) {
                const data = await response.json();
                if (data.success) {
                    localStorage.setItem('authToken', data.data.token);
                    goto('/admin');
                } else {
                    errorMessage = data.message;
                }
            } else {
                errorMessage = 'Login failed. Please check your credentials.';
            }
        } catch (error) {
            errorMessage = 'An error occurred during login.';
            console.error(error);
        }
    }
</script>

<div class="flex items-center justify-center h-screen">
    <div class="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
        <h1 class="text-2xl font-bold text-center">Admin Login</h1>
        <form on:submit|preventDefault={login}>
            <div>
                <label for="email" class="text-sm font-medium">Email</label>
                <input type="text" id="email" bind:value={email} class="w-full px-3 py-2 mt-1 border rounded-md" required>
            </div>
            <div>
                <label for="password" class="text-sm font-medium">Password</label>
                <input type="password" id="password" bind:value={password} class="w-full px-3 py-2 mt-1 border rounded-md" required>
            </div>
            {#if errorMessage}
                <p class="text-sm text-red-500">{errorMessage}</p>
            {/if}
            <button type="submit" class="w-full px-4 py-2 mt-4 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
                Login
            </button>
        </form>
    </div>
</div>
