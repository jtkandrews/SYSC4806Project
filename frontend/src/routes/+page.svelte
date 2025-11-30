<script lang="ts">

  import { createBook } from '$lib/api';
  import { role } from '$lib/session';
  import { goto } from '$app/navigation';
  import { onDestroy } from 'svelte';
  import { addToCart, cartItemCount } from '$lib/stores/cart';
  import { addBookToStore, booksStore, setBooks } from '$lib/stores/books';
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

  function handleAddToCart(book: Book) {
    cartError = '';
    cartMessage = '';
    clearCartMessageTimeout();

    try {
      addToCart(book);
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

  {#if $booksStore.length === 0}
    <div class="empty-state">
      <h2 class="empty-state-title">No books available</h2>
      <p class="empty-state-text">Check back soon for new arrivals!</p>
    </div>
  {:else}
    <div class="books-grid">
      <!-- {#each books as book} -->
      {#each $booksStore as book}
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
          <label>
            <span>Genres (Select all that apply)</span>
          </label>
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

  .cart-feedback {
    margin: 0 0 1rem 0;
    padding: 0.75rem 1rem;
    border-radius: 0.5rem;
    font-size: 0.95rem;
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