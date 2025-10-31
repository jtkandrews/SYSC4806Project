<script>
  export let data;
  const b = data.book;
</script>

<div style="max-width: 800px; margin: 2rem auto; padding: 0 1rem;">
  <a href="/" style="text-decoration:underline">← Back</a>
  <h1 style="margin:.5rem 0">{b.title}</h1>
  <p style="color:#555">{b.author}{#if b.publisher} — {b.publisher}{/if}</p>
  <p style="margin-top:.75rem">{b.description}</p>
  <p style="margin-top:.75rem; font-weight:600">
    {b.price.toLocaleString(undefined,{style:'currency',currency:'USD'})}
  </p>
  <p style="color:#666; font-size:.9rem">ISBN: {b.isbn} • In stock: {b.inventory}</p>
</div>
