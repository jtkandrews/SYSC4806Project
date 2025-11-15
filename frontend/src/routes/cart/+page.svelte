<script lang="ts">
  import { role } from '$lib/session';
  import { booksStore } from '$lib/stores/books';
  import { cartStore, cartItemCount, cartTotalCost, checkout, removeFromCart } from '$lib/stores/cart';
  import { derived } from 'svelte/store';

  const cartWithInventory = derived([cartStore, booksStore], ([$cart, $books]) =>
    $cart.map((item) => {
      const currentBook = $books.find((book) => book.isbn === item.isbn);
      return {
        ...item,
        available: currentBook?.inventory ?? 0
      };
    })
  );

  let errorMessage = '';
  let successMessage = '';
  let isCheckingOut = false;

  function handleCheckout() {
    if ($role !== 'USER') {
      errorMessage = 'Only regular users can check out.';
      return;
    }

    errorMessage = '';
    successMessage = '';
    isCheckingOut = true;

    try {
      checkout();
      successMessage = 'Checkout complete! Your books are on the way.';
    } catch (error) {
      errorMessage = error instanceof Error ? error.message : 'Checkout failed.';
    } finally {
      isCheckingOut = false;
    }
  }
</script>

<div class="cart-container">
  <h1 class="cart-title">Your Cart</h1>

  {#if $role !== 'USER'}
    <p class="cart-guard">Only the regular user role can access the checkout experience.</p>
  {/if}

  {#if errorMessage}
    <div class="cart-alert error">{errorMessage}</div>
  {/if}

  {#if successMessage}
    <div class="cart-alert success">{successMessage}</div>
  {/if}

  {#if $cartItemCount === 0}
    <div class="empty-cart">
      <p>Your cart is empty. Go back to the <a href="/" class="link">catalog</a> to add some books!</p>
    </div>
  {:else}
    <div class="cart-items">
      {#each $cartWithInventory as item}
        <div class="cart-item">
          <div class="cart-item-cover">
            {#if item.imageUrl}
              <img src={item.imageUrl} alt={`Cover for ${item.title}`} />
            {:else}
              <span class="cart-item-placeholder">📚</span>
            {/if}
          </div>
          <div class="cart-item-details">
            <h2>{item.title}</h2>
            <p>Quantity: {item.quantity}</p>
            <p>
              Item total:
              {(item.quantity * item.price).toLocaleString(undefined, { style: 'currency', currency: 'USD' })}
            </p>
            <p class="availability">{item.available} remaining in stock</p>
            <button class="btn btn-secondary" on:click={() => removeFromCart(item.isbn)}>Remove</button>
          </div>
        </div>
      {/each}
    </div>

    <div class="cart-summary">
      <p>Total items: {$cartItemCount}</p>
      <p>
        Order total:
        {$cartTotalCost.toLocaleString(undefined, { style: 'currency', currency: 'USD' })}
      </p>

      <button class="btn btn-primary" disabled={$role !== 'USER' || isCheckingOut} on:click={handleCheckout}>
        {isCheckingOut ? 'Processing...' : 'Checkout'}
      </button>
    </div>
  {/if}
</div>

<style>
  .cart-container {
    max-width: 960px;
    margin: 0 auto;
    padding: 2rem 1.5rem;
  }

  .cart-title {
    font-size: 2rem;
    font-weight: 700;
    margin-bottom: 1.5rem;
  }

  .cart-guard {
    background: #f3f4f6;
    border-left: 4px solid #2563eb;
    padding: 1rem;
    border-radius: 0.5rem;
    margin-bottom: 1.5rem;
    color: #1f2937;
  }

  .cart-alert {
    padding: 0.75rem 1rem;
    border-radius: 0.5rem;
    margin-bottom: 1rem;
  }

  .cart-alert.error {
    background: #fee2e2;
    color: #991b1b;
    border: 1px solid #fecaca;
  }

  .cart-alert.success {
    background: #dcfce7;
    color: #166534;
    border: 1px solid #bbf7d0;
  }

  .empty-cart {
    background: #ffffff;
    border-radius: 0.75rem;
    padding: 2rem;
    text-align: center;
    box-shadow: 0 10px 30px -20px rgba(15, 23, 42, 0.2);
  }

  .cart-items {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-bottom: 2rem;
  }

  .cart-item {
    display: flex;
    gap: 1rem;
    background: #ffffff;
    border-radius: 0.75rem;
    box-shadow: 0 10px 30px -20px rgba(15, 23, 42, 0.2);
    overflow: hidden;
  }

  .cart-item-cover {
    width: 140px;
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f9fafb;
  }

  .cart-item-cover img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .cart-item-placeholder {
    font-size: 2rem;
  }

  .cart-item-details {
    padding: 1.5rem;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .cart-item-details h2 {
    font-size: 1.25rem;
    font-weight: 600;
  }

  .availability {
    color: #4b5563;
    font-size: 0.875rem;
  }

  .cart-summary {
    background: #ffffff;
    border-radius: 0.75rem;
    padding: 1.5rem;
    box-shadow: 0 10px 30px -20px rgba(15, 23, 42, 0.2);
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .link {
    color: #2563eb;
    text-decoration: underline;
  }

  @media (max-width: 640px) {
    .cart-item {
      flex-direction: column;
    }

    .cart-item-cover {
      width: 100%;
      height: 240px;
    }
  }
</style>