// frontend/src/routes/+page.ts
import type { PageLoad } from './$types';

export const load: PageLoad = async ({ fetch, url }) => {
    const params = new URLSearchParams();

    const genre = url.searchParams.get('genre') ?? '';
    const minPrice = url.searchParams.get('minPrice') ?? '';
    const maxPrice = url.searchParams.get('maxPrice') ?? '';
    const sortBy = url.searchParams.get('sortBy') ?? 'title';
    const order = url.searchParams.get('order') ?? 'asc';

    if (genre) params.set('genre', genre);
    if (minPrice) params.set('minPrice', minPrice);
    if (maxPrice) params.set('maxPrice', maxPrice);
    if (sortBy) params.set('sortBy', sortBy);
    if (order) params.set('order', order);

    const query = params.toString();

    const res = await fetch(`http://localhost:8080/api/books${query ? `?${query}` : ''}`, {
        credentials: 'include'
    });

    if (!res.ok) {
        return { books: [] };
    }

    const books = await res.json();
    return { books };
};
