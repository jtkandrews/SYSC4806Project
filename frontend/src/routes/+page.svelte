<script lang="ts">
  import { createBook } from '../lib/api';
  import { role } from '$lib/session';

  export let data;
  let books = data.initialBooks || [];

  // Sorting and filtering
  let sortBy = 'title';
  let order = 'asc';
  let genre = '';
  let minPrice = '';
  let maxPrice = '';

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

  // Fetch filtered/sorted books
  async function fetchBooks() {
    try {
      let url = `/api/books?sortBy=${sortBy}&order=${order}`;
      if (genre) url += `&genre=${genre}`;
      if (minPrice) url += `&minPrice=${minPrice}`;
      if (maxPrice) url += `&maxPrice=${maxPrice}`;

      const res = await fetch(url);
      books = await res.json();
    } catch (err) {
      console.error('Error fetching books:', err);
    }
  }

  // Handle sorting and filtering
  $: fetchBooks();

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
    if (price <= 0) {
      addError = 'Price must be greater than 0';
      return;
    }

    const inventory = parseInt(formData.inventory);
    if (inventory < 0) {
      addError = 'Inventory must be 0 or greater';
      return;
    }

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
      addError = `Failed to add book: ${error instanceof Error ? error.message : 'Unknown error'}`;
    } finally {
      isSubmitting = false;
    }
  }

  function handleModalBackdropClick(e) {
    if (e.target === e.currentTarget) closeAddBookModal();
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

  <!-- Sorting and Filtering Controls -->
  <div class="filters">
    <select bind:value={sortBy}>
      <option value="title">Sort By: Title</option>
      <option value="price">Sort By: Price</option>
      <option value="inventory">Sort By: Inventory</option>
    </select>

    <select bind:value={order}>
      <option value="asc">Ascending</option>
      <option value="desc">Descending</option>
    </select>

    <!-- Genre dropdown -->
    <select bind:value={genre}>
      <option value="">All Genres</option>
      <option value="Fiction">Fiction</option>
      <option value="Non-Fiction">Non-Fiction</option>
      <option value="Science Fiction">Science Fiction</option>
      <option value="Fantasy">Fantasy</option>
      <option value="Romance">Romance</option>
      <option value="Mystery">Mystery</option>
      <option value="Horror">Horror</option>
      <option value="Biography">Biography</option>
    </select>

    <input type="number" placeholder="Min Price" bind:value={minPrice} min="0" />
    <input type="number" placeholder="Max Price" bind:value={maxPrice} min="0" />
  </div>

  <!-- Book Display -->
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
            <div class="book-card-pricing">
              <p class="book-card-price">
                {(+book.price).toLocaleString(undefined, { style: 'currency', currency: 'USD' })}
              </p>
              {#if book.inventory > 0}
                <p class="book-card-stock">{book.inventory} {book.inventory === 1 ? 'copy' : 'copies'} available</p>
              {:else}
                <p class="book-card-stock" style="color: #ef4444;">Out of stock</p>
              {/if}
            </div>
            <button class="btn btn-primary btn-add-cart" disabled={book.inventory === 0}>
              Add to Cart
            </button>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .filters {
    display: flex;
    gap: 8px;
    margin-bottom: 20px;
  }
  select, input {
    padding: 6px;
    border-radius: 4px;
    border: 1px solid #ccc;
  }
</style>
