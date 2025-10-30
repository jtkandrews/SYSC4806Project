import type { Book } from './types';

const API_BASE = import.meta.env.VITE_API_BASE ?? ''; // dev uses proxy

export async function getAllBooks(fetchFn: typeof fetch = fetch): Promise<Book[]> {
    const res = await fetchFn(`${API_BASE}/api/books`);
    if (!res.ok) throw new Error(`Failed to load books (${res.status})`);
    return res.json();
}

export async function getBookByIsbn(isbn: string, fetchFn: typeof fetch = fetch): Promise<Book> {
    const res = await fetchFn(`${API_BASE}/api/books/${encodeURIComponent(isbn)}`);
    if (!res.ok) throw new Error(`Book ${isbn} not found`);
    return res.json();
}

export async function searchBooks(query: string, fetchFn: typeof fetch = fetch): Promise<Book[]> {
    const res = await fetchFn(`${API_BASE}/api/books/search?` + new URLSearchParams({ query }));
    if (!res.ok) throw new Error(`Search failed (${res.status})`);
    return res.json();
}
