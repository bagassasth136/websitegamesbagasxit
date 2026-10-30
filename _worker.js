export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    // Cuma nangkep request dari URL yang ada /api
    if (url.pathname.startsWith('/api')) {
      const headers = {
        "content-type": "application/json",
        "access-control-allow-origin": "*",
        "access-control-allow-headers": "*",
      };

      // Kalau GET (Cek Config)
      if (request.method === "GET") {
        const config = {
          "app": {
            "slug": "sociosxiters",
            "versionLabel": "3.9.8",
            "versionCode": 398
          },
          "maintenance": {
            "active": false
          }
        };
        return new Response(JSON.stringify(config), { headers });
      }

      // Kalau POST (Validasi Key)
      if (request.method === "POST") {
        const success = {
          "ok": true,
          "code": "VALID",
          "access": {
            "permanent": true
          }
        };
        return new Response(JSON.stringify(success), { headers });
      }

      return new Response("Method Not Allowed", { status: 405 });
    }

    // Kalau bukan /api (misal buka web game), biarin lewat
    return env.ASSETS.fetch(request);
  }
}
