package com.apkadmin.bagasxit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private FrameLayout contentFrame;
    private LinearLayout subTabContainer;
    private TextView tvStatKeys, tvStatOnline;
    private View sidebarLayout;

    private int currentMenu = 0; // 0=Dash, 1=Key, 2=Store, 3=APK, 4=System
    private int currentSubTab = 0;

    private JSONArray cachedKeys = new JSONArray(), cachedSuspect = new JSONArray(), cachedBlocked = new JSONArray();
    private int renderOffset = 0;
    private static final int RENDER_STEP = 50;
    private Handler autoRefreshHandler = new Handler(Looper.getMainLooper());
    private Runnable autoRefreshRunnable;
    private String currentFilter = "all", currentSearch = "";
    private int selectedMaxDevices = 5;
    private boolean maintCheckboxInitialized = false;
    private JSONArray cachedStore = new JSONArray(), cachedApk = new JSONArray(), cachedComments = new JSONArray(), cachedBadWords = new JSONArray(), cachedCategories = new JSONArray();
    private TextView activeScreenshotDisplay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        contentFrame = findViewById(R.id.contentFrame);
        subTabContainer = findViewById(R.id.subTabContainer);
        sidebarLayout = findViewById(R.id.sidebarLayout);
        tvStatKeys = findViewById(R.id.tvStatKeys);
        tvStatOnline = findViewById(R.id.tvStatOnline);

        findViewById(R.id.btnMenu).setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));
        findViewById(R.id.btnMenuDash).setOnClickListener(v -> openMenu(0));
        findViewById(R.id.btnMenuKey).setOnClickListener(v -> openMenu(1));
        findViewById(R.id.btnMenuStore).setOnClickListener(v -> openMenu(2));
        findViewById(R.id.btnMenuApk).setOnClickListener(v -> openMenu(3)); 
        findViewById(R.id.btnMenuSystem).setOnClickListener(v -> openMenu(4));
        findViewById(R.id.btnMenuWeb).setOnClickListener(v -> openMenu(5));

        openMenu(0);
        loadStats();
        startAutoRefresh();
    }

    private void openMenu(int menuIndex) {
        currentMenu = menuIndex;
        currentSubTab = 0;
        drawerLayout.closeDrawer(Gravity.LEFT);
        subTabContainer.removeAllViews();
        
        String[] tabs;
        switch (menuIndex) {
            case 1: tabs = new String[]{"Generate", "List Key", "List Multi"}; break;
            case 3: tabs = new String[]{"Update APK", "Komentar", "Kategori", "Filter"}; break;
            case 4: tabs = new String[]{"Broadcast", "Logs", "Suspect", "Links", "Monitor", "Security"}; break;
            case 5: tabs = new String[]{"List Game", "Tambah Game", "Kategori", "Ulasan", "Web Settings"}; break;
            default: tabs = new String[]{"Overview"};
        }

        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            TextView tv = new TextView(this);
            tv.setText(tabs[i]);
            tv.setTextSize(12f);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setPadding(dp(16), dp(8), dp(16), dp(8));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dp(4), 0);
            tv.setLayoutParams(lp);

            if (i == 0) {
                tv.setBackgroundResource(R.drawable.bg_tab_capsule);
                tv.setTextColor(Color.parseColor("#A78BFA"));
            } else {
                tv.setTextColor(Color.parseColor("#94A3B8"));
            }

            tv.setOnClickListener(v -> {
                currentSubTab = idx;
                for (int j = 0; j < subTabContainer.getChildCount(); j++) {
                    TextView child = (TextView) subTabContainer.getChildAt(j);
                    child.setBackground(null);
                    child.setTextColor(Color.parseColor("#94A3B8"));
                }
                tv.setBackgroundResource(R.drawable.bg_tab_capsule);
                tv.setTextColor(Color.parseColor("#A78BFA"));
                loadSubTabContent();
            });
            subTabContainer.addView(tv);
        }

        if (subTabContainer.getChildCount() > 0) subTabContainer.getChildAt(0).performClick();
        else loadSubTabContent();
    }
    
    private void loadSubTabContent() {
    contentFrame.removeAllViews();
    if (currentMenu == 0) {
        View v = LayoutInflater.from(this).inflate(R.layout.content_dashboard, contentFrame, false);
        contentFrame.addView(v);
        loadDashboard(v);
    } else if (currentMenu == 1) {
        if (currentSubTab == 0) {
            View v = LayoutInflater.from(this).inflate(R.layout.sub_key_generate, contentFrame, false);
            contentFrame.addView(v);
            bindGenerateKey(v);
        } else if (currentSubTab == 1) {
            View v = LayoutInflater.from(this).inflate(R.layout.sub_key_list, contentFrame, false);
            contentFrame.addView(v);
            bindListKey(v);
            loadKeys();
        } else {
            View v = LayoutInflater.from(this).inflate(R.layout.sub_key_multi, contentFrame, false);
            contentFrame.addView(v);
            bindMultiKey(v);
            loadMultiKeys();
        }
    } else if (currentMenu == 2) {
        View v = LayoutInflater.from(this).inflate(R.layout.content_store, contentFrame, false);
        contentFrame.addView(v);
        bindStore(v);
        loadStore();
    } else if (currentMenu == 3) {
        if (currentSubTab == 0) {
            View v = LayoutInflater.from(this).inflate(R.layout.sub_apk_update, contentFrame, false);
            contentFrame.addView(v);
            bindApkUpdate(v);
            loadApk();
        } else if (currentSubTab == 1) {
            View v = LayoutInflater.from(this).inflate(R.layout.sub_apk_comments, contentFrame, false);
            contentFrame.addView(v);
            bindComments(v);
            loadComments();
        } else if (currentSubTab == 2) {
            View v = LayoutInflater.from(this).inflate(R.layout.sub_apk_kategori, contentFrame, false);
            contentFrame.addView(v);
            bindKategori(v);
            loadCategories();
        } else {
            View v = LayoutInflater.from(this).inflate(R.layout.sub_apk_filter, contentFrame, false);
            contentFrame.addView(v);
            bindFilter(v);
            loadBadWords();
        }
    } else if (currentMenu == 4) {
    loadSystemSubTab();
    } else if (currentMenu == 5) {
    loadWebUtamaSubTab();
  }
}

private void loadSystemSubTab() {
    if (currentSubTab == 0) {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_sys_bcast, contentFrame, false);
        contentFrame.addView(v);
        bindBroadcast(v);
        loadBroadcasts();
        loadMaintStatus(v);
    } else if (currentSubTab == 1) {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_sys_logs, contentFrame, false);
        contentFrame.addView(v);
        bindLogs(v);
        loadLogs();
    } else if (currentSubTab == 2) {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_sys_suspect, contentFrame, false);
        contentFrame.addView(v);
        bindSuspect(v);
        loadSuspect();
    } else if (currentSubTab == 3) {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_sys_links, contentFrame, false);
        contentFrame.addView(v);
        bindLinks(v);
        loadLinks();
    } else if (currentSubTab == 4) {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_sys_monitor, contentFrame, false);
        contentFrame.addView(v);
        bindMonitor(v);
        loadMonitor();
    } else {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_sys_security, contentFrame, false);
        contentFrame.addView(v);
        bindSecurity(v);
        loadSecurity();
    }
}

private void loadWebUtamaSubTab() {
    if (currentSubTab == 0) {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_web_games, contentFrame, false);
        contentFrame.addView(v); bindWebGames(v); loadWebGames();
    } else if (currentSubTab == 1) {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_web_add_game, contentFrame, false);
        contentFrame.addView(v); bindFormGame(v, null);
    } else if (currentSubTab == 2) {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_web_kategori, contentFrame, false);
        contentFrame.addView(v); bindWebKategori(v); loadWebCategories();
    } else if (currentSubTab == 3) {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_web_reviews, contentFrame, false);
        contentFrame.addView(v); bindWebReviews(v); loadWebReviews();
    } else if (currentSubTab == 4) {
        View v = LayoutInflater.from(this).inflate(R.layout.sub_sys_websettings, contentFrame, false);
        contentFrame.addView(v); bindWebSettings(v);
    }
}

private void bindWebGames(View v) {
    TextView btnAdd = v.findViewById(R.id.btnAddWebGame);
    LinearLayout container = v.findViewById(R.id.webGamesContainer);
    btnAdd.setOnClickListener(x -> showAddGameDialog(container));
}

private void bindWebKategori(View v) {
    EditText etNewCategory = v.findViewById(R.id.etNewWebCategory);
    TextView btnAdd = v.findViewById(R.id.btnAddWebCategory);
    btnAdd.setOnClickListener(x -> {
        String name = etNewCategory.getText().toString().trim().toUpperCase();
        if (name.isEmpty()) return;
        SupabaseHelper.addGameCategory(name, (success, msg) -> runOnUiThread(() -> {
            if (success) { etNewCategory.setText(""); loadWebCategories(); }
        }));
    });
}

private void loadWebGames() {
    View v = contentFrame.getChildAt(0);
    if (v == null) return;
    LinearLayout c = v.findViewById(R.id.webGamesContainer);
    if (c == null) return;
    c.removeAllViews();
    SupabaseHelper.fetchGames((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null || data.length() == 0) { c.addView(emptyText("Belum ada game. Silakan tambah.")); return; }
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) c.addView(createGameCard(o, c));
        }
    }));
}

private View createGameCard(JSONObject o, LinearLayout parent) {
    String gameId = o.optString("game_id", ""), title = o.optString("title", ""), dev = o.optString("developer", ""), cat = o.optString("category", "");
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(14), dp(12), dp(14), dp(12));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setStroke(dp(1), Color.parseColor("#A78BFA"));
    bg.setCornerRadius(dp(10));
    card.setBackground(bg);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(8));
    card.setLayoutParams(p);

    TextView tvTitle = new TextView(this);
    tvTitle.setText(title);
    tvTitle.setTextColor(Color.parseColor("#F8FAFC"));
    tvTitle.setTextSize(12f);
    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
    card.addView(tvTitle);

    TextView tvMeta = new TextView(this);
    tvMeta.setText(dev + " · " + cat + " · " + gameId);
    tvMeta.setTextColor(Color.parseColor("#94A3B8"));
    tvMeta.setTextSize(9f);
    tvMeta.setPadding(0, dp(4), 0, 0);
    card.addView(tvMeta);

    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setPadding(0, dp(8), 0, 0);

    // --- TOMBOL EDIT ---
    TextView btnEdit = createMiniBtn("EDIT", "#F59E0B", "#1F1A0A");
    LinearLayout.LayoutParams lpEdit = new LinearLayout.LayoutParams(0, dp(32), 1f);
    lpEdit.setMargins(0, 0, dp(6), 0);
    btnEdit.setLayoutParams(lpEdit);
    btnEdit.setOnClickListener(x -> {
        // Pindah ke Sub-tab index 1 (Tambah Game)
        currentSubTab = 1;
        for (int i = 0; i < subTabContainer.getChildCount(); i++) {
            TextView tv = (TextView) subTabContainer.getChildAt(i);
            tv.setBackground(null); 
            tv.setTextColor(Color.parseColor("#94A3B8"));
        }
        TextView tabEdit = (TextView) subTabContainer.getChildAt(1);
        tabEdit.setBackgroundResource(R.drawable.bg_tab_capsule);
        tabEdit.setTextColor(Color.parseColor("#A78BFA"));
        
        // Tampilkan form edit
        contentFrame.removeAllViews();
        View v = LayoutInflater.from(this).inflate(R.layout.sub_web_add_game, contentFrame, false);
        contentFrame.addView(v);
        bindFormGame(v, o); // Lempar data game (o) buat diedit
    });
    row.addView(btnEdit);

    // --- TOMBOL DELETE ---
    TextView btnDel = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    btnDel.setLayoutParams(new LinearLayout.LayoutParams(0, dp(32), 1f));
    btnDel.setOnClickListener(x -> new AlertDialog.Builder(this)
            .setTitle("Hapus Game").setMessage("Hapus " + title + "?")
            .setPositiveButton("Hapus", (d, w) -> SupabaseHelper.deleteGame(gameId, (s, m) -> runOnUiThread(() -> {
                Toast.makeText(this, s ? "Terhapus" : m, Toast.LENGTH_SHORT).show();
                if (s) loadWebGames();
            })))
            .setNegativeButton("Batal", null).show());
    row.addView(btnDel);

    card.addView(row);
    return card;
}

