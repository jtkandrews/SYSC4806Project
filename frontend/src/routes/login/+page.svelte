<script lang="ts">
    import { goto } from '$app/navigation';
    import { ownerLogin, userLogin } from '$lib/session';

    let username = '';
    let password = '';
    let error = '';
    let isSubmitting = false;

    async function submit() {
        error = '';
        if (!username.trim() || !password) {
            error = 'Enter a username and password.';
            return;
        }

        isSubmitting = true;
        try {
            // Try user login first; if that fails and username is owner, fall back to owner login
            try {
                await userLogin(username.trim(), password);
            } catch (err) {
                if (username.trim().toLowerCase() === 'owner') {
                    await ownerLogin(password);
                } else {
                    throw err;
                }
            }
            await goto('/');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Login failed';
        } finally {
            isSubmitting = false;
        }
    }
</script>

<div class="flex flex-col items-center mt-20 gap-6">
    <div class="bg-white shadow rounded p-8 w-full max-w-md">
        <h1 class="text-2xl font-semibold mb-4 text-center">Sign in to Amazin</h1>

        <div class="flex flex-col gap-3">
            <input
                bind:value={username}
                placeholder="Username"
                class="border px-3 py-2 rounded"
                autocomplete="username"
            />

            <input
                type="password"
                bind:value={password}
                placeholder="Password"
                class="border px-3 py-2 rounded"
                autocomplete="current-password"
            />
        </div>

        <button
            class="bg-blue-600 text-white px-4 py-2 rounded w-full mt-6 disabled:opacity-60"
            on:click={submit}
            disabled={isSubmitting}
        >
            {isSubmitting ? 'Signing in...' : 'Login'}
        </button>

        {#if error}
            <p class="text-red-600 mt-3 text-center">{error}</p>
        {/if}
    </div>
</div>
