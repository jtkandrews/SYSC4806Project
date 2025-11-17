<script lang="ts">
  import { onMount } from 'svelte';
  import { role } from '$lib/session';

  // Book type
  type Book = {
    isbn: string;
    title: string;
    author: string;
    genre?: string;
    description?: string;
    price: number;
    inventory: number;
    imageUrl?: string;
  };

  // UI State
  let books: Book[] = [];
  let sortBy = 'price';
  let order = 'asc';
  let genre = 'All';
  let minPrice = '';
  let maxPrice = '';

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

  async function loadBooks() {
    const params = new URLSearchParams();

    if (sortBy) params.set('sortBy', sortBy);
    if (order) params.set('order', order);
    if (genre !== 'All') params.set('genre', genre);
    if (minPrice) params.set('minPrice', minPrice);
    if (maxPrice) params.set('maxPrice', maxPrice);

    const url = `http://localhost:8080/api/books?${params.toString()}`;

    try {
      const res = await fetch(url);
      books = await res.json();
    } catch (err) {
      console.error('Error loading books', err);
      books = [];
    }
  }

  function resetFilters() {
    sortBy = 'price';
    order = 'asc';
    genre = 'All';
    minPrice = '';
    maxPrice = '';
    loadBooks();
  }

  function cover(book: Book) {
    if (book.imageUrl && book.imageUrl.trim() !== '') return book.imageUrl;
    return `https://covers.openlibrary.org/b/isbn/${book.isbn}-L.jpg`;
  }

  onMount(loadBooks);
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
    {/if}
  </header>

  <!-- FILTER BAR -->
  <div class="filters">
    <div class="row">

      <div class="filter">
        <label>Sort By</label>
        <select bind:value={sortBy}>
          <option value="price">Price</option>
          <option value="title">Title</option>
          <option value="inventory">Inventory</option>
        </select>
      </div>

      <div class="filter">
        <label>Order</label>
        <select bind:value={order}>
          <option value="asc">Ascending</option>
          <option value="desc">Descending</option>
        </select>
      </div>

      <div class="filter">
        <label>Genre</label>
        <select bind:value={genre}>
          {#each GENRES as g}
            <option value={g}>{g}</option>
          {/each}
        </select>
      </div>

      <div class="filter">
        <label>Min Price</label>
        <input type="number" bind:value={minPrice}/>
      </div>

      <div class="filter">
        <label>Max Price</label>
        <input type="number" bind:value={maxPrice}/>
      </div>

      <div class="buttons">
        <button class="apply" on:click={loadBooks}>Apply</button>
        <button class="reset" on:click={resetFilters}>Reset</button>
      </div>
    </div>
  </div>

  <!-- BOOKS GRID -->
  {#if books.length === 0}
    <p>No books found with these filters.</p>
  {:else}
    <div class="grid">
      {#each books as book}
        <article class="card">
          <div class="image">
            <img src={cover(book)} alt={book.title} loading="lazy">
          </div>

          <h2 class="title">
            <a href={`/book/${book.isbn}`}>{book.title}</a>
          </h2>

          <p class="author">{book.author}</p>
          {#if book.genre}<p class="genre">{book.genre}</p>{/if}

          <div class="footer">
            <span class="price">${book.price}</span>

            {#if book.inventory > 0}
              <span class="stock in">In Stock</span>
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

  .add-btn {
    background: #2563eb;
    color: white;
    padding: 0.6rem 1.2rem;
    border-radius: 30px;
    text-decoration: none;
  }

  .filters {
    margin-top: 1.5rem;
    padding: 1rem;
    background: #f1f5f9;
    border-radius: 10px;
  }

  .row { display: flex; gap: 1rem; flex-wrap: wrap; }

  .filter { display: flex; flex-direction: column; min-width: 120px; }
  label { font-size: 0.8rem; color: #555; margin-bottom: 2px; }

  select, input {
    padding: 0.4rem;
    border-radius: 6px;
    border: 1px solid #ccc;
  }

  .buttons { display: flex; gap: 0.5rem; align-items: flex-end; margin-left: auto; }
  .apply { background: #2563eb; color: white; padding: 0.5rem 1rem; border-radius: 6px; border: none; }
  .reset { background: #e2e8f0; padding: 0.5rem 1rem; border-radius: 6px; border: none; }

  .grid { margin-top: 2rem; display: grid; grid-template-columns: repeat(auto-fill,minmax(200px,1fr)); gap: 1.5rem; }

  .card { background: white; border: 1px solid #ddd; padding: 1rem; border-radius: 10px; }

  .image img { width: 100%; height: 260px; object-fit: cover; border-radius: 6px; }

  .title { margin: 0.5rem 0; font-size: 1rem; font-weight: 600; }
  .author { color: #555; margin: 0; }
  .genre { color: #888; margin: 0.3rem 0; }

  .footer { display: flex; justify-content: space-between; margin-top: 0.5rem; }
  .price { font-weight: bold; }
  .stock.in { color: #16a34a; }
  .stock.out { color: #dc2626; }
</style>