private View currentMediaRowView;
private JSONArray currentMediaList = new JSONArray();

private void bindFormGame(View v, final JSONObject existingGame) {
    EditText etId = v.findViewById(R.id.etGameId), etTitle = v.findViewById(R.id.etGameTitle), etDev = v.findViewById(R.id.etGameDev), etCat = v.findViewById(R.id.etGameCat), etDesc = v.findViewById(R.id.etGameDesc), etChangelog = v.findViewById(R.id.etGameChangelog), etLabel = v.findViewById(R.id.etGameLabel), etLabelColor = v.findViewById(R.id.etGameLabelColor), etSecurity = v.findViewById(R.id.etGameSecurity), etApkUrl = v.findViewById(R.id.etGameApkUrl), etShareUrl = v.findViewById(R.id.etGameShareUrl);
    TextView tvGameImg = v.findViewById(R.id.tvGameImg), btnSave = v.findViewById(R.id.btnSaveGame), btnAddMedia = v.findViewById(R.id.btnAddMedia);
    LinearLayout mediaContainer = v.findViewById(R.id.mediaContainer);
    
    currentMediaList = new JSONArray();
    mediaContainer.removeAllViews();

    if (existingGame != null) {
        etId.setText(existingGame.optString("game_id")); etId.setEnabled(false);
        etTitle.setText(existingGame.optString("title"));
        etDev.setText(existingGame.optString("developer"));
        etCat.setText(existingGame.optString("category"));
        etDesc.setText(existingGame.optString("desc_game"));
        etChangelog.setText(existingGame.optString("changelog"));
        etLabel.setText(existingGame.optString("label_text"));
        etLabelColor.setText(existingGame.optString("label_color"));
        etSecurity.setText(existingGame.optString("security_info"));
        etApkUrl.setText(existingGame.optString("apk_url"));
        etShareUrl.setText(existingGame.optString("share_url"));
        String img = existingGame.optString("img");
        if (!img.isEmpty()) { tvGameImg.setText(img); tvGameImg.setTag(img); }
        
        SupabaseHelper.fetchGameMedia(existingGame.optString("game_id"), (success, data, message) -> runOnUiThread(() -> {
            if (success && data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject m = data.optJSONObject(i);
                    if (m != null) {
                        currentMediaList.put(m);
                        addMediaRow(mediaContainer, m.optString("media_type"), m.optString("url"), m.optInt("sort_order"));
                    }
                }
            }
        }));
    }

    tvGameImg.setOnClickListener(x -> {
        activeScreenshotDisplay = tvGameImg;
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    });

    btnAddMedia.setOnClickListener(x -> addMediaRow(mediaContainer, "image", "", currentMediaList.length() + 1));

    btnSave.setOnClickListener(x -> {
        String gId = etId.getText().toString().trim(), gTitle = etTitle.getText().toString().trim();
        if (gId.isEmpty() || gTitle.isEmpty()) { Toast.makeText(this, "ID dan Judul wajib!", Toast.LENGTH_SHORT).show(); return; }
        try {
            JSONObject data = new JSONObject();
            data.put("game_id", gId); data.put("title", gTitle); data.put("developer", etDev.getText().toString().trim());
            data.put("category", etCat.getText().toString().trim()); data.put("desc_game", etDesc.getText().toString().trim());
            data.put("changelog", etChangelog.getText().toString().trim()); data.put("label_text", etLabel.getText().toString().trim());
            data.put("label_color", etLabelColor.getText().toString().trim()); data.put("security_info", etSecurity.getText().toString().trim());
            data.put("apk_url", etApkUrl.getText().toString().trim()); data.put("img", tvGameImg.getTag() != null ? tvGameImg.getTag().toString() : "");
            data.put("is_active", true);

            SupabaseHelper.OnComplete saveCb = (success, message) -> runOnUiThread(() -> {
                if (success) {
                    saveMediaList(gId, () -> runOnUiThread(() -> {
                        Toast.makeText(this, "Game & Media tersimpan!", Toast.LENGTH_SHORT).show();
                        currentSubTab = 0; loadSubTabContent();
                    }));
                } else Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });

            if (existingGame == null) SupabaseHelper.addGame(data, saveCb);
            else SupabaseHelper.updateGame(gId, data, saveCb);
        } catch (Exception e) { Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
    });
}

private void addMediaRow(LinearLayout container, String defaultType, String defaultUrl, int defaultSort) {
    View row = LayoutInflater.from(this).inflate(R.layout.item_media, container, false);
    TextView btnImg = row.findViewById(R.id.btnTypeImage);
    TextView btnVid = row.findViewById(R.id.btnTypeVideo);
    TextView tvUrl = row.findViewById(R.id.tvMediaUrl);
    EditText etSort = row.findViewById(R.id.etMediaSort);
    TextView btnDel = row.findViewById(R.id.btnDeleteMedia);

    final String[] currentType = {defaultType};
    
    tvUrl.setText(defaultUrl.isEmpty() ? "Tap untuk Upload/Link" : defaultUrl);
    if (!defaultUrl.isEmpty()) tvUrl.setTag(defaultUrl);
    etSort.setText(String.valueOf(defaultSort));

    Runnable updateUI = () -> {
        if (currentType[0].equals("image")) {
            btnImg.setBackgroundResource(R.drawable.tab_active); btnImg.setTextColor(Color.parseColor("#0A0A0F"));
            btnVid.setBackgroundResource(R.drawable.chip_bg); btnVid.setTextColor(Color.parseColor("#94A3B8"));
        } else {
            btnVid.setBackgroundResource(R.drawable.tab_active); btnVid.setTextColor(Color.parseColor("#0A0A0F"));
            btnImg.setBackgroundResource(R.drawable.chip_bg); btnImg.setTextColor(Color.parseColor("#94A3B8"));
        }
    };
    updateUI.run();

    btnImg.setOnClickListener(x -> { currentType[0] = "image"; updateUI.run(); });
    btnVid.setOnClickListener(x -> { currentType[0] = "video"; updateUI.run(); });

    tvUrl.setOnClickListener(x -> {
        if (currentType[0].equals("image")) {
            activeScreenshotDisplay = tvUrl;
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
            intent.setType("image/*"); startActivityForResult(intent, 1001);
        } else {
            final EditText input = new EditText(this);
            input.setHint("Paste Link YouTube");
            new AlertDialog.Builder(this).setTitle("Link Video").setView(input).setPositiveButton("OK", (d, w) -> {
                String url = input.getText().toString().trim();
                if (!url.isEmpty()) { tvUrl.setText(url); tvUrl.setTag(url); }
            }).show();
        }
    });

    btnDel.setOnClickListener(x -> container.removeView(row));
    container.addView(row);
}

private void saveMediaList(String gameId, Runnable onDone) {
    SupabaseHelper.deleteGameMedia(gameId, (s, m) -> runOnUiThread(() -> {
        LinearLayout container = contentFrame.findViewById(R.id.mediaContainer);
        if (container == null) { onDone.run(); return; }
        int count = container.getChildCount();
        if (count == 0) { onDone.run(); return; }
        
        final int[] saved = {0};
        for (int i = 0; i < count; i++) {
            View row = container.getChildAt(i);
            TextView tvUrl = row.findViewById(R.id.tvMediaUrl);
            TextView btnImg = row.findViewById(R.id.btnTypeImage);
            EditText etSort = row.findViewById(R.id.etMediaSort);
            
            String type = (btnImg.getCurrentTextColor() == Color.parseColor("#0A0A0F")) ? "image" : "video";
            
            String url = tvUrl.getTag() != null ? tvUrl.getTag().toString() : "";
            int sort = 0; try { sort = Integer.parseInt(etSort.getText().toString()); } catch (Exception ignored) {}
            
            if (!url.isEmpty()) {
                SupabaseHelper.addGameMedia(gameId, type, url, sort, (isSuccess, msg) -> runOnUiThread(() -> {
                    saved[0]++;
                    if (saved[0] == count) onDone.run();
                }));
            } else {
                saved[0]++;
                if (saved[0] == count) onDone.run();
            }
        }
    }));
}

private void bindWebReviews(View v) {
    EditText etSearch = v.findViewById(R.id.etSearchReviewGame);
    TextView btnLoad = v.findViewById(R.id.btnLoadReviews);
    LinearLayout container = v.findViewById(R.id.reviewsContainer);
    btnLoad.setOnClickListener(x -> {
        String gid = etSearch.getText().toString().trim();
        if (gid.isEmpty()) { Toast.makeText(this, "Masukkan Game ID", Toast.LENGTH_SHORT).show(); return; }
        container.removeAllViews();
        SupabaseHelper.fetchGameReviews(gid, (success, data, message) -> runOnUiThread(() -> {
            if (!success || data == null || data.length() == 0) { container.addView(emptyText("Belum ada ulasan")); return; }
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(14), dp(12), dp(14), dp(12));
                GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.parseColor("#14141C")); bg.setCornerRadius(dp(10)); card.setBackground(bg);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); p.setMargins(0, 0, 0, dp(8)); card.setLayoutParams(p);
                TextView tvName = new TextView(this); tvName.setText(o.optString("name") + " • " + o.optInt("rating") + "★"); tvName.setTextColor(Color.parseColor("#A78BFA")); tvName.setTextSize(12f); tvName.setTypeface(null, android.graphics.Typeface.BOLD); card.addView(tvName);
                TextView tvText = new TextView(this); tvText.setText(o.optString("review_text")); tvText.setTextColor(Color.parseColor("#F8FAFC")); tvText.setTextSize(11f); tvText.setPadding(0, dp(6), 0, dp(6)); card.addView(tvText);
                TextView btnDel = createMiniBtn("HAPUS", "#EF4444", "#1F0A0A"); btnDel.setLayoutParams(new LinearLayout.LayoutParams(dp(80), dp(32)));
                btnDel.setOnClickListener(y -> SupabaseHelper.deleteGameReview(o.optString("id"), (s, m) -> runOnUiThread(() -> { if (s) btnLoad.performClick(); })));
                card.addView(btnDel);
                container.addView(card);
            }
        }));
    });
}

private void loadWebReviews() { }
private void loadWebCategories() {
    View v = contentFrame.getChildAt(0);
    if (v == null) return;
    LinearLayout c = v.findViewById(R.id.webCategoriesContainer);
    if (c == null) return;
    c.removeAllViews();
    SupabaseHelper.fetchGameCategories((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null || data.length() == 0) { c.addView(emptyText("Belum ada kategori")); return; }
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) c.addView(createWebCategoryRow(o));
        }
    }));
}

private View createWebCategoryRow(JSONObject o) {
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(14), dp(10), dp(14), dp(10));
    TextView tvName = new TextView(this);
    tvName.setText(o.optString("name", ""));
    tvName.setTextColor(Color.parseColor("#F8FAFC"));
    tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    row.addView(tvName);
    TextView btnDel = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    btnDel.setLayoutParams(new LinearLayout.LayoutParams(dp(85), dp(32)));
    btnDel.setOnClickListener(x -> SupabaseHelper.deleteGameCategory(o.optString("id", ""), (s, m) -> runOnUiThread(() -> { if (s) loadWebCategories(); })));
    row.addView(btnDel);
    return row;
}

private void showAddGameDialog(final LinearLayout parent) {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(20), dp(20), dp(20));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setCornerRadius(dp(16));
    root.setBackground(bg);

    EditText etId = makeInput("", "Game ID (Unik)");
    EditText etTitle = makeInput("", "Judul Game");
    EditText etDev = makeInput("", "Developer");
    EditText etCat = makeInput("", "Kategori");
    EditText etDesc = makeInput("", "Deskripsi Game");

    TextView tvGameImg = new TextView(this);
    tvGameImg.setText("Tap untuk pilih gambar");
    tvGameImg.setTextColor(Color.parseColor("#A78BFA"));
    tvGameImg.setPadding(0, dp(10), 0, dp(10));
    tvGameImg.setOnClickListener(x -> {
        activeScreenshotDisplay = tvGameImg;
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    });

    root.addView(etId);
    root.addView(etTitle);
    root.addView(etDev);
    root.addView(etCat);
    root.addView(etDesc);
    root.addView(tvGameImg);

    AlertDialog dialog = new AlertDialog.Builder(this).setView(root).setPositiveButton("SIMPAN", null).setNegativeButton("BATAL", null).create();
    dialog.show();
    if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#14141C")));
    
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(x -> {
        String gId = etId.getText().toString().trim();
        String gTitle = etTitle.getText().toString().trim();
        String gDev = etDev.getText().toString().trim();
        String gCat = etCat.getText().toString().trim();
        String gDesc = etDesc.getText().toString().trim();
        String gImg = tvGameImg.getTag() != null ? tvGameImg.getTag().toString() : "";
        
        if (gId.isEmpty() || gTitle.isEmpty()) { 
            Toast.makeText(this, "ID dan Judul wajib!", Toast.LENGTH_SHORT).show(); 
            return; 
        }
        
        try {
            JSONObject data = new JSONObject();
            data.put("game_id", gId); 
            data.put("title", gTitle); 
            data.put("developer", gDev); 
            data.put("category", gCat); 
            data.put("desc_game", gDesc); 
            data.put("img", gImg); 
            data.put("is_active", true);
            
            SupabaseHelper.addGame(data, (success, message) -> runOnUiThread(() -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                if (success) { dialog.dismiss(); loadWebGames(); }
            }));
        } catch (Exception e) { 
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show(); 
        }
    });
}

