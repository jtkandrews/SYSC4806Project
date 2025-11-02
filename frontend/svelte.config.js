import adapter from '@sveltejs/adapter-static';
import preprocess from 'svelte-preprocess';

export default {
	kit: {
		adapter: adapter({ fallback: 'index.html' }) // SPA fallback for client routes
	},
	preprocess: preprocess()
};
