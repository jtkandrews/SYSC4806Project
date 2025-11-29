import type { LayoutLoad } from './$types';
import { redirect } from '@sveltejs/kit';

export const load: LayoutLoad = async ({ fetch, url }) => {
    const res = await fetch('/api/auth/me', { credentials: 'include' });

    if (!res.ok) {
        // Only the login page can be viewed without a session
        if (url.pathname !== '/login') {
            throw redirect(302, '/login');
        }
        return { session: null };
    }

    const data = await res.json();
    const hasSession = data && data.role && data.username;

    if (!hasSession) {
        if (url.pathname !== '/login') {
            throw redirect(302, '/login');
        }
        return { session: null };
    }

    // Already logged in but visiting /login
    if (url.pathname === '/login') {
        throw redirect(302, '/');
    }

    return {
        session: {
            id: data.id,
            username: data.username,
            role: data.role === 'OWNER' ? 'OWNER' : 'USER'
        }
    };
};
