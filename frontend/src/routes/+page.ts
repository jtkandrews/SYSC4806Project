import type { PageLoad } from './$types';
import { getAllBooks } from '$lib/api';

// export const load: PageLoad = async ({ fetch }) => {
//     const books = await getAllBooks(fetch);
//     return { initialBooks: books };
// };
export const load: PageLoad = async ({ fetch }) => {
    const books = await getAllBooks(fetch);
    console.log('Loaded books:', books.length);
    return { initialBooks: books };
};
