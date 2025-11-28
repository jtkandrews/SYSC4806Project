<script lang="ts">
  import { onMount } from 'svelte';
  import { derived } from 'svelte/store';
  import { role } from '$lib/session'; // 🔹 NEW: role store
  import { addToCart } from '$lib/stores/cart';
  import { booksStore } from '$lib/stores/books';
  import type { Book } from '$lib/types';

  export let data;

  // Predefined genre options
  const genreOptions = [
    'Fiction',
    'Non-Fiction',
    'Science Fiction',
    'Fantasy',
    'Mystery',
    'Thriller',
    'Romance',
    'Horror',
    'Biography',
    'History',
    'Self-Help',
    'Poetry',
    'Drama',
    'Adventure',
    'Children\'s',
    'Young Adult',
    'Graphic Novel',
    'Cookbook',
    'Travel',
    'Science',
    'Philosophy',
    'Religion',
    'Historical Fiction',
    'Business',
    'Other'
  ];

  let isEditModalOpen = false;
  let editFormData = {
    genres: [] as string[],
    description: '',
    price: 0,
    inventory: 0
  };
  let isSubmitting = false;
  let editError = '';
  let mouseDownTarget: EventTarget | null = null;
  let addToCartQuantity = 1;
  let addToCartError = '';
  let addToCartSuccess = '';

  // Compute the image URL from the ISBN
  $: imageUrl = `https://covers.openlibrary.org/b/isbn/${data.book.isbn}-L.jpg`;

  const bookFromStore = derived(booksStore, ($books) => $books.find((book) => book.isbn === data.book.isbn));
  let currentBook: Book = data.book;

  $: currentBook = $bookFromStore ?? data.book;

  onMount(() => {
    booksStore.update((current) => {
      const index = current.findIndex((book) => book.isbn === data.book.isbn);
      if (index === -1) {
        return [...current, data.book];
      }
      const updated = [...current];
      updated[index] = { ...updated[index], ...data.book };
      return updated;
    });
  });

  $: {
    const maxQuantity = Math.max(currentBook.inventory, 1);
    if (addToCartQuantity > maxQuantity) {
      addToCartQuantity = maxQuantity;
    }
    if (addToCartQuantity < 1) {
      addToCartQuantity = 1;
    }
  }

  function decreaseAddToCartQuantity() {
    if (addToCartQuantity > 1) {
      addToCartQuantity -= 1;
    }
  }

  function increaseAddToCartQuantity() {
    if (addToCartQuantity < currentBook.inventory) {
      addToCartQuantity += 1;
    }
  }

  function handleAddToCart() {
    addToCartError = '';
    addToCartSuccess = '';

    try {
      addToCart(currentBook, addToCartQuantity);
      const copyText = addToCartQuantity === 1 ? 'copy' : 'copies';
      addToCartSuccess = `Added ${addToCartQuantity} ${copyText} of "${currentBook.title}" to your cart.`;
    } catch (error) {
      addToCartError = error instanceof Error ? error.message : 'Unable to add book to cart.';
    }
  }

  // Reactive declaration to update form data when book data changes
  $: if (data.book) {
    if (!isEditModalOpen) {
      // Only reset form data when modal is closed to avoid interfering with editing
      editFormData = {
        genres: data.book.genre ? data.book.genre.split(', ').map((g: string) => g.trim()) : [],
        description: data.book.description || '',
        price: data.book.price,
        inventory: data.book.inventory
      };
    }
  }

  function handleEditGenreToggle(genre: string) {
    if (editFormData.genres.includes(genre)) {
      editFormData.genres = editFormData.genres.filter(g => g !== genre);
    } else {
      editFormData.genres = [...editFormData.genres, genre];
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

    // Validate description length
    if (editFormData.description.length > 500) {
      editError = 'Description cannot exceed 500 characters';
      isSubmitting = false;
      return;
    }

    // Validate price is greater than 0
    if (editFormData.price <= 0) {
      editError = 'Price must be greater than 0';
      isSubmitting = false;
      return;
    }

    // Validate price integer part is 4 digits max (before decimal)
    const priceString = editFormData.price.toString();
    const priceParts = priceString.split('.');
    if (priceParts[0].length > 4) {
      editError = 'Price cannot exceed 4 digits (max 9999.99)';
      isSubmitting = false;
      return;
    }

    // Validate inventory is 4 digits max
    const inventoryString = editFormData.inventory.toString();
    if (inventoryString.length > 4) {
      editError = 'Inventory cannot exceed 4 digits (max 9999)';
      isSubmitting = false;
      return;
    }

    try {
      const response = await fetch(`/api/owner/books/${data.book.isbn}`, { // 🔹 changed path to owner
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include', // 🔹 include cookie for owner auth
        body: JSON.stringify({
          isbn: data.book.isbn,
          title: data.book.title,
          author: data.book.author,
          publisher: data.book.publisher,
          genre: editFormData.genres.length > 0 ? editFormData.genres.join(', ') : null,
          description: editFormData.description,
          imageUrl: imageUrl,
          price: editFormData.price,
          inventory: editFormData.inventory
        })
      });

      if (!response.ok) {
        throw new Error('Failed to update book');
      }

      // Update the book data and trigger reactivity
      data.book = await response.json();
      booksStore.update((current) =>
        current.map((book) => (book.isbn === data.book.isbn ? data.book : book))
      );

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
      genres: data.book.genre ? data.book.genre.split(', ').map((g: string) => g.trim()) : [],
      description: data.book.description || '',
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

  // @ts-ignore
  function handleEditPriceInput(e) {
    // Limit to 4 digits before decimal point (e.g., 9999.99)
    const input = e.target.value;

    // Allow empty input - don't update editFormData
    if (input === '' || input === null) {
      return;
    }

    // Remove non-numeric except decimal point
    let cleaned = input.replace(/[^\d.]/g, '');
    // Split by decimal point
    const parts = cleaned.split('.');
    // Limit integer part to 4 digits and decimal part to 2 digits
    if (parts[0].length > 4) {
      parts[0] = parts[0].slice(0, 4);
    }
    if (parts[1] && parts[1].length > 2) {
      parts[1] = parts[1].slice(0, 2);
    }

    // Only update if we have valid digits
    if (parts[0] !== '') {
      const newValue = parseFloat(parts.join('.'));
      if (!isNaN(newValue)) {
        editFormData.price = newValue;
      }
    }
  }

  // @ts-ignore
  function handleEditInventoryInput(e) {
    // Limit to 4 digits (max 9999)
    const input = e.target.value;

    // Allow empty input - don't update editFormData
    if (input === '' || input === null) {
      return;
    }

    const cleaned = input.replace(/\D/g, '').slice(0, 4);

    // Only update if we have valid digits
    if (cleaned !== '') {
      const newValue = parseInt(cleaned);
      if (!isNaN(newValue)) {
        editFormData.inventory = newValue;
      }
    }
  }
</script>

<div class="book-detail">
  <div class="header-controls">
    <a href="/" class="back-link">
      <span>←</span>
      <span>Back to Books</span>
    </a>

    {#if $role === 'OWNER'}  <!-- 🔹 only owners see Edit button -->
      <button class="btn btn-secondary" on:click={openEditModal}>
        ✎ Edit
      </button>
    {/if}
  </div>

  <div class="detail-header">
    <h1 class="detail-title">{currentBook.title}</h1>
    <p class="detail-author">by {currentBook.author}</p>
    {#if currentBook.publisher}
      <p class="detail-meta">Published by {currentBook.publisher}</p>
    {/if}
  </div>

  <div class="detail-content">
    <div class="detail-image-section">
      {#if currentBook.imageUrl}
        <img src={currentBook.imageUrl} alt="{currentBook.title} cover" class="detail-image" />
      {:else}
        <div class="detail-image-placeholder">
          <span>📚</span>
        </div>
      {/if}
    </div>
    <div class="detail-text-section">
      {#if currentBook.genre}
        <p class="detail-info-item">
          <strong>Genre:</strong> {currentBook.genre}
        </p>
      {/if}
      {#if currentBook.description}
        <p class="detail-description">{currentBook.description}</p>
      {:else}
        <p class="detail-description" style="color: var(--text-secondary); font-style: italic;">
          No description available for this book.
        </p>
      {/if}
    </div>

    <div class="detail-price-section">
      <div class="detail-price-info">
        <p class="detail-price">
          {currentBook.price.toLocaleString(undefined, {style: 'currency', currency: 'USD'})}
        </p>
        <div class="detail-info">
          <p class="detail-info-item">
            <strong>ISBN:</strong> {currentBook.isbn}
          </p>
          <p class="detail-info-item">
            <strong>Stock:</strong>
            {#if currentBook.inventory > 0}
              <span style="color: #10b981;">{currentBook.inventory} {currentBook.inventory === 1 ? 'copy' : 'copies'} available</span>
            {:else}
              <span style="color: #ef4444;">Out of stock</span>
            {/if}
          </p>
        </div>
      </div>
      {#if $role !== 'OWNER'}
        {#if addToCartError}
          <div class="cart-feedback error">{addToCartError}</div>
        {/if}
        {#if addToCartSuccess}
          <div class="cart-feedback success">{addToCartSuccess}</div>
        {/if}
        <div class="detail-cart-actions">
          <div class="detail-quantity">
            <span class="detail-quantity-label">Quantity</span>
            <div class="detail-quantity-controls">
              <button
                class="detail-quantity-button"
                on:click={decreaseAddToCartQuantity}
                aria-label="Decrease quantity"
                type="button"
              >
                −
              </button>
              <input
                type="number"
                min="1"
                max={Math.max(currentBook.inventory, 1)}
                bind:value={addToCartQuantity}
                class="detail-quantity-input"
              />
              <button
                class="detail-quantity-button"
                on:click={increaseAddToCartQuantity}
                aria-label="Increase quantity"
                type="button"
                disabled={addToCartQuantity >= currentBook.inventory}
              >
                +
              </button>
            </div>
          </div>
          <button
            class="btn btn-primary btn-add-cart-large"
            disabled={currentBook.inventory === 0}
            on:click|preventDefault={handleAddToCart}
          >
            Add to Cart
          </button>
        </div>
      {/if}
    </div>
  </div>
</div>

{#if $role === 'OWNER' && isEditModalOpen}  <!-- 🔹 owner-only edit modal -->
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
            <label>
              <span>Genres (Select all that apply)</span>
            </label>
            <div class="checkbox-grid">
              {#each genreOptions as genre}
                <label class="checkbox-label">
                  <input
                    type="checkbox"
                    value={genre}
                    checked={editFormData.genres.includes(genre)}
                    on:change={() => handleEditGenreToggle(genre)}
                  />
                  <span>{genre}</span>
                </label>
              {/each}
            </div>
          </div>

          <div class="form-group">
            <label for="description">
              <span>Description</span>
            </label>
            <textarea
              id="description"
              bind:value={editFormData.description}
              maxlength="500"
              placeholder="Enter a description of the book"
              rows="5"
            ></textarea>
          </div>

          <div class="form-group">
            <label for="imageUrl">Image URL</label>
            <input
              type="text"
              id="imageUrl"
              value={imageUrl}
              disabled
              readonly
            />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="price">
                <span>Price</span>
              </label>
              <input
                type="number"
                id="price"
                bind:value={editFormData.price}
                on:input={handleEditPriceInput}
                step="0.01"
                min="0.01"
                required
              />
            </div>

            <div class="form-group">
              <label for="inventory">
                <span>Inventory</span>
              </label>
              <input
                type="number"
                id="inventory"
                bind:value={editFormData.inventory}
                on:input={handleEditInventoryInput}
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

<style>
  .detail-cart-actions {
    display: flex;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
  }

  .detail-quantity {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .detail-quantity-label {
    font-weight: 600;
    color: var(--text-primary);
  }

  .detail-quantity-controls {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
  }

  .detail-quantity-button {
    width: 2.25rem;
    height: 2.25rem;
    border-radius: 9999px;
    border: none;
    background: #e5e7eb;
    color: #111827;
    font-size: 1.25rem;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: background 0.2s ease;
  }

  .detail-quantity-button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .detail-quantity-button:not(:disabled):hover {
    background: #d1d5db;
  }

  .detail-quantity-input {
    width: 4rem;
    text-align: center;
    padding: 0.25rem 0.5rem;
    border: 1px solid #d1d5db;
    border-radius: 0.375rem;
  }

  .cart-feedback {
    padding: 0.75rem 1rem;
    border-radius: 0.75rem;
    font-size: 0.9375rem;
    margin-bottom: 0.75rem;
  }

  .cart-feedback.success {
    background: #dcfce7;
    color: #166534;
    border: 1px solid #bbf7d0;
  }

  .cart-feedback.error {
    background: #fee2e2;
    color: #991b1b;
    border: 1px solid #fecaca;
  }
</style>