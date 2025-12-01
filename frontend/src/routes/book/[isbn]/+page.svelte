<script lang="ts">
  import { onMount } from "svelte";
  import { derived } from "svelte/store";
  import { role } from "$lib/session";
  import { addToCart } from "$lib/stores/cart";
  import { booksStore } from "$lib/stores/books";
  import type { Book } from "$lib/types";

  export let data;

  let isEditModalOpen = false;
  let editError = "";
  let isSubmitting = false;
  let addToCartQuantity = 1;
  let addToCartError = "";
  let addToCartSuccess = "";

  // Image URL
  $: imageUrl = `https://covers.openlibrary.org/b/isbn/${data.book.isbn}-L.jpg`;

  const bookFromStore = derived(booksStore, ($books) =>
    $books.find((b) => b.isbn === data.book.isbn)
  );

  let currentBook: Book = data.book;
  $: currentBook = $bookFromStore ?? data.book;

  // Sync store on mount
  onMount(() => {
    booksStore.update((current) => {
      const idx = current.findIndex((b) => b.isbn === currentBook.isbn);
      if (idx === -1) return [...current, currentBook];
      const updated = [...current];
      updated[idx] = { ...updated[idx], ...currentBook };
      return updated;
    });
  });

  // Quantity constraints
  $: {
    const max = Math.max(currentBook.inventory, 1);
    if (addToCartQuantity > max) addToCartQuantity = max;
    if (addToCartQuantity < 1) addToCartQuantity = 1;
  }

  function increaseAddToCartQuantity() {
    if (addToCartQuantity < currentBook.inventory) addToCartQuantity++;
  }
  function decreaseAddToCartQuantity() {
    if (addToCartQuantity > 1) addToCartQuantity--;
  }

  async function handleAddToCart() {
    addToCartError = "";
    addToCartSuccess = "";
    try {
      await addToCart(currentBook, addToCartQuantity);
      addToCartSuccess = `Added ${addToCartQuantity} copy/copies of "${currentBook.title}".`;
    } catch (err) {
      addToCartError = err instanceof Error ? err.message : "Error adding to cart.";
    }
  }

  // ---------- OWNER EDIT ----------
  let editFormData = {
    genres: [] as string[],
    description: "",
    price: 0,
    inventory: 0
  };

  $: if (!isEditModalOpen) {
    editFormData = {
      genres: currentBook.genre ? currentBook.genre.split(", ").map((g) => g.trim()) : [],
      description: currentBook.description || "",
      price: currentBook.price,
      inventory: currentBook.inventory
    };
  }

  function handleEditGenreToggle(g: string) {
    if (editFormData.genres.includes(g))
      editFormData.genres = editFormData.genres.filter((x) => x !== g);
    else editFormData.genres.push(g);
  }

  async function handleSaveEdit() {
    isSubmitting = true;
    editError = "";

    try {
      const res = await fetch(`/api/owner/books/${currentBook.isbn}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          isbn: currentBook.isbn,
          title: currentBook.title,
          author: currentBook.author,
          publisher: currentBook.publisher,
          genre: editFormData.genres.join(", "),
          description: editFormData.description,
          price: editFormData.price,
          inventory: editFormData.inventory,
          imageUrl
        })
      });

      if (!res.ok) throw new Error("Failed to update book.");

      const updated = await res.json();
      currentBook = updated;

      booksStore.update((list) =>
        list.map((b) => (b.isbn === currentBook.isbn ? updated : b))
      );

      isEditModalOpen = false;
    } catch (err) {
      editError = err instanceof Error ? err.message : "Unknown error.";
    } finally {
      isSubmitting = false;
    }
  }
</script>

<!-- PAGE LAYOUT -->
<section class="book-detail">
  <a href="/" class="back-link">← Back to Books</a>

  <div class="detail-header">
    <h1 class="title">{currentBook.title}</h1>
    <p class="author">by {currentBook.author}</p>
    {#if currentBook.publisher}
      <p class="publisher">Published by {currentBook.publisher}</p>
    {/if}

    {#if $role === "OWNER"}
      <button class="edit-btn" on:click={() => (isEditModalOpen = true)}>
        ✎ Edit Book
      </button>
    {/if}
  </div>

  <div class="detail-body">
    <div class="image-section">
      <img src={imageUrl} alt="{currentBook.title}" class="cover" />
    </div>

    <div class="info-section">
      {#if currentBook.genre}
        <p class="genre"><strong>Genre:</strong> {currentBook.genre}</p>
      {/if}

      <p class="description">
        {#if currentBook.description}
          {currentBook.description}
        {:else}
          <span class="empty">No description available.</span>
        {/if}
      </p>

      <p class="price">
        {currentBook.price.toLocaleString(undefined, {
          style: "currency",
          currency: "USD"
        })}
      </p>

      <p class="stock">
        {#if currentBook.inventory > 0}
          <span class="instock">{currentBook.inventory} in stock</span>
        {:else}
          <span class="outstock">Out of stock</span>
        {/if}
      </p>

      <!-- USER ADD TO CART -->
      {#if $role === "USER"}
        <div class="cart-controls">
          <button class="qty-btn" on:click={decreaseAddToCartQuantity}>−</button>
          <input
            class="qty-input"
            type="number"
            min="1"
            max={currentBook.inventory}
            bind:value={addToCartQuantity}
          />
          <button
            class="qty-btn"
            on:click={increaseAddToCartQuantity}
            disabled={addToCartQuantity >= currentBook.inventory}
          >
            +
          </button>

          <button
            class="add-cart-btn"
            disabled={currentBook.inventory === 0}
            on:click|preventDefault={handleAddToCart}
          >
            Add to Cart
          </button>
        </div>

        {#if addToCartError}
          <p class="feedback error">{addToCartError}</p>
        {/if}

        {#if addToCartSuccess}
          <p class="feedback success">{addToCartSuccess}</p>
        {/if}
      {/if}
    </div>
  </div>
</section>

<!-- OWNER EDIT MODAL -->
{#if $role === "OWNER" && isEditModalOpen}
  <div class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <h2>Edit Book</h2>
        <button class="close" on:click={() => (isEditModalOpen = false)}>✕</button>
      </div>

      <div class="modal-body">
        {#if editError}
          <p class="feedback error">{editError}</p>
        {/if}

        <label>Genres</label>
        <div class="genre-grid">
          {#each ["Fiction", "Fantasy", "Action", "Romance", "Mystery", "Thriller", "Biography", "History", "Other"] as g}
            <label class="checkbox">
              <input
                type="checkbox"
                value={g}
                checked={editFormData.genres.includes(g)}
                on:change={() => handleEditGenreToggle(g)}
              />
              {g}
            </label>
          {/each}
        </div>

        <label>Description</label>
        <textarea bind:value={editFormData.description}></textarea>

        <label>Price</label>
        <input type="number" bind:value={editFormData.price} min="1" step="0.01" />

        <label>Inventory</label>
        <input type="number" bind:value={editFormData.inventory} min="0" step="1" />

        <div class="modal-footer">
          <button class="cancel-btn" on:click={() => (isEditModalOpen = false)}>Cancel</button>
          <button class="save-btn" on:click={handleSaveEdit} disabled={isSubmitting}>
            {isSubmitting ? "Saving..." : "Save Changes"}
          </button>
        </div>
      </div>
    </div>
  </div>
{/if}

<style>
  .back-link { text-decoration:none; color:#2563eb; font-size:1rem; }
  .book-detail { max-width:1100px; margin:auto; padding:2rem; }

  .detail-header { margin-bottom:1.5rem; }
  .title { font-size:2rem; font-weight:700; }
  .author { font-size:1.2rem; color:#555; margin-top:0.2rem; }
  .publisher { color:#777; }

  .edit-btn {
    margin-top:1rem;
    background:#2563eb;
    color:white;
    padding:.4rem 1rem;
    border:none;
    border-radius:4px;
  }

  .detail-body { display:flex; gap:2rem; margin-top:2rem; }
  .image-section { flex:1; }
  .cover { width:100%; border-radius:10px; }

  .info-section { flex:2; }

  .genre { margin-top:.3rem; color:#333; }
  .description { margin:1rem 0; line-height:1.4; }
  .empty { color:#888; font-style:italic; }

  .price { margin-top:1rem; font-size:1.5rem; font-weight:700; }
  .instock { color:#16a34a; }
  .outstock { color:#dc2626; }

  .cart-controls { display:flex; gap:.5rem; margin-top:1rem; }
  .qty-btn { width:2.5rem; height:2.5rem; border-radius:50%; border:none; background:#ddd; }
  .qty-input { width:3rem; text-align:center; }

  .add-cart-btn {
    background:#2563eb;
    color:white;
    border:none;
    padding:.5rem 1rem;
    border-radius:6px;
  }

  .feedback { margin-top:.5rem; padding:.5rem; border-radius:6px; }
  .feedback.error { background:#fee2e2; color:#991b1b; }
  .feedback.success { background:#dcfce7; color:#166534; }

  /* MODAL */
  .modal-overlay {
    position:fixed;
    top:0; left:0;
    width:100%; height:100%;
    background:rgba(0,0,0,.5);
    display:flex; align-items:center; justify-content:center;
  }

  .modal {
    background:white;
    padding:1.5rem;
    border-radius:10px;
    width:500px;
  }

  .modal-header { display:flex; justify-content:space-between; align-items:center; }
  .close { background:none; border:none; font-size:1.5rem; cursor:pointer; }

  .modal-body label { margin-top:1rem; display:block; font-weight:600; }

  textarea { width:100%; height:100px; }

  .genre-grid {
    display:grid;
    grid-template-columns:repeat(2,1fr);
    gap:.3rem;
    margin-bottom:1rem;
  }

  .modal-footer {
    margin-top:1.5rem;
    display:flex;
    justify-content:flex-end;
    gap:.7rem;
  }

  .cancel-btn {
    background:#ccc;
    border:none;
    padding:.5rem 1rem;
    border-radius:6px;
  }

  .save-btn {
    background:#2563eb;
    color:white;
    border:none;
    padding:.5rem 1rem;
    border-radius:6px;
  }
</style>