private void loadStats() {
    SupabaseHelper.fetchKeys((success, data, message) -> runOnUiThread(() -> {
        if (success && data != null) tvStatKeys.setText("Keys: " + data.length());
    }));
    SupabaseHelper.fetchOnlineCount((success, data, message) -> runOnUiThread(() -> {
        if (success && data != null) {
            long now = System.currentTimeMillis();
            int active = 0;
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                long ts = o.optLong("last_seen", 0);
                if (ts > 0 && (now - ts) <= 120000) active++;
            }
            tvStatOnline.setText("Online: " + active);
        } else tvStatOnline.setText("Online: 0");
    }));
}

private void loadDashboard(View root) {
    TextView tvTotal = root.findViewById(R.id.tvDashTotal);
    TextView tvOnline = root.findViewById(R.id.tvDashOnline);
    TextView tvActive = root.findViewById(R.id.tvDashActive);
    TextView tvExpired = root.findViewById(R.id.tvDashExpired);
    TextView tvBound = root.findViewById(R.id.tvDashBound);
    LinearLayout activityContainer = root.findViewById(R.id.dashActivityContainer);

    SupabaseHelper.fetchKeys((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null) return;
        long now = System.currentTimeMillis();
        int active = 0, expired = 0, bound = 0;
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o == null) continue;
            long expiresAt = parseIso(o.optString("expires_at", ""));
            String hwid = o.isNull("hwid") ? "" : o.optString("hwid", "");
            boolean isActive = o.optBoolean("is_active", false);
            boolean isBound = hwid != null && !hwid.isEmpty();
            boolean isExpired = expiresAt > 0 && now > expiresAt;
            if (isActive && !isExpired) active++;
            if (isExpired) expired++;
            if (isBound) bound++;
        }
        tvTotal.setText(String.valueOf(data.length()));
        tvActive.setText("✅ Active: " + active);
        tvExpired.setText("❌ Expired: " + expired);
        tvBound.setText("🟡 Bound: " + bound);
    }));

    SupabaseHelper.fetchOnlineCount((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null) return;
        long now = System.currentTimeMillis();
        int active = 0;
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o == null) continue;
            long ts = o.optLong("last_seen", 0);
            if (ts > 0 && (now - ts) <= 120000) active++;
        }
        tvOnline.setText(String.valueOf(active));
    }));

    SupabaseHelper.fetchLogs((success, data, message) -> runOnUiThread(() -> {
        activityContainer.removeAllViews();
        if (!success || data == null || data.length() == 0) {
            activityContainer.addView(emptyText("Belum ada aktivitas"));
            return;
        }
        int max = Math.min(5, data.length());
        for (int i = 0; i < max; i++) {
            JSONObject o = data.optJSONObject(i);
            if (o == null) continue;
            String action = o.optString("action", "");
            if (action.isEmpty()) continue;
            TextView tv = new TextView(this);
            tv.setText("• " + action);
            tv.setTextColor(Color.parseColor("#C4C0E2"));
            tv.setTextSize(11f);
            tv.setPadding(0, dp(4), 0, dp(4));
            activityContainer.addView(tv);
        }
    }));
}

private long parseIso(String iso) {
    if (iso == null || iso.isEmpty() || iso.equals("null")) return 0;
    try {
        String clean = iso.trim();
        if (clean.contains(".")) clean = clean.substring(0, clean.indexOf("."));
        if (clean.endsWith("Z")) clean = clean.substring(0, clean.length() - 1);
        if (clean.contains("+")) clean = clean.substring(0, clean.indexOf("+"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        Date d = sdf.parse(clean);
        return d != null ? d.getTime() : 0;
    } catch (Exception e) { return 0; }
}

private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density); }

private TextView emptyText(String text) {
    TextView tv = new TextView(this);
    tv.setText(text);
    tv.setTextColor(Color.parseColor("#64748B"));
    tv.setTextSize(11f);
    tv.setGravity(Gravity.CENTER);
    tv.setPadding(0, dp(40), 0, dp(40));
    return tv;
}

private void bindGenerateKey(View v) {
    EditText etKey = v.findViewById(R.id.etKey);
    EditText etDuration = v.findViewById(R.id.etDuration);
    v.findViewById(R.id.chip1D).setOnClickListener(x -> etDuration.setText("24"));
    v.findViewById(R.id.chip3D).setOnClickListener(x -> etDuration.setText("72"));
    v.findViewById(R.id.chip7D).setOnClickListener(x -> etDuration.setText("168"));
    v.findViewById(R.id.chip30D).setOnClickListener(x -> etDuration.setText("720"));
    v.findViewById(R.id.btnAutoKey).setOnClickListener(x -> etKey.setText("BX-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase()));
    v.findViewById(R.id.btnCreateKey).setOnClickListener(x -> {
        String key = etKey.getText().toString().trim();
        String durStr = etDuration.getText().toString().trim();
        if (key.isEmpty() || durStr.isEmpty()) { Toast.makeText(this, "Isi key dan durasi!", Toast.LENGTH_SHORT).show(); return; }
        long durHours;
        try { durHours = Long.parseLong(durStr); } catch (Exception e) { Toast.makeText(this, "Durasi harus angka!", Toast.LENGTH_SHORT).show(); return; }
        SupabaseHelper.createKey(key, durHours * 3600 * 1000, "Admin-Panel", (success, message) -> runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            if (success) { SupabaseHelper.addLog("Created key: " + key); etKey.setText(""); etDuration.setText(""); loadStats(); }
        }));
    });
}

private void bindListKey(View v) {
    EditText etSearch = v.findViewById(R.id.etSearch);
    TextView filterAll = v.findViewById(R.id.filterAll);
    TextView filterActive = v.findViewById(R.id.filterActive);
    TextView filterExpired = v.findViewById(R.id.filterExpired);
    TextView filterBound = v.findViewById(R.id.filterBound);
    TextView btnDeleteFiltered = v.findViewById(R.id.btnDeleteFiltered);
    LinearLayout keysContainer = v.findViewById(R.id.keysContainer);

    etSearch.addTextChangedListener(new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void onTextChanged(CharSequence s, int a, int b, int c) { currentSearch = s.toString().toLowerCase().trim(); renderOffset = 0; renderKeys(keysContainer); }
        @Override public void afterTextChanged(Editable s) {}
    });

    View.OnClickListener filterClick = x -> {
        TextView tv = (TextView) x;
        currentFilter = tv.getText().toString().toLowerCase();
        renderOffset = 0;
        TextView[] arr = {filterAll, filterActive, filterExpired, filterBound};
        for (TextView t : arr) { t.setBackgroundResource(R.drawable.chip_bg); t.setTextColor(Color.parseColor("#0A0A0F")); }
        tv.setBackgroundResource(R.drawable.tab_active);
        updateDeleteButton(btnDeleteFiltered);
        renderKeys(keysContainer);
    };
    filterAll.setOnClickListener(filterClick);
    filterActive.setOnClickListener(filterClick);
    filterExpired.setOnClickListener(filterClick);
    filterBound.setOnClickListener(filterClick);

    btnDeleteFiltered.setOnClickListener(x -> deleteFiltered());
}

private void updateDeleteButton(TextView btn) {
    String label;
    switch (currentFilter) {
        case "active": label = "DELETE ALL ACTIVE"; break;
        case "expired": label = "DELETE ALL EXPIRED"; break;
        case "bound": label = "DELETE ALL BOUND"; break;
        default: label = "DELETE ALL"; break;
    }
    btn.setText(label);
}

private void loadKeys() {
    SupabaseHelper.fetchKeys((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null) { Toast.makeText(this, "Gagal memuat key", Toast.LENGTH_SHORT).show(); return; }
        cachedKeys = data;
        renderOffset = 0;
        View v = contentFrame.getChildAt(0);
        if (v != null) {
            LinearLayout kc = v.findViewById(R.id.keysContainer);
            if (kc != null) renderKeys(kc);
        }
    }));
}

private void renderKeys(LinearLayout keysContainer) {
    if (keysContainer == null) return;
    keysContainer.removeAllViews();
    if (cachedKeys.length() == 0) { keysContainer.addView(emptyText("Belum ada key")); return; }
    long now = System.currentTimeMillis();
    int matchCount = 0, rendered = 0, startIdx = renderOffset, endIdx = renderOffset + RENDER_STEP;
    for (int i = 0; i < cachedKeys.length(); i++) {
        JSONObject o = cachedKeys.optJSONObject(i);
        if (o == null) continue;
        String keyName = o.optString("key", "");
        long expiresAt = parseIso(o.optString("expires_at", ""));
        String hwid = o.isNull("hwid") ? "" : o.optString("hwid", "");
        boolean isActive = o.optBoolean("is_active", false);
        int maxDevices = o.optInt("max_devices", 1);
        boolean bound = hwid != null && !hwid.isEmpty();
        boolean expired = expiresAt > 0 && now > expiresAt;
        if (maxDevices > 1 || maxDevices == 0) continue;
        if (currentFilter.equals("active") && (expired || !isActive)) continue;
        if (currentFilter.equals("expired") && !expired) continue;
        if (currentFilter.equals("bound") && !bound) continue;
        if (!currentSearch.isEmpty() && !keyName.toLowerCase().contains(currentSearch)) continue;
        if (matchCount >= startIdx && matchCount < endIdx) {
            keysContainer.addView(createKeyCard(o, keyName, expiresAt, hwid, bound, maxDevices));
            rendered++;
        }
        matchCount++;
    }
    if (matchCount == 0) { keysContainer.addView(emptyText("Tidak ada key yang cocok")); return; }
    if (endIdx < matchCount) {
        int remaining = matchCount - endIdx;
        TextView btnMore = createMiniBtn("LOAD MORE (" + remaining + " left)", "#A78BFA", "#1A0E2E");
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44));
        p.setMargins(0, dp(10), 0, dp(10));
        btnMore.setLayoutParams(p);
        btnMore.setOnClickListener(v -> { renderOffset += RENDER_STEP; renderKeys(keysContainer); });
        keysContainer.addView(btnMore);
    }
    TextView info = new TextView(this);
    info.setText("Showing " + rendered + " of " + matchCount + " keys");
    info.setTextColor(Color.parseColor("#64748B"));
    info.setTextSize(10f);
    info.setGravity(Gravity.CENTER);
    info.setPadding(0, dp(8), 0, dp(8));
    keysContainer.addView(info);
}

