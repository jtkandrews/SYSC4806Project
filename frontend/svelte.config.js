import adapter from '@sveltejs/adapter-static';
import preprocess from 'svelte-preprocess';

export default {
	kit: {
		adapter: adapter({ fallback: 'index.html' }) // SPA fallback for client routes
	},
	preprocess: preprocess(),
	compilerOptions: {
		accessors: true
	},
	onwarn: (warning, handler) => {
		// Suppress specific a11y warnings for modal overlays
		if (warning.code === 'a11y-click-events-have-key-events' ||
			warning.code === 'a11y-no-noninteractive-element-interactions') {
			return;
		}
		handler(warning);
	}
};
