import { getAllBooks } from '$lib/api';
import type { PageLoad } from './$types';

export const load: PageLoad = async ({ fetch, url }) => {
    const sortBy = (url.searchParams.get('sortBy') as 'title' | 'price' | 'inventory') ?? 'title';
    const order = (url.searchParams.get('order') as 'asc' | 'desc') ?? 'asc';

    const books = await getAllBooks(fetch, sortBy, order);
    return { initialBooks: books, sortBy, order };
};
