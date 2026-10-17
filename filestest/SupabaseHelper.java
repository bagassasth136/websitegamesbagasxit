package com.apkadmin.bagasxit;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupabaseHelper {

    public static final String SB_URL = "https://qlzwqbbvjscfjaicbhks.supabase.co";
    public static final String SB_ANON = "sb_publishable_DRIOqI7O90TinECl4K6FJA_l0nIaPCY";
    public static final String ADMIN_PASS = "BAGASXIT 2006";
    public static final String ADMIN_API = SB_URL + "/functions/v1/admin-api";

    private static final ExecutorService EXEC = Executors.newFixedThreadPool(4);
    private static boolean initialized = false;

    public static void initAuth(Context ctx) {
        initialized = true;
    }

    public static boolean isAuthReady() {
        return initialized;
    }

    public static void waitForAuth(final Runnable callback) {
        callback.run();
    }

    public interface OnComplete {
        void onResult(boolean success, String message);
    }

    public interface OnData {
        void onResult(boolean success, JSONArray data, String message);
    }

    public interface OnSingleData {
        void onResult(boolean success, JSONObject data, String message);
    }

    private static String callAdminApi(JSONObject payload) throws Exception {
        URL url = new URL(ADMIN_API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", SB_ANON);
        conn.setRequestProperty("Authorization", "Bearer " + SB_ANON);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(90000);
        conn.setDoOutput(true);

        payload.put("pass", ADMIN_PASS);

        OutputStream os = conn.getOutputStream();
        os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        int code = conn.getResponseCode();
        BufferedReader reader;
        if (code >= 200 && code < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            reader = new BufferedReader(new InputStreamReader(
                conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(),
                StandardCharsets.UTF_8));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();
        if (code < 200 || code >= 300) {
            throw new Exception("HTTP " + code + ": " + sb.toString());
        }
        return sb.toString();
    }

    private static void callAsync(final JSONObject payload, final OnComplete cb) {
        EXEC.execute(new Runnable() {
            @Override public void run() {
                try {
                    callAdminApi(payload);
                    if (cb != null) cb.onResult(true, "OK");
                } catch (Exception e) {
                    if (cb != null) cb.onResult(false, e.getMessage());
                }
            }
        });
    }

    private static void callGetArray(final JSONObject payload, final OnData cb) {
        EXEC.execute(new Runnable() {
            @Override public void run() {
                try {
                    String resp = callAdminApi(payload);
                    JSONObject obj = new JSONObject(resp);
                    if (!obj.optBoolean("ok", false)) {
                        if (cb != null) cb.onResult(false, null, obj.optString("error", "Failed"));
                        return;
                    }
                    JSONArray arr = obj.optJSONArray("data");
                    if (arr == null) arr = new JSONArray();
                    if (cb != null) cb.onResult(true, arr, "OK");
                } catch (Exception e) {
                    if (cb != null) cb.onResult(false, null, e.getMessage());
                }
            }
        });
    }

    private static void callGetObject(final JSONObject payload, final OnSingleData cb) {
        EXEC.execute(new Runnable() {
            @Override public void run() {
                try {
                    String resp = callAdminApi(payload);
                    JSONObject obj = new JSONObject(resp);
                    if (!obj.optBoolean("ok", false)) {
                        if (cb != null) cb.onResult(false, null, obj.optString("error", "Failed"));
                        return;
                    }
                    JSONObject data = obj.optJSONObject("data");
                    if (cb != null) cb.onResult(true, data, "OK");
                } catch (Exception e) {
                    if (cb != null) cb.onResult(false, null, e.getMessage());
                }
            }
        });
    }

    public static void fetchStats(OnSingleData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "stats"); } catch (Exception e) {}
        callGetObject(p, cb);
    }

    public static void fetchKeys(OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "list-keys"); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void fetchAdmins(OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "list-admins"); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void fetchLogs(OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "list-logs"); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void fetchBroadcasts(OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "list-broadcasts"); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void fetchMaintenance(OnSingleData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "get-maintenance"); } catch (Exception e) {}
        callGetObject(p, cb);
    }

    public static void fetchOnlineCount(OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "live-users"); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void fetchSuspect(OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "list-suspect"); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void searchSuspect(String fp, OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "search-suspect"); p.put("fp", fp); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void fetchLinks(OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "list-links"); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void fetchRateLimit(OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "rate-limit"); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void fetchUsedLinks(OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "used-links"); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void fetchBlocked(OnData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "list-blocked"); } catch (Exception e) {}
        callGetArray(p, cb);
    }

    public static void fetchHealth(OnSingleData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "health"); } catch (Exception e) {}
        callGetObject(p, cb);
    }

    public static void fetchServerInfo(OnSingleData cb) {
        JSONObject p = new JSONObject();
        try { p.put("action", "server-info"); } catch (Exception e) {}
        callGetObject(p, cb);
    }

    public static void createKey(String key, long durationMs, String assignedTo, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try {
            p.put("action", "create-key");
            p.put("key", key);
            p.put("duration_ms", durationMs);
            p.put("max_devices", 1);
            p.put("assigned_to", assignedTo != null ? assignedTo : "Admin");
        } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void createMultiKey(String key, long durationMs, int maxDevices, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try {
            p.put("action", "create-key");
            p.put("key", key);
            p.put("duration_ms", durationMs);
            p.put("max_devices", maxDevices);
            p.put("assigned_to", "Admin-Multi");
        } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void deleteKey(String key, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "delete-key"); p.put("key", key); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void deleteKeysBatch(JSONArray keys, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "delete-keys-batch"); p.put("keys", keys); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void resetHwid(String key, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "reset-hwid"); p.put("key", key); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void resetAllDevices(String key, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "reset-all-devices"); p.put("key", key); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void removeDeviceFromKey(final String key, final String hwid, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "remove-device"); p.put("key", key); p.put("hwid", hwid); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void cleanupExpired(final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "cleanup-expired"); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void addLink(String url, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "add-link"); p.put("url", url); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void bulkAddLinks(JSONArray urls, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "bulk-add-links"); p.put("urls", urls); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void deleteLink(String id, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "delete-link"); p.put("id", id); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void toggleLink(String id, boolean isActive, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "toggle-link"); p.put("id", id); p.put("is_active", isActive); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void refreshLinks(final OnComplete callback) {
        JSONObject p = new JSONObject();
        try {
            p.put("action", "refresh-links");
            p.put("count", 100);
            p.put("target_base", "https://bxit.my.id/getkey/");
        } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void blockFp(String fp, String reason, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "block-fp"); p.put("fp", fp); p.put("reason", reason); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void unblockFp(String fp, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "unblock-fp"); p.put("fp", fp); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void addAdmin(String username, String password, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "add-admin"); p.put("username", username); p.put("password", password); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void deleteAdmin(String id, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "delete-admin"); p.put("id", id); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void addLog(final String action) {
    final JSONObject p = new JSONObject();
    try { p.put("action", "add-log"); p.put("log", action); } catch (Exception e) {}
    new Thread(new Runnable() {
        @Override public void run() {
            try {
                String resp = callAdminApi(p);
                android.util.Log.d("BAGASXIT_LOG", "addLog ok: " + resp);
            } catch (Exception e) {
                android.util.Log.e("BAGASXIT_LOG", "addLog FAIL: " + e.getMessage());
            }
        }
      }).start();
    }

    public static void sendBroadcast(String title, String message, final OnComplete callback) {
        sendBroadcast(title, message, true, true, true, callback);
    }

    public static void sendBroadcast(String title, String message, boolean showWeb, boolean showMain, boolean showApk, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try {
            p.put("action", "send-broadcast");
            p.put("title", title);
            p.put("message", message);
            p.put("show_web", showWeb);
            p.put("show_main", showMain);
            p.put("show_apk", showApk);
        } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void deleteBroadcast(String id, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try { p.put("action", "delete-broadcast"); p.put("id", id); } catch (Exception e) {}
        callAsync(p, callback);
    }

    public static void setMaintenance(boolean active, String message, final OnComplete callback) {
        setMaintenance(active, message, true, true, true, callback);
    }

    public static void setMaintenance(boolean active, String message, boolean applyWeb, boolean applyMain, boolean applyApk, final OnComplete callback) {
        JSONObject p = new JSONObject();
        try {
            p.put("action", "set-maintenance");
            p.put("maintenance", active);
            p.put("message", message != null ? message : "");
            p.put("apply_web", applyWeb);
            p.put("apply_main", applyMain);
            p.put("apply_apk", applyApk);
        } catch (Exception e) {}
        callAsync(p, callback);
    }

public static void fetchStore(OnData cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "list-store"); } catch (Exception e) {}
    callGetArray(p, cb);
}

public static void addStore(String category, String name, int price, String priceText,
                            String description, String emoji, int sortOrder, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try {
        p.put("action", "add-store");
        p.put("category", category);
        p.put("name", name);
        p.put("price", price);
        p.put("price_text", priceText);
        p.put("description", description);
        p.put("emoji", emoji);
        p.put("sort_order", sortOrder);
    } catch (Exception e) {}
    callAsync(p, cb);
}

public static void editStore(String id, JSONObject patch, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try {
        p.put("action", "edit-store");
        p.put("id", id);
        if (patch.has("category")) p.put("category", patch.optString("category"));
        if (patch.has("name")) p.put("name", patch.optString("name"));
        if (patch.has("price")) p.put("price", patch.optInt("price"));
        if (patch.has("price_text")) p.put("price_text", patch.optString("price_text"));
        if (patch.has("description")) p.put("description", patch.optString("description"));
        if (patch.has("emoji")) p.put("emoji", patch.optString("emoji"));
        if (patch.has("sort_order")) p.put("sort_order", patch.optInt("sort_order"));
        if (patch.has("is_active")) p.put("is_active", patch.optBoolean("is_active"));
    } catch (Exception e) {}
    callAsync(p, cb);
}

public static void deleteStore(String id, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "delete-store"); p.put("id", id); } catch (Exception e) {}
    callAsync(p, cb);
}

