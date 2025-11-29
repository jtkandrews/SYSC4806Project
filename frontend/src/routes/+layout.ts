import type { LayoutLoad } from './$types';
import { redirect } from '@sveltejs/kit';

export const load: LayoutLoad = async ({ fetch, url }) => {
    const onLoginPage = url.pathname === '/login';
    const res = await fetch('/api/auth/me', { credentials: 'include' });

    if (!res.ok) {
        // Only the login page can be viewed without a session
        if (!onLoginPage) {
            throw redirect(302, '/login');
        }
        return { session: null, showNav: false };
    }

    const data = await res.json();
    const hasSession = data && data.role && data.username;

    if (!hasSession) {
        if (!onLoginPage) {
            throw redirect(302, '/login');
        }
        return { session: null, showNav: false };
    }

    // Already logged in but visiting /login
    if (onLoginPage) {
        throw redirect(302, '/');
    }

    return {
        session: {
            id: data.id,
            username: data.username,
            role: data.role === 'OWNER' ? 'OWNER' : 'USER'
        },
        showNav: true
    };
};
