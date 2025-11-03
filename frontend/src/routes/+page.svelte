<script lang="ts">
  import { goto } from '$app/navigation';
  import { page } from '$app/stores';
  import { onMount } from 'svelte';
  import type { PageData } from './$types';

  export let data: PageData;

  // initialize local state
  let sortBy: 'title' | 'price' | 'inventory' = data.sortBy ?? 'title';
  let order: 'asc' | 'desc' = data.order ?? 'asc';
  let books = data.initialBooks ?? [];

  // sync data from store when navigation happens
  $: {
    const { sortBy: sb, order: ord, initialBooks } = $page.data;
    if (sb && sb !== sortBy) sortBy = sb;
    if (ord && ord !== order) order = ord;
    if (initialBooks) books = initialBooks;
  }

  // handle dropdown changes — note NO replaceState
  function changeSort() {
    const newUrl = `?sortBy=${sortBy}&order=${order}`;
    // push to history so load() runs and $page updates
    goto(newUrl, { keepFocus: true, noScroll: true });
  }

  onMount(() => {
    console.log('Books loaded:', books);
  });
</script>

<div class="container">
  <div class="page-header">
    <h1 class="page-title">Browse Our Collection</h1>
    <p class="page-subtitle">Discover great books at amazin prices</p>

    <!-- Sorting controls -->
    <div class="sort-controls">
      <div class="sort-field">
        <label for="sortBy">Sort by:</label>
        <select id="sortBy" bind:value={sortBy} on:change={changeSort}>
          <option value="title">Title</option>
          <option value="price">Price</option>
          <option value="inventory">Inventory</option>
        </select>
      </div>

      <div class="sort-order">
        <label for="order">Order:</label>
        <select id="order" bind:value={order} on:change={changeSort}>
          <option value="asc">Ascending ↑</option>
          <option value="desc">Descending ↓</option>
        </select>
      </div>
    </div>

  </div>


  {#if books.length === 0}
    <div class="empty-state">
      <h2 class="empty-state-title">No books available</h2>
      <p class="empty-state-text">Check back soon for new arrivals!</p>
    </div>
  {:else}
    <div class="books-grid">
      {#each books as book}
        <div class="book-card">
          <a href={`/book/${book.isbn}`} class="book-card-link">
            {#if book.imageUrl?.trim()}
              <img
                src={book.imageUrl}
                alt={`${book.title} cover`}
                class="book-card-image"
                loading="lazy"
              />
            {:else}
              <div class="book-card-image-placeholder">
                <span>📚</span>
              </div>
            {/if}
            <h3 class="book-card-title">{book.title}</h3>
            <p class="book-card-author">by {book.author}</p>
          </a>

          <div class="book-card-footer">
            <div class="book-card-pricing">
              <p class="book-card-price">
                {(+book.price).toLocaleString(undefined, { style: 'currency', currency: 'USD' })}
              </p>
              {#if book.inventory > 0}
                <p class="book-card-stock">
                  {book.inventory} {book.inventory === 1 ? 'copy' : 'copies'} available
                </p>
              {:else}
                <p class="book-card-stock" style="color: #ef4444;">
                  Out of stock
                </p>
              {/if}
            </div>

            <button
              class="btn btn-primary btn-add-cart"
              disabled={book.inventory === 0}
              on:click|stopPropagation|preventDefault={() => {
                // TODO: Add to cart functionality
              }}
            >
              Add to Cart
            </button>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>