private void bindMultiKey(View v) {
    EditText etMultiKey = v.findViewById(R.id.etMultiKey);
    EditText etMultiDuration = v.findViewById(R.id.etMultiDuration);
    EditText etCustomMax = v.findViewById(R.id.etCustomMax);
    TextView tvMultiMaxDisplay = v.findViewById(R.id.tvMultiMaxDisplay);
    LinearLayout multiContainer = v.findViewById(R.id.multiContainer);

    v.findViewById(R.id.mchip1D).setOnClickListener(x -> etMultiDuration.setText("24"));
    v.findViewById(R.id.mchip3D).setOnClickListener(x -> etMultiDuration.setText("72"));
    v.findViewById(R.id.mchip7D).setOnClickListener(x -> etMultiDuration.setText("168"));
    v.findViewById(R.id.mchip30D).setOnClickListener(x -> etMultiDuration.setText("720"));
    v.findViewById(R.id.btnMultiAutoKey).setOnClickListener(x -> etMultiKey.setText("BX-MULTI-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase()));

    View.OnClickListener devClick = x -> {
        TextView tv = (TextView) x;
        int count = Integer.parseInt(tv.getText().toString().replace("♾ UNLIMITED", "0"));
        selectedMaxDevices = count;
        tvMultiMaxDisplay.setText("Selected: " + (count == 0 ? "UNLIMITED" : count + " devices"));
        etCustomMax.setText("");
    };
    v.findViewById(R.id.dev5).setOnClickListener(devClick);
    v.findViewById(R.id.dev50).setOnClickListener(devClick);
    v.findViewById(R.id.dev100).setOnClickListener(devClick);
    v.findViewById(R.id.dev1000).setOnClickListener(devClick);
    v.findViewById(R.id.devUnlimited).setOnClickListener(devClick);

    etCustomMax.addTextChangedListener(new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
            String txt = s.toString().trim();
            if (!txt.isEmpty()) {
                try {
                    int val = Integer.parseInt(txt);
                    selectedMaxDevices = val;
                    tvMultiMaxDisplay.setText("Selected: " + (val == 0 ? "UNLIMITED" : val + " devices"));
                } catch (Exception ignored) {}
            }
        }
        @Override public void afterTextChanged(Editable s) {}
    });

    v.findViewById(R.id.btnCreateMultiKey).setOnClickListener(x -> {
        String key = etMultiKey.getText().toString().trim();
        String durStr = etMultiDuration.getText().toString().trim();
        if (key.isEmpty() || durStr.isEmpty()) { Toast.makeText(this, "Isi key dan durasi!", Toast.LENGTH_SHORT).show(); return; }
        long durHours;
        try { durHours = Long.parseLong(durStr); } catch (Exception e) { Toast.makeText(this, "Durasi harus angka!", Toast.LENGTH_SHORT).show(); return; }
        int maxDev = selectedMaxDevices;
        SupabaseHelper.createMultiKey(key, durHours * 3600 * 1000, maxDev, (success, message) -> runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            if (success) { SupabaseHelper.addLog("Created multi key: " + key); etMultiKey.setText(""); etMultiDuration.setText(""); loadMultiKeys(); loadStats(); }
        }));
    });
}

private void loadMultiKeys() {
    View v = contentFrame.getChildAt(0);
    if (v == null) return;
    LinearLayout mc = v.findViewById(R.id.multiContainer);
    if (mc == null) return;
    mc.removeAllViews();
    SupabaseHelper.fetchKeys((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null) return;
        int count = 0;
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o == null) continue;
            int maxDev = o.optInt("max_devices", 1);
            if (maxDev > 1 || maxDev == 0) {
                mc.addView(createKeyCard(o, o.optString("key", ""), parseIso(o.optString("expires_at", "")), o.isNull("hwid") ? "" : o.optString("hwid", ""), false, maxDev));
                count++;
            }
        }
        if (count == 0) mc.addView(emptyText("Belum ada multi key"));
    }));
}

private View createKeyCard(JSONObject o, String key, long expiresAt, String hwid, boolean bound, int maxDev) {
    final List<String> hwidList = new ArrayList<>();
    JSONArray listArr = o.optJSONArray("hwid_list");
    if (listArr != null) {
        for (int i = 0; i < listArr.length(); i++) {
            String v = listArr.optString(i, "");
            if (!v.isEmpty()) hwidList.add(v);
        }
    }
    boolean isMulti = (maxDev > 1 || maxDev == 0);
    int connected = isMulti ? hwidList.size() : (bound ? 1 : 0);

    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(16), dp(14), dp(16), dp(14));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setStroke(dp(1), isMulti ? Color.parseColor("#A78BFA") : Color.parseColor("#262630"));
    bg.setCornerRadius(dp(12));
    card.setBackground(bg);
    LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    cardParams.setMargins(0, 0, 0, dp(10));
    card.setLayoutParams(cardParams);

    String title = isMulti ? (key + " · " + connected + "/" + (maxDev == 0 ? "∞" : String.valueOf(maxDev))) : ((bound ? "BOUND · " : "FREE · ") + key);
    TextView tvTitle = new TextView(this);
    tvTitle.setText(title);
    tvTitle.setTextColor(Color.parseColor("#F8FAFC"));
    tvTitle.setTextSize(12f);
    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
    card.addView(tvTitle);

    if (expiresAt > 0) {
        TextView tvExp = new TextView(this);
        String expStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(expiresAt));
        long remainMs = expiresAt - System.currentTimeMillis();
        String remainStr = remainMs > 0 ? (remainMs / 3600000) + "h " + ((remainMs % 3600000) / 60000) + "m left" : "expired";
        tvExp.setText(expStr + " · " + remainStr);
        tvExp.setTextColor(Color.parseColor("#94A3B8"));
        tvExp.setTextSize(10f);
        tvExp.setPadding(0, dp(4), 0, 0);
        card.addView(tvExp);
    }

    LinearLayout row1 = new LinearLayout(this);
    row1.setOrientation(LinearLayout.HORIZONTAL);
    row1.setPadding(0, dp(10), 0, 0);
    TextView btnCopyKey = createMiniBtn("COPY KEY", "#A78BFA", "#1A0E2E");
    TextView btnCopyHwid = createMiniBtn(isMulti ? "VIEW DEVICES" : "COPY HWID", "#10B981", "#0A1F14");
    LinearLayout.LayoutParams r1p1 = new LinearLayout.LayoutParams(0, dp(36), 1f);
    r1p1.setMargins(0, 0, dp(6), 0);
    btnCopyKey.setLayoutParams(r1p1);
    btnCopyHwid.setLayoutParams(new LinearLayout.LayoutParams(0, dp(36), 1f));

    btnCopyKey.setOnClickListener(v -> {
        ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cb != null) {
            cb.setPrimaryClip(ClipData.newPlainText("Key", key));
            Toast.makeText(MainActivity.this, "Key copied", Toast.LENGTH_SHORT).show();
        }
    });
    btnCopyHwid.setOnClickListener(v -> {
        if (isMulti) showDeviceListDialog(key, hwidList, maxDev);
        else {
            if (hwid == null || hwid.isEmpty()) { Toast.makeText(MainActivity.this, "No HWID bound", Toast.LENGTH_SHORT).show(); return; }
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) {
                cb.setPrimaryClip(ClipData.newPlainText("HWID", hwid));
                Toast.makeText(MainActivity.this, "HWID copied", Toast.LENGTH_SHORT).show();
            }
        }
    });
    row1.addView(btnCopyKey);
    row1.addView(btnCopyHwid);
    card.addView(row1);

    LinearLayout row2 = new LinearLayout(this);
    row2.setOrientation(LinearLayout.HORIZONTAL);
    row2.setPadding(0, dp(6), 0, 0);
    TextView btnReset = createMiniBtn(isMulti ? "RESET ALL" : "RESET HWID", "#F59E0B", "#1F1A0A");
    TextView btnDelete = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    LinearLayout.LayoutParams r2p1 = new LinearLayout.LayoutParams(0, dp(36), 1f);
    r2p1.setMargins(0, 0, dp(6), 0);
    btnReset.setLayoutParams(r2p1);
    btnDelete.setLayoutParams(new LinearLayout.LayoutParams(0, dp(36), 1f));

    btnReset.setOnClickListener(v -> {
        if (isMulti) {
            SupabaseHelper.resetAllDevices(key, (success, msg) -> runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (success) { SupabaseHelper.addLog("Reset all devices: " + key); loadMultiKeys(); }
            }));
        } else {
            SupabaseHelper.resetHwid(key, (success, msg) -> runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (success) { SupabaseHelper.addLog("Reset HWID: " + key); loadKeys(); }
            }));
        }
    });
    btnDelete.setOnClickListener(v -> new AlertDialog.Builder(MainActivity.this)
            .setTitle("Delete Key")
            .setMessage("Hapus key " + key + "?")
            .setPositiveButton("Hapus", (d, w) -> SupabaseHelper.deleteKey(key, (success, msg) -> runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (success) { SupabaseHelper.addLog("Deleted key: " + key); loadKeys(); loadMultiKeys(); loadStats(); }
            }))).setNegativeButton("Batal", null).show());

    row2.addView(btnReset);
    row2.addView(btnDelete);
    card.addView(row2);
    return card;
}

private void showDeviceListDialog(String key, List<String> hwidList, long maxDev) {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(20), dp(20), dp(20));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setCornerRadius(dp(16));
    root.setBackground(bg);

    TextView tvTitle = new TextView(this);
    tvTitle.setText(hwidList.size() + "/" + (maxDev == 0 ? "∞" : String.valueOf(maxDev)) + " DEVICES");
    tvTitle.setTextSize(14f);
    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
    tvTitle.setTextColor(Color.parseColor("#A78BFA"));
    tvTitle.setGravity(Gravity.CENTER);
    tvTitle.setPadding(0, 0, 0, dp(14));
    root.addView(tvTitle);

    if (hwidList.isEmpty()) {
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText("Tidak ada device terhubung");
        tvEmpty.setTextSize(11f);
        tvEmpty.setTextColor(Color.parseColor("#64748B"));
        tvEmpty.setGravity(Gravity.CENTER);
        tvEmpty.setPadding(0, dp(20), 0, dp(20));
        root.addView(tvEmpty);
    } else {
        for (final String hwid : hwidList) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(8), dp(10), dp(8));
            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setColor(Color.parseColor("#0F0B21"));
            rowBg.setCornerRadius(dp(8));
            row.setBackground(rowBg);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, dp(6));
            row.setLayoutParams(rowParams);

            TextView tvHwid = new TextView(this);
            tvHwid.setText(hwid.length() > 20 ? hwid.substring(0, 8) + "..." + hwid.substring(hwid.length() - 6) : hwid);
            tvHwid.setTextSize(10f);
            tvHwid.setTextColor(Color.parseColor("#F8FAFC"));
            tvHwid.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tvHwid);

            TextView btnRemove = createMiniBtn("REMOVE", "#EF4444", "#1F0A0A");
            btnRemove.setLayoutParams(new LinearLayout.LayoutParams(dp(75), dp(30)));
            btnRemove.setTextSize(9f);
            btnRemove.setOnClickListener(v -> SupabaseHelper.removeDeviceFromKey(key, hwid, (success, msg) -> runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (success) loadMultiKeys();
            })));
            row.addView(btnRemove);
            root.addView(row);
        }
    }
    AlertDialog dialog = new AlertDialog.Builder(this).setView(root).setPositiveButton("TUTUP", null).create();
    dialog.show();
    if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#14141C")));
}

private void deleteFiltered() {
    new AlertDialog.Builder(this)
            .setTitle("Delete " + currentFilter.toUpperCase() + " keys")
            .setMessage("Hapus semua key dengan filter: " + currentFilter + "?\n\nTidak bisa dibatalkan.")
            .setPositiveButton("HAPUS", (d, w) -> {
                long now = System.currentTimeMillis();
                JSONArray toDelete = new JSONArray();
                for (int i = 0; i < cachedKeys.length(); i++) {
                    JSONObject o = cachedKeys.optJSONObject(i);
                    if (o == null) continue;
                    String keyName = o.optString("key", "");
                    long expiresAt = parseIso(o.optString("expires_at", ""));
                    String hwid = o.isNull("hwid") ? "" : o.optString("hwid", "");
                    boolean isActive = o.optBoolean("is_active", false), bound = hwid != null && !hwid.isEmpty(), expired = expiresAt > 0 && now > expiresAt;
                    boolean shouldDelete = false;
                    if (currentFilter.equals("all")) shouldDelete = true;
                    else if (currentFilter.equals("active")) shouldDelete = (isActive && !expired);
                    else if (currentFilter.equals("expired")) shouldDelete = expired;
                    else if (currentFilter.equals("bound")) shouldDelete = bound;
                    if (shouldDelete && !keyName.isEmpty()) toDelete.put(keyName);
                }
                if (toDelete.length() == 0) { Toast.makeText(this, "Tidak ada key untuk dihapus", Toast.LENGTH_SHORT).show(); return; }
                Toast.makeText(this, "Menghapus " + toDelete.length() + " keys...", Toast.LENGTH_SHORT).show();
                SupabaseHelper.deleteKeysBatch(toDelete, (success, msg) -> runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, success ? toDelete.length() + " keys terhapus" : msg, Toast.LENGTH_LONG).show();
                    if (success) { SupabaseHelper.addLog("Batch deleted " + toDelete.length() + " keys"); loadKeys(); loadStats(); }
                }));
            }).setNegativeButton("Batal", null).show();
}

