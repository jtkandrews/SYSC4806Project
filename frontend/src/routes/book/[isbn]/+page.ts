import type { PageLoad } from './$types';
import { getBookByIsbn } from '$lib/api';

export const load: PageLoad = async ({ params, fetch }) => {
    const book = await getBookByIsbn(params.isbn, fetch);
    return { book };
};
