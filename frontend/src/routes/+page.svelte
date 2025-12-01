<script lang="ts">
  import { onMount } from 'svelte';
  import { role } from '$lib/session';
  import { cartItemCount } from '$lib/stores/cart';
  import { setBooks } from '$lib/stores/books';
  import type { Book } from '$lib/types';

  // ---------- UI STATE ----------
  let books: Book[] = [];
  let isLoading = false;
  let loadError = '';

  let sortBy: 'price' | 'title' | 'inventory' = 'price';
  let order: 'asc' | 'desc' = 'asc';
  let genre = 'All';
  let minPrice: string | number = '';
  let maxPrice: string | number = '';

  const GENRES = [
    'All',
    'Fantasy',
    'Action',
    'Romance',
    'Mystery',
    'Thriller',
    'Philosophy',
    'Self-help',
    'Historical fiction'
  ];

  // ---------- HELPERS ----------
  function cover(book: Book) {
    if (book.imageUrl && book.imageUrl.trim() !== '') return book.imageUrl;
    return `https://covers.openlibrary.org/b/isbn/${book.isbn}-L.jpg`;
  }

  // Safely convert min/max input (string | number | '') to number or null
  function toNumberOrNull(value: unknown): number | null {
    if (value === '' || value === null || value === undefined) {
      return null;
    }
    const n = Number(value);
    return Number.isNaN(n) ? null : n;
  }

  // Fetch books from backend.
  // If useFilters = false → no query params (just /api/books).
  async function fetchBooks(useFilters: boolean) {
    isLoading = true;
    loadError = '';

    try {
      const params = new URLSearchParams();

      if (useFilters) {
        // Sorting
        if (sortBy) params.set('sortBy', sortBy);
        if (order) params.set('order', order);

        // Only send genre if not "All"
        if (genre !== 'All') params.set('genre', genre);
      }

      const qs = params.toString();
      const url = qs ? `/api/books?${qs}` : '/api/books';

      const res = await fetch(url);
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || `Failed to load books (status ${res.status})`);
      }

      let data: Book[] = await res.json();

      // Local min/max price filtering
      if (useFilters) {
        const min = toNumberOrNull(minPrice);
        const max = toNumberOrNull(maxPrice);

        if (min !== null) {
          data = data.filter((b) => b.price >= min);
        }
        if (max !== null) {
          data = data.filter((b) => b.price <= max);
        }
      }

      books = data;
      setBooks(data); // keep global store in sync
    } catch (err) {
      console.error('Error loading books', err);
      loadError = err instanceof Error ? err.message : 'Unknown error loading books';
      books = [];
      setBooks([]);
    } finally {
      isLoading = false;
    }
  }

  function applyFilters() {
    fetchBooks(true);
  }

  function resetFilters() {
    sortBy = 'price';
    order = 'asc';
    genre = 'All';
    minPrice = '';
    maxPrice = '';
    fetchBooks(false);
  }

  onMount(() => {
    // Initial load: no filters → just /api/books
    fetchBooks(false);
  });
</script>

