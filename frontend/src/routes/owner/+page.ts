import type { PageLoad } from './$types';
import { redirect } from '@sveltejs/kit';

export const load: PageLoad = async ({ fetch }) => {
    const r = await fetch('/api/auth/me', { credentials: 'include' });
    if (!r.ok) throw redirect(302, '/');
    const { role } = await r.json();
    if (role !== 'OWNER') throw redirect(302, '/');
    return {};
};