private void bindStore(View v) {
    TextView btnAdd = v.findViewById(R.id.btnAddStore);
    LinearLayout container = v.findViewById(R.id.storeContainer);
    btnAdd.setOnClickListener(x -> showStoreDialog(null, container));
}

private void loadStore() {
    View v = contentFrame.getChildAt(0);
    if (v == null) return;
    LinearLayout sc = v.findViewById(R.id.storeContainer);
    if (sc == null) return;
    sc.removeAllViews();
    SupabaseHelper.fetchStore((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null || data.length() == 0) {
            sc.addView(emptyText("Belum ada produk"));
            return;
        }
        cachedStore = data;
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) sc.addView(createStoreCard(o, sc));
        }
    }));
}

private View createStoreCard(JSONObject o, LinearLayout parent) {
    String id = o.optString("id", ""), cat = o.optString("category", ""), name = o.optString("name", "");
    int price = o.optInt("price", 0);
    String priceText = o.optString("price_text", price > 0 ? "Rp " + price : "Nego");
    boolean isActive = o.optBoolean("is_active", true);
    String desc = o.optString("description", "");

    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(14), dp(12), dp(14), dp(12));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setStroke(dp(1), isActive ? Color.parseColor("#A78BFA") : Color.parseColor("#262630"));
    bg.setCornerRadius(dp(10));
    card.setBackground(bg);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(8));
    card.setLayoutParams(p);

    TextView tvTop = new TextView(this);
    tvTop.setText((isActive ? "AKTIF · " : "OFF · ") + cat);
    tvTop.setTextSize(9f);
    tvTop.setTextColor(Color.parseColor("#06B6D4"));
    tvTop.setTypeface(null, android.graphics.Typeface.BOLD);
    card.addView(tvTop);

    TextView tvName = new TextView(this);
    tvName.setText(name);
    tvName.setTextColor(Color.parseColor("#F8FAFC"));
    tvName.setTextSize(12f);
    tvName.setTypeface(null, android.graphics.Typeface.BOLD);
    tvName.setPadding(0, dp(4), 0, 0);
    card.addView(tvName);

    TextView tvPrice = new TextView(this);
    tvPrice.setText(priceText);
    tvPrice.setTextColor(Color.parseColor("#A855F7"));
    tvPrice.setTextSize(11f);
    tvPrice.setTypeface(null, android.graphics.Typeface.BOLD);
    card.addView(tvPrice);

    if (!desc.isEmpty()) {
        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextColor(Color.parseColor("#94A3B8"));
        tvDesc.setTextSize(9f);
        tvDesc.setPadding(0, dp(4), 0, 0);
        card.addView(tvDesc);
    }

    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setPadding(0, dp(8), 0, 0);
    TextView btnEdit = createMiniBtn("EDIT", "#A78BFA", "#1A0E2E");
    TextView btnToggle = createMiniBtn(isActive ? "OFF" : "ON", isActive ? "#F59E0B" : "#22C55E", isActive ? "#1F1A0A" : "#0A1F14");
    TextView btnDel = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    LinearLayout.LayoutParams r1 = new LinearLayout.LayoutParams(0, dp(32), 1f);
    r1.setMargins(0, 0, dp(4), 0);
    btnEdit.setLayoutParams(r1);
    LinearLayout.LayoutParams r2 = new LinearLayout.LayoutParams(0, dp(32), 1f);
    r2.setMargins(0, 0, dp(4), 0);
    btnToggle.setLayoutParams(r2);
    btnDel.setLayoutParams(new LinearLayout.LayoutParams(0, dp(32), 1f));

    btnEdit.setOnClickListener(x -> showStoreDialog(o, parent));
    btnToggle.setOnClickListener(x -> SupabaseHelper.toggleStore(id, !isActive, (s, m) -> runOnUiThread(() -> { if (s) loadStore(); })));
    btnDel.setOnClickListener(x -> new AlertDialog.Builder(this)
            .setTitle("Hapus Produk").setMessage("Hapus " + name + "?")
            .setPositiveButton("Hapus", (d, w) -> SupabaseHelper.deleteStore(id, (s, m) -> runOnUiThread(() -> { if (s) loadStore(); })))
            .setNegativeButton("Batal", null).show());
    row.addView(btnEdit);
    row.addView(btnToggle);
    row.addView(btnDel);
    card.addView(row);
    return card;
}

private void showStoreDialog(final JSONObject existing, final LinearLayout parent) {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(20), dp(20), dp(20));

    EditText etCat = makeInput(existing == null ? "" : existing.optString("category", ""), "Kategori");
    EditText etName = makeInput(existing == null ? "" : existing.optString("name", ""), "Nama produk");
    EditText etPrice = makeInput(existing == null ? "" : String.valueOf(existing.optInt("price", 0)), "Harga (0 = Nego)");
    EditText etDesc = makeInput(existing == null ? "" : existing.optString("description", ""), "Deskripsi");
    etDesc.setMinLines(2);
    root.addView(etCat);
    root.addView(etName);
    root.addView(etPrice);
    root.addView(etDesc);

    AlertDialog dialog = new AlertDialog.Builder(this).setView(root).setPositiveButton("SIMPAN", null).setNegativeButton("BATAL", null).create();
    dialog.show();
    if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#14141C")));
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        String cat = etCat.getText().toString().trim(), name = etName.getText().toString().trim(), desc = etDesc.getText().toString().trim();
        int price = 0;
        try { price = Integer.parseInt(etPrice.getText().toString().trim()); } catch (Exception ignored) {}
        if (cat.isEmpty() || name.isEmpty()) { Toast.makeText(this, "Kategori & nama wajib", Toast.LENGTH_SHORT).show(); return; }
        final int fp = price;
        if (existing == null) {
            SupabaseHelper.addStore(cat, name, fp, "", desc, "", 0, (s, m) -> runOnUiThread(() -> { if (s) { dialog.dismiss(); loadStore(); } }));
        } else {
            try {
                JSONObject patch = new JSONObject();
                patch.put("category", cat); patch.put("name", name); patch.put("price", fp); patch.put("description", desc);
                SupabaseHelper.editStore(existing.optString("id", ""), patch, (s, m) -> runOnUiThread(() -> { if (s) { dialog.dismiss(); loadStore(); } }));
            } catch (Exception ignored) {}
        }
    });
}

private void bindApkUpdate(View v) {
    TextView btnAdd = v.findViewById(R.id.btnAddApk);
    btnAdd.setOnClickListener(x -> showApkDialog(null));
}

private void loadApk() {
    View v = contentFrame.getChildAt(0);
    if (v == null) return;
    LinearLayout ac = v.findViewById(R.id.apkContainer);
    if (ac == null) return;
    ac.removeAllViews();
    SupabaseHelper.fetchApkUpdates((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null || data.length() == 0) { ac.addView(emptyText("Belum ada APK")); return; }
        cachedApk = data;
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) ac.addView(createApkCard(o));
        }
    }));
}

private View createApkCard(JSONObject o) {
    String id = o.optString("id", ""), cat = o.optString("category", ""), name = o.optString("name", ""), ver = o.optString("version", "");
    boolean isActive = o.optBoolean("is_active", true);
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(14), dp(12), dp(14), dp(12));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setStroke(dp(1), isActive ? Color.parseColor("#06B6D4") : Color.parseColor("#262630"));
    bg.setCornerRadius(dp(10));
    card.setBackground(bg);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(8));
    card.setLayoutParams(p);

    TextView tvName = new TextView(this);
    tvName.setText(name);
    tvName.setTextColor(Color.parseColor("#F8FAFC"));
    tvName.setTextSize(12f);
    tvName.setTypeface(null, android.graphics.Typeface.BOLD);
    card.addView(tvName);

    TextView tvMeta = new TextView(this);
    tvMeta.setText(cat + " · " + ver);
    tvMeta.setTextColor(Color.parseColor("#A855F7"));
    tvMeta.setTextSize(10f);
    card.addView(tvMeta);

    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setPadding(0, dp(8), 0, 0);
    TextView btnEdit = createMiniBtn("EDIT", "#A78BFA", "#1A0E2E");
    TextView btnDel = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    btnEdit.setLayoutParams(new LinearLayout.LayoutParams(0, dp(32), 1f));
    btnDel.setLayoutParams(new LinearLayout.LayoutParams(0, dp(32), 1f));
    btnEdit.setOnClickListener(x -> showApkDialog(o));
    btnDel.setOnClickListener(x -> SupabaseHelper.deleteApkUpdate(id, (s, m) -> runOnUiThread(() -> { if (s) loadApk(); })));
    row.addView(btnEdit);
    row.addView(btnDel);
    card.addView(row);
    return card;
}

private void showApkDialog(final JSONObject existing) {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(20), dp(20), dp(20));

    EditText etCat = makeInput(existing == null ? "" : existing.optString("category", ""), "Kategori");
    EditText etName = makeInput(existing == null ? "" : existing.optString("name", ""), "Nama APK");
    EditText etVer = makeInput(existing == null ? "" : existing.optString("version", ""), "Versi");
    EditText etSize = makeInput(existing == null ? "" : existing.optString("size", ""), "Ukuran (15 MB)");
    EditText etCh = makeInput(existing == null ? "" : existing.optString("changelog", ""), "Changelog");
    EditText etLink = makeInput(existing == null ? "" : existing.optString("link", ""), "Link Download");
    EditText etGetkey = makeInput(existing == null ? "" : existing.optString("getkey_link", ""), "Link Getkey");

    TextView tvImg = new TextView(this);
    tvImg.setText("Tap untuk pilih gambar");
    tvImg.setTextColor(Color.parseColor("#A78BFA"));
    tvImg.setPadding(0, dp(10), 0, dp(10));
    if (existing != null && !existing.optString("screenshot", "").isEmpty()) {
        tvImg.setText(existing.optString("screenshot"));
        tvImg.setTag(existing.optString("screenshot"));
    }
    tvImg.setOnClickListener(x -> {
        activeScreenshotDisplay = tvImg;
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    });

    root.addView(etCat);
    root.addView(etName);
    root.addView(etVer);
    root.addView(etSize);
    root.addView(etCh);
    root.addView(etLink);
    root.addView(etGetkey);
    root.addView(tvImg);

    AlertDialog dialog = new AlertDialog.Builder(this).setView(root).setPositiveButton("SIMPAN", null).setNegativeButton("BATAL", null).create();
    dialog.show();
    if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#14141C")));
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        String cat = etCat.getText().toString().trim(), name = etName.getText().toString().trim(), ver = etVer.getText().toString().trim(), size = etSize.getText().toString().trim(), ch = etCh.getText().toString().trim(), link = etLink.getText().toString().trim(), getkey = etGetkey.getText().toString().trim();
        String screenshot = tvImg.getTag() != null ? tvImg.getTag().toString() : "";
        if (name.isEmpty()) return;
        if (existing == null) {
            SupabaseHelper.addApkUpdate(cat, name, ver, size, ch, link, getkey, screenshot, 0, (s, m) -> runOnUiThread(() -> { if (s) { dialog.dismiss(); loadApk(); } }));
        } else {
            try {
                JSONObject patch = new JSONObject();
                patch.put("category", cat); patch.put("name", name); patch.put("version", ver); patch.put("size", size); patch.put("changelog", ch); patch.put("link", link); patch.put("getkey_link", getkey); patch.put("screenshot", screenshot);
                SupabaseHelper.editApkUpdate(existing.optString("id", ""), patch, (s, m) -> runOnUiThread(() -> { if (s) { dialog.dismiss(); loadApk(); } }));
            } catch (Exception ignored) {}
        }
    });
}

private void bindComments(View v) {
    // no button to bind directly, data loaded on loadComments
}

private void loadComments() {
    View v = contentFrame.getChildAt(0);
    if (v == null) return;
    LinearLayout cc = v.findViewById(R.id.commentsContainer);
    if (cc == null) return;
    cc.removeAllViews();
    SupabaseHelper.fetchComments((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null || data.length() == 0) { cc.addView(emptyText("Belum ada komentar")); return; }
        cachedComments = data;
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) cc.addView(createCommentCard(o));
        }
    }));
}

