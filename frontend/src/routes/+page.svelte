<script lang="ts">
  import { createBook } from '../lib/api';
  import { role } from '$lib/session';

  export let data;
  let books = data.initialBooks || [];

  // Sorting and filtering
  let sortField = '';
  let sortOrder = 'asc';
  let genre = '';
  let minPrice = '';
  let maxPrice = '';

  async function applyFilters() {
    let url = '/api/books?';
    if (sortField) url += `sort=${sortField}&order=${sortOrder}&`;
    if (genre) url += `genre=${encodeURIComponent(genre)}&`;
    if (minPrice) url += `minPrice=${minPrice}&`;
    if (maxPrice) url += `maxPrice=${maxPrice}&`;

    try {
      const res = await fetch(url);
      books = await res.json();
    } catch (err) {
      console.error('Error fetching books:', err);
    }
  }

  // Add book modal
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

    isSubmitting = true;

    try {
      const bookToAdd = {
        isbn: isbnDigits,
        title: formData.title,
        author: formData.author,
        genre: formData.genre || undefined,
        price: parseFloat(formData.price),
        inventory: parseInt(formData.inventory),
        imageUrl: `https://covers.openlibrary.org/b/isbn/${isbnDigits}-L.jpg`,
        description: formData.description || undefined
      };

      const newBook = await createBook(bookToAdd);
      books = [newBook, ...books];
      alert('Book added successfully!');
      closeAddBookModal();
    } catch (error) {
      console.error('Error adding book:', error);
      addError = 'Failed to add book.';
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
  </div>

  <!-- Filters -->
  <div class="filters">
    <select bind:value={sortField}>
      <option value="">Sort By</option>
      <option value="title">Title</option>
      <option value="price">Price</option>
      <option value="inventory">Inventory</option>
    </select>

    <select bind:value={sortOrder}>
      <option value="asc">Ascending</option>
      <option value="desc">Descending</option>
    </select>

    <!-- Genre dropdown -->
    <select bind:value={genre}>
      <option value="">All Genres</option>
      <option value="Fiction">Fiction</option>
      <option value="Non-fiction">Non-fiction</option>
      <option value="Romance">Romance</option>
      <option value="Mystery">Mystery</option>
      <option value="Thriller">Thriller</option>
      <option value="Fantasy">Fantasy</option>
      <option value="Science Fiction">Science Fiction</option>
      <option value="Biography">Biography</option>
      <option value="History">History</option>
      <option value="Self-Help">Self-Help</option>
    </select>

    <input type="number" placeholder="Min Price" bind:value={minPrice} />
    <input type="number" placeholder="Max Price" bind:value={maxPrice} />
    <button class="btn btn-secondary" on:click={applyFilters}>Apply</button>
  </div>

  {#if books.length === 0}
    <div class="empty-state">
      <h2>No books available</h2>
      <p>Check back soon for new arrivals!</p>
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
            <p class="book-card-price">
              {(+book.price).toLocaleString(undefined, { style: 'currency', currency: 'USD' })}
            </p>
            <p class="book-card-stock">
              {book.inventory > 0 ? `${book.inventory} copies available` : 'Out of stock'}
            </p>
            <button class="btn btn-primary" disabled={book.inventory === 0}>Add to Cart</button>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

<!-- Add Book Modal -->
{#if $role === 'OWNER' && showAddBookModal}
  <div class="modal-backdrop" on:click={(e) => e.target === e.currentTarget && closeAddBookModal()}>
    <div class="modal">
      <div class="modal-header">
        <h2>Add New Book</h2>
        <button class="modal-close" on:click={closeAddBookModal}>✕</button>
      </div>

      <form on:submit={handleAddBook} class="modal-form">
        {#if addError}
          <div class="error-message">{addError}</div>
        {/if}

        <div class="form-group">
          <label>Title *</label>
          <input type="text" bind:value={formData.title} required />
        </div>

        <div class="form-group">
          <label>Author *</label>
          <input type="text" bind:value={formData.author} required />
        </div>

        <div class="form-group">
          <label>ISBN *</label>
          <input type="text" bind:value={formData.isbn} required />
        </div>

        <div class="form-group">
          <label>Genre</label>
          <select bind:value={formData.genre}>
            <option value="">Select Genre</option>
            <option value="Fiction">Fiction</option>
            <option value="Non-fiction">Non-fiction</option>
            <option value="Romance">Romance</option>
            <option value="Mystery">Mystery</option>
            <option value="Thriller">Thriller</option>
            <option value="Fantasy">Fantasy</option>
            <option value="Science Fiction">Science Fiction</option>
            <option value="Biography">Biography</option>
            <option value="History">History</option>
            <option value="Self-Help">Self-Help</option>
          </select>
        </div>

        <div class="form-group">
          <label>Description</label>
          <textarea bind:value={formData.description}></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Price *</label>
            <input type="number" bind:value={formData.price} step="0.01" required />
          </div>

          <div class="form-group">
            <label>Inventory *</label>
            <input type="number" bind:value={formData.inventory} required />
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" on:click={closeAddBookModal}>Cancel</button>
          <button type="submit" class="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Adding...' : 'Add Book'}
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}
