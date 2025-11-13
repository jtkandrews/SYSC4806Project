<script lang="ts">
  import { createBook } from '../lib/api';
  import { role } from '$lib/session'; // 🔹 NEW: owner/user role store

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

  // @ts-ignore
  async function handleAddBook(e) {
    e.preventDefault();
    addError = ''; // Clear any previous errors

    // Validate required fields
    if (!formData.title || !formData.author || !formData.isbn || !formData.price || !formData.inventory) {
      addError = 'Please fill in all required fields';
      return;
    }

    // Validate title length
    if (formData.title.length > 150) {
      addError = 'Title cannot exceed 150 characters';
      return;
    }

    // Validate author length
    if (formData.author.length > 100) {
      addError = 'Author cannot exceed 100 characters';
      return;
    }

    // Validate ISBN is exactly 13 digits
    const isbnDigits = formData.isbn.replace(/\D/g, '');
    if (isbnDigits.length !== 13) {
      addError = 'ISBN must be exactly 13 digits';
      return;
    }

    // Validate genre length
    if (formData.genre.length > 100) {
      addError = 'Genre cannot exceed 100 characters';
      return;
    }

    // Validate description length
    if (formData.description.length > 500) {
      addError = 'Description cannot exceed 500 characters';
      return;
    }

    // Validate price is greater than 0
    const price = parseFloat(formData.price);
    if (price <= 0) {
      addError = 'Price must be greater than 0';
      return;
    }

    // Validate price integer part is 4 digits max (before decimal)
    const priceString = formData.price.toString();
    const priceParts = priceString.split('.');
    if (priceParts[0].length > 4) {
      addError = 'Price cannot exceed 4 digits (max 9999.99)';
      return;
    }

    // Validate inventory is 4 digits max
    const inventory = parseInt(formData.inventory);
    const inventoryString = formData.inventory.toString();
    if (inventoryString.length > 4) {
      addError = 'Inventory cannot exceed 4 digits (max 9999)';
      return;
    }

    isSubmitting = true;

    try {
      // Prepare the book data with proper types for the backend
      const bookToAdd = {
        isbn: isbnDigits, // Use the cleaned ISBN with only digits
        title: formData.title,
        author: formData.author,
        genre: formData.genre || undefined,
        price: price,
        inventory: inventory,
        imageUrl: `https://covers.openlibrary.org/b/isbn/${isbnDigits}-L.jpg`,
        description: formData.description || undefined
      };

      // Call the backend API to create the book
      // @ts-ignore
      const newBook = await createBook(bookToAdd);

      // Add the new book to the front of the list
      books = [newBook, ...books];

      // Show success message and close modal
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
    // Only allow digits and hyphens
    const input = e.target.value;
    formData.isbn = input.replace(/[^\d-]/g, '');
  }

  // @ts-ignore
  function handlePriceInput(e) {
    // Limit to 4 digits before decimal point (e.g., 9999.99)
    const input = e.target.value;
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
    const newValue = parts.join('.');
    formData.price = newValue;
  }

  // @ts-ignore
  function handleInventoryInput(e) {
    // Limit to 4 digits (max 9999)
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

      {#if $role === 'OWNER'}  <!-- 🔹 NEW: only owners see Add Book -->
        <button class="btn btn-primary" on:click={openAddBookModal}>
          ➕ Add Book
        </button>
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
                {(+book.price).toLocaleString(undefined, { style: 'currency', currency: 'USD' })}
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

<!-- Add Book Modal (owner-only) -->
{#if $role === 'OWNER' && showAddBookModal}  <!-- 🔹 NEW: guard modal too -->
  <!-- @ts-ignore -->
  <div
    class="modal-backdrop"
    on:click={handleModalBackdropClick}
    on:keydown={e => e.key === 'Escape' && closeAddBookModal()}
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
          <label for="genre">
            <span>Genre</span>
          </label>
          <input
            type="text"
            id="genre"
            bind:value={formData.genre}
            maxlength="100"
            placeholder="Enter book genre (e.g., Science Fiction)"
          />
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
