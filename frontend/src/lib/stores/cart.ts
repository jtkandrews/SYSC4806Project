import { derived, get, writable } from 'svelte/store';
import type { Book } from '$lib/types';
import { booksStore } from './books';

export type CartItem = {
    isbn: string;
    title: string;
    price: number;
    quantity: number;
    imageUrl: string;
};

export const cartStore = writable<CartItem[]>([]);

export const cartItemCount = derived(cartStore, (items) =>
    items.reduce((sum, item) => sum + item.quantity, 0)
);

export const cartTotalCost = derived(cartStore, (items) =>
    items.reduce((sum, item) => sum + item.quantity * item.price, 0)
);

export function addToCart(book: Book): void {
    const availableInventory = get(booksStore).find((b) => b.isbn === book.isbn)?.inventory ?? 0;
    if (availableInventory === 0) {
        throw new Error('This book is currently out of stock.');
    }

    const currentItems = get(cartStore);
    const existingItem = currentItems.find((item) => item.isbn === book.isbn);
    const nextQuantity = (existingItem?.quantity ?? 0) + 1;

    if (nextQuantity > availableInventory) {
        throw new Error('Not enough stock available for this book.');
    }

    const updatedItems = existingItem
        ? currentItems.map((item) =>
              item.isbn === book.isbn ? { ...item, quantity: nextQuantity } : item
          )
        : [...currentItems, { isbn: book.isbn, title: book.title, price: book.price, quantity: 1, imageUrl: book.imageUrl }];

    cartStore.set(updatedItems);
}

export function removeFromCart(isbn: string): void {
    const currentItems = get(cartStore);
    cartStore.set(currentItems.filter((item) => item.isbn !== isbn));
}

export function clearCart(): void {
    cartStore.set([]);
}

export function checkout(): void {
    const items = get(cartStore);
    if (items.length === 0) {
        throw new Error('Your cart is empty.');
    }

    const books = get(booksStore);
    const updatedInventoryByIsbn: Record<string, number> = {};

    for (const item of items) {
        const book = books.find((b) => b.isbn === item.isbn);
        if (!book) {
            throw new Error(`The book with ISBN ${item.isbn} is no longer available.`);
        }
        if (item.quantity > book.inventory) {
            throw new Error(`Only ${book.inventory} copies of "${book.title}" remain.`);
        }
        updatedInventoryByIsbn[item.isbn] = book.inventory - item.quantity;
    }

    booksStore.update((current) =>
        current.map((book) =>
            updatedInventoryByIsbn[book.isbn] !== undefined
                ? { ...book, inventory: updatedInventoryByIsbn[book.isbn] }
                : book
        )
    );

    clearCart();
}