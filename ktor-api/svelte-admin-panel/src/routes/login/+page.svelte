<script>
    import { goto } from '$app/navigation';
    import { fade } from 'svelte/transition';
	import { auth } from '$lib/authStore';
    import { api } from '$lib/api';

    let email = '';
    let password = '';
    let errorMessage = '';
    let loading = false;

    async function login() {
        loading = true;
        errorMessage = '';
        try {
            const data = await api.login(email, password);
            if (data.success) {
                auth.login(data.data.token);
                goto('/admin/dashboard');
            } else {
                errorMessage = data.message;
            }
        } catch (error) {
            errorMessage = error.message || 'An error occurred during login.';
        } finally {
            loading = false;
        }
    }
</script>

<div class="flex items-center justify-center min-h-screen bg-gradient-to-br from-indigo-50 to-blue-100 p-4">
    <div in:fade={{ duration: 300, delay: 150 }} class="w-full max-w-md overflow-hidden bg-white rounded-2xl shadow-xl">
        <div class="px-8 pt-8 pb-6 bg-blue-600">
            <h1 class="text-2xl font-bold text-white text-center">Admin Login</h1>
        </div>
        
        <div class="p-8">
            <form on:submit|preventDefault={login} class="space-y-6">
                <div class="space-y-2">
                    <label for="email" class="text-sm font-medium text-gray-700">Email</label>
                    <div class="relative">
                        <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                            </svg>
                        </div>
                        <input 
                            type="text" 
                            id="email" 
                            bind:value={email} 
                            class="w-full pl-10 pr-3 py-3 border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" 
                            placeholder="Enter your username" 
                            required
                        >
                    </div>
                </div>
                
                <div class="space-y-2">
                    <label for="password" class="text-sm font-medium text-gray-700">Password</label>
                    <div class="relative">
                        <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                            </svg>
                        </div>
                        <input 
                            type="password" 
                            id="password" 
                            bind:value={password} 
                            class="w-full pl-10 pr-3 py-3 border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" 
                            placeholder="••••••••" 
                            required
                        >
                    </div>
                </div>
                
                {#if errorMessage}
                    <div transition:fade class="p-3 bg-red-50 text-red-700 rounded-lg">
                        <p class="text-sm">{errorMessage}</p>
                    </div>
                {/if}
                
                <button 
                    type="submit" 
                    class="w-full flex justify-center items-center py-3 px-4 font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 shadow-md transition-all duration-200"
                    disabled={loading}
                >
                    {#if loading}
                        <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Signing in...
                    {:else}
                        Sign in
                    {/if}
                </button>
            </form>
        </div>
    </div>
</div>