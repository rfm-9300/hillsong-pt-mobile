<script>
    import { onMount } from 'svelte';

    let users = [];

    onMount(async () => {
        const token = localStorage.getItem('authToken');
        const response = await fetch('/api/users', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (response.ok) {
            const data = await response.json();
            users = data.data.users;
        }
    });
</script>

<div class="p-8">
    <h1 class="text-2xl font-bold">Manage Users</h1>

    <div class="mt-8">
        <table class="w-full text-left bg-white rounded-lg shadow-md">
            <thead>
                <tr>
                    <th class="p-4">Name</th>
                    <th class="p-4">Email</th>
                    <th class="p-4">Verified</th>
                    <th class="p-4">Role</th>
                </tr>
            </thead>
            <tbody>
                {#each users as user}
                    <tr class="border-t">
                        <td class="p-4">{user.profile.firstName} {user.profile.lastName}</td>
                        <td class="p-4">{user.email}</td>
                        <td class="p-4">{user.verified ? 'Yes' : 'No'}</td>
                        <td class="p-4">{user.profile.isAdmin ? 'Admin' : 'User'}</td>
                    </tr>
                {/each}
            </tbody>
        </table>
    </div>
</div>
