<script lang="ts">
  import { createBook } from '../lib/api';
  import { role } from '$lib/session';

  export let data;
  let books = data.initialBooks || [];

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

    if (formData.title.length > 150) {
      addError = 'Title cannot exceed 150 characters';
      return;
    }

    if (formData.author.length > 100) {
      addError = 'Author cannot exceed 100 characters';
      return;
    }

    const isbnDigits = formData.isbn.replace(/\D/g, '');
    if (isbnDigits.length !== 13) {
      addError = 'ISBN must be exactly 13 digits';
      return;
    }

    if (formData.genre.length > 100) {
      addError = 'Genre cannot exceed 100 characters';
      return;
    }

    if (formData.description.length > 500) {
      addError = 'Description cannot exceed 500 characters';
      return;
    }

    const price = parseFloat(formData.price);
    if (price <= 0) {
      addError = 'Price must be greater than 0';
      return;
    }

    const priceParts = formData.price.toString().split('.');
    if (priceParts[0].length > 4) {
      addError = 'Price cannot exceed 4 digits (max 9999.99)';
      return;
    }

    const inventory = parseInt(formData.inventory);
    if (formData.inventory.toString().length > 4) {
      addError = 'Inventory cannot exceed 4 digits (max 9999)';
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
      const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
      addError = `Failed to add book: ${errorMessage}`;
    } finally {
      isSubmitting = false;
    }
  }

  function handleModalBackdropClick(e) {
    if (e.target === e.currentTarget) closeAddBookModal();
  }

  function handleIsbnInput(e) {
    formData.isbn = e.target.value.replace(/[^\d-]/g, '');
  }

  function handlePriceInput(e) {
    let cleaned = e.target.value.replace(/[^\d.]/g, '');
    const parts = cleaned.split('.');
    if (parts[0].length > 4) parts[0] = parts[0].slice(0, 4);
    if (parts[1] && parts[1].length > 2) parts[1] = parts[1].slice(0, 2);
    formData.price = parts.join('.');
  }

  function handleInventoryInput(e) {
    formData.inventory = e.target.value.replace(/\D/g, '').slice(0, 4);
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
        <button class="btn btn-primary" on:click={openAddBookModal}>➕ Add Book</button>
      {/if}
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

            <button class="btn btn-primary btn-add-cart" disabled={book.inventory === 0}>
              Add to Cart
            </button>
          </div>
        </div>
      {/each}
    </div>
  {/if}
</div>

{#if $role === 'OWNER' && showAddBookModal}
  <div class="modal-backdrop" on:click={handleModalBackdropClick}>
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
          <label for="title">Title *</label>
          <input id="title" type="text" bind:value={formData.title} maxlength="150" required />
        </div>

        <div class="form-group">
          <label for="author">Author *</label>
          <input id="author" type="text" bind:value={formData.author} maxlength="100" required />
        </div>

        <div class="form-group">
          <label for="isbn">ISBN *</label>
          <input id="isbn" type="text" bind:value={formData.isbn} on:input={handleIsbnInput} required />
        </div>

        <div class="form-group">
          <label for="genre">Genre</label>
          <select id="genre" bind:value={formData.genre}>
            <option value="">Select a genre</option>
            <option value="Fiction">Fiction</option>
            <option value="Non-Fiction">Non-Fiction</option>
            <option value="Science Fiction">Science Fiction</option>
            <option value="Fantasy">Fantasy</option>
            <option value="Mystery">Mystery</option>
            <option value="Romance">Romance</option>
            <option value="Biography">Biography</option>
            <option value="History">History</option>
          </select>
        </div>

        <div class="form-group">
          <label for="description">Description</label>
          <textarea id="description" bind:value={formData.description} maxlength="500" rows="4"></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="price">Price (USD) *</label>
            <input id="price" type="number" bind:value={formData.price} on:input={handlePriceInput} step="0.01" min="0.01" required />
          </div>

          <div class="form-group">
            <label for="inventory">Inventory *</label>
            <input id="inventory" type="number" bind:value={formData.inventory} on:input={handleInventoryInput} min="0" required />
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" on:click={closeAddBookModal} disabled={isSubmitting}>Cancel</button>
          <button type="submit" class="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? 'Adding...' : 'Add Book'}
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}
