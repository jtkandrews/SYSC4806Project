<script>
  import { onMount } from 'svelte';

  export let data;
  const books = data.initialBooks || [];

  onMount(() => {
    if (books.length > 0) {
      console.log('First book:', books[0]);
      console.log('Has imageUrl?', books[0].imageUrl);
      console.log('ImageUrl value:', books[0].imageUrl);
    }
  });
</script>

<div class="container">
  <div class="page-header">
    <h1 class="page-title">Browse Our Collection</h1>
    <p class="page-subtitle">Discover great books at amazin prices</p>
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