<script lang="ts">
    import { onMount, onDestroy } from "svelte";
    import {goto} from "$app/navigation";

    const DISPLAY_COUNT = 8;

    export let data;
    let books = data.initialBooks || [];

    let bookIndex = 0;
    let timer: number;

    const handleKey = (e: KeyboardEvent) => {
        if (e.key === "ArrowLeft") prevBook();
        if (e.key === "ArrowRight") nextBook();
    };

    // Start slideshow
    onMount(() => {
        changeBook(0);
        timer = setInterval(myTimer, 5000);

        window.addEventListener("keydown", handleKey);
        return () => {
            if (timer) {
                clearInterval(timer);
                timer = 0;
            }
            window.removeEventListener("keydown", handleKey);
        };
    });

    function myTimer() {
        bookIndex++;
    }

    //this causes a race condition  that needs to be fixed
    // function resetTimer() {
    //     clearInterval(timer);
    //     timer = setInterval(myTimer, 5000);
    // }

    function nextBook() {
        bookIndex++;
        // resetTimer();
    }

    function prevBook() {
        bookIndex--;
        // resetTimer();
    }

    function changeBook(n: number) {
        bookIndex = n;
        // resetTimer();
    }

    //On variable change it will change variable value to be within bounds of 0 and display_count
    $: normalizedIndex = ((bookIndex % DISPLAY_COUNT) + DISPLAY_COUNT) % DISPLAY_COUNT;
</script>

<style>
    body {
        margin: 0;
    }

    * {
        box-sizing: border-box;
    }

    /* Title */
    h1 {
        text-align: center;
        top: 20px;
        margin-bottom: 20px;
    }

    img {
        vertical-align: middle;
    }

    /* Position the image container (needed to position the left and right arrows) */
    .container {
        position: relative;
        width: 60%;
        margin: auto;
    }

    /* Subtitle */
    .container h2 {
        text-align: left;
        margin: 0;
        font-size: 18px;
        font-weight: bold;
    }

    /* Hide the Book Card by default */
    .book-card {
        height: 500px;
        width: 100%;
        display: none;
        position: relative;
    }

    /* Set image of book to left 40% */
    .book-card img {
        height: 100%;
        width: 40%;
        object-fit: contain;
        float: left;
    }

    /* Set Description of book to right 60% */
    .book-card-description {
        height: 100%;
        width: 60%;
        float: right;
        padding: 20px;
    }

    h5 {
        margin: 0;
        padding-top: 100px;
        font-size: 18px;
        font-weight: bold;
    }

    p {
        font-size: 14px;
        margin: 10px 0;
    }

    p.price {
        font-size: 28px;
        font-weight: bold;
        color: #d2730e;
    }

    .button-container {
        display: flex;
        justify-content: start;
        align-items: end;
    }

    .add-to-cart-button {
        display: inline-block;
        margin-top: 10px;
        padding: 8px 16px;
        background-color: var(--primary-color);
        color: white;
        text-decoration: none;
        border-radius: 4px;
    }

    /* Change colour when hovering over button */
    .add-to-cart-button:hover {
        background-color: var(--primary-hover);
    }

    /* Add a pointer when hovering over the thumbnail images */
    .cursor {
        cursor: pointer;
    }

    /* Next & previous buttons */
    .prev,
    .next {
        cursor: pointer;
        position: absolute;
        top: 30%;
        width: auto;
        padding: 16px;
        color: black;
        font-weight: bold;
        font-size: 20px;
        border: none;
        background: none;
        user-select: none;
    }

    /* Position the "left button" to the left */
    .prev {
        left: 0;
    }

    /* Position the "next button" to the right */
    .next {
        right: 0;
    }

    /* On hover, add a translucent black background color */
    .prev:hover,
    .next:hover {
        background-color: rgba(0, 0, 0, 0.8);
    }

    /* Number text (1/3 etc) */
    .book-card-number {
        color: black;
        font-size: 12px;
        padding: 8px 12px;
        position: absolute;
        top: 0;
    }

    /* Set container to show caption */
    .caption-container {
        text-align: center;
        background-color: #222;
        padding: 2px 16px;
        color: white;
        width: 100%;
        margin: auto;
    }

    /* Set height for row */
    .row {
        margin: auto;
        height: 200px;
        width: 100%;
        /*background-color: gray;*/
    }

    .row:after {
        content: "";
        display: table;
        clear: both;
    }

    /* 8 columns side by side */
    .column {
        float: left;
        height: 100%;
        width: 12.5%;
    }

    /* Style the thumbnail button wrapper */
    .thumbnail-button {
        padding: 0;             /* remove default button padding */
        border: none;           /* remove button border */
        background: none;       /* remove default button background */
        cursor: pointer;
        height: 100%;           /* match the row/column height */
        width: 100%;            /* match the column width */
        display: block;         /* ensure it behaves like a block for layout */
    }

    /* The image inside the button keeps original styling */
    .thumbnail-button img {
        height: 100%;
        width: 100%;
        opacity: 0.6;
        object-fit: fill;
    }

    /* Hover/active effect remains the same */
    .thumbnail-button.active img,
    .thumbnail-button img:hover {
        opacity: 1;
    }
</style>

<h1>Recommended Books</h1>

<div class="container" >
    <h2>Based on other users</h2>

    <!-- For each loop that will show only selected index and hide the rest -->
    {#each books.slice(0, 8) as book, i}
        <!-- Container for book cards that will be displayed (block) or hidden (none)-->
        <div class="book-card" style="display: {i === normalizedIndex ? 'block' : 'none'};">
            <div class="book-card-number">{i + 1}/8</div>

            <!-- If image url exists show image -->
            {#if book.imageUrl?.trim()}
                <img
                        src={book.imageUrl}
                        alt={`${book.title} cover`}
                        class="book-card-image"
                        loading="lazy"
                />
            {:else} <!-- Else show placeholder image -->
                <div class="book-card-image-placeholder">
                    <span>📚</span>
                </div>
            {/if}

            <!-- Shows book in detail -->
            <div class="book-card-description">
                <h5>{book.title}</h5>
                <p><strong>Author(s):</strong> {book.author}</p>
                <p><strong>Genre:</strong> {book.genre}</p>
                <p><strong>Publisher:</strong> {book.publisher}</p>
                <p><strong>Description:</strong> {book.description}</p>
                <p class="price">${book.price}</p>

                <div class="button-container">
<!--                    <form action="/api/cart" method="post">-->
<!--                        <input type="hidden" name="bookIsbn" value={book.isbn} />-->
                        <button class="add-to-cart-button cursor" type="submit" on:click={() => goto('/cart')}>Add to Cart</button>
<!--                    </form>-->
                </div>
            </div>
        </div>
    {/each}

    <!-- Next and previous buttons to cycle images -->
    <button
            type="button"
            class="prev"
            on:click={prevBook}
    >
        &#10094; <!-- Unicode for left arrow -->
    </button>

    <button
            type="button"
            class="next"
            on:click={nextBook}
    >
        &#10095; <!-- Unicode for right arrow -->
    </button>

    <!-- Image text -->
    <div class="caption-container">
        <p id="caption">{books[normalizedIndex]?.title}</p>
    </div>

    <!-- Thumbnail images -->
    <div class="row">
        {#each books.slice(0,8) as book, i}
            <div class="column">
                <button type="button"
                        class="thumbnail-button {i === normalizedIndex ? 'active' : ''}"
                        on:click={() => changeBook(i)} >
                    <img src={book.imageUrl} alt={book.title} />
                </button>
            </div>
        {/each}
    </div>
</div>