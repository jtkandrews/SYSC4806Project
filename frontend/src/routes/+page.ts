import { getAllBooks } from '$lib/api';

export const load = async ({ fetch }: { fetch: typeof globalThis.fetch }) => {
    const books = await getAllBooks(fetch);
    console.log('Loaded books:', books.length);
    return { initialBooks: books };
};