public static void toggleStore(String id, boolean isActive, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "toggle-store"); p.put("id", id); p.put("is_active", isActive); } catch (Exception e) {}
    callAsync(p, cb);
}

public static void fetchApkUpdates(OnData cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "list-apk"); } catch (Exception e) {}
    callGetArray(p, cb);
}

public static void addApkUpdate(String category, String name, String version, String size,
                                String changelog, String link, String getkeyLink, String screenshot,
                                int sortOrder, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try {
        p.put("action", "add-apk");
        p.put("category", category);
        p.put("name", name);
        p.put("version", version);
        p.put("size", size);
        p.put("changelog", changelog);
        p.put("link", link);
        p.put("getkey_link", getkeyLink);
        p.put("screenshot", screenshot);
        p.put("sort_order", sortOrder);
    } catch (Exception e) {}
    callAsync(p, cb);
}

public static void editApkUpdate(String id, JSONObject patch, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try {
        p.put("action", "edit-apk");
        p.put("id", id);
        if (patch.has("category")) p.put("category", patch.optString("category"));
        if (patch.has("name")) p.put("name", patch.optString("name"));
        if (patch.has("version")) p.put("version", patch.optString("version"));
        if (patch.has("size")) p.put("size", patch.optString("size"));
        if (patch.has("changelog")) p.put("changelog", patch.optString("changelog"));
        if (patch.has("link")) p.put("link", patch.optString("link"));
        if (patch.has("getkey_link")) p.put("getkey_link", patch.optString("getkey_link"));
        if (patch.has("screenshot")) p.put("screenshot", patch.optString("screenshot"));
        if (patch.has("sort_order")) p.put("sort_order", patch.optInt("sort_order"));
        if (patch.has("is_active")) p.put("is_active", patch.optBoolean("is_active"));
    } catch (Exception e) {}
    callAsync(p, cb);
}

