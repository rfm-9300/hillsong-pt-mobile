import { writable } from 'svelte/store';

const isBrowser = typeof window !== 'undefined';

function createAuthStore() {
	const { subscribe, set } = writable(isBrowser && !!localStorage.getItem('authToken'));

	return {
		subscribe,
		login: (token) => {
			if (isBrowser) {
				localStorage.setItem('authToken', token);
				set(true);
			}
		},
		logout: () => {
			if (isBrowser) {
				localStorage.removeItem('authToken');
				set(false);
			}
		}
	};
}

export const auth = createAuthStore();