private View createCommentCard(JSONObject o) {
    String id = o.optString("id", ""), name = o.optString("name", ""), comment = o.optString("comment", "");
    boolean isHidden = o.optBoolean("is_hidden", false);
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(14), dp(12), dp(14), dp(12));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setCornerRadius(dp(10));
    card.setBackground(bg);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(8));
    card.setLayoutParams(p);

    TextView tvName = new TextView(this);
    tvName.setText(name + (isHidden ? " (HIDDEN)" : ""));
    tvName.setTextColor(Color.parseColor("#A78BFA"));
    tvName.setTextSize(12f);
    tvName.setTypeface(null, android.graphics.Typeface.BOLD);
    card.addView(tvName);

    TextView tvComment = new TextView(this);
    tvComment.setText(comment);
    tvComment.setTextColor(Color.parseColor("#F8FAFC"));
    tvComment.setTextSize(11f);
    tvComment.setPadding(0, dp(8), 0, 0);
    card.addView(tvComment);

    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setPadding(0, dp(8), 0, 0);
    TextView btnHide = createMiniBtn(isHidden ? "SHOW" : "HIDE", isHidden ? "#22C55E" : "#F59E0B", isHidden ? "#0A1F14" : "#1F1A0A");
    TextView btnDel = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    btnHide.setLayoutParams(new LinearLayout.LayoutParams(0, dp(32), 1f));
    btnDel.setLayoutParams(new LinearLayout.LayoutParams(0, dp(32), 1f));
    btnHide.setOnClickListener(x -> SupabaseHelper.hideComment(id, !isHidden, (s, m) -> runOnUiThread(() -> { if (s) loadComments(); })));
    btnDel.setOnClickListener(x -> SupabaseHelper.deleteComment(id, (s, m) -> runOnUiThread(() -> { if (s) loadComments(); })));
    row.addView(btnHide);
    row.addView(btnDel);
    card.addView(row);
    return card;
}

private void bindKategori(View v) {
    EditText etNewCategory = v.findViewById(R.id.etNewCategory);
    TextView btnAdd = v.findViewById(R.id.btnAddCategory);
    btnAdd.setOnClickListener(x -> addCategory(etNewCategory));
}

private void addCategory(EditText et) {
    String name = et.getText().toString().trim().toUpperCase();
    if (name.isEmpty()) return;
    SupabaseHelper.addCategory(name, 0, (success, msg) -> runOnUiThread(() -> {
        if (success) { et.setText(""); loadCategories(); }
    }));
}

private void loadCategories() {
    View v = contentFrame.getChildAt(0);
    if (v == null) return;
    LinearLayout cc = v.findViewById(R.id.categoriesContainer);
    if (cc == null) return;
    cc.removeAllViews();
    SupabaseHelper.fetchCategories((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null || data.length() == 0) { cc.addView(emptyText("Belum ada kategori")); return; }
        cachedCategories = data;
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) cc.addView(createCategoryRow(o, cc));
        }
    }));
}

private View createCategoryRow(JSONObject o, LinearLayout parent) {
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(14), dp(10), dp(14), dp(10));
    TextView tvName = new TextView(this);
    tvName.setText(o.optString("name", ""));
    tvName.setTextColor(Color.parseColor("#F8FAFC"));
    tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    row.addView(tvName);
    TextView btnDel = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    btnDel.setLayoutParams(new LinearLayout.LayoutParams(dp(85), dp(32)));
    btnDel.setOnClickListener(x -> SupabaseHelper.deleteCategory(o.optString("id", ""), (s, m) -> runOnUiThread(() -> { if (s) loadCategories(); })));
    row.addView(btnDel);
    return row;
}

private void bindFilter(View v) {
    EditText etBadWord = v.findViewById(R.id.etBadWord);
    TextView btnAddBadWord = v.findViewById(R.id.btnAddBadWord);
    btnAddBadWord.setOnClickListener(x -> {
        String word = etBadWord.getText().toString().trim().toLowerCase();
        if (word.isEmpty()) return;
        SupabaseHelper.addBadWord(word, (success, msg) -> runOnUiThread(() -> {
            if (success) { etBadWord.setText(""); loadBadWords(); }
        }));
    });
}

private void loadBadWords() {
    View v = contentFrame.getChildAt(0);
    if (v == null) return;
    LinearLayout bc = v.findViewById(R.id.badWordsContainer);
    if (bc == null) return;
    bc.removeAllViews();
    SupabaseHelper.fetchBadWords((success, data, message) -> runOnUiThread(() -> {
        if (!success || data == null || data.length() == 0) { bc.addView(emptyText("Belum ada kata filter")); return; }
        cachedBadWords = data;
        for (int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            if (o != null) bc.addView(createBadWordRow(o));
        }
    }));
}

private View createBadWordRow(JSONObject o) {
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(14), dp(10), dp(14), dp(10));
    TextView tvWord = new TextView(this);
    tvWord.setText(o.optString("word", ""));
    tvWord.setTextColor(Color.parseColor("#EF4444"));
    tvWord.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    row.addView(tvWord);
    TextView btnDel = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    btnDel.setLayoutParams(new LinearLayout.LayoutParams(dp(85), dp(32)));
    btnDel.setOnClickListener(x -> SupabaseHelper.deleteBadWord(o.optString("id", ""), (s, m) -> runOnUiThread(() -> { if (s) loadBadWords(); })));
    row.addView(btnDel);
    return row;
}

private EditText makeInput(String value, String hint) {
    EditText et = new EditText(this);
    et.setText(value);
    et.setHint(hint);
    et.setTextColor(Color.parseColor("#F8FAFC"));
    et.setHintTextColor(Color.parseColor("#64748B"));
    et.setTextSize(12f);
    et.setBackgroundResource(R.drawable.edit_bg);
    et.setPadding(dp(14), dp(14), dp(14), dp(14));
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(8));
    et.setLayoutParams(p);
    return et;
}

