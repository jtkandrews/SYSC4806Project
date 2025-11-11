<script>
  import { onMount } from 'svelte';

  export let data;
  const books = data.initialBooks || [];

  let showAddBookModal = false;
  let formData = {
    title: '',
    author: '',
    isbn: '',
    description: '',
    price: '',
    inventory: '',
    imageUrl: ''
  };

  onMount(() => {
    if (books.length > 0) {
      console.log('First book:', books[0]);
      console.log('Has imageUrl?', books[0].imageUrl);
      console.log('ImageUrl value:', books[0].imageUrl);
    }
  });

  function openAddBookModal() {
    showAddBookModal = true;
  }

  function closeAddBookModal() {
    showAddBookModal = false;
    resetForm();
  }

  function resetForm() {
    formData = {
      title: '',
      author: '',
      isbn: '',
      description: '',
      price: '',
      inventory: '',
      imageUrl: ''
    };
  }

  // @ts-ignore
  async function handleAddBook(e) {
    e.preventDefault();

    // Validate required fields
    if (!formData.title || !formData.author || !formData.isbn || !formData.price || !formData.inventory) {
      alert('Please fill in all required fields');
      return;
    }

    try {
      // TODO: Send form data to backend API
      console.log('Adding book:', formData);
      alert('Book added successfully!');
      closeAddBookModal();
    } catch (error) {
      console.error('Error adding book:', error);
      alert('Failed to add book');
    }
  }

  // @ts-ignore
  function handleModalBackdropClick(e) {
    if (e.target === e.currentTarget) {
      closeAddBookModal();
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
      <button class="btn btn-primary" on:click={openAddBookModal}>
        ➕ Add Book
      </button>
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

<!-- Add Book Modal -->
{#if showAddBookModal}
  <!-- @ts-ignore -->
  <div class="modal-backdrop" on:click={handleModalBackdropClick} on:keydown={e => e.key === 'Escape' && closeAddBookModal()} role="presentation" aria-labelledby="modal-title">
    <div class="modal">
      <div class="modal-header">
        <h2 id="modal-title">Add New Book</h2>
        <button class="modal-close" on:click={closeAddBookModal} aria-label="Close dialog">✕</button>
      </div>

      <form on:submit={handleAddBook} class="modal-form">
        <div class="form-group">
          <label for="title">Title <span class="required">*</span></label>
          <input
            type="text"
            id="title"
            bind:value={formData.title}
            placeholder="Enter book title"
            required
          />
        </div>

        <div class="form-group">
          <label for="author">Author <span class="required">*</span></label>
          <input
            type="text"
            id="author"
            bind:value={formData.author}
            placeholder="Enter author name"
            required
          />
        </div>

        <div class="form-group">
          <label for="isbn">ISBN <span class="required">*</span></label>
          <input
            type="text"
            id="isbn"
            bind:value={formData.isbn}
            placeholder="Enter ISBN (e.g., 978-0-123456-78-9)"
            required
          />
        </div>

        <div class="form-group">
          <label for="description">Description</label>
          <textarea
            id="description"
            bind:value={formData.description}
            placeholder="Enter book description"
            rows="4"
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="price">Price (USD) <span class="required">*</span></label>
            <input
              type="number"
              id="price"
              bind:value={formData.price}
              placeholder="0.00"
              step="0.01"
              min="0"
              required
            />
          </div>

          <div class="form-group">
            <label for="inventory">Inventory <span class="required">*</span></label>
            <input
              type="number"
              id="inventory"
              bind:value={formData.inventory}
              placeholder="0"
              min="0"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label for="imageUrl">Image URL</label>
          <input
            type="url"
            id="imageUrl"
            bind:value={formData.imageUrl}
            placeholder="https://example.com/image.jpg"
          />
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" on:click={closeAddBookModal}>
            Cancel
          </button>
          <button type="submit" class="btn btn-primary">
            Add Book
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}

