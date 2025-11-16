<script lang="ts">
  import { createBook } from '../lib/api';
  import { role } from '$lib/session';

  export let data;
  let books = data.initialBooks || [];

  // Added filtering and sorting logic
  let selectedGenre = '';
  let selectedSort = '';
  let selectedOrder = 'asc';

  // Fetch filtered and sorted books
  async function fetchBooks() {
    try {
      let url = '/api/books?';
      if (selectedSort) url += `sortBy=${selectedSort}&order=${selectedOrder}&`;
      if (selectedGenre) url += `genre=${encodeURIComponent(selectedGenre)}`;
      const res = await fetch(url);
      if (!res.ok) throw new Error('Failed to fetch books');
      books = await res.json();
    } catch (err) {
      console.error(err);
    }
  }

  // Automatically re-fetch when filters change
  $: fetchBooks();

  // Modal and form logic
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

      const newBook = await createBook(bookToAdd);

      books = [newBook, ...books];
      alert('Book added successfully!');
      closeAddBookModal();
    } catch (error) {
      console.error('Error adding book:', error);
      addError = 'Failed to add book';
    } finally {
      isSubmitting = false;
    }
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

    <!-- Filters Section -->
    <div class="filters">
      <label>Sort by:</label>
      <select bind:value={selectedSort}>
        <option value="">None</option>
        <option value="title">Title</option>
        <option value="price">Price</option>
        <option value="inventory">Inventory</option>
      </select>

      <label>Order:</label>
      <select bind:value={selectedOrder}>
        <option value="asc">Ascending</option>
        <option value="desc">Descending</option>
      </select>

      <label>Genre:</label>
      <select bind:value={selectedGenre}>
        <option value="">All</option>
        <option value="Fiction">Fiction</option>
        <option value="Non-Fiction">Non-Fiction</option>
        <option value="Science">Science</option>
        <option value="Romance">Romance</option>
        <option value="Mystery">Mystery</option>
      </select>
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
              <img src={book.imageUrl} alt={`${book.title} cover`} class="book-card-image" loading="lazy" />
            {:else}
              <div class="book-card-image-placeholder"><span>📚</span></div>
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
                <p class="book-card-stock" style="color: #ef4444;">Out of stock</p>
              {/if}
            </div>

            <button
              class="btn btn-primary btn-add-cart"
              disabled={book.inventory === 0}
              on:click|stopPropagation|preventDefault={() => {}}
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
    on:click={(e) => e.target === e.currentTarget && closeAddBookModal()}
    on:keydown={(e) => e.key === 'Escape' && closeAddBookModal()}
    role="presentation"
    aria-labelledby="modal-title"
  >
    <div class="modal">
      <div class="modal-header">
        <h2 id="modal-title">Add New Book</h2>
        <button class="modal-close" on:click={closeAddBookModal} aria-label="Close dialog">✕</button>
      </div>

      <form on:submit={handleAddBook} class="modal-form">
        {#if addError}
          <div class="error-message">{addError}</div>
        {/if}

        <div class="form-group">
          <label>Title *</label>
          <input type="text" bind:value={formData.title} placeholder="Enter book title" required />
        </div>

        <div class="form-group">
          <label>Author *</label>
          <input type="text" bind:value={formData.author} placeholder="Enter author name" required />
        </div>

        <div class="form-group">
          <label>ISBN *</label>
          <input type="text" bind:value={formData.isbn} placeholder="Enter ISBN (13 digits)" required />
        </div>

        <div class="form-group">
          <label>Genre</label>
          <input type="text" bind:value={formData.genre} placeholder="Enter book genre" />
        </div>

        <div class="form-group">
          <label>Description</label>
          <textarea bind:value={formData.description} placeholder="Enter book description" rows="4"></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Price (USD) *</label>
            <input type="number" bind:value={formData.price} placeholder="0.00" step="0.01" min="0.01" required />
          </div>

          <div class="form-group">
            <label>Inventory *</label>
            <input type="number" bind:value={formData.inventory} placeholder="0" min="0" required />
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" on:click={closeAddBookModal} disabled={isSubmitting}>
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
