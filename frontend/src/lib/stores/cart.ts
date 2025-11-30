import { derived, get, writable } from 'svelte/store';
import { booksStore } from './books';
import {
    checkoutCart,
    deleteCartItem,
    fetchCart,
    setCartItemQuantity,
    type CartItemResponse,
    type OrderResponse
} from '$lib/api';
import type { Book } from '$lib/types';

export type CartItem = {
    isbn: string;
    title: string;
    price: number;
    quantity: number;
    imageUrl: string;
};

export const cartStore = writable<CartItem[]>([]);
export const cartLoaded = writable(false);

export const cartItemCount = derived(cartStore, (items) =>
    items.reduce((sum, item) => sum + item.quantity, 0)
);

export const cartTotalCost = derived(cartStore, (items) =>
    items.reduce((sum, item) => sum + item.quantity * item.price, 0)
);

export async function loadCart(fetchFn: typeof fetch = fetch): Promise<void> {
    const items = await fetchCart(fetchFn);
    applyCart(items);
    cartLoaded.set(true);
}

export function resetCart(): void {
    cartStore.set([]);
    cartLoaded.set(false);
}

export async function addToCart(book: Book, quantity = 1): Promise<void> {
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

    const updated = await setCartItemQuantity(book.isbn, nextQuantity);
    applyCart(updated);
}

export async function updateCartItemQuantity(isbn: string, quantity: number): Promise<void> {
    if (!Number.isInteger(quantity) || quantity < 0) {
        throw new Error('Quantity must be zero or a positive whole number.');
    }
    if (quantity === 0) {
        await removeFromCart(isbn);
        return;
    }

    const availableInventory = get(booksStore).find((b) => b.isbn === isbn)?.inventory ?? 0;

    if (quantity > availableInventory) {
        throw new Error(`Only ${availableInventory} copies remain in stock.`);
    }
    const updated = await setCartItemQuantity(isbn, quantity);
    applyCart(updated);
}


export async function removeFromCart(isbn: string): Promise<void> {
    const updated = await deleteCartItem(isbn);
    applyCart(updated);
}

export function clearCart(): void {
    cartStore.set([]);
}

export async function checkout(): Promise<OrderResponse> {
    const response = await checkoutCart();
    booksStore.update((current) => {
        const inventoryMap = new Map(response.updatedBooks.map((book) => [book.isbn, book.inventory]));
        return current.map((book) =>
            inventoryMap.has(book.isbn) ? { ...book, inventory: inventoryMap.get(book.isbn) ?? book.inventory } : book
        );
    });
    clearCart();
    return response.order;
}

function applyCart(items: CartItemResponse[]): void {
    cartStore.set(
        items.map((item) => ({
            isbn: item.isbn,
            title: item.title,
            price: item.price,
            quantity: item.quantity,
            imageUrl: item.imageUrl
        }))

    );
}