<section class="page">
  <!-- HEADER -->
  <header class="hero">
    <div>
      <h1 class="hero-title">Browse Our Collection</h1>
      <p class="hero-subtitle">Great books at amazin prices</p>
    </div>

    {#if $role === 'OWNER'}
      <a class="add-btn" href="/owner">+ Add Book</a>
    {:else}
      <a class="cart-top" href="/cart">
        Cart ({$cartItemCount})
      </a>
    {/if}
  </header>

  <!-- FILTER BAR -->
  <div class="filters">
    <div class="row">
      <div class="filter">
        <label for="sortBy">Sort By</label>
        <select id="sortBy" bind:value={sortBy}>
          <option value="price">Price</option>
          <option value="title">Title</option>
          <option value="inventory">Inventory</option>
        </select>
      </div>

      <div class="filter">
        <label for="order">Order</label>
        <select id="order" bind:value={order}>
          <option value="asc">Ascending</option>
          <option value="desc">Descending</option>
        </select>
      </div>

      <div class="filter">
        <label for="genre">Genre</label>
        <select id="genre" bind:value={genre}>
          {#each GENRES as g}
            <option value={g}>{g}</option>
          {/each}
        </select>
      </div>

      <div class="filter">
        <label for="minPrice">Min Price</label>
        <input
          id="minPrice"
          type="number"
          bind:value={minPrice}
          min="0"
          step="0.01"
        />
      </div>

      <div class="filter">
        <label for="maxPrice">Max Price</label>
        <input
          id="maxPrice"
          type="number"
          bind:value={maxPrice}
          min="0"
          step="0.01"
        />
      </div>

      <div class="buttons">
        <button class="apply" type="button" on:click={applyFilters}>Apply</button>
        <button class="reset" type="button" on:click={resetFilters}>Reset</button>
      </div>
    </div>
  </div>

  <!-- STATUS MESSAGES -->
  {#if isLoading}
    <p>Loading books…</p>
  {:else if loadError}
    <p style="color: #b91c1c;">Error loading books: {loadError}</p>
  {:else if books.length === 0}
    <p>No books found. Adjust your filters.</p>
  {:else}
    <!-- BOOKS GRID -->
    <div class="grid">
      {#each books as book}
        <article class="card">
          <div class="image">
            <a href={`/book/${book.isbn}`}>
              <img src={cover(book)} alt={book.title} loading="lazy" />
            </a>
          </div>

          <h2 class="title">
            <a href={`/book/${book.isbn}`}>{book.title}</a>
          </h2>

          <p class="author">by {book.author}</p>
          {#if book.genre}<p class="genre">{book.genre}</p>{/if}

          <div class="footer">
            <span class="price">
              ${book.price.toFixed(2)}
            </span>

            {#if book.inventory > 0}
              <span class="stock in">{book.inventory} in stock</span>
            {:else}
              <span class="stock out">Out of Stock</span>
            {/if}
          </div>
        </article>
      {/each}
    </div>
  {/if}
</section>

<style>
  .page { max-width: 1100px; margin: auto; padding: 2rem; }

  .hero { display: flex; justify-content: space-between; align-items: center; }
  .hero-title { font-size: 2rem; font-weight: 700; }
  .hero-subtitle { color: #666; }

  .add-btn,
  .cart-top {
    background: #2563eb;
    color: white;
    padding: 0.6rem 1.2rem;
    border-radius: 30px;
    text-decoration: none;
    font-size: 0.9rem;
  }

  .filters {
    margin-top: 1.5rem;
    padding: 1rem;
    background: #f1f5f9;
    border-radius: 10px;
  }

  .row { display: flex; gap: 1rem; flex-wrap: wrap; }

  .filter { display: flex; flex-direction: column; min-width: 140px; }
  label { font-size: 0.8rem; color: #555; margin-bottom: 2px; }

  select,
  input {
    padding: 0.4rem;
    border-radius: 6px;
    border: 1px solid #ccc;
    font-size: 0.9rem;
  }

  .buttons { display: flex; gap: 0.5rem; align-items: flex-end; margin-left: auto; }
  .apply {
    background: #2563eb;
    color: white;
    padding: 0.5rem 1.3rem;
    border-radius: 6px;
    border: none;
    cursor: pointer;
  }
  .reset {
    background: #e2e8f0;
    padding: 0.5rem 1.1rem;
    border-radius: 6px;
    border: none;
    cursor: pointer;
  }

  .grid {
    margin-top: 2rem;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1.5rem;
  }

  .card { background: white; border: 1px solid #ddd; padding: 1rem; border-radius: 10px; }

  .image img { width: 100%; height: 260px; object-fit: cover; border-radius: 6px; }

  .title { margin: 0.5rem 0; font-size: 1rem; font-weight: 600; }
  .author { color: #555; margin: 0; }
  .genre { color: #888; margin: 0.3rem 0; font-size: 0.85rem; }

  .footer {
    display: flex;
    justify-content: space-between;
    margin-top: 0.5rem;
    align-items: center;
  }
  .price { font-weight: bold; }
  .stock.in { color: #16a34a; font-size: 0.85rem; }
  .stock.out { color: #dc2626; font-size: 0.85rem; }
</style>
