<script lang="ts">
  import '../app.css';

  // Session state & helpers
  import { user, role, refreshSession, logout, type SessionUser } from '$lib/session';
  import { cartItemCount, loadCart, resetCart } from '$lib/stores/cart';
  import { get } from 'svelte/store';
  import { onMount } from 'svelte';

  export let data;

  // Seed the client store from server-loaded session data
  onMount(() => {
    const sessionUser: SessionUser | null = data?.session
            ? { ...data.session, role: data.session.role === 'OWNER' ? 'OWNER' : 'USER' }
            : null;

    user.set(sessionUser);

    const loadUserCart = async () => {
      const currentUser = get(user);
      if (currentUser?.role === 'USER') {
        try {
          await loadCart();
        } catch (err) {
          console.error('Failed to load cart', err);
        }
      } else {
        resetCart();
      }
    };
    if (!data?.session) {
      refreshSession().then(loadUserCart);
    } else {
      loadUserCart();
    }
  });

  async function doLogout() {
    await logout();
    resetCart();
    window.location.href = '/login';
  }
</script>

{#if data?.showNav}
  <header>
    <nav class="flex justify-between items-center px-6 py-4 bg-white shadow">
      <a href="/" class="logo" aria-label="Amazin Bookstore home">
        <img src="/amazin.png" alt="Amazin Bookstore logo" class="logo-image" width="140" />
      </a>

      <div class="flex items-center gap-3 text-sm">
        <a href="/" class="btn btn-secondary">Browse</a>

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
          <a href="/cart" class="btn btn-secondary">
            Cart ({$cartItemCount})
          </a>


          <a href="/orders" class="btn btn-secondary">Orders</a>

          <span class="btn btn-secondary role-chip">Role: USER ({$user?.username || 'guest'})</span>

          <button class="btn btn-primary" on:click={doLogout}>
            Logout
          </button>
        {/if}
      </div>
    </nav>
  </header>
{/if}

<main class="min-h-[calc(100vh-80px)] bg-gray-50">
  <slot />
</main>

<style>
  .nav-actions {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 0.95rem;
  }

  .role-chip {
    cursor: default;
    font-weight: 600;
  }
  main {
    min-height: calc(100vh - 80px);
  }
</style>
