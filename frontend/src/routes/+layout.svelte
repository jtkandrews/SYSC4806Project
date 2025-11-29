<script lang="ts">
  import '../app.css';

  // Session state & helpers
  import { user, role, refreshSession, logout } from '$lib/session';
  import { cartItemCount } from '$lib/stores/cart';
  import { onMount } from 'svelte';

  export let data;

  // Seed the client store from server-loaded session data
  onMount(() => {
    user.set(data?.session ?? null);
    if (!data?.session) {
      refreshSession();
    }
  });

  async function doLogout() {
    await logout();
    window.location.href = '/login';
  }
</script>

<header>
  <nav class="flex justify-between items-center px-6 py-4 bg-white shadow">
    <a href="/" class="logo" aria-label="Amazin Bookstore home">
      <img src="/amazin.png" alt="Amazin Bookstore logo" class="logo-image" width="140" />
    </a>

    <div class="flex items-center gap-3 text-sm">
      <a href="/" class="underline">Browse</a>
      <a href="/owner" class="underline">Owner</a>

      {#if $role === 'OWNER'}
        <!-- Owner controls -->
        <span class="text-gray-700">Role: OWNER ({$user?.username || 'owner'})</span>

        <button
                class="bg-blue-600 text-white px-3 py-1 rounded"
                on:click={doLogout}
        >
          Logout
        </button>

      {:else}
        <!-- Normal user controls -->
        <a href="/cart" class="underline">
          Cart ({$cartItemCount})
        </a>

        <span class="text-gray-700">Role: USER ({$user?.username || 'guest'})</span>

        <!-- Go to dedicated login page -->
        <a
                href="/login"
                class="bg-blue-600 text-white px-3 py-1 rounded"
        >
          Login
        </a>
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
