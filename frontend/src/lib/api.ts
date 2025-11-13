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
    const res = await fetchFn(`${API_BASE}/api/books`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(book)
    });
    if (!res.ok) throw new Error(`Failed to create book (${res.status})`);
    return res.json();
}

