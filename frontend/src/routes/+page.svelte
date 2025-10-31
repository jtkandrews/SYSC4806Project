<script>
  export let data;
  const books = data.initialBooks || [];
</script>

<div style="max-width: 1200px; margin: 2rem auto; padding: 0 1rem;">
  <h1 style="margin-bottom: 1.5rem">Books</h1>
  <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1rem;">
    {#each books as book}
      <a href="/book/{book.isbn}" style="text-decoration: none; color: inherit; display: block; padding: 1rem; border: 1px solid #ddd; border-radius: 4px; transition: box-shadow 0.2s;">
        <h3 style="margin: 0 0 0.5rem 0; font-size: 1.1rem;">{book.title}</h3>
        <p style="margin: 0 0 0.5rem 0; color: #666; font-size: 0.9rem;">{book.author}</p>
        <p style="margin: 0; font-weight: 600; color: #0066cc;">
          {book.price.toLocaleString(undefined, {style: 'currency', currency: 'USD'})}
        </p>
      </a>
    {/each}
  </div>
</div>

