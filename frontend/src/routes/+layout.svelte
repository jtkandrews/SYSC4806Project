<script lang="ts">
  import '../app.css';

  // Session state & helpers
  import { user, role, refreshSession, logout } from '$lib/session';
  import { cartItemCount } from '$lib/stores/cart';
  import { onMount } from 'svelte';

  // Refresh session at startup
  onMount(() => {
    refreshSession();
  });

  async function doLogout() {
    await logout();
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
        <span class="text-gray-700">Role: OWNER</span>

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

        <span class="text-gray-700">Role: USER</span>

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
