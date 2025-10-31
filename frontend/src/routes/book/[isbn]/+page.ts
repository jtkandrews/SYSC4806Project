import { getBookByIsbn } from '$lib/api';

export const load = async ({ params, fetch }: { params: { isbn: string }, fetch: typeof globalThis.fetch }) => {
    const book = await getBookByIsbn(params.isbn, fetch);
    return { book };
};
