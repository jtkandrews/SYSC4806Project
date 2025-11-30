import type { PageLoad } from './$types';
import { fetchOrders } from '$lib/api';


export const load: PageLoad = (async ({ fetch }) => {
    const orders = await fetchOrders(fetch);
    return {orders};
}) satisfies PageLoad;
