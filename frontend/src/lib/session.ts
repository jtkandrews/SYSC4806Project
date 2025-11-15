// Cookie-backed session helpers for the real owner mode.
// We never read cookies on the client (HttpOnly). Instead we ask the backend
// via /api/auth/me and store the role in a Svelte store for UI convenience.

import { writable } from 'svelte/store';

export type Role = 'USER' | 'OWNER';

export const role = writable<Role>('USER');

export async function refreshRole(fetchFn: typeof fetch = fetch): Promise<void> {
    try {
        const r = await fetchFn('/api/auth/me', { credentials: 'include' });
        if (!r.ok) {
            role.set('USER');
            return;
        }
        const data = (await r.json()) as { role?: string };
        role.set(data.role === 'OWNER' ? 'OWNER' : 'USER');
    } catch {
        role.set('USER');
    }
}

export async function ownerLogin(password: string, fetchFn: typeof fetch = fetch): Promise<void> {
    const r = await fetchFn('/api/auth/owner-login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include', // include cookie set by server
        body: JSON.stringify({ password })
    });
    if (!r.ok) {
        throw new Error('Incorrect owner password');
    }
    await refreshRole(fetchFn);
}

export async function logout(fetchFn: typeof fetch = fetch): Promise<void> {
    await fetchFn('/api/auth/logout', {
        method: 'POST',
        credentials: 'include'
    });
    await refreshRole(fetchFn);
}
