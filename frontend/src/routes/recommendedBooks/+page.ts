import {getAllBooks, getAllRecBooks} from '$lib/api';

export const load = async ({ fetch }: { fetch: typeof globalThis.fetch }) => {
    const books = await getAllRecBooks(fetch);
    console.log('Loaded recommended books:', books.length);
    return { recBooks: books };
};
