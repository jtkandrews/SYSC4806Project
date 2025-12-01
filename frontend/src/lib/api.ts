// src/lib/api.ts
import type { Book } from './types';

// In dev, Vite proxy forwards /api -> http://localhost:8080
// In prod (single JAR), the frontend is served by Spring Boot on the same origin.
// Leaving API_BASE empty makes fetch('/api/...') hit the same server.
const API_BASE = '';

export async function getAllBooks(fetchFn: typeof fetch = fetch): Promise<Book[]> {
    const res = await fetchFn(`${API_BASE}/api/books`);
    if (!res.ok) throw new Error(`Failed to load books (${res.status})`);
    return res.json();
}

export async function getBookByIsbn(isbn: string, fetchFn: typeof fetch = fetch): Promise<Book> {
    const res = await fetchFn(`${API_BASE}/api/books/${encodeURIComponent(isbn)}`);
    if (!res.ok) throw new Error(`Book ${isbn} not found (${res.status})`);
    return res.json();
}

export async function searchBooks(query: string, fetchFn: typeof fetch = fetch): Promise<Book[]> {
    const params = new URLSearchParams({ query });
    const res = await fetchFn(`${API_BASE}/api/books?${params.toString()}`); // if your backend uses /search, switch line below
    // const res = await fetchFn(`${API_BASE}/api/books/search?${params.toString()}`);
    if (!res.ok) throw new Error(`Search failed (${res.status})`);
    return res.json();
}

export async function createBook(book: Partial<Book>, fetchFn: typeof fetch = fetch): Promise<Book> {
    const res = await fetchFn(`${API_BASE}/api/owner/books`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(book)
    });
    if (!res.ok) throw new Error(`Failed to create book (${res.status})`);
    return res.json();
}

export async function getAllRecBooks(fetchFn: typeof fetch = fetch): Promise<Book[]> {
    const res = await fetchFn(`${API_BASE}/api/books/recommended_books`);
    if (!res.ok) throw new Error(`Failed to load recommended books (${res.status})`);
    return res.json();
}

export type CartItemResponse = {
    isbn: string;
    title: string;
    price: number;
    quantity: number;
    imageUrl: string;
    inventory: number;
};

export async function fetchCart(fetchFn: typeof fetch = fetch): Promise<CartItemResponse[]> {
    const res = await fetchFn(`${API_BASE}/api/cart`, { credentials: 'include' });
    if (!res.ok) {
        throw new Error('Failed to load cart');
    }
    return res.json();
}

export async function setCartItemQuantity(
    isbn: string,
    quantity: number,
    fetchFn: typeof fetch = fetch
    ): Promise<CartItemResponse[]> {
        const res = await fetchFn(`${API_BASE}/api/cart/items`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ isbn, quantity })
        });
    if (!res.ok) {
        const message = await extractErrorMessage(res, 'Unable to update cart');
        throw new Error(message);
    }

    return res.json();
}

export async function deleteCartItem(
    isbn: string,
    fetchFn: typeof fetch = fetch
): Promise<CartItemResponse[]> {
    const res = await fetchFn(`${API_BASE}/api/cart/items/${encodeURIComponent(isbn)}`, {
        method: 'DELETE',
        credentials: 'include'
    });

    if (!res.ok) {
        const message = await extractErrorMessage(res, 'Unable to remove item');
        throw new Error(message);
    }

    return res.json();
}

export type OrderItemResponse = {
    isbn: string;
    title: string;
    price: number;
    quantity: number;
    imageUrl: string;
};

export type OrderResponse = {
    id: number;
    createdAt: string;
    items: OrderItemResponse[];
};

export type CheckoutResponse = {
    order: OrderResponse;
    updatedBooks: Book[];
};

export async function checkoutCart(fetchFn: typeof fetch = fetch): Promise<CheckoutResponse> {
    const res = await fetchFn(`${API_BASE}/api/cart/checkout`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include'
    });

    if (!res.ok) {
        const message = await extractErrorMessage(res, `Checkout failed (${res.status})`);
        throw new Error(message);
    }

    return res.json();
}

export async function fetchOrders(fetchFn: typeof fetch = fetch): Promise<OrderResponse[]> {
    const res = await fetchFn(`${API_BASE}/api/orders`, { credentials: 'include' });
    if (!res.ok) {
        const message = await extractErrorMessage(res, 'Unable to load orders');
        throw new Error(message);
    }
    return res.json();
}

async function extractErrorMessage(res: Response, fallback: string): Promise<string> {
    try {
        const data = await res.json();
        if (typeof (data as { message?: string })?.message === 'string') {
            return (data as { message: string }).message;
        }
    } catch {
        // ignore
    }

    try {
        const text = await res.text();
        if (text) return text;
    } catch {
        // ignore
    }

    return fallback;
}


