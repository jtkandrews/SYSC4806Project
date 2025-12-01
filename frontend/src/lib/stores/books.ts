import { writable, get } from 'svelte/store';
import type { Book } from '$lib/types';

export const booksStore = writable<Book[]>([]);

export function setBooks(books: Book[]): void {
    booksStore.set(books);
}

export function addBookToStore(book: Book): void {
    booksStore.update((current) => [book, ...current]);
}

export function updateBookInventory(isbn: string, inventory: number): void {
    booksStore.update((current) =>
        current.map((book) => (book.isbn === isbn ? { ...book, inventory } : book))
    );
}

export function findBookInStore(isbn: string): Book | undefined {
    return get(booksStore).find((book) => book.isbn === isbn);
}
