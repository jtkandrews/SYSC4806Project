<script lang="ts">
    import { goto } from '$app/navigation';
    import { ownerLogin, userLogin } from '$lib/session';

    let mode: 'user' | 'owner' = 'user';
    let username = '';
    let password = '';
    let ownerPassword = '';
    let error = '';

    async function submit() {
        error = '';
        try {
            if (mode === 'user') {
                if (!username.trim() || !password) {
                    error = 'Enter a username and password.';
                    return;
                }
                await userLogin(username.trim(), password);
            } else {
                if (!ownerPassword) {
                    error = 'Enter the owner password.';
                    return;
                }
                await ownerLogin(ownerPassword);
            }
            await goto('/');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Login failed';
        }
    }
</script>

<div class="flex flex-col items-center mt-20 gap-6">
    <div class="bg-white shadow rounded p-8 w-full max-w-md">
        <h1 class="text-2xl font-semibold mb-4 text-center">Sign in to Amazin</h1>

        <div class="flex gap-2 mb-4">
            <button
                class={`flex-1 px-3 py-2 rounded border ${mode === 'user' ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-gray-700 border-gray-300'}`}
                on:click={() => (mode = 'user')}
                type="button"
            >
                User Login
            </button>
            <button
                class={`flex-1 px-3 py-2 rounded border ${mode === 'owner' ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-gray-700 border-gray-300'}`}
                on:click={() => (mode = 'owner')}
                type="button"
            >
                Owner Login
            </button>
        </div>

        {#if mode === 'user'}
            <div class="flex flex-col gap-3">
                <input
                    bind:value={username}
                    placeholder="Username"
                    class="border px-3 py-2 rounded"
                />

                <input
                    type="password"
                    bind:value={password}
                    placeholder="Password"
                    class="border px-3 py-2 rounded"
                />
            </div>
        {:else}
            <div class="flex flex-col gap-3">
                <input
                    type="password"
                    bind:value={ownerPassword}
                    placeholder="Owner password"
                    class="border px-3 py-2 rounded"
                />
            </div>
        {/if}

        <button
            class="bg-blue-600 text-white px-4 py-2 rounded w-full mt-6"
            on:click={submit}
        >
            {mode === 'user' ? 'Login as User' : 'Login as Owner'}
        </button>

        {#if error}
            <p class="text-red-600 mt-3 text-center">{error}</p>
        {/if}
    </div>
</div>
