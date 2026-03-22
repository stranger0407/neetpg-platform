const CACHE_NAME = 'neetpg-v2-azure';
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/manifest.json',
];

const APP_SHELL = '/index.html';

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(STATIC_ASSETS))
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
    )
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  const { request } = event;
  const requestUrl = new URL(request.url);
  const isSameOrigin = requestUrl.origin === self.location.origin;

  // Skip non-GET requests
  if (request.method !== 'GET') return;

  // Let cross-origin requests pass through untouched.
  if (!isSameOrigin) return;

  // For API requests, try network first, then cache
  if (requestUrl.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(request)
        .then((response) => {
          const clone = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(request, clone));
          return response;
        })
        .catch(() => caches.match(request).then((cached) => cached || new Response(null, { status: 503, statusText: 'Offline' })))
    );
    return;
  }

  // For navigation requests (SPA routes like /subjects, /practice, etc.)
  // use network first; if offline, serve cached app shell.
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request)
        .catch(() => caches.match(APP_SHELL))
        .then((response) => response || new Response('Offline', { status: 503, statusText: 'Offline' }))
    );
    return;
  }

  // For actual static assets (JS, CSS, images), try cache first, then network
  event.respondWith(
    caches.match(request).then((cached) => {
      if (cached) return cached;
      return fetch(request)
        .then((response) => {
          if (response.ok) {
            const clone = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(request, clone));
          }
          return response;
        })
        .catch(() => new Response(null, { status: 504, statusText: 'Gateway Timeout' }));
    })
  );
});
