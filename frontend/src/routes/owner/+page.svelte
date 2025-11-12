<script lang="ts">
    // page is already guarded by +page.ts load
</script>

<section class="max-w-3xl mx-auto p-4">
    <h1 class="text-2xl font-bold mb-2">Owner Dashboard</h1>
    <p class="mb-6 opacity-80">
        Only owners can access this page. Use the controls below to manage books.
    </p>

    <div class="border rounded p-4 mb-4">
        <h2 class="font-semibold mb-2">Add / Upload Book</h2>
        <p class="opacity-75">Hook to <code>POST /api/owner/books</code>.</p>
    </div>

    <div class="border rounded p-4">
        <h2 class="font-semibold mb-2">Edit / Delete Books</h2>
        <p class="opacity-75">
            Hook to <code>PUT /api/owner/books/&#123;isbn&#125;</code> and
            <code>DELETE /api/owner/books/&#123;isbn&#125;</code>.
        </p>
    </div>
</section>