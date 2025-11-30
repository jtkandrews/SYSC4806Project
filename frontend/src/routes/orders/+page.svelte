<script lang="ts">
    import type { OrderResponse } from '$lib/api';

    export let data: { orders: OrderResponse[] };

    const orders = data.orders ?? [];

    const formatDate = (value: string) => new Date(value).toLocaleString();
    const orderTotal = (order: OrderResponse) =>
        order.items.reduce((sum, item) => sum + item.price * item.quantity, 0);
</script>

<div class="orders-container">
    <h1>Order History</h1>

    {#if orders.length === 0}
        <div class="empty-state">
            <p>You haven't placed any orders yet.</p>
            <a class="link" href="/">Browse books</a>
        </div>
    {:else}
        <div class="orders-grid">
            {#each orders as order}
                <section class="order-card">
                    <div class="order-header">
                        <div>
                            <p class="order-label">Order</p>
                            <h2>#{order.id}</h2>
                        </div>
                        <div>
                            <p class="order-label">Placed</p>
                            <p class="order-meta">{formatDate(order.createdAt)}</p>
                        </div>
                        <div>
                            <p class="order-label">Total</p>
                            <p class="order-meta">{orderTotal(order).toLocaleString(undefined, { style: 'currency', currency: 'USD' })}</p>
                        </div>
                    </div>

                    <div class="order-items">
                        {#each order.items as item}
                            <div class="order-item">
                                <div class="order-cover">
                                    {#if item.imageUrl}
                                        <img src={item.imageUrl} alt={`Cover for ${item.title}`} />
                                    {:else}
                                        <span class="order-cover-placeholder">📚</span>
                                    {/if}
                                </div>
                                <div class="order-details">
                                    <p class="order-title">{item.title}</p>
                                    <p class="order-meta">ISBN {item.isbn}</p>
                                    <p class="order-meta">{item.quantity} × {item.price.toLocaleString(undefined, { style: 'currency', currency: 'USD' })}</p>
                                </div>
                            </div>
                        {/each}
                    </div>
                </section>
            {/each}
        </div>
    {/if}
</div>

<style>
    .orders-container {
        max-width: 960px;
        margin: 0 auto;
        padding: 2rem 1.5rem;
    }

    h1 {
        font-size: 2rem;
        margin-bottom: 1.5rem;
    }

    .orders-grid {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .order-card {
        background: #fff;
        border: 1px solid #e5e7eb;
        border-radius: 0.75rem;
        box-shadow: 0 8px 30px -20px rgba(0, 0, 0, 0.3);
        padding: 1.25rem;
    }

    .order-header {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
        gap: 1rem;
        border-bottom: 1px solid #e5e7eb;
        padding-bottom: 1rem;
        margin-bottom: 1rem;
    }

    .order-label {
        margin: 0;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        font-size: 0.75rem;
        color: #6b7280;
    }

    .order-meta {
        margin: 0.1rem 0 0;
        color: #111827;
        font-weight: 600;
    }

    .order-items {
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
    }

    .order-item {
        display: grid;
        grid-template-columns: auto 1fr;
        gap: 0.75rem;
        align-items: center;
    }

    .order-cover {
        width: 72px;
        height: 90px;
        border: 1px solid #e5e7eb;
        border-radius: 0.5rem;
        overflow: hidden;
        display: grid;
        place-items: center;
        background: #f9fafb;
    }

    .order-cover img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .order-cover-placeholder {
        font-size: 1.4rem;
    }

    .order-details {
        display: flex;
        flex-direction: column;
        gap: 0.1rem;
    }

    .order-title {
        margin: 0;
        font-weight: 700;
        color: #111827;
    }

    .order-meta {
        margin: 0;
    }

    .empty-state {
        background: #fff;
        border: 1px dashed #cbd5e1;
        border-radius: 0.75rem;
        padding: 1.5rem;
        text-align: center;
        color: #475569;
    }

    .link {
        color: #2563eb;
        font-weight: 600;
        text-decoration: none;
    }

    .link:hover {
        text-decoration: underline;
    }
</style>