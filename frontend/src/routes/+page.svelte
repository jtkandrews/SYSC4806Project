<script lang="ts">

  import { createBook } from '$lib/api';
  import { role } from '$lib/session';
  import { goto } from '$app/navigation';
  import { onDestroy } from 'svelte';
  import { addToCart, cartItemCount } from '$lib/stores/cart';
  import { addBookToStore, booksStore, setBooks } from '$lib/stores/books';
  import {
    filterState,
    filteredSortedBooks,
    updateSortBy,
    toggleGenre,
    updatePriceRange,
    updateSearchTerm,
    resetFilters,
    type SortOption
  } from '$lib/stores/filterSort';
  import type { Book } from '$lib/types';


  export let data;
  setBooks(data.initialBooks || [])

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

  let showAddBookModal = false;
  let isSubmitting = false;
  let addError = '';
  let cartMessage = '';
  let cartError = '';
  let cartMessageTimeout: ReturnType<typeof setTimeout> | null = null;
  let showFilters = false;

  let formData = {
    title: '',
    author: '',
    isbn: '',
    description: '',
    genres: [] as string[],
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
      genres: [],
      price: '',
      inventory: ''
    };
  }

  function handleGenreToggle(genre: string) {
    if (formData.genres.includes(genre)) {
      formData.genres = formData.genres.filter(g => g !== genre);
    } else {
      formData.genres = [...formData.genres, genre];
    }
  }

  // @ts-ignore
  async function handleAddBook(e) {
    e.preventDefault();
    addError = '';

    // Required fields
    if (!formData.title || !formData.author || !formData.isbn || !formData.price || !formData.inventory) {
      addError = 'Please fill in all required fields';
      return;
    }

    // Title length
    if (formData.title.length > 150) {
      addError = 'Title cannot exceed 150 characters';
      return;
    }

    // Author length
    if (formData.author.length > 100) {
      addError = 'Author cannot exceed 100 characters';
      return;
    }

    // ISBN 13 digits
    const isbnDigits = formData.isbn.replace(/\D/g, '');
    if (isbnDigits.length !== 13) {
      addError = 'ISBN must be exactly 13 digits';
      return;
    }

    // Description length
    if (formData.description.length > 500) {
      addError = 'Description cannot exceed 500 characters';
      return;
    }

    // Price > 0
    const price = parseFloat(formData.price);
    if (price <= 0) {
      addError = 'Price must be greater than 0';
      return;
    }

    // Price integer part max 4 digits
    const priceString = formData.price.toString();
    const priceParts = priceString.split('.');
    if (priceParts[0].length > 4) {
      addError = 'Price cannot exceed 4 digits (max 9999.99)';
      return;
    }

    // Inventory max 4 digits
    const inventory = parseInt(formData.inventory);
    const inventoryString = formData.inventory.toString();
    if (inventoryString.length > 4) {
      addError = 'Inventory cannot exceed 4 digits (max 9999)';
      return;
    }

    isSubmitting = true;

    try {
      const bookToAdd = {
        isbn: isbnDigits,
        title: formData.title,
        author: formData.author,
        genre: formData.genres.length > 0 ? formData.genres.join(', ') : undefined,
        price,
        inventory,
        imageUrl: `https://covers.openlibrary.org/b/isbn/${isbnDigits}-L.jpg`,
        description: formData.description || undefined
      };

      // @ts-ignore
      const newBook = await createBook(bookToAdd);

      // books = [newBook, ...books];
      addBookToStore(newBook)
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
  function clearCartMessageTimeout() {
    if (cartMessageTimeout) {
      clearTimeout(cartMessageTimeout);
      cartMessageTimeout = null;
    }
  }

  async function handleAddToCart(book: Book) {
    cartError = '';
    cartMessage = '';
    clearCartMessageTimeout();

    try {
      await addToCart(book);
      cartMessage = `Added "${book.title}" to your cart.`;
      cartMessageTimeout = setTimeout(() => {
        cartMessage = '';
        cartMessageTimeout = null;
      }, 2500);
    } catch (error) {
      cartError = error instanceof Error ? error.message : 'Unable to add book to cart.';
    }
  }

  onDestroy(() => {
    clearCartMessageTimeout();
  });
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
        {:else}
        <div class="header-actions">
          <a href="/cart" class="btn btn-secondary">
            🛒 View Cart ({$cartItemCount})
          </a>
        </div>
      {/if}
    </div>
  </div>


  <button on:click={() => goto('/recommendedBooks')}>
    Go to Recommended Books
  </button>

  <!-- Filter and Sort Controls -->
  <div class="filter-section">
    <div class="filter-header">
      <h2>Filter & Sort</h2>
      <button class="btn btn-secondary btn-sm" on:click={() => showFilters = !showFilters}>
        {showFilters ? '▼ Hide Filters' : '▶ Show Filters'}
      </button>
    </div>

    {#if showFilters}
      <div class="filter-controls">
        <!-- Search -->
        <div class="filter-group">
          <label for="search">Search Books:</label>
          <input
            type="text"
            id="search"
            placeholder="Search by title, author, or description..."
            value={$filterState.searchTerm}
            on:input={(e) => updateSearchTerm(e.currentTarget.value)}
            class="filter-input"
          />
        </div>

        <!-- Sort Options -->
        <div class="filter-group">
          <label for="sort">Sort By:</label>
          <select
            id="sort"
            value={$filterState.sortBy}
            on:change={(e) => {
              // @ts-ignore
              updateSortBy(e.currentTarget.value);
            }}
            class="filter-select"
          >
            <option value="none">None (Original Order)</option>
            <option value="title-asc">Title (A-Z)</option>
            <option value="title-desc">Title (Z-A)</option>
            <option value="price-asc">Price (Low to High)</option>
            <option value="price-desc">Price (High to Low)</option>
            <option value="author-asc">Author (A-Z)</option>
            <option value="author-desc">Author (Z-A)</option>
          </select>
        </div>

        <!-- Price Range -->
        <div class="filter-group">
          <div>Price Range:</div>
          <div class="price-range">
            <div class="price-input-group">
              <label for="min-price">Min:</label>
              <input
                type="number"
                id="min-price"
                min="0"
                step="0.01"
                value={$filterState.minPrice}
                on:change={(e) => updatePriceRange(parseFloat(e.currentTarget.value) || 0, $filterState.maxPrice)}
                class="filter-input-small"
              />
            </div>
            <div class="price-input-group">
              <label for="max-price">Max:</label>
              <input
                type="number"
                id="max-price"
                min="0"
                step="0.01"
                value={$filterState.maxPrice}
                on:change={(e) => updatePriceRange($filterState.minPrice, parseFloat(e.currentTarget.value) || 10000)}
                class="filter-input-small"
              />
            </div>
          </div>
        </div>

        <!-- Genre Filter -->
        <div class="filter-group">
          <div>Filter by Genre:</div>
          <div class="genre-filter-list">
            {#each genreOptions as genre}
              <label class="genre-checkbox">
                <input
                  type="checkbox"
                  checked={$filterState.genres.includes(genre)}
                  on:change={() => toggleGenre(genre)}
                />
                <span>{genre}</span>
              </label>
            {/each}
          </div>
        </div>

        <!-- Reset Button -->
        <button class="btn btn-secondary" on:click={() => resetFilters()}>
          Reset Filters
        </button>
      </div>
    {/if}
  </div>

  {#if $filteredSortedBooks.length === 0}
    <div class="empty-state">
      <h2 class="empty-state-title">{$booksStore.length === 0 ? 'No books available' : 'No books match your filters'}</h2>
      <p class="empty-state-text">{$booksStore.length === 0 ? 'Check back soon for new arrivals!' : 'Try adjusting your search criteria.'}</p>
    </div>
  {:else}
    <div class="books-info">
      <p>Showing <strong>{$filteredSortedBooks.length}</strong> of <strong>{$booksStore.length}</strong> books</p>
    </div>
    <div class="books-grid">
      <!-- {#each books as book} -->
      {#each $filteredSortedBooks as book}
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

            {#if $role === 'USER'}
              <button
                      class="btn btn-primary btn-add-cart"
                      disabled={book.inventory === 0}
                      on:click|stopPropagation|preventDefault={() => handleAddToCart(book)}
              >
                Add to Cart
              </button>
            {/if}
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

{#if $role === 'OWNER' && showAddBookModal}
  <!-- @ts-ignore -->
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
          <label for="title">
            <span>Title <span class="required">*</span></span>
          </label>
          <input
                  type="text"
                  id="title"
                  bind:value={formData.title}
                  maxlength="150"
                  placeholder="Enter book title"
                  required
          />
        </div>

        <div class="form-group">
          <label for="author">
            <span>Author <span class="required">*</span></span>
          </label>
          <input
                  type="text"
                  id="author"
                  bind:value={formData.author}
                  maxlength="100"
                  placeholder="Enter author name"
                  required
          />
        </div>

        <div class="form-group">
          <label for="isbn">
            <span>ISBN <span class="required">*</span></span>
          </label>
          <input
                  type="text"
                  id="isbn"
                  bind:value={formData.isbn}
                  on:input={handleIsbnInput}
                  placeholder="Enter ISBN (e.g., 978-0-545-58289-5)"
                  required
          />
        </div>

        <div class="form-group">
          <div style="font-weight: 600; margin-bottom: 0.5rem;">Genres (Select all that apply)</div>
          <div class="checkbox-grid">
            {#each genreOptions as genre}
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  value={genre}
                  checked={formData.genres.includes(genre)}
                  on:change={() => handleGenreToggle(genre)}
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
                  bind:value={formData.description}
                  maxlength="500"
                  placeholder="Enter book description"
                  rows="4"
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="price">
              <span>Price (USD) <span class="required">*</span></span>
            </label>
            <input
                    type="number"
                    id="price"
                    bind:value={formData.price}
                    on:input={handlePriceInput}
                    placeholder="0.00"
                    step="0.01"
                    min="0.01"
                    required
            />
          </div>

          <div class="form-group">
            <label for="inventory">
              <span>Inventory <span class="required">*</span></span>
            </label>
            <input
                    type="number"
                    id="inventory"
                    bind:value={formData.inventory}
                    on:input={handleInventoryInput}
                    placeholder="0"
                    min="0"
                    required
            />
          </div>
        </div>

        <div class="modal-footer">
          <button
                  type="button"
                  class="btn btn-secondary"
                  on:click={closeAddBookModal}
                  disabled={isSubmitting}
          >
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

<style>
  .header-actions {
    display: flex;
    align-items: center;
  }

  /* Filter and Sort Styles */
  .filter-section {
    margin: 2rem 0;
    padding: 1.5rem;
    background: #f8f9fa;
    border-radius: 0.5rem;
    border: 1px solid #e5e7eb;
  }

  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
  }

  .filter-header h2 {
    margin: 0;
    font-size: 1.25rem;
    color: #1f2937;
  }

  .btn-sm {
    padding: 0.5rem 1rem;
    font-size: 0.875rem;
  }

  .filter-controls {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 1.5rem;
    padding-top: 1rem;
  }

  .filter-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .filter-group label {
    font-weight: 600;
    color: #374151;
    font-size: 0.95rem;
  }

  .filter-input,
  .filter-select {
    padding: 0.75rem;
    border: 1px solid #d1d5db;
    border-radius: 0.375rem;
    font-size: 0.95rem;
    background-color: white;
    color: #1f2937;
  }

  .filter-input:focus,
  .filter-select:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }

  .price-range {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.75rem;
  }

  .price-input-group {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .price-input-group label {
    font-weight: 500;
    font-size: 0.85rem;
  }

  .filter-input-small {
    padding: 0.5rem;
    border: 1px solid #d1d5db;
    border-radius: 0.375rem;
    font-size: 0.9rem;
  }

  .genre-filter-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 0.75rem;
  }

  .genre-checkbox {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    font-size: 0.9rem;
    color: #374151;
    user-select: none;
  }

  .genre-checkbox input[type="checkbox"] {
    width: 1.125rem;
    height: 1.125rem;
    cursor: pointer;
    accent-color: #3b82f6;
  }

  .genre-checkbox:hover {
    color: #1f2937;
  }

  .books-info {
    margin: 1rem 0;
    padding: 0.75rem 1rem;
    background: #e3f2fd;
    border-left: 4px solid #3b82f6;
    border-radius: 0.375rem;
    color: #1565c0;
    font-size: 0.95rem;
  }

  .books-info strong {
    color: #0d47a1;
  }
</style>