public static void deleteApkUpdate(String id, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "delete-apk"); p.put("id", id); } catch (Exception e) {}
    callAsync(p, cb);
}

public static void toggleApkUpdate(String id, boolean isActive, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "toggle-apk"); p.put("id", id); p.put("is_active", isActive); } catch (Exception e) {}
    callAsync(p, cb);
}

// ============================================
// COMMENTS
// ============================================

public static void addComment(int apkId, String name, String comment, String device,
                              String country, String ip, String fp, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try {
        p.put("action", "add-comment");
        p.put("apk_id", apkId);
        p.put("name", name);
        p.put("comment", comment);
        p.put("device", device);
        p.put("country", country);
        p.put("ip", ip);
        p.put("fp", fp);
    } catch (Exception e) {}
    callAsync(p, cb);
}

public static void fetchComments(OnData cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "list-comments"); p.put("show_hidden", true); } catch (Exception e) {}
    callGetArray(p, cb);
}

public static void hideComment(String id, boolean isHidden, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "hide-comment"); p.put("id", id); p.put("is_hidden", isHidden); } catch (Exception e) {}
    callAsync(p, cb);
}

public static void deleteComment(String id, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "delete-comment"); p.put("id", id); } catch (Exception e) {}
    callAsync(p, cb);
}

