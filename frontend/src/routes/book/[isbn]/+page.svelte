<script>
  export let data;
  const b = data.book;
</script>

<div class="book-detail">
  <a href="/" class="back-link">
    <span>←</span>
    <span>Back to Books</span>
  </a>

  <div class="detail-header">
    <h1 class="detail-title">{b.title}</h1>
    <p class="detail-author">by {b.author}</p>
    {#if b.publisher}
      <p class="detail-meta">Published by {b.publisher}</p>
    {/if}
  </div>

  <div class="detail-content">
    <div class="detail-image-section">
      {#if b.imageUrl}
        <img src={b.imageUrl} alt="{b.title} cover" class="detail-image" />
      {:else}
        <div class="detail-image-placeholder">
          <span>📚</span>
        </div>
      {/if}
    </div>
    <div class="detail-text-section">
      {#if b.genre}
        <p class="detail-info-item">
          <strong>Genre:</strong> {b.genre}
        </p>
      {/if}
      {#if b.description}
        <p class="detail-description">{b.description}</p>
      {:else}
        <p class="detail-description" style="color: var(--text-secondary); font-style: italic;">
          No description available for this book.
        </p>
      {/if}
    </div>

    <div class="detail-price-section">
      <div class="detail-price-info">
        <p class="detail-price">
          {b.price.toLocaleString(undefined, {style: 'currency', currency: 'USD'})}
        </p>
        <div class="detail-info">
          <p class="detail-info-item">
            <strong>ISBN:</strong> {b.isbn}
          </p>
          <p class="detail-info-item">
            <strong>Stock:</strong> 
            {#if b.inventory > 0}
              <span style="color: #10b981;">{b.inventory} {b.inventory === 1 ? 'copy' : 'copies'} available</span>
            {:else}
              <span style="color: #ef4444;">Out of stock</span>
            {/if}
          </p>
        </div>
      </div>
      <button 
        class="btn btn-primary btn-add-cart-large" 
        disabled={b.inventory === 0}
        on:click|preventDefault={() => {
          // TODO: Add to cart functionality
        }}
      >
        Add to Cart
      </button>
    </div>
  </div>
</div>