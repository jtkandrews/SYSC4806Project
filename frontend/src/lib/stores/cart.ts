import { derived, get, writable } from 'svelte/store';
import { checkoutCart } from '$lib/api';
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

export function addToCart(book: Book, quantity = 1): void {
    if (!Number.isInteger(quantity) || quantity <= 0) {
        throw new Error('Quantity must be at least 1.');
    }

    const availableInventory =
        get(booksStore).find((b) => b.isbn === book.isbn)?.inventory ?? book.inventory ?? 0;
    if (availableInventory === 0) {
        throw new Error('This book is currently out of stock.');
    }

    const currentItems = get(cartStore);
    const existingItem = currentItems.find((item) => item.isbn === book.isbn);
    const nextQuantity = (existingItem?.quantity ?? 0) + quantity;

    if (nextQuantity > availableInventory) {
        throw new Error(`Only ${availableInventory} copies of "${book.title}" remain.`);
    }

    const updatedItems = existingItem
        ? currentItems.map((item) =>
              item.isbn === book.isbn ? { ...item, quantity: nextQuantity } : item
          )
          : [
            ...currentItems,
            {
                isbn: book.isbn,
                title: book.title,
                price: book.price,
                quantity,
                imageUrl: book.imageUrl
            }
        ];
    cartStore.set(updatedItems);
}

export function updateCartItemQuantity(isbn: string, quantity: number): void {
    if (!Number.isInteger(quantity) || quantity < 0) {
        throw new Error('Quantity must be zero or a positive whole number.');
    }

    const availableInventory = get(booksStore).find((b) => b.isbn === isbn)?.inventory ?? 0;

    if (quantity > availableInventory) {
        throw new Error(`Only ${availableInventory} copies remain in stock.`);
    }

    const currentItems = get(cartStore);
    const existingItem = currentItems.find((item) => item.isbn === isbn);

    if (!existingItem) {
        throw new Error('This item is not in your cart.');
    }

    if (quantity === 0) {
        removeFromCart(isbn);
        return;
    }

    cartStore.set(
        currentItems.map((item) =>
            item.isbn === isbn
                ? {
                      ...item,
                      quantity
                  }
                : item
        )
    );
}


export function removeFromCart(isbn: string): void {
    const currentItems = get(cartStore);
    cartStore.set(currentItems.filter((item) => item.isbn !== isbn));
}

export function clearCart(): void {
    cartStore.set([]);
}

export async function checkout(): Promise<void> {
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
    const updatedBooks = await checkoutCart(
        items.map((item) => ({ isbn: item.isbn, quantity: item.quantity }))
    );

    for (const book of updatedBooks) {
        updatedInventoryByIsbn[book.isbn] = book.inventory;
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