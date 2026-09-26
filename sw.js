// sw.js
const CACHE_NAME = 'bagasxit-v1';
const urlsToCache = [
  '.',
  'index.html',
  'Minecraft.jpg',
  'free fire.jpg',
  'Epic Conquest 2.jpg',
  'Subway Surfers.jpg',
  'Stumble Guys.jpg',
  'DeadTarget.jpg',
  'DroneShadow.jpg',
  'DynamonsWorld.jpg',
  'EpicPlane.jpg',
  'FalloutShelter.jpg',
  'FrostWorld.jpg',
  'GalaxyAttack.jpg',
  'GoingBalls.jpg',
  'HomeScapes.jpg',
  'HouseDesigner.jpg',
  'HunterAssassin.jpg',
  'LastDayEarth.jpg',
  'MadSkillBmx.jpg',
  'MagicTilles.jpg',
  'Monopoly.jpg',
  'monoposto.jpg',
  'TalkingAngela.jpg',
  'TalkingHank.jpg',
  'whatsapp.png',
  'telegram.png'
];

self.addEventListener('install', e => {
  e.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(keys => {
      return Promise.all(keys.map(key => {
        if (key !== CACHE_NAME) return caches.delete(key);
      }));
    })
  );
});

self.addEventListener('fetch', e => {
  e.respondWith(
    caches.match(e.request)
      .then(response => response || fetch(e.request))
  );
});