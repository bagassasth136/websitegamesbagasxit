export async function onRequest(context) {
    // Tangkap request
    const request = context.request;
    const url = new URL(request.url);

    // 1. CORS biar nggak diblokir
    const headers = {
        "content-type": "application/json",
        "access-control-allow-origin": "*",
        "access-control-allow-headers": "*",
    };

    // 2. Kalau GET (Cek Config) -> balikin config palsu
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

    // 3. Kalau POST (Validasi Key) -> balikin key valid
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

    // Default kalau method lain
    return new Response("Method Not Allowed", { status: 405 });
}
