/** @type {import('@sveltejs/kit').Handle} */
export function handle({ event, resolve }) {
    return resolve(event, {
        filterSerializedResponseHeaders: (name) => {
            return name === 'content-type' || name === 'content-length';
        }
    });
}
