<script lang="ts">
    import { userLogin } from '$lib/session';
    let username = '';
    let password = '';
    let error = '';

    async function submit() {
        error = '';
        try {
            await userLogin(username, password);
            window.location.href = '/'; // redirect after login
        } catch {
            error = 'Invalid username or password';
        }
    }
</script>

<div class="flex flex-col items-center mt-20">
    <h1 class="text-2xl mb-4">Login</h1>

    <input
            bind:value={username}
            placeholder="Username"
            class="border px-3 py-2 rounded mb-2"
    />

    <input
            type="password"
            bind:value={password}
            placeholder="Password"
            class="border px-3 py-2 rounded mb-2"
    />

    <button
            class="bg-blue-600 text-white px-4 py-2 rounded"
            on:click={submit}
    >
        Login
    </button>

    {#if error}
        <p class="text-red-600 mt-2">{error}</p>
    {/if}
</div>
