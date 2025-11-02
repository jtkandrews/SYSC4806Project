<script>
  import '../app.css';
</script>

<header>
  <nav>
    <a href="/" class="logo">
      <span></span>
      <span> Amazin Bookstore</span>
    </a>
  </nav>
</header>

<main>
  <slot />
</main>

<style>
  main {
    min-height: calc(100vh - 80px);
  }
</style>
