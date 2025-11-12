<script lang="ts">
  import '../app.css';
  import { role, refreshRole, ownerLogin, logout } from '$lib/session';
  import { onMount } from 'svelte';

  let pw = '';
  let error = '';

  onMount(() => {
    refreshRole();
  });

  async function doLogin() {
    error = '';
    try {
      await ownerLogin(pw);
      pw = '';
    } catch {
      error = 'Incorrect password';
    }
  }
</script>

<header>
  <nav class="flex justify-between items-center px-6 py-4 bg-white shadow">
    <a href="/" class="text-blue-700 font-semibold text-lg">
      Amazin Bookstore
    </a>

    <div class="flex items-center gap-3 text-sm">
      <a href="/" class="underline">Browse</a>
      <a href="/owner" class="underline">Owner</a>

      {#if $role === 'OWNER'}
        <span class="text-gray-700">Role: OWNER</span>
        <button
                class="bg-blue-600 text-white px-3 py-1 rounded"
                on:click={() => logout()}
        >
          Logout
        </button>
      {:else}
        <span class="text-gray-700">Role: USER</span>
        <input
                type="password"
                placeholder="Owner password"
                bind:value={pw}
                class="border px-2 py-1 rounded"
        />
        <button
                class="bg-blue-600 text-white px-3 py-1 rounded"
                on:click={doLogin}
        >
          Login as Owner
        </button>
        {#if error}
          <span class="text-red-600">{error}</span>
        {/if}
      {/if}
    </div>
  </nav>
</header>

<main class="min-h-[calc(100vh-80px)] bg-gray-50">
  <slot />
</main>

<style>
  main {
    min-height: calc(100vh - 80px);
  }
</style>