// ============================================
// BAD WORDS
// ============================================

public static void fetchBadWords(OnData cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "list-bad-words"); } catch (Exception e) {}
    callGetArray(p, cb);
}

public static void addBadWord(String word, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "add-bad-word"); p.put("word", word); } catch (Exception e) {}
    callAsync(p, cb);
}

// ============================================
// KATEGORI
// ============================================

public static void fetchCategories(OnData cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "list-categories"); } catch (Exception e) {}
    callGetArray(p, cb);
}

public static void addCategory(String name, int sortOrder, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "add-category"); p.put("name", name); p.put("sort_order", sortOrder); } catch (Exception e) {}
    callAsync(p, cb);
}

public static void deleteCategory(String id, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "delete-category"); p.put("id", id); } catch (Exception e) {}
    callAsync(p, cb);
}

public static void deleteBadWord(String id, final OnComplete cb) {
    JSONObject p = new JSONObject();
    try { p.put("action", "delete-bad-word"); p.put("id", id); } catch (Exception e) {}
    callAsync(p, cb);
}

// ============================================
// UPLOAD GAMBAR KE GITHUB
// ============================================

public interface OnUploadResult {
    void onResult(boolean success, String url, String message);
}

private static final String GH_TOKEN = "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";
private static final String GH_REPO = "bagassasth136/websitegamesbagasxit";
private static final String GH_PATH = "updatesapk/Img";

public static void uploadImageToGitHub(final String base64Image, final String filename, final OnUploadResult cb) {
    EXEC.execute(new Runnable() {
        @Override public void run() {
            try {
                java.net.URL url = new java.net.URL("https://api.github.com/repos/" + GH_REPO + "/contents/" + GH_PATH + "/" + filename);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "token " + GH_TOKEN);
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);
                conn.setDoOutput(true);

                org.json.JSONObject req = new org.json.JSONObject();
                req.put("message", "Upload via APK admin: " + filename);
                req.put("content", base64Image);
                req.put("branch", "main");

                java.io.OutputStream os = conn.getOutputStream();
                os.write(req.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(
                    code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();

                if (code < 200 || code >= 300) {
                    cb.onResult(false, null, "HTTP " + code + ": " + sb.toString());
                    return;
                }

                String rawUrl = "https://raw.githubusercontent.com/" + GH_REPO + "/main/" + GH_PATH + "/" + filename;
                cb.onResult(true, rawUrl, "OK");

            } catch (Exception e) {
                cb.onResult(false, null, e.getMessage());
            }
        }
    });
}
}