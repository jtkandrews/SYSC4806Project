// frontend/src/routes/+page.ts
import type { PageLoad } from './$types';

/**
 * We do all book loading + filtering on the client in +page.svelte.
 * This loader just returns an empty object.
 */
export const load: PageLoad = async () => {
	return {};
};