private TextView createMiniBtn(String text, String textColor, String bgColor) {
    TextView btn = new TextView(this);
    btn.setText(text);
    btn.setTextSize(10f);
    btn.setTypeface(null, android.graphics.Typeface.BOLD);
    btn.setTextColor(Color.parseColor(textColor));
    btn.setGravity(Gravity.CENTER);
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor(bgColor));
    bg.setStroke(dp(1), Color.parseColor(textColor));
    bg.setCornerRadius(dp(8));
    btn.setBackground(bg);
    btn.setClickable(true);
    btn.setFocusable(true);
    return btn;
}

    private void bindBroadcast(View v) {
        EditText etBcastTitle = v.findViewById(R.id.etBcastTitle);
        EditText etBcastMessage = v.findViewById(R.id.etBcastMessage);
        CheckBox cbBcastWeb = v.findViewById(R.id.cbBcastWeb);
        CheckBox cbBcastMain = v.findViewById(R.id.cbBcastMain);
        CheckBox cbBcastApk = v.findViewById(R.id.cbBcastApk);
        CheckBox cbMaintWeb = v.findViewById(R.id.cbMaintWeb);
        CheckBox cbMaintMain = v.findViewById(R.id.cbMaintMain);
        CheckBox cbMaintApk = v.findViewById(R.id.cbMaintApk);
        EditText etMaintMessage = v.findViewById(R.id.etMaintMessage);
        TextView tvMaintStatus = v.findViewById(R.id.tvMaintStatus);

        v.findViewById(R.id.btnSendBcast).setOnClickListener(x -> {
            String title = etBcastTitle.getText().toString().trim();
            String message = etBcastMessage.getText().toString().trim();
            if (title.isEmpty() || message.isEmpty()) { Toast.makeText(this, "Isi judul dan pesan!", Toast.LENGTH_SHORT).show(); return; }
            boolean showWeb = cbBcastWeb.isChecked(), showMain = cbBcastMain.isChecked(), showApk = cbBcastApk.isChecked();
            if (!showWeb && !showMain && !showApk) { Toast.makeText(this, "Pilih minimal 1 target!", Toast.LENGTH_SHORT).show(); return; }
            SupabaseHelper.sendBroadcast(title, message, showWeb, showMain, showApk, (success, msg) -> runOnUiThread(() -> {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                if (success) { SupabaseHelper.addLog("Broadcast: " + title); etBcastTitle.setText(""); etBcastMessage.setText(""); loadBroadcasts(); }
            }));
        });

        v.findViewById(R.id.btnMaintOn).setOnClickListener(x -> {
            String msg = etMaintMessage.getText().toString().trim();
            if (msg.isEmpty()) msg = "Server maintenance, coba lagi nanti.";
            boolean applyWeb = cbMaintWeb.isChecked(), applyMain = cbMaintMain.isChecked(), applyApk = cbMaintApk.isChecked();
            if (!applyWeb && !applyMain && !applyApk) { Toast.makeText(this, "Pilih minimal 1 target!", Toast.LENGTH_SHORT).show(); return; }
            final String fMsg = msg;
            SupabaseHelper.setMaintenance(true, fMsg, applyWeb, applyMain, applyApk, (success, m) -> runOnUiThread(() -> {
                Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
                if (success) { SupabaseHelper.addLog("Maintenance ON"); loadMaintStatus(v); }
            }));
        });
        v.findViewById(R.id.btnMaintOff).setOnClickListener(x -> {
            boolean applyWeb = cbMaintWeb.isChecked(), applyMain = cbMaintMain.isChecked(), applyApk = cbMaintApk.isChecked();
            SupabaseHelper.setMaintenance(false, "", applyWeb, applyMain, applyApk, (success, m) -> runOnUiThread(() -> {
                Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
                if (success) { SupabaseHelper.addLog("Maintenance OFF"); loadMaintStatus(v); }
            }));
        });
    }

    private void loadBroadcasts() {
        View v = contentFrame.getChildAt(0);
        if (v == null) return;
        LinearLayout bc = v.findViewById(R.id.bcastContainer);
        if (bc == null) return;
        bc.removeAllViews();
        SupabaseHelper.fetchBroadcasts((success, data, message) -> runOnUiThread(() -> {
            if (!success || data == null || data.length() == 0) { bc.addView(emptyText("Belum ada broadcast")); return; }
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o != null) bc.addView(createBcastCard(o));
            }
        }));
    }

    private View createBcastCard(JSONObject o) {
        String id = o.optString("id", ""), title = o.optString("title", ""), message = o.optString("message", "");
        long ts = parseIso(o.optString("timestamp", ""));
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setStroke(dp(1), Color.parseColor("#262630"));
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(p);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.parseColor("#A78BFA"));
        tvTitle.setTextSize(12f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvTitle);

        TextView tvMsg = new TextView(this);
        tvMsg.setText(message);
        tvMsg.setTextColor(Color.parseColor("#F8FAFC"));
        tvMsg.setTextSize(11f);
        tvMsg.setPadding(0, dp(6), 0, 0);
        card.addView(tvMsg);

        StringBuilder targets = new StringBuilder("Target: ");
        if (o.optBoolean("show_web", false)) targets.append("Web ");
        if (o.optBoolean("show_main", false)) targets.append("Main ");
        if (o.optBoolean("show_apk", false)) targets.append("APK ");
        TextView tvTarget = new TextView(this);
        tvTarget.setText(targets.toString().trim());
        tvTarget.setTextColor(Color.parseColor("#A78BFA"));
        tvTarget.setTextSize(9f);
        tvTarget.setPadding(0, dp(6), 0, 0);
        card.addView(tvTarget);

        if (ts > 0) {
            TextView tvTime = new TextView(this);
            tvTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(ts)));
            tvTime.setTextColor(Color.parseColor("#94A3B8"));
            tvTime.setTextSize(9f);
            tvTime.setPadding(0, dp(6), 0, 0);
            card.addView(tvTime);
        }

        TextView btnDelete = createMiniBtn("DELETE BROADCAST", "#EF4444", "#1F0A0A");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(36));
        bp.setMargins(0, dp(10), 0, 0);
        btnDelete.setLayoutParams(bp);
        btnDelete.setOnClickListener(x -> new AlertDialog.Builder(this).setTitle("Hapus Broadcast").setMessage("Hapus broadcast ini?").setPositiveButton("Hapus", (d, w) -> SupabaseHelper.deleteBroadcast(id, (s, m) -> runOnUiThread(() -> {
            if (s) { SupabaseHelper.addLog("Deleted broadcast: " + title); loadBroadcasts(); }
        }))).setNegativeButton("Batal", null).show());
        card.addView(btnDelete);
        return card;
    }

    private void loadMaintStatus(View v) {
        TextView tvStatus = v.findViewById(R.id.tvMaintStatus);
        if (tvStatus == null) return;
        SupabaseHelper.fetchMaintenance((success, data, message) -> runOnUiThread(() -> {
            if (success && data != null) {
                boolean active = data.optBoolean("maintenance", false);
                String msg = data.optString("message", "");
                if (active) {
                    tvStatus.setText("Status: ON" + (!msg.isEmpty() ? " · " + msg : ""));
                    tvStatus.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    tvStatus.setText("Status: OFF");
                    tvStatus.setTextColor(Color.parseColor("#22C55E"));
                }
                if (!maintCheckboxInitialized) {
                    maintCheckboxInitialized = true;
                    CheckBox cbWeb = v.findViewById(R.id.cbMaintWeb), cbMain = v.findViewById(R.id.cbMaintMain), cbApk = v.findViewById(R.id.cbMaintApk);
                    if (cbWeb != null) cbWeb.setChecked(data.optBoolean("apply_web", true));
                    if (cbMain != null) cbMain.setChecked(data.optBoolean("apply_main", true));
                    if (cbApk != null) cbApk.setChecked(data.optBoolean("apply_apk", true));
                }
            } else {
                tvStatus.setText("Status: OFF");
                tvStatus.setTextColor(Color.parseColor("#22C55E"));
            }
        }));
    }

    private void bindLogs(View v) {
        // no button to bind directly, data loaded on loadLogs
    }

    private void loadLogs() {
        View v = contentFrame.getChildAt(0);
        if (v == null) return;
        LinearLayout lc = v.findViewById(R.id.logsContainer);
        if (lc == null) return;
        lc.removeAllViews();
        SupabaseHelper.fetchLogs((success, data, message) -> runOnUiThread(() -> {
            if (!success || data == null || data.length() == 0) { lc.addView(emptyText("Belum ada log")); return; }
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                String action = o.optString("action", "");
                long ts = parseIso(o.optString("timestamp", ""));
                if (action.isEmpty()) continue;
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(dp(14), dp(10), dp(14), dp(10));
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor("#14141C"));
                bg.setCornerRadius(dp(10));
                row.setBackground(bg);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                p.setMargins(0, 0, 0, dp(8));
                row.setLayoutParams(p);
                TextView tvLog = new TextView(this);
                String timeStr = ts > 0 ? "[" + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(ts)) + "] " : "";
                tvLog.setText("• " + timeStr + action);
                tvLog.setTextColor(Color.parseColor("#C4C0E2"));
                tvLog.setTextSize(11f);
                row.addView(tvLog);
                lc.addView(row);
            }
        }));
    }

    private void bindSuspect(View v) {
        EditText etSearchSuspect = v.findViewById(R.id.etSearchSuspect);
        TextView btnSuspectAll = v.findViewById(R.id.btnSuspectAll);
        TextView btnSuspectBlocked = v.findViewById(R.id.btnSuspectBlocked);
        etSearchSuspect.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { renderSuspect(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        btnSuspectAll.setOnClickListener(x -> { btnSuspectAll.setBackgroundResource(R.drawable.tab_active); btnSuspectBlocked.setBackgroundResource(R.drawable.chip_bg); renderSuspect(); });
        btnSuspectBlocked.setOnClickListener(x -> { btnSuspectBlocked.setBackgroundResource(R.drawable.tab_active); btnSuspectAll.setBackgroundResource(R.drawable.chip_bg); renderSuspect(); });
    }

    private void loadSuspect() {
        SupabaseHelper.fetchSuspect((success, data, message) -> runOnUiThread(() -> { if (success && data != null) { cachedSuspect = data; renderSuspect(); } }));
        SupabaseHelper.fetchBlocked((success, data, message) -> runOnUiThread(() -> { if (success && data != null) { cachedBlocked = data; renderSuspect(); } }));
    }

    private void renderSuspect() {
        View v = contentFrame.getChildAt(0);
        if (v == null) return;
        LinearLayout sc = v.findViewById(R.id.suspectContainer);
        if (sc == null) return;
        sc.removeAllViews();
        if (cachedSuspect.length() == 0) { sc.addView(emptyText("Tidak ada suspect")); return; }
        List<String> blockedFps = new ArrayList<>();
        for (int i = 0; i < cachedBlocked.length(); i++) {
            JSONObject b = cachedBlocked.optJSONObject(i);
            if (b != null) blockedFps.add(b.optString("fp", ""));
        }
        int count = 0;
        for (int i = 0; i < cachedSuspect.length(); i++) {
            JSONObject o = cachedSuspect.optJSONObject(i);
            if (o == null) continue;
            String fp = o.optString("fp", "");
            if (!blockedFps.contains(fp)) continue;
            if (count >= 200) break;
            sc.addView(createSuspectCard(o, true));
            count++;
        }
        if (count == 0) sc.addView(emptyText("Tidak ada suspect blocked"));
    }

    private View createSuspectCard(JSONObject o, boolean isBlocked) {
        String fp = o.optString("fp", ""), reason = o.optString("reason", "unknown"), ip = o.optString("ip", "");
        long ts = parseIso(o.optString("created_at", ""));
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setStroke(dp(1), isBlocked ? Color.parseColor("#EF4444") : Color.parseColor("#262630"));
        bg.setCornerRadius(dp(10));
        card.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, dp(8));
        card.setLayoutParams(p);

        TextView tvFp = new TextView(this);
        tvFp.setText(fp.length() > 20 ? fp.substring(0, 12) + "..." : fp);
        tvFp.setTextColor(isBlocked ? Color.parseColor("#EF4444") : Color.parseColor("#F59E0B"));
        tvFp.setTextSize(11f);
        tvFp.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvFp);

        TextView tvInfo = new TextView(this);
        String timeStr = ts > 0 ? new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date(ts)) : "";
        tvInfo.setText("Reason: " + reason + (!ip.isEmpty() ? " · IP: " + ip : "") + (timeStr.isEmpty() ? "" : " · " + timeStr));
        tvInfo.setTextColor(Color.parseColor("#94A3B8"));
        tvInfo.setTextSize(9f);
        tvInfo.setPadding(0, dp(4), 0, 0);
        card.addView(tvInfo);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, 0);
        TextView btnCopy = createMiniBtn("COPY FP", "#A78BFA", "#1A0E2E");
        TextView btnBlock = createMiniBtn(isBlocked ? "UNBLOCK" : "BLOCK", isBlocked ? "#22C55E" : "#EF4444", isBlocked ? "#0A1F14" : "#1F0A0A");
        LinearLayout.LayoutParams rp1 = new LinearLayout.LayoutParams(0, dp(32), 1f);
        rp1.setMargins(0, 0, dp(6), 0);
        btnCopy.setLayoutParams(rp1);
        btnBlock.setLayoutParams(new LinearLayout.LayoutParams(0, dp(32), 1f));
        btnCopy.setOnClickListener(x -> {
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) { cb.setPrimaryClip(ClipData.newPlainText("FP", fp)); Toast.makeText(this, "FP copied", Toast.LENGTH_SHORT).show(); }
        });
        btnBlock.setOnClickListener(x -> {
            if (isBlocked) SupabaseHelper.unblockFp(fp, (s, m) -> runOnUiThread(() -> { if (s) loadSuspect(); }));
            else SupabaseHelper.blockFp(fp, "manual", (s, m) -> runOnUiThread(() -> { if (s) loadSuspect(); }));
        });
        row.addView(btnCopy);
        row.addView(btnBlock);
        card.addView(row);
        return card;
    }

    private void bindLinks(View v) {
        TextView btnAddLink = v.findViewById(R.id.btnAddLink);
        TextView btnBulkImport = v.findViewById(R.id.btnBulkImport);
        TextView btnRefreshLinks = v.findViewById(R.id.btnRefreshLinks);
        btnAddLink.setOnClickListener(x -> showAddLinkDialog());
        btnBulkImport.setOnClickListener(x -> showBulkImportDialog());
        btnRefreshLinks.setOnClickListener(x -> confirmRefreshLinks());
    }

    private void loadLinks() {
        View v = contentFrame.getChildAt(0);
        if (v == null) return;
        LinearLayout lc = v.findViewById(R.id.linksContainer);
        if (lc == null) return;
        lc.removeAllViews();
        SupabaseHelper.fetchLinks((success, data, message) -> runOnUiThread(() -> {
            if (!success || data == null) { lc.addView(emptyText("Gagal memuat links")); return; }
            int activeCount = 0;
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o != null && o.optBoolean("is_active", false)) activeCount++;
            }
            TextView tvTitle = v.findViewById(R.id.tvLinksTitle);
            if (tvTitle != null) tvTitle.setText("LINK LIST (" + activeCount + "/" + data.length() + " active)");
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o != null) lc.addView(createLinkCard(o));
            }
        }));
    }

    private View createLinkCard(JSONObject o) {
        String id = o.optString("id", ""), url = o.optString("url", "");
        boolean isActive = o.optBoolean("is_active", false);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(10), dp(14), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setStroke(dp(1), isActive ? Color.parseColor("#22C55E") : Color.parseColor("#262630"));
        bg.setCornerRadius(dp(10));
        card.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, dp(8));
        card.setLayoutParams(p);

        TextView tvUrl = new TextView(this);
        tvUrl.setText(url);
        tvUrl.setTextColor(isActive ? Color.parseColor("#F8FAFC") : Color.parseColor("#64748B"));
        tvUrl.setTextSize(11f);
        card.addView(tvUrl);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, 0);
        TextView btnCopy = createMiniBtn("COPY", "#A78BFA", "#1A0E2E");
        TextView btnToggle = createMiniBtn(isActive ? "OFF" : "ON", isActive ? "#F59E0B" : "#22C55E", isActive ? "#1F1A0A" : "#0A1F14");
        TextView btnDelete = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
        LinearLayout.LayoutParams rp1 = new LinearLayout.LayoutParams(0, dp(32), 1f);
        rp1.setMargins(0, 0, dp(4), 0);
        btnCopy.setLayoutParams(rp1);
        LinearLayout.LayoutParams rp2 = new LinearLayout.LayoutParams(0, dp(32), 1f);
        rp2.setMargins(0, 0, dp(4), 0);
        btnToggle.setLayoutParams(rp2);
        btnDelete.setLayoutParams(new LinearLayout.LayoutParams(0, dp(32), 1f));
        btnCopy.setOnClickListener(x -> { ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); if (cb != null) { cb.setPrimaryClip(ClipData.newPlainText("Link", url)); Toast.makeText(this, "URL copied", Toast.LENGTH_SHORT).show(); } });
        btnToggle.setOnClickListener(x -> SupabaseHelper.toggleLink(id, !isActive, (s, m) -> runOnUiThread(() -> { if (s) loadLinks(); })));
        btnDelete.setOnClickListener(x -> new AlertDialog.Builder(this).setTitle("Hapus Link").setMessage("Hapus link ini?").setPositiveButton("Hapus", (d, w) -> SupabaseHelper.deleteLink(id, (s, m) -> runOnUiThread(() -> { if (s) loadLinks(); }))).setNegativeButton("Batal", null).show());
        row.addView(btnCopy);
        row.addView(btnToggle);
        row.addView(btnDelete);
        card.addView(row);
        return card;
    }

    private void showAddLinkDialog() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(20));
        EditText etUrl = new EditText(this);
        etUrl.setHint("https://move2link.co/xxxxx");
        etUrl.setTextColor(Color.parseColor("#F8FAFC"));
        etUrl.setHintTextColor(Color.parseColor("#64748B"));
        etUrl.setTextSize(12f);
        etUrl.setBackgroundResource(R.drawable.edit_bg);
        etUrl.setPadding(dp(14), dp(14), dp(14), dp(14));
        root.addView(etUrl);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(root).setPositiveButton("TAMBAH", null).setNegativeButton("BATAL", null).create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty() || !url.startsWith("http")) { Toast.makeText(this, "URL tidak valid", Toast.LENGTH_SHORT).show(); return; }
            SupabaseHelper.addLink(url, (success, msg) -> runOnUiThread(() -> { Toast.makeText(this, success ? "Link ditambahkan" : msg, Toast.LENGTH_SHORT).show(); if (success) { dialog.dismiss(); loadLinks(); } }));
        });
    }

    private void showBulkImportDialog() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(20));
        EditText etUrls = new EditText(this);
        etUrls.setHint("https://move2link.co/xxx1\nhttps://move2link.co/xxx2");
        etUrls.setTextColor(Color.parseColor("#F8FAFC"));
        etUrls.setHintTextColor(Color.parseColor("#64748B"));
        etUrls.setTextSize(11f);
        etUrls.setBackgroundResource(R.drawable.edit_bg);
        etUrls.setPadding(dp(14), dp(14), dp(14), dp(14));
        etUrls.setMinLines(5);
        etUrls.setGravity(Gravity.TOP);
        etUrls.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        root.addView(etUrls);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(root).setPositiveButton("IMPORT", null).setNegativeButton("BATAL", null).create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String text = etUrls.getText().toString().trim();
            if (text.isEmpty()) { Toast.makeText(this, "Kosong", Toast.LENGTH_SHORT).show(); return; }
            String[] lines = text.split("\n");
            Set<String> unique = new HashSet<>();
            JSONArray urls = new JSONArray();
            for (String line : lines) {
                String clean = line.trim();
                if (clean.startsWith("http") && !unique.contains(clean)) { unique.add(clean); urls.put(clean); }
            }
            if (urls.length() == 0) { Toast.makeText(this, "Tidak ada URL valid", Toast.LENGTH_SHORT).show(); return; }
            SupabaseHelper.bulkAddLinks(urls, (success, msg) -> runOnUiThread(() -> { Toast.makeText(this, success ? urls.length() + " link ditambahkan" : msg, Toast.LENGTH_SHORT).show(); if (success) { dialog.dismiss(); loadLinks(); } }));
        });
    }

    private void confirmRefreshLinks() {
        new AlertDialog.Builder(this).setTitle("Refresh Links").setMessage("Generate 100 link baru?\nLink lama akan dihapus permanen.").setPositiveButton("REFRESH", (d, w) -> {
            Toast.makeText(this, "Generating 100 links...", Toast.LENGTH_LONG).show();
            SupabaseHelper.refreshLinks((success, message) -> runOnUiThread(() -> {
                if (success) { Toast.makeText(this, "Refresh berhasil!", Toast.LENGTH_LONG).show(); SupabaseHelper.addLog("Refreshed 100 links"); loadLinks(); }
                else { Toast.makeText(this, "Gagal: " + message, Toast.LENGTH_LONG).show(); }
            }));
        }).setNegativeButton("Batal", null).show();
    }

    private void bindMonitor(View v) {
        // data loaded on loadMonitor
    }

    private void loadMonitor() {
        View v = contentFrame.getChildAt(0);
        if (v == null) return;
        TextView tvLiveCount = v.findViewById(R.id.tvLiveCount);
        TextView tvServerUptime = v.findViewById(R.id.tvServerUptime);
        LinearLayout rateContainer = v.findViewById(R.id.rateContainer);
        LinearLayout usedContainer = v.findViewById(R.id.usedContainer);

        SupabaseHelper.fetchOnlineCount((success, data, message) -> runOnUiThread(() -> {
            if (success && data != null) {
                long now = System.currentTimeMillis();
                int active = 0;
                for (int i = 0; i < data.length(); i++) {
                    JSONObject o = data.optJSONObject(i);
                    if (o == null) continue;
                    long ts = o.optLong("last_seen", 0);
                    if (ts > 0 && (now - ts) <= 120000) active++;
                }
                if (tvLiveCount != null) tvLiveCount.setText("Online: " + active + " user");
            } else if (tvLiveCount != null) tvLiveCount.setText("Online: 0");
        }));
        SupabaseHelper.fetchServerInfo((success, data, message) -> runOnUiThread(() -> {
            if (success && data != null && tvServerUptime != null) tvServerUptime.setText("Total: " + data.optLong("total_keys", 0) + " · Active: " + data.optLong("active_keys", 0) + " · Expired: " + data.optLong("expired_keys", 0));
        }));
        SupabaseHelper.fetchRateLimit((success, data, message) -> runOnUiThread(() -> {
            if (rateContainer == null) return;
            rateContainer.removeAllViews();
            if (!success || data == null || data.length() == 0) { rateContainer.addView(emptyText("Tidak ada rate limit")); return; }
            for (int i = 0; i < Math.min(20, data.length()); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o != null) rateContainer.addView(createSimpleRow(o.optString("fp", ""), "getkey count: " + o.optInt("count", 0)));
            }
        }));
        SupabaseHelper.fetchUsedLinks((success, data, message) -> runOnUiThread(() -> {
            if (usedContainer == null) return;
            usedContainer.removeAllViews();
            if (!success || data == null || data.length() == 0) { usedContainer.addView(emptyText("Tidak ada used links")); return; }
            for (int i = 0; i < Math.min(30, data.length()); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o != null) usedContainer.addView(createSimpleRow(o.optString("fp", ""), "link_id: " + o.optInt("link_id", 0)));
            }
        }));
    }

    private View createSimpleRow(String left, String right) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(12), dp(8), dp(12), dp(8));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setCornerRadius(dp(8));
        row.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, dp(6));
        row.setLayoutParams(p);
        TextView tvLeft = new TextView(this);
        tvLeft.setText(left);
        tvLeft.setTextColor(Color.parseColor("#F8FAFC"));
        tvLeft.setTextSize(10f);
        tvLeft.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvLeft);
        TextView tvRight = new TextView(this);
        tvRight.setText(right);
        tvRight.setTextColor(Color.parseColor("#A78BFA"));
        tvRight.setTextSize(10f);
        tvRight.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(tvRight);
        return row;
    }
    
    private void bindWebSettings(View v) {
    EditText etTitle = v.findViewById(R.id.wsSiteTitle);
    EditText etAbout = v.findViewById(R.id.wsAbout);
    EditText etFooter = v.findViewById(R.id.wsFooter);
    EditText etWa = v.findViewById(R.id.wsWa);
    EditText etTg = v.findViewById(R.id.wsTg);
    EditText etWaReq = v.findViewById(R.id.wsWaReq);
    TextView btnSave = v.findViewById(R.id.btnSaveWs);
    
    SupabaseHelper.fetchWebSettings((success, data, message) -> runOnUiThread(() -> {
        if (success && data != null) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                String k = o.optString("key"), val = o.optString("value");
                if (k.equals("site_title")) etTitle.setText(val);
                if (k.equals("about_text")) etAbout.setText(val);
                if (k.equals("footer_text")) etFooter.setText(val);
                if (k.equals("wa_channel")) etWa.setText(val);
                if (k.equals("tg_channel")) etTg.setText(val);
                if (k.equals("wa_request")) etWaReq.setText(val);
            }
        }
    }));

    btnSave.setOnClickListener(x -> {
        JSONArray updates = new JSONArray();
        try {
            updates.put(new JSONObject().put("key", "site_title").put("value", etTitle.getText().toString()));
            updates.put(new JSONObject().put("key", "about_text").put("value", etAbout.getText().toString()));
            updates.put(new JSONObject().put("key", "footer_text").put("value", etFooter.getText().toString()));
            updates.put(new JSONObject().put("key", "wa_channel").put("value", etWa.getText().toString()));
            updates.put(new JSONObject().put("key", "tg_channel").put("value", etTg.getText().toString()));
            updates.put(new JSONObject().put("key", "wa_request").put("value", etWaReq.getText().toString()));
        } catch (Exception ignored) {}
        SupabaseHelper.saveWebSettings(updates, (success, msg) -> runOnUiThread(() -> {
            Toast.makeText(this, success ? "Settings tersimpan!" : msg, Toast.LENGTH_SHORT).show();
        }));
    });
}

    private void bindSecurity(View v) {
        EditText etBlockFp = v.findViewById(R.id.etBlockFp);
        TextView btnBlockFp = v.findViewById(R.id.btnBlockFp);
        TextView btnCleanup = v.findViewById(R.id.btnCleanup);
        btnBlockFp.setOnClickListener(x -> {
            String fp = etBlockFp.getText().toString().trim();
            if (fp.isEmpty()) { Toast.makeText(this, "Masukkan FP", Toast.LENGTH_SHORT).show(); return; }
            SupabaseHelper.blockFp(fp, "manual", (s, m) -> runOnUiThread(() -> { Toast.makeText(this, s ? "Blocked" : m, Toast.LENGTH_SHORT).show(); if (s) { etBlockFp.setText(""); loadSecurity(); } }));
        });
        btnCleanup.setOnClickListener(x -> new AlertDialog.Builder(this).setTitle("Cleanup Expired").setMessage("Hapus semua key expired?").setPositiveButton("HAPUS", (d, w) -> SupabaseHelper.cleanupExpired((s, m) -> runOnUiThread(() -> { Toast.makeText(this, s ? "Cleanup selesai" : m, Toast.LENGTH_SHORT).show(); if (s) { loadKeys(); loadStats(); } }))).setNegativeButton("Batal", null).show());
    }

    private void loadSecurity() {
        View v = contentFrame.getChildAt(0);
        if (v == null) return;
        LinearLayout blockedContainer = v.findViewById(R.id.blockedContainer);
        LinearLayout healthContainer = v.findViewById(R.id.healthContainer);
        SupabaseHelper.fetchBlocked((success, data, message) -> runOnUiThread(() -> {
            if (blockedContainer == null) return;
            blockedContainer.removeAllViews();
            if (!success || data == null || data.length() == 0) { blockedContainer.addView(emptyText("Tidak ada FP blocked")); return; }
            cachedBlocked = data;
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                final String fp = o.optString("fp", ""), ip = o.optString("ip", "");
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(dp(10), dp(8), dp(10), dp(8));
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor("#1F0A0A"));
                bg.setCornerRadius(dp(8));
                row.setBackground(bg);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                p.setMargins(0, 0, 0, dp(6));
                row.setLayoutParams(p);
                TextView tvFp = new TextView(this);
                tvFp.setText(fp + (!ip.isEmpty() ? "\nIP: " + ip : ""));
                tvFp.setTextColor(Color.parseColor("#EF4444"));
                tvFp.setTextSize(10f);
                tvFp.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(tvFp);
                TextView btnUnblock = createMiniBtn("UNBLOCK", "#22C55E", "#0A1F14");
                btnUnblock.setLayoutParams(new LinearLayout.LayoutParams(dp(80), dp(30)));
                btnUnblock.setTextSize(9f);
                btnUnblock.setOnClickListener(x -> SupabaseHelper.unblockFp(fp, (s, msg) -> runOnUiThread(() -> { if (s) loadSecurity(); })));
                row.addView(btnUnblock);
                blockedContainer.addView(row);
            }
        }));
        SupabaseHelper.fetchHealth((success, data, message) -> runOnUiThread(() -> {
            if (healthContainer == null) return;
            healthContainer.removeAllViews();
            if (!success || data == null) { healthContainer.addView(emptyText("Tidak bisa cek health")); return; }
            java.util.Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String fn = keys.next();
                int status = data.optInt(fn, 0);
                boolean ok = (status >= 200 && status < 500) || status == 401 || status == 400;
                healthContainer.addView(createSimpleRow((ok ? "OK " : "ERR ") + fn, "HTTP " + status));
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                android.net.Uri uri = data.getData();
                android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
                String base64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP);
                String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
                if (activeScreenshotDisplay != null) { activeScreenshotDisplay.setText("Uploading..."); activeScreenshotDisplay.setTextColor(Color.parseColor("#F59E0B")); }
                Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
                SupabaseHelper.uploadImageToGitHub(base64, filename, (success, url, message) -> runOnUiThread(() -> {
                    if (success) {
                        if (activeScreenshotDisplay != null) { activeScreenshotDisplay.setText(url); activeScreenshotDisplay.setTag(url); activeScreenshotDisplay.setTextColor(Color.parseColor("#A855F7")); }
                        Toast.makeText(MainActivity.this, "Gambar terupload!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (activeScreenshotDisplay != null) { activeScreenshotDisplay.setText("Gagal: " + message); activeScreenshotDisplay.setTextColor(Color.parseColor("#EF4444")); }
                    }
                }));
            } catch (Exception e) { Toast.makeText(this, "Gagal pilih gambar", Toast.LENGTH_LONG).show(); }
        }
    }

    private void startAutoRefresh() {
        autoRefreshRunnable = new Runnable() {
            @Override public void run() {
                try { if (currentMenu == 0) { loadStats(); View v = contentFrame.getChildAt(0); if (v != null) loadDashboard(v); } } catch (Exception ignored) {}
                autoRefreshHandler.postDelayed(this, 60000);
            }
        };
        autoRefreshHandler.postDelayed(autoRefreshRunnable, 60000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoRefreshRunnable != null) autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }
}