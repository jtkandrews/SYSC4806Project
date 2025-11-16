<script lang="ts">
  import { createBook } from '../lib/api';
  import { role } from '$lib/session';
  import { onMount } from 'svelte';

  export let data;
  let books = data.initialBooks || [];

  // Sorting and filtering
  let sortBy = '';
  let order = 'asc';
  let genreFilter = '';
  let minPrice = '';
  let maxPrice = '';

  async function fetchBooks() {
    let url = 'http://localhost:8080/api/books?';
    if (sortBy) url += `sortBy=${sortBy}&order=${order}&`;
    if (genreFilter) url += `genre=${genreFilter}&`;
    if (minPrice && maxPrice) url += `minPrice=${minPrice}&maxPrice=${maxPrice}&`;

    try {
      const res = await fetch(url);
      books = await res.json();
    } catch (err) {
      console.error('Error fetching books:', err);
    }
  }

  onMount(fetchBooks);

  let showAddBookModal = false;
  let isSubmitting = false;
  let addError = '';

  let formData = {
    title: '',
    author: '',
    isbn: '',
    description: '',
    genre: '',
    price: '',
    inventory: ''
  };

  function openAddBookModal() {
    showAddBookModal = true;
  }

  function closeAddBookModal() {
    showAddBookModal = false;
    resetForm();
    addError = '';
  }

  function resetForm() {
    formData = {
      title: '',
      author: '',
      isbn: '',
      description: '',
      genre: '',
      price: '',
      inventory: ''
    };
  }

  // @ts-ignore
  async function handleAddBook(e) {
    e.preventDefault();
    addError = '';

    if (!formData.title || !formData.author || !formData.isbn || !formData.price || !formData.inventory) {
      addError = 'Please fill in all required fields';
      return;
    }

    const isbnDigits = formData.isbn.replace(/\D/g, '');
    if (isbnDigits.length !== 13) {
      addError = 'ISBN must be exactly 13 digits';
      return;
    }

    const price = parseFloat(formData.price);
    if (price <= 0) {
      addError = 'Price must be greater than 0';
      return;
    }

    const inventory = parseInt(formData.inventory);

    isSubmitting = true;

    try {
      const bookToAdd = {
        isbn: isbnDigits,
        title: formData.title,
        author: formData.author,
        genre: formData.genre || undefined,
        price,
        inventory,
        imageUrl: `https://covers.openlibrary.org/b/isbn/${isbnDigits}-L.jpg`,
        description: formData.description || undefined
      };

      // @ts-ignore
      const newBook = await createBook(bookToAdd);

      books = [newBook, ...books];
      alert('Book added successfully!');
      closeAddBookModal();
    } catch (error) {
      console.error('Error adding book:', error);
      const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
      addError = `Failed to add book: ${errorMessage}`;
    } finally {
      isSubmitting = false;
    }
  }

  // @ts-ignore
  function handleModalBackdropClick(e) {
    if (e.target === e.currentTarget) {
      closeAddBookModal();
    }
  }

  // @ts-ignore
  function handleIsbnInput(e) {
    const input = e.target.value;
    formData.isbn = input.replace(/[^\d-]/g, '');
  }

  // @ts-ignore
  function handlePriceInput(e) {
    const input = e.target.value;
    let cleaned = input.replace(/[^\d.]/g, '');
    const parts = cleaned.split('.');
    if (parts[0].length > 4) {
      parts[0] = parts[0].slice(0, 4);
    }
    if (parts[1] && parts[1].length > 2) {
      parts[1] = parts[1].slice(0, 2);
    }
    const newValue = parts.join('.');
    formData.price = newValue;
  }

  // @ts-ignore
  function handleInventoryInput(e) {
    const input = e.target.value;
    const cleaned = input.replace(/\D/g, '').slice(0, 4);
    formData.inventory = cleaned;
  }
</script>

<div class="container">
  <div class="page-header">
    <div class="page-header-content">
      <div>
        <h1 class="page-title">Browse Our Collection</h1>
        <p class="page-subtitle">Discover great books at amazin prices</p>
      </div>

      {#if $role === 'OWNER'}
        <button class="btn btn-primary" on:click={openAddBookModal}>
          ➕ Add Book
        </button>
      {/if}
    </div>

    <!-- Sorting and Filtering UI -->
    <div class="filters" style="margin-top: 1rem; display: flex; gap: 0.5rem; flex-wrap: wrap;">
      <select bind:value={sortBy} on:change={fetchBooks}>
        <option value="">Sort By</option>
        <option value="title">Title</option>
        <option value="price">Price</option>
        <option value="inventory">Inventory</option>
      </select>

      <select bind:value={order} on:change={fetchBooks}>
        <option value="asc">Ascending</option>
        <option value="desc">Descending</option>
      </select>

      <input
        type="text"
        placeholder="Genre"
        bind:value={genreFilter}
        on:input={fetchBooks}
      />

      <input
        type="number"
        placeholder="Min Price"
        bind:value={minPrice}
        on:input={fetchBooks}
      />

      <input
        type="number"
        placeholder="Max Price"
        bind:value={maxPrice}
        on:input={fetchBooks}
      />
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
                {(+book.price).toLocaleString(undefined, {
                  style: 'currency',
                  currency: 'USD'
                })}
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

{#if $role === 'OWNER' && showAddBookModal}
  <div
    class="modal-backdrop"
    on:click={handleModalBackdropClick}
    on:keydown={(e) => e.key === 'Escape' && closeAddBookModal()}
    role="presentation"
    aria-labelledby="modal-title"
  >
    <div class="modal">
      <div class="modal-header">
        <h2 id="modal-title">Add New Book</h2>
        <button class="modal-close" on:click={closeAddBookModal} aria-label="Close dialog">
          ✕
        </button>
      </div>

      <form on:submit={handleAddBook} class="modal-form">
        {#if addError}
          <div class="error-message">{addError}</div>
        {/if}

        <div class="form-group">
          <label for="title"><span>Title *</span></label>
          <input type="text" id="title" bind:value={formData.title} required />
        </div>

        <div class="form-group">
          <label for="author"><span>Author *</span></label>
          <input type="text" id="author" bind:value={formData.author} required />
        </div>

        <div class="form-group">
          <label for="isbn"><span>ISBN *</span></label>
          <input
            type="text"
            id="isbn"
            bind:value={formData.isbn}
            on:input={handleIsbnInput}
            required
          />
        </div>

        <div class="form-group">
          <label for="genre"><span>Genre</span></label>
          <input type="text" id="genre" bind:value={formData.genre} />
        </div>

        <div class="form-group">
          <label for="description"><span>Description</span></label>
          <textarea id="description" bind:value={formData.description}></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="price"><span>Price *</span></label>
            <input
              type="number"
              id="price"
              bind:value={formData.price}
              on:input={handlePriceInput}
              min="0.01"
              step="0.01"
              required
            />
          </div>

          <div class="form-group">
            <label for="inventory"><span>Inventory *</span></label>
            <input
              type="number"
              id="inventory"
              bind:value={formData.inventory}
              on:input={handleInventoryInput}
              min="0"
              required
            />
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" on:click={closeAddBookModal}>
            Cancel
          </button>
          <button type="submit" class="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Adding...' : 'Add Book'}
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}
