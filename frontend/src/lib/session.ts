import { writable } from 'svelte/store';

export type Role = 'USER' | 'OWNER';

export interface SessionUser {
    id: number;
    username: string;
    role: Role;
}

export const user = writable<SessionUser | null>(null);

export const role = {
    subscribe: (run: (value: Role) => void) =>
        user.subscribe(u => run(u?.role ?? 'USER'))
};

export async function refreshSession(fetchFn: typeof fetch = fetch): Promise<void> {
    try {
        const r = await fetchFn('/api/auth/me', { credentials: 'include' });

        if (!r.ok) {
            user.set(null);
            return;
        }

        const data = await r.json();
        if (!data || !data.role) {
            user.set(null);
            return;
        }

        user.set({
            id: data.id,
            username: data.username,
            role: data.role === 'OWNER' ? 'OWNER' : 'USER'
        });

    } catch (err) {
        console.error('refreshSession failed:', err);
        user.set(null);
    }
}

// Keep backward compatibility
export const refreshRole = refreshSession;


export async function userLogin(username: string, password: string, fetchFn: typeof fetch = fetch): Promise<void> {
    const r = await fetchFn('/api/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({ username, password })
    });

    if (!r.ok) {
        throw new Error('Invalid username or password');
    }

    await refreshSession(fetchFn);
}


export async function ownerLogin(password: string, fetchFn: typeof fetch = fetch): Promise<void> {
    const r = await fetchFn('/api/auth/owner-login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ password })
    });

    if (!r.ok) {
        throw new Error('Incorrect owner password');
    }

    await refreshSession(fetchFn);
}


export async function logout(fetchFn: typeof fetch = fetch): Promise<void> {
    await fetchFn('/api/auth/logout', {
        method: 'POST',
        credentials: 'include'
    });

    user.set(null);
}