<script lang="ts">
  export let data;

  let isEditModalOpen = false;
  let editFormData = {
    genre: data.book.genre || '',
    description: data.book.description || '',
    imageUrl: data.book.imageUrl || '',
    price: data.book.price,
    inventory: data.book.inventory
  };
  let isSubmitting = false;
  let editError = '';
  let mouseDownTarget: EventTarget | null = null;

  // Reactive declaration to update form data when book data changes
  $: if (data.book) {
    if (!isEditModalOpen) {
      // Only reset form data when modal is closed to avoid interfering with editing
      editFormData = {
        genre: data.book.genre || '',
        description: data.book.description || '',
        imageUrl: data.book.imageUrl || '',
        price: data.book.price,
        inventory: data.book.inventory
      };
    }
  }

  function handleOverlayMouseDown(e: MouseEvent) {
    mouseDownTarget = e.target;
  }

  function handleOverlayClick(e: MouseEvent) {
    // Only close if the click started and ended on the overlay itself (not a drag)
    if (e.target === mouseDownTarget && e.currentTarget === e.target) {
      closeEditModal();
    }
  }

  async function handleSaveEdit() {
    isSubmitting = true;
    editError = '';

    try {
      const response = await fetch(`/api/books/${data.book.isbn}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          isbn: data.book.isbn,
          title: data.book.title,
          author: data.book.author,
          publisher: data.book.publisher,
          genre: editFormData.genre,
          description: editFormData.description,
          imageUrl: editFormData.imageUrl,
          price: editFormData.price,
          inventory: editFormData.inventory
        })
      });

      if (!response.ok) {
        throw new Error('Failed to update book');
      }

      const updatedBook = await response.json();

      // Update the book data and trigger reactivity
      data.book = updatedBook;

      // Close modal
      isEditModalOpen = false;
    } catch (error) {
      editError = error instanceof Error ? error.message : 'An error occurred';
      console.error('Error updating book:', error);
    } finally {
      isSubmitting = false;
    }
  }

  function openEditModal() {
    editFormData = {
      genre: data.book.genre || '',
      description: data.book.description || '',
      imageUrl: data.book.imageUrl || '',
      price: data.book.price,
      inventory: data.book.inventory
    };
    editError = '';
    isEditModalOpen = true;
  }

  function closeEditModal() {
    isEditModalOpen = false;
    editError = '';
  }
</script>

<div class="book-detail">
  <div class="header-controls">
    <a href="/" class="back-link">
      <span>←</span>
      <span>Back to Books</span>
    </a>
    <button class="btn btn-secondary" on:click={openEditModal}>
      ✎ Edit
    </button>
  </div>

  <div class="detail-header">
    <h1 class="detail-title">{data.book.title}</h1>
    <p class="detail-author">by {data.book.author}</p>
    {#if data.book.publisher}
      <p class="detail-meta">Published by {data.book.publisher}</p>
    {/if}
  </div>

  <div class="detail-content">
    <div class="detail-image-section">
      {#if data.book.imageUrl}
        <img src={data.book.imageUrl} alt="{data.book.title} cover" class="detail-image" />
      {:else}
        <div class="detail-image-placeholder">
          <span>📚</span>
        </div>
      {/if}
    </div>
    <div class="detail-text-section">
      {#if data.book.genre}
        <p class="detail-info-item">
          <strong>Genre:</strong> {data.book.genre}
        </p>
      {/if}
      {#if data.book.description}
        <p class="detail-description">{data.book.description}</p>
      {:else}
        <p class="detail-description" style="color: var(--text-secondary); font-style: italic;">
          No description available for this book.
        </p>
      {/if}
    </div>

    <div class="detail-price-section">
      <div class="detail-price-info">
        <p class="detail-price">
          {data.book.price.toLocaleString(undefined, {style: 'currency', currency: 'USD'})}
        </p>
        <div class="detail-info">
          <p class="detail-info-item">
            <strong>ISBN:</strong> {data.book.isbn}
          </p>
          <p class="detail-info-item">
            <strong>Stock:</strong>
            {#if data.book.inventory > 0}
              <span style="color: #10b981;">{data.book.inventory} {data.book.inventory === 1 ? 'copy' : 'copies'} available</span>
            {:else}
              <span style="color: #ef4444;">Out of stock</span>
            {/if}
          </p>
        </div>
      </div>
      <button
        class="btn btn-primary btn-add-cart-large"
        disabled={data.book.inventory === 0}
        on:click|preventDefault={() => {
          // TODO: Add to cart functionality
        }}
      >
        Add to Cart
      </button>
    </div>
  </div>
</div>

{#if isEditModalOpen}
  <!-- svelte-ignore a11y-no-noninteractive-element-interactions a11y-click-events-have-key-events -->
  <div
    class="modal-overlay"
    role="dialog"
    aria-modal="true"
    on:mousedown={handleOverlayMouseDown}
    on:click={handleOverlayClick}
    on:keydown={(e) => e.key === 'Escape' && closeEditModal()}
  >
    <!-- svelte-ignore a11y-no-noninteractive-element-interactions a11y-click-events-have-key-events -->
    <div class="modal-content" role="document" on:click|stopPropagation>
      <div class="modal-header">
        <h2>Edit Book</h2>
        <button class="modal-close" on:click={closeEditModal}>✕</button>
      </div>

      <div class="modal-body">
        {#if editError}
          <div class="error-message">{editError}</div>
        {/if}

        <form on:submit|preventDefault={handleSaveEdit}>
          <div class="form-group">
            <label for="genre">Genre</label>
            <input
              type="text"
              id="genre"
              bind:value={editFormData.genre}
              placeholder="e.g., Fiction, Non-Fiction, Science Fiction"
            />
          </div>

          <div class="form-group">
            <label for="description">Description</label>
            <textarea
              id="description"
              bind:value={editFormData.description}
              placeholder="Enter a description of the book"
              rows="5"
            ></textarea>
          </div>

          <div class="form-group">
            <label for="imageUrl">Image URL</label>
            <input
              type="url"
              id="imageUrl"
              bind:value={editFormData.imageUrl}
              placeholder="https://example.com/image.jpg"
            />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="price">Price</label>
              <input
                type="number"
                id="price"
                bind:value={editFormData.price}
                step="0.01"
                min="0.01"
                required
              />
            </div>

            <div class="form-group">
              <label for="inventory">Inventory</label>
              <input
                type="number"
                id="inventory"
                bind:value={editFormData.inventory}
                step="1"
                min="0"
                required
              />
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" on:click={closeEditModal} disabled={isSubmitting}>
              Cancel
            </button>
            <button type="submit" class="btn btn-primary" disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
{/if}
