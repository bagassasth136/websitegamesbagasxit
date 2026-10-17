package com.apkadmin.bagasxit;

import androidx.appcompat.app.AppCompatActivity;

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
import android.view.View;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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

    private TextView btnTabDash, btnTabGen, btnTabMulti, btnTabKeys, btnTabAdmin, btnTabLogs, btnTabBcast, btnTabSuspect, btnTabLinks, btnTabMonitor, btnTabSecurity, btnTabStore, btnTabApk, btnTabComments, btnTabFilter, btnTabKategori;
    private View viewDash, viewGen, viewMulti, viewKeys, viewAdmin, viewLogs, viewBcast, viewSuspect, viewLinks, viewMonitor, viewSecurity, viewStore, viewApk, viewComments, viewFilter, viewKategori;

    private TextView tvStatKeys, tvStatOnline;

    private EditText etKey, etDuration;
    private EditText etMultiKey, etMultiDuration, etCustomMax;
    private TextView tvMultiMaxDisplay;
    private LinearLayout multiContainer;
    private int selectedMaxDevices = 5;

    private EditText etSearch;
    private LinearLayout keysContainer;

    private EditText etAdminUser, etAdminPass;
    private LinearLayout adminContainer;

    private LinearLayout logsContainer;

    private EditText etBcastTitle, etBcastMessage;
    private LinearLayout bcastContainer;
    private EditText etMaintMessage;
    private TextView tvMaintStatus, btnMaintOn, btnMaintOff;
    private CheckBox cbBcastWeb, cbBcastMain, cbBcastApk;
    private CheckBox cbMaintWeb, cbMaintMain, cbMaintApk;
    private boolean maintCheckboxInitialized = false;

    private TextView filterAll, filterActive, filterExpired, filterBound;
    private TextView btnDeleteFiltered;
    private String currentFilter = "all";
    private String currentSearch = "";

    private TextView tvDashToday, tvDashWeek, tvDashActive, tvDashExpired, tvDashBound, tvDashExpiringSoon;
    private LinearLayout chartContainer;

    private EditText etSearchSuspect;
    private TextView btnSuspectAll, btnSuspectBlocked;
    private LinearLayout suspectContainer;
    private String currentSuspectFilter = "all";

    private TextView tvLinksTitle, btnAddLink, btnBulkImport, btnRefreshLinks;
    private LinearLayout linksContainer;

    private TextView tvLiveCount, tvServerUptime;
    private LinearLayout rateContainer, usedContainer;

    private EditText etBlockFp;
    private TextView btnBlockFp, btnCleanup;
    private LinearLayout blockedContainer, healthContainer;

    private JSONArray cachedKeys = new JSONArray();
    private JSONArray cachedSuspect = new JSONArray();
    private JSONArray cachedBlocked = new JSONArray();
    private int renderOffset = 0;
    private static final int RENDER_STEP = 100;

    private Handler autoRefreshHandler = new Handler(Looper.getMainLooper());
    private Runnable autoRefreshRunnable;
    private int currentTab = 0;

    private boolean tabLoadedDash = false;
    private boolean tabLoadedGen = false;
    private boolean tabLoadedMulti = false;
    private boolean tabLoadedKeys = false;
    private boolean tabLoadedAdmin = false;
    private boolean tabLoadedLogs = false;
    private boolean tabLoadedBcast = false;
    private boolean tabLoadedSuspect = false;
    private boolean tabLoadedLinks = false;
    private boolean tabLoadedMonitor = false;
    private boolean tabLoadedSecurity = false;
    private boolean tabLoadedStore = false;
    private boolean tabLoadedApk = false;
    private LinearLayout storeContainer, apkContainer;
    private JSONArray cachedStore = new JSONArray();
    private JSONArray cachedApk = new JSONArray();
    private boolean tabLoadedComments = false;
    private boolean tabLoadedFilter = false;
    private LinearLayout commentsContainer, badWordsContainer;
    private EditText etBadWord;
    private TextView btnAddBadWord;
    private JSONArray cachedComments = new JSONArray();
    private JSONArray cachedBadWords = new JSONArray();
    private boolean tabLoadedKategori = false;
    private LinearLayout categoriesContainer;
    private EditText etNewCategory;
    private TextView btnAddCategory;
    private JSONArray cachedCategories = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTabDash = findViewById(R.id.btnTabDash);
        btnTabGen = findViewById(R.id.btnTabGen);
        btnTabMulti = findViewById(R.id.btnTabMulti);
        btnTabKeys = findViewById(R.id.btnTabKeys);
        btnTabAdmin = findViewById(R.id.btnTabAdmin);
        btnTabLogs = findViewById(R.id.btnTabLogs);
        btnTabBcast = findViewById(R.id.btnTabBcast);
        btnTabSuspect = findViewById(R.id.btnTabSuspect);
        btnTabLinks = findViewById(R.id.btnTabLinks);
        btnTabMonitor = findViewById(R.id.btnTabMonitor);
        btnTabSecurity = findViewById(R.id.btnTabSecurity);
        btnTabStore = findViewById(R.id.btnTabStore);
        btnTabApk = findViewById(R.id.btnTabApk);
        btnTabComments = findViewById(R.id.btnTabComments);
        btnTabFilter = findViewById(R.id.btnTabFilter);
        btnTabKategori = findViewById(R.id.btnTabKategori);

        viewDash = findViewById(R.id.viewDash);
        viewGen = findViewById(R.id.viewGen);
        viewMulti = findViewById(R.id.viewMulti);
        viewKeys = findViewById(R.id.viewKeys);
        viewAdmin = findViewById(R.id.viewAdmin);
        viewLogs = findViewById(R.id.viewLogs);
        viewBcast = findViewById(R.id.viewBcast);
        viewSuspect = findViewById(R.id.viewSuspect);
        viewLinks = findViewById(R.id.viewLinks);
        viewMonitor = findViewById(R.id.viewMonitor);
        viewSecurity = findViewById(R.id.viewSecurity);
        viewStore = findViewById(R.id.viewStore);
        viewApk = findViewById(R.id.viewApk);
        storeContainer = findViewById(R.id.storeContainer);
        apkContainer = findViewById(R.id.apkContainer);
        viewComments = findViewById(R.id.viewComments);
        viewFilter = findViewById(R.id.viewFilter);
        commentsContainer = findViewById(R.id.commentsContainer);
        badWordsContainer = findViewById(R.id.badWordsContainer);
        etBadWord = findViewById(R.id.etBadWord);
        btnAddBadWord = findViewById(R.id.btnAddBadWord);
        viewKategori = findViewById(R.id.viewKategori);
        categoriesContainer = findViewById(R.id.categoriesContainer);
        etNewCategory = findViewById(R.id.etNewCategory);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        tvStatKeys = findViewById(R.id.tvStatKeys);
        tvStatOnline = findViewById(R.id.tvStatOnline);

        etKey = findViewById(R.id.etKey);
        etDuration = findViewById(R.id.etDuration);
        etMultiKey = findViewById(R.id.etMultiKey);
        etMultiDuration = findViewById(R.id.etMultiDuration);
        etCustomMax = findViewById(R.id.etCustomMax);
        tvMultiMaxDisplay = findViewById(R.id.tvMultiMaxDisplay);
        multiContainer = findViewById(R.id.multiContainer);

        etSearch = findViewById(R.id.etSearch);
        keysContainer = findViewById(R.id.keysContainer);

        etAdminUser = findViewById(R.id.etAdminUser);
        etAdminPass = findViewById(R.id.etAdminPass);
        adminContainer = findViewById(R.id.adminContainer);

        logsContainer = findViewById(R.id.logsContainer);

        etBcastTitle = findViewById(R.id.etBcastTitle);
        etBcastMessage = findViewById(R.id.etBcastMessage);
        bcastContainer = findViewById(R.id.bcastContainer);
        etMaintMessage = findViewById(R.id.etMaintMessage);
        tvMaintStatus = findViewById(R.id.tvMaintStatus);
        btnMaintOn = findViewById(R.id.btnMaintOn);
        btnMaintOff = findViewById(R.id.btnMaintOff);

        filterAll = findViewById(R.id.filterAll);
        filterActive = findViewById(R.id.filterActive);
        filterExpired = findViewById(R.id.filterExpired);
        filterBound = findViewById(R.id.filterBound);
        btnDeleteFiltered = findViewById(R.id.btnDeleteFiltered);

        tvDashToday = findViewById(R.id.tvDashToday);
        tvDashWeek = findViewById(R.id.tvDashWeek);
        tvDashActive = findViewById(R.id.tvDashActive);
        tvDashExpired = findViewById(R.id.tvDashExpired);
        tvDashBound = findViewById(R.id.tvDashBound);
        tvDashExpiringSoon = findViewById(R.id.tvDashExpiringSoon);
        chartContainer = findViewById(R.id.chartContainer);

        etSearchSuspect = findViewById(R.id.etSearchSuspect);
        btnSuspectAll = findViewById(R.id.btnSuspectAll);
        btnSuspectBlocked = findViewById(R.id.btnSuspectBlocked);
        suspectContainer = findViewById(R.id.suspectContainer);

        tvLinksTitle = findViewById(R.id.tvLinksTitle);
        btnAddLink = findViewById(R.id.btnAddLink);
        btnBulkImport = findViewById(R.id.btnBulkImport);
        btnRefreshLinks = findViewById(R.id.btnRefreshLinks);
        linksContainer = findViewById(R.id.linksContainer);

        tvLiveCount = findViewById(R.id.tvLiveCount);
        tvServerUptime = findViewById(R.id.tvServerUptime);
        rateContainer = findViewById(R.id.rateContainer);
        usedContainer = findViewById(R.id.usedContainer);

        etBlockFp = findViewById(R.id.etBlockFp);
        btnBlockFp = findViewById(R.id.btnBlockFp);
        btnCleanup = findViewById(R.id.btnCleanup);
        blockedContainer = findViewById(R.id.blockedContainer);
        healthContainer = findViewById(R.id.healthContainer);

        cbBcastWeb = findViewById(R.id.cbBcastWeb);
        cbBcastMain = findViewById(R.id.cbBcastMain);
        cbBcastApk = findViewById(R.id.cbBcastApk);
        cbMaintWeb = findViewById(R.id.cbMaintWeb);
        cbMaintMain = findViewById(R.id.cbMaintMain);
        cbMaintApk = findViewById(R.id.cbMaintApk);

        btnTabDash.setOnClickListener(v -> switchTab(0));
        btnTabGen.setOnClickListener(v -> switchTab(1));
        btnTabMulti.setOnClickListener(v -> switchTab(2));
        btnTabKeys.setOnClickListener(v -> switchTab(3));
        btnTabAdmin.setOnClickListener(v -> switchTab(4));
        btnTabLogs.setOnClickListener(v -> switchTab(5));
        btnTabBcast.setOnClickListener(v -> switchTab(6));
        btnTabSuspect.setOnClickListener(v -> switchTab(7));
        btnTabLinks.setOnClickListener(v -> switchTab(8));
        btnTabMonitor.setOnClickListener(v -> switchTab(9));
        btnTabSecurity.setOnClickListener(v -> switchTab(10));
        btnTabStore.setOnClickListener(v -> switchTab(11));
        btnTabApk.setOnClickListener(v -> switchTab(12));
        btnTabComments.setOnClickListener(v -> switchTab(13));
        btnTabFilter.setOnClickListener(v -> switchTab(14));
        btnTabKategori.setOnClickListener(v -> switchTab(15));
        btnAddCategory.setOnClickListener(v -> addCategory());

        btnAddBadWord.setOnClickListener(v -> addBadWord());

        filterAll.setOnClickListener(v -> setFilter("all"));
        filterActive.setOnClickListener(v -> setFilter("active"));
        filterExpired.setOnClickListener(v -> setFilter("expired"));
        filterBound.setOnClickListener(v -> setFilter("bound"));

        btnSuspectAll.setOnClickListener(v -> setSuspectFilter("all"));
        btnSuspectBlocked.setOnClickListener(v -> setSuspectFilter("blocked"));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                currentSearch = s.toString().toLowerCase().trim();
                renderOffset = 0;
                renderKeys();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etSearchSuspect.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                renderSuspect();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.chip1D).setOnClickListener(v -> etDuration.setText("24"));
        findViewById(R.id.chip3D).setOnClickListener(v -> etDuration.setText("72"));
        findViewById(R.id.chip7D).setOnClickListener(v -> etDuration.setText("168"));
        findViewById(R.id.chip30D).setOnClickListener(v -> etDuration.setText("720"));

        findViewById(R.id.btnAutoKey).setOnClickListener(v -> {
            String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            etKey.setText("BX-" + code);
        });
        findViewById(R.id.btnCreateKey).setOnClickListener(v -> createKey());

        findViewById(R.id.mchip1D).setOnClickListener(v -> etMultiDuration.setText("24"));
        findViewById(R.id.mchip3D).setOnClickListener(v -> etMultiDuration.setText("72"));
        findViewById(R.id.mchip7D).setOnClickListener(v -> etMultiDuration.setText("168"));
        findViewById(R.id.mchip30D).setOnClickListener(v -> etMultiDuration.setText("720"));

        findViewById(R.id.btnMultiAutoKey).setOnClickListener(v -> {
            String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            etMultiKey.setText("BX-MULTI-" + code);
        });

        findViewById(R.id.dev5).setOnClickListener(v -> setMaxDevices(5));
        findViewById(R.id.dev50).setOnClickListener(v -> setMaxDevices(50));
        findViewById(R.id.dev100).setOnClickListener(v -> setMaxDevices(100));
        findViewById(R.id.dev1000).setOnClickListener(v -> setMaxDevices(1000));
        findViewById(R.id.devUnlimited).setOnClickListener(v -> setMaxDevices(0));

        etCustomMax.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                String txt = s.toString().trim();
                if (!txt.isEmpty()) {
                    try {
                        int val = Integer.parseInt(txt);
                        selectedMaxDevices = val;
                        tvMultiMaxDisplay.setText("Selected: " + (val == 0 ? "UNLIMITED" : val + " devices"));
                    } catch (Exception e) {}
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnCreateMultiKey).setOnClickListener(v -> createMultiKey());
        findViewById(R.id.btnAddAdmin).setOnClickListener(v -> addAdmin());
        findViewById(R.id.btnSendBcast).setOnClickListener(v -> sendBroadcast());
        btnMaintOn.setOnClickListener(v -> setMaintenance(true));
        btnMaintOff.setOnClickListener(v -> setMaintenance(false));
        btnDeleteFiltered.setOnClickListener(v -> deleteFiltered());

        btnAddLink.setOnClickListener(v -> showAddLinkDialog());
        btnBulkImport.setOnClickListener(v -> showBulkImportDialog());
        btnRefreshLinks.setOnClickListener(v -> confirmRefreshLinks());

        btnBlockFp.setOnClickListener(v -> blockFpManual());
        btnCleanup.setOnClickListener(v -> cleanupExpired());

        switchTab(0);
        updateDeleteButton();
        setMaxDevices(5);

        loadStats();
        loadDashboard();
        startAutoRefresh();
    }
    
    
    @Override
protected void onDestroy() {
    super.onDestroy();
    stopAutoRefresh();
}

private TextView activeScreenshotDisplay = null;

@Override
protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.getData() != null) {
        try {
            android.net.Uri uri = data.getData();
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] bytes = baos.toByteArray();
            String base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
            
            String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
            
            if (activeScreenshotDisplay != null) {
                activeScreenshotDisplay.setText("Uploading...");
                activeScreenshotDisplay.setTextColor(Color.parseColor("#F59E0B"));
            }
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

            SupabaseHelper.uploadImageToGitHub(base64, filename, new SupabaseHelper.OnUploadResult() {
                @Override
                public void onResult(boolean success, String url, String message) {
                    runOnUiThread(() -> {
                        if (success) {
                            if (activeScreenshotDisplay != null) {
                                activeScreenshotDisplay.setText(url);
                                activeScreenshotDisplay.setTag(url); // Simpen URL di tag
                                activeScreenshotDisplay.setTextColor(Color.parseColor("#A855F7"));
                            }
                            Toast.makeText(MainActivity.this, "✅ Gambar terupload!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (activeScreenshotDisplay != null) {
                                activeScreenshotDisplay.setText("Gagal: " + message);
                                activeScreenshotDisplay.setTextColor(Color.parseColor("#EF4444"));
                            }
                            Toast.makeText(MainActivity.this, "❌ " + message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Gagal pilih gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

private void startAutoRefresh() {
    autoRefreshRunnable = new Runnable() {
        @Override public void run() {
            try { refreshCurrentTab(); } catch (Exception ignored) {}
            autoRefreshHandler.postDelayed(this, 60000);
        }
    };
    autoRefreshHandler.postDelayed(autoRefreshRunnable, 60000);
}

private void stopAutoRefresh() {
    if (autoRefreshRunnable != null) autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
}

private void refreshCurrentTab() {
    switch (currentTab) {
        case 0: loadDashboard(); loadStats(); break;
        case 5: loadLogs(); break;
        case 6: loadBroadcasts(); loadMaintStatus(); break;
        case 7: loadSuspect(); break;
        case 8: loadLinks(); break;
        case 9: loadMonitor(); break;
        case 10: loadSecurity(); break;
        default: break;
    }
}

private void setMaxDevices(int count) {
    selectedMaxDevices = count;
    tvMultiMaxDisplay.setText("Selected: " + (count == 0 ? "UNLIMITED" : count + " devices"));
    etCustomMax.setText("");
}

private void switchTab(int index) {
    currentTab = index;
    viewDash.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
    viewGen.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
    viewMulti.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
    viewKeys.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
    viewAdmin.setVisibility(index == 4 ? View.VISIBLE : View.GONE);
    viewLogs.setVisibility(index == 5 ? View.VISIBLE : View.GONE);
    viewBcast.setVisibility(index == 6 ? View.VISIBLE : View.GONE);
    viewSuspect.setVisibility(index == 7 ? View.VISIBLE : View.GONE);
    viewLinks.setVisibility(index == 8 ? View.VISIBLE : View.GONE);
    viewMonitor.setVisibility(index == 9 ? View.VISIBLE : View.GONE);
    viewSecurity.setVisibility(index == 10 ? View.VISIBLE : View.GONE);
    viewStore.setVisibility(index == 11 ? View.VISIBLE : View.GONE);
    viewApk.setVisibility(index == 12 ? View.VISIBLE : View.GONE);
    viewComments.setVisibility(index == 13 ? View.VISIBLE : View.GONE);
    viewFilter.setVisibility(index == 14 ? View.VISIBLE : View.GONE);
    viewKategori.setVisibility(index == 15 ? View.VISIBLE : View.GONE);

    TextView[] tabs = {btnTabDash, btnTabGen, btnTabMulti, btnTabKeys, btnTabAdmin, btnTabLogs, btnTabBcast, btnTabSuspect, btnTabLinks, btnTabMonitor, btnTabSecurity, btnTabStore, btnTabApk, btnTabComments, btnTabFilter, btnTabKategori};
    for (int i = 0; i < tabs.length; i++) {
        if (i == index) {
            tabs[i].setBackgroundResource(R.drawable.tab_active);
            tabs[i].setTextColor(0xFF0A0A0F);
        } else {
            tabs[i].setBackgroundResource(R.drawable.tab_inactive);
            tabs[i].setTextColor(0xFFF8FAFC);
        }
    }

    if (index == 0 && !tabLoadedDash) { tabLoadedDash = true; loadDashboard(); loadStats(); }
    else if (index == 0) { loadDashboard(); loadStats(); }
    if (index == 3) { renderOffset = 0; loadKeys(); tabLoadedKeys = true; }
    if (index == 4 && !tabLoadedAdmin) { tabLoadedAdmin = true; loadAdmins(); }
    if (index == 5 && !tabLoadedLogs) { tabLoadedLogs = true; loadLogs(); }
    if (index == 6 && !tabLoadedBcast) { tabLoadedBcast = true; loadBroadcasts(); loadMaintStatus(); }
    if (index == 7 && !tabLoadedSuspect) { tabLoadedSuspect = true; loadSuspect(); }
    if (index == 8 && !tabLoadedLinks) { tabLoadedLinks = true; loadLinks(); }
    if (index == 9 && !tabLoadedMonitor) { tabLoadedMonitor = true; loadMonitor(); }
    if (index == 10 && !tabLoadedSecurity) { tabLoadedSecurity = true; loadSecurity(); }
    if (index == 11 && !tabLoadedStore) { tabLoadedStore = true; loadStore(); }
    if (index == 12 && !tabLoadedApk) { tabLoadedApk = true; loadApk(); }
    if (index == 13 && !tabLoadedComments) { tabLoadedComments = true; loadComments(); }
    if (index == 14 && !tabLoadedFilter) { tabLoadedFilter = true; loadBadWords(); }
    if (index == 15 && !tabLoadedKategori) { tabLoadedKategori = true; loadCategories(); }
}

private void setFilter(String filter) {
    currentFilter = filter;
    renderOffset = 0;
    TextView[] filters = {filterAll, filterActive, filterExpired, filterBound};
    String[] names = {"all", "active", "expired", "bound"};
    for (int i = 0; i < filters.length; i++) {
        if (names[i].equals(filter)) filters[i].setBackgroundResource(R.drawable.tab_active);
        else filters[i].setBackgroundResource(R.drawable.chip_bg);
        filters[i].setTextColor(0xFF0A0A0F);
    }
    updateDeleteButton();
    renderKeys();
}

private void setSuspectFilter(String filter) {
    currentSuspectFilter = filter;
    if (filter.equals("all")) {
        btnSuspectAll.setBackgroundResource(R.drawable.tab_active);
        btnSuspectBlocked.setBackgroundResource(R.drawable.chip_bg);
    } else {
        btnSuspectAll.setBackgroundResource(R.drawable.chip_bg);
        btnSuspectBlocked.setBackgroundResource(R.drawable.tab_active);
    }
    btnSuspectAll.setTextColor(0xFF0A0A0F);
    btnSuspectBlocked.setTextColor(0xFF0A0A0F);
    renderSuspect();
}

private void updateDeleteButton() {
    String label;
    switch (currentFilter) {
        case "active": label = "⚠ DELETE ALL ACTIVE"; break;
        case "expired": label = "⚠ DELETE ALL EXPIRED"; break;
        case "bound": label = "⚠ DELETE ALL BOUND"; break;
        default: label = "⚠ DELETE ALL"; break;
    }
    btnDeleteFiltered.setText(label);
}

private void createKey() {
    String key = etKey.getText().toString().trim();
    String durStr = etDuration.getText().toString().trim();
    if (key.isEmpty() || durStr.isEmpty()) {
        Toast.makeText(this, "Enter key and duration!", Toast.LENGTH_SHORT).show();
        return;
    }
    long durHours;
    try { durHours = Long.parseLong(durStr); }
    catch (NumberFormatException e) {
        Toast.makeText(this, "Duration must be a number!", Toast.LENGTH_SHORT).show();
        return;
    }
    long durMs = durHours * 3600 * 1000;
    SupabaseHelper.createKey(key, durMs, "Admin-Panel", (success, message) -> {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            if (success) {
                SupabaseHelper.addLog("Created key: " + key);
                etKey.setText(""); etDuration.setText("");
                loadKeys();
                loadStats();
            }
        });
    });
}

private void createMultiKey() {
    String key = etMultiKey.getText().toString().trim();
    String durStr = etMultiDuration.getText().toString().trim();
    if (key.isEmpty() || durStr.isEmpty()) {
        Toast.makeText(this, "Enter key and duration!", Toast.LENGTH_SHORT).show();
        return;
    }
    long durHours;
    try { durHours = Long.parseLong(durStr); }
    catch (NumberFormatException e) {
        Toast.makeText(this, "Duration must be a number!", Toast.LENGTH_SHORT).show();
        return;
    }
    long durMs = durHours * 3600 * 1000;
    final int maxDev = selectedMaxDevices;
    SupabaseHelper.createMultiKey(key, durMs, maxDev, (success, message) -> {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            if (success) {
                SupabaseHelper.addLog("Created multi key: " + key + " (" + (maxDev == 0 ? "unlimited" : maxDev) + ")");
                etMultiKey.setText("");
                etMultiDuration.setText("");
                loadKeys();
                loadStats();
            }
        });
    });
}

private void addAdmin() {
    String user = etAdminUser.getText().toString().trim();
    String pass = etAdminPass.getText().toString().trim();
    if (user.isEmpty() || pass.isEmpty()) {
        Toast.makeText(this, "Enter username and password!", Toast.LENGTH_SHORT).show();
        return;
    }
    SupabaseHelper.addAdmin(user, pass, (success, message) -> {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            if (success) {
                SupabaseHelper.addLog("Added admin: " + user);
                etAdminUser.setText(""); etAdminPass.setText("");
                loadAdmins();
            }
        });
    });
}

private void setMaintenance(boolean active) {
    String message = etMaintMessage.getText().toString().trim();
    if (message.isEmpty()) message = active ? "Server maintenance, coba lagi nanti." : "";

    boolean applyWeb = cbMaintWeb != null && cbMaintWeb.isChecked();
    boolean applyMain = cbMaintMain != null && cbMaintMain.isChecked();
    boolean applyApk = cbMaintApk != null && cbMaintApk.isChecked();

    if (active && !applyWeb && !applyMain && !applyApk) {
        Toast.makeText(this, "Centang minimal 1 target!", Toast.LENGTH_SHORT).show();
        return;
    }

    final String finalMsg = message;
    SupabaseHelper.setMaintenance(active, finalMsg, applyWeb, applyMain, applyApk, (success, msg) -> {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            if (success) {
                SupabaseHelper.addLog(active ? "Maintenance ON" : "Maintenance OFF");
                loadMaintStatus();
            }
        });
    });
}

private void sendBroadcast() {
    String title = etBcastTitle.getText().toString().trim();
    String message = etBcastMessage.getText().toString().trim();
    if (title.isEmpty() || message.isEmpty()) {
        Toast.makeText(this, "Enter title and message!", Toast.LENGTH_SHORT).show();
        return;
    }

    boolean showWeb = cbBcastWeb != null && cbBcastWeb.isChecked();
    boolean showMain = cbBcastMain != null && cbBcastMain.isChecked();
    boolean showApk = cbBcastApk != null && cbBcastApk.isChecked();

    if (!showWeb && !showMain && !showApk) {
        Toast.makeText(this, "Centang minimal 1 target!", Toast.LENGTH_SHORT).show();
        return;
    }

    SupabaseHelper.sendBroadcast(title, message, showWeb, showMain, showApk, (success, msg) -> {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            if (success) {
                SupabaseHelper.addLog("Broadcast: " + title);
                etBcastTitle.setText(""); etBcastMessage.setText("");
                loadBroadcasts();
            }
        });
    });
}

private int dp(int value) {
    return (int) (value * getResources().getDisplayMetrics().density);
}

private void loadStats() {
    SupabaseHelper.fetchKeys((success, data, message) -> {
        runOnUiThread(() -> {
            if (success && data != null) {
                tvStatKeys.setText("🔑 Keys: " + data.length());
            }
        });
    });
    SupabaseHelper.fetchOnlineCount((success, data, message) -> {
        runOnUiThread(() -> {
            if (success && data != null) {
                long now = System.currentTimeMillis();
                int active = 0;
                for (int i = 0; i < data.length(); i++) {
                    JSONObject o = data.optJSONObject(i);
                    if (o == null) continue;
                    long ts = o.optLong("last_seen", 0);
                    if (ts > 0 && (now - ts) <= 120000) active++;
                }
                tvStatOnline.setText("🟢 Online: " + active);
            } else {
                tvStatOnline.setText("🟢 Online: 0");
            }
        });
    });
}

private void loadDashboard() {
    SupabaseHelper.fetchKeys((success, data, message) -> {
        runOnUiThread(() -> {
            if (!success || data == null) return;
            long now = System.currentTimeMillis();
            long oneDayAgo = now - 24 * 3600 * 1000L;
            long sevenDaysAgo = now - 7 * 24 * 3600 * 1000L;
            long oneDayAhead = now + 24 * 3600 * 1000L;
            int today = 0, week = 0, active = 0, expired = 0, bound = 0, expiringSoon = 0;
            long[] dayCounts = new long[7];

            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                long createdAt = parseIso(o.optString("created_at", ""));
                long expiresAt = parseIso(o.optString("expires_at", ""));
                String hwid = o.isNull("hwid") ? "" : o.optString("hwid", "");
                boolean isActive = o.optBoolean("is_active", false);
                boolean isBound = hwid != null && !hwid.isEmpty();
                boolean isExpired = expiresAt > 0 && now > expiresAt;

                if (createdAt > 0) {
                    if (createdAt >= oneDayAgo) today++;
                    if (createdAt >= sevenDaysAgo) week++;
                    long daysAgo = (now - createdAt) / (24 * 3600 * 1000L);
                    if (daysAgo >= 0 && daysAgo < 7) {
                        int idx = 6 - (int) daysAgo;
                        if (idx >= 0 && idx < 7) dayCounts[idx]++;
                    }
                }
                if (isActive && !isExpired) active++;
                if (isExpired) expired++;
                if (isBound) bound++;
                if (expiresAt > 0 && expiresAt > now && expiresAt <= oneDayAhead) expiringSoon++;
            }

            tvDashToday.setText("📅 Keys created today: " + today);
            tvDashWeek.setText("📈 Keys created (7 days): " + week);
            tvDashActive.setText("✅ Active keys: " + active);
            tvDashExpired.setText("❌ Expired keys: " + expired);
            tvDashBound.setText("🟡 Bound keys: " + bound);
            tvDashExpiringSoon.setText("⏳ Expiring in 24h: " + expiringSoon);
            renderChart(dayCounts);
        });
    });
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
    } catch (Exception e) {
        return 0;
    }
}

private void renderChart(long[] counts) {
    chartContainer.removeAllViews();
    long max = 1;
    for (long c : counts) if (c > max) max = c;
    String[] labels = new String[7];
    SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
    for (int i = 6; i >= 0; i--) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -i);
        labels[6 - i] = sdf.format(c.getTime());
    }
    for (int i = 0; i < 7; i++) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        TextView tvVal = new TextView(this);
        tvVal.setText(String.valueOf(counts[i]));
        tvVal.setTextSize(10f);
        tvVal.setTextColor(Color.parseColor("#A78BFA"));
        tvVal.setGravity(Gravity.CENTER);
        col.addView(tvVal);
        View bar = new View(this);
        int barHeight = (int) ((counts[i] * 120) / max);
        if (barHeight < 4) barHeight = 4;
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, barHeight);
        barParams.setMargins(8, 4, 8, 0);
        bar.setLayoutParams(barParams);
        GradientDrawable barBg = new GradientDrawable();
        barBg.setColor(Color.parseColor("#A78BFA"));
        barBg.setCornerRadius(dp(4));
        bar.setBackground(barBg);
        col.addView(bar);
        TextView tvLabel = new TextView(this);
        tvLabel.setText(labels[i]);
        tvLabel.setTextSize(9f);
        tvLabel.setTextColor(Color.parseColor("#64748B"));
        tvLabel.setGravity(Gravity.CENTER);
        tvLabel.setPadding(0, dp(4), 0, 0);
        col.addView(tvLabel);
        chartContainer.addView(col);
    }
}

private void loadKeys() {
    SupabaseHelper.fetchKeys((success, data, message) -> {
        runOnUiThread(() -> {
            if (!success || data == null) {
                Toast.makeText(MainActivity.this, "Failed to load keys", Toast.LENGTH_SHORT).show();
                return;
            }
            cachedKeys = data;
            renderOffset = 0;
            renderKeys();
            loadMultiKeys();
        });
    });
}

private void loadMultiKeys() {
    if (multiContainer == null) return;
    multiContainer.removeAllViews();
    if (cachedKeys.length() == 0) return;
    int count = 0;
    for (int i = 0; i < cachedKeys.length(); i++) {
        JSONObject o = cachedKeys.optJSONObject(i);
        if (o == null) continue;
        int maxDev = o.optInt("max_devices", 1);
        if (maxDev > 1 || maxDev == 0) {
            multiContainer.addView(createKeyCard(o,
                o.optString("key", ""),
                parseIso(o.optString("expires_at", "")),
                o.isNull("hwid") ? "" : o.optString("hwid", ""),
                false, maxDev));
            count++;
        }
    }
    if (count == 0) {
        TextView tv = new TextView(this);
        tv.setText("📂 No multi keys yet");
        tv.setTextColor(Color.parseColor("#64748B"));
        tv.setTextSize(11f);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dp(20), 0, dp(20));
        multiContainer.addView(tv);
    }
}


    private void renderKeys() {
        if (keysContainer == null) return;
        keysContainer.removeAllViews();
        if (cachedKeys.length() == 0) {
            keysContainer.addView(emptyText("📂 No keys yet"));
            return;
        }
        long now = System.currentTimeMillis();
        int matchCount = 0, rendered = 0;
        int startIdx = renderOffset;
        int endIdx = renderOffset + RENDER_STEP;

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
            boolean isMulti = (maxDevices > 1 || maxDevices == 0);

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

        if (matchCount == 0) {
            if (!currentSearch.isEmpty()) keysContainer.addView(emptyText("🔍 No keys match: " + currentSearch));
            else keysContainer.addView(emptyText("🔍 No keys match filter: " + currentFilter));
            return;
        }

        if (endIdx < matchCount) {
            int remaining = matchCount - endIdx;
            TextView btnMore = createMiniBtn("⬇ LOAD MORE (" + remaining + " left)", "#A78BFA", "#1A0E2E");
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(44));
            p.setMargins(0, dp(10), 0, dp(10));
            btnMore.setLayoutParams(p);
            btnMore.setOnClickListener(v -> {
                renderOffset += RENDER_STEP;
                renderKeys();
            });
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

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardParams);

        String title;
        if (isMulti) {
            String limit = (maxDev == 0) ? "∞" : String.valueOf(maxDev);
            title = "👥 " + key + " · " + connected + "/" + limit;
        } else {
            title = (bound ? "🟡 " : "🟢 ") + key + (bound ? " (bound)" : " (free)");
        }

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.parseColor("#F8FAFC"));
        tvTitle.setTextSize(12f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvTitle);

        if (expiresAt > 0) {
            TextView tvExp = new TextView(this);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String expStr = sdf.format(new Date(expiresAt));
            long remainMs = expiresAt - System.currentTimeMillis();
            String remainStr = remainMs > 0
                    ? (remainMs / 3600000) + "h " + ((remainMs % 3600000) / 60000) + "m left"
                    : "expired";
            tvExp.setText("⏰ " + expStr + " · " + remainStr);
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
        LinearLayout.LayoutParams r1p2 = new LinearLayout.LayoutParams(0, dp(36), 1f);
        btnCopyHwid.setLayoutParams(r1p2);

        btnCopyKey.setOnClickListener(v -> {
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) {
                cb.setPrimaryClip(ClipData.newPlainText("Key", key));
                Toast.makeText(MainActivity.this, "Key copied: " + key, Toast.LENGTH_SHORT).show();
            }
        });

        btnCopyHwid.setOnClickListener(v -> {
            if (isMulti) {
                showDeviceListDialog(key, hwidList, maxDev);
            } else {
                if (hwid == null || hwid.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No HWID bound", Toast.LENGTH_SHORT).show();
                    return;
                }
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
        LinearLayout.LayoutParams r2p2 = new LinearLayout.LayoutParams(0, dp(36), 1f);
        btnDelete.setLayoutParams(r2p2);

        btnReset.setOnClickListener(v -> {
            if (isMulti) {
                SupabaseHelper.resetAllDevices(key, (success, msg) -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        if (success) {
                            SupabaseHelper.addLog("Reset all devices: " + key);
                            loadKeys();
                        }
                    });
                });
            } else {
                SupabaseHelper.resetHwid(key, (success, msg) -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        if (success) {
                            SupabaseHelper.addLog("Reset HWID: " + key);
                            loadKeys();
                        }
                    });
                });
            }
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete Key")
                    .setMessage("Delete key " + key + "?")
                    .setPositiveButton("Delete", (d, w) -> {
                        SupabaseHelper.deleteKey(key, (success, msg) -> {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                if (success) {
                                    SupabaseHelper.addLog("Deleted key: " + key);
                                    loadKeys();
                                    loadStats();
                                }
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null).show();
        });

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
        String limit = (maxDev == 0) ? "∞" : String.valueOf(maxDev);
        tvTitle.setText("👥 " + hwidList.size() + "/" + limit + " DEVICES");
        tvTitle.setTextSize(14f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#A78BFA"));
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, 0, 0, dp(14));
        root.addView(tvTitle);

        if (hwidList.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No devices connected");
            tvEmpty.setTextSize(11f);
            tvEmpty.setTextColor(Color.parseColor("#64748B"));
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, dp(20), 0, dp(20));
            root.addView(tvEmpty);
        } else {
            for (final String hwid : hwidList) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(dp(10), dp(8), dp(10), dp(8));

                GradientDrawable rowBg = new GradientDrawable();
                rowBg.setColor(Color.parseColor("#0F0B21"));
                rowBg.setCornerRadius(dp(8));
                row.setBackground(rowBg);

                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.setMargins(0, 0, 0, dp(6));
                row.setLayoutParams(rowParams);

                TextView tvHwid = new TextView(this);
                String shortHwid = hwid.length() > 20 ? hwid.substring(0, 8) + "..." + hwid.substring(hwid.length() - 6) : hwid;
                tvHwid.setText("📱 " + shortHwid);
                tvHwid.setTextSize(10f);
                tvHwid.setTextColor(Color.parseColor("#F8FAFC"));
                tvHwid.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(tvHwid);

                TextView btnRemove = createMiniBtn("REMOVE", "#EF4444", "#1F0A0A");
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(75), dp(30));
                btnRemove.setLayoutParams(rp);
                btnRemove.setTextSize(9f);
                btnRemove.setOnClickListener(v -> {
                    SupabaseHelper.removeDeviceFromKey(key, hwid, (success, msg) -> {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            if (success) loadKeys();
                        });
                    });
                });
                row.addView(btnRemove);
                root.addView(row);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(root)
                .setPositiveButton("CLOSE", null)
                .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#14141C")));
        }
    }

    private void deleteFiltered() {
        String filterLabel = currentFilter.toUpperCase();
        new AlertDialog.Builder(this)
                .setTitle("Delete " + filterLabel + " keys")
                .setMessage("Delete all keys with filter: " + filterLabel + "?\n\nThis cannot be undone.")
                .setPositiveButton("DELETE", (d, w) -> {
                    long now = System.currentTimeMillis();
                    JSONArray toDelete = new JSONArray();
                    for (int i = 0; i < cachedKeys.length(); i++) {
                        JSONObject o = cachedKeys.optJSONObject(i);
                        if (o == null) continue;
                        String keyName = o.optString("key", "");
                        long expiresAt = parseIso(o.optString("expires_at", ""));
                        String hwid = o.isNull("hwid") ? "" : o.optString("hwid", "");
                        boolean isActive = o.optBoolean("is_active", false);
                        boolean bound = hwid != null && !hwid.isEmpty();
                        boolean expired = expiresAt > 0 && now > expiresAt;

                        boolean shouldDelete = false;
                        if (currentFilter.equals("all")) shouldDelete = true;
                        else if (currentFilter.equals("active")) shouldDelete = (isActive && !expired);
                        else if (currentFilter.equals("expired")) shouldDelete = expired;
                        else if (currentFilter.equals("bound")) shouldDelete = bound;

                        if (shouldDelete && !keyName.isEmpty()) toDelete.put(keyName);
                    }

                    if (toDelete.length() == 0) {
                        Toast.makeText(this, "No keys to delete", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(this, "⏳ Deleting " + toDelete.length() + " keys...", Toast.LENGTH_SHORT).show();
                    SupabaseHelper.deleteKeysBatch(toDelete, (success, msg) -> {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, success ? "✅ " + toDelete.length() + " keys deleted" : "❌ " + msg, Toast.LENGTH_LONG).show();
                            if (success) {
                                SupabaseHelper.addLog("Batch deleted " + toDelete.length() + " keys");
                                loadKeys();
                                loadStats();
                            }
                        });
                    });
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void loadAdmins() {
        SupabaseHelper.fetchAdmins((success, data, message) -> {
            runOnUiThread(() -> {
                if (adminContainer == null) return;
                adminContainer.removeAllViews();
                if (!success || data == null || data.length() == 0) {
                    adminContainer.addView(emptyText("📂 No admins yet"));
                    return;
                }
                for (int i = 0; i < data.length(); i++) {
                    JSONObject o = data.optJSONObject(i);
                    if (o == null) continue;
                    String id = o.optString("id", "");
                    String username = o.optString("username", "unknown");
                    boolean isMaster = o.optBoolean("is_master", false);
                    adminContainer.addView(createAdminCard(id, username, isMaster));
                }
            });
        });
    }

    private View createAdminCard(String id, String username, boolean isMaster) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setStroke(dp(1), Color.parseColor("#262630"));
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardParams);
        TextView tvName = new TextView(this);
        tvName.setText(username + (isMaster ? " 👑" : ""));
        tvName.setTextColor(isMaster ? Color.parseColor("#A78BFA") : Color.parseColor("#F8FAFC"));
        tvName.setTextSize(12f);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(tvName);
        if (!isMaster) {
            TextView btnDelete = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(90), dp(36));
            btnDelete.setLayoutParams(p);
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Admin")
                        .setMessage("Delete admin " + username + "?")
                        .setPositiveButton("Delete", (d, w) -> {
                            SupabaseHelper.deleteAdmin(id, (success, message) -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                    if (success) {
                                        SupabaseHelper.addLog("Deleted admin: " + username);
                                        loadAdmins();
                                    }
                                });
                            });
                        })
                        .setNegativeButton("Cancel", null).show();
            });
            card.addView(btnDelete);
        }
        return card;
    }

    private void loadLogs() {
        SupabaseHelper.fetchLogs((success, data, message) -> {
            runOnUiThread(() -> {
                if (logsContainer == null) return;
                logsContainer.removeAllViews();
                if (!success || data == null || data.length() == 0) {
                    logsContainer.addView(emptyText("📂 No activity yet"));
                    return;
                }
                for (int i = 0; i < data.length(); i++) {
                    JSONObject o = data.optJSONObject(i);
                    if (o == null) continue;
                    String action = o.optString("action", "");
                    long timestamp = parseIso(o.optString("timestamp", ""));
                    if (action.isEmpty()) continue;
                    LinearLayout row = new LinearLayout(MainActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(dp(14), dp(10), dp(14), dp(10));
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor(Color.parseColor("#14141C"));
                    bg.setCornerRadius(dp(10));
                    row.setBackground(bg);
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    p.setMargins(0, 0, 0, dp(8));
                    row.setLayoutParams(p);
                    TextView tvLog = new TextView(MainActivity.this);
                    String timeStr = "";
                    if (timestamp > 0) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                        timeStr = "[" + sdf.format(new Date(timestamp)) + "] ";
                    }
                    tvLog.setText("• " + timeStr + action);
                    tvLog.setTextColor(Color.parseColor("#C4C0E2"));
                    tvLog.setTextSize(11f);
                    row.addView(tvLog);
                    logsContainer.addView(row);
                }
            });
        });
    }

    private void loadBroadcasts() {
        SupabaseHelper.fetchBroadcasts((success, data, message) -> {
            runOnUiThread(() -> {
                if (bcastContainer == null) return;
                bcastContainer.removeAllViews();
                if (!success || data == null || data.length() == 0) {
                    bcastContainer.addView(emptyText("📂 No broadcasts yet"));
                    return;
                }
                for (int i = 0; i < data.length(); i++) {
                    JSONObject o = data.optJSONObject(i);
                    if (o == null) continue;
                    String id = o.optString("id", "");
                    String title = o.optString("title", "");
                    String msg = o.optString("message", "");
                    long ts = parseIso(o.optString("timestamp", ""));
                    bcastContainer.addView(createBcastCard(id, title, msg, ts, o));
                }
            });
        });
    }

    private View createBcastCard(String id, String title, String message, long ts, JSONObject o) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setStroke(dp(1), Color.parseColor("#262630"));
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardParams);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("📢 " + title);
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

        boolean showWeb = o.optBoolean("show_web", false);
        boolean showMain = o.optBoolean("show_main", false);
        boolean showApk = o.optBoolean("show_apk", false);
        StringBuilder targets = new StringBuilder("Target: ");
        if (showWeb) targets.append("🌐 Web ");
        if (showMain) targets.append("🏠 Main ");
        if (showApk) targets.append("📱 APK ");

        TextView tvTarget = new TextView(this);
        tvTarget.setText(targets.toString().trim());
        tvTarget.setTextColor(Color.parseColor("#A78BFA"));
        tvTarget.setTextSize(9f);
        tvTarget.setPadding(0, dp(6), 0, 0);
        card.addView(tvTarget);

        if (ts > 0) {
            TextView tvTime = new TextView(this);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            tvTime.setText("🕒 " + sdf.format(new Date(ts)));
            tvTime.setTextColor(Color.parseColor("#94A3B8"));
            tvTime.setTextSize(9f);
            tvTime.setPadding(0, dp(6), 0, 0);
            card.addView(tvTime);
        }

        TextView btnDelete = createMiniBtn("DELETE BROADCAST", "#EF4444", "#1F0A0A");
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(36));
        p.setMargins(0, dp(10), 0, 0);
        btnDelete.setLayoutParams(p);
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete Broadcast")
                    .setMessage("Delete this broadcast?")
                    .setPositiveButton("Delete", (d, w) -> {
                        SupabaseHelper.deleteBroadcast(id, (success, msg) -> {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                if (success) {
                                    SupabaseHelper.addLog("Deleted broadcast: " + title);
                                    loadBroadcasts();
                                }
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null).show();
        });
        card.addView(btnDelete);
        return card;
    }

    private void loadMaintStatus() {
        SupabaseHelper.fetchMaintenance((success, data, message) -> {
            runOnUiThread(() -> {
                if (tvMaintStatus == null) return;
                if (success && data != null) {
                    boolean active = data.optBoolean("maintenance", false);
                    String msg = data.optString("message", "");
                    if (active) {
                        tvMaintStatus.setText("⚠️ Status: ON" + (msg != null && !msg.isEmpty() ? " · " + msg : ""));
                        tvMaintStatus.setTextColor(Color.parseColor("#EF4444"));
                    } else {
                        tvMaintStatus.setText("✅ Status: OFF");
                        tvMaintStatus.setTextColor(Color.parseColor("#22C55E"));
                    }
                    if (!maintCheckboxInitialized) {
                        maintCheckboxInitialized = true;
                        if (cbMaintWeb != null) cbMaintWeb.setChecked(data.optBoolean("apply_web", true));
                        if (cbMaintMain != null) cbMaintMain.setChecked(data.optBoolean("apply_main", true));
                        if (cbMaintApk != null) cbMaintApk.setChecked(data.optBoolean("apply_apk", true));
                    }
                } else {
                    tvMaintStatus.setText("✅ Status: OFF");
                    tvMaintStatus.setTextColor(Color.parseColor("#22C55E"));
                }
            });
        });
    }

    private void loadSuspect() {
        SupabaseHelper.fetchSuspect((success, data, message) -> {
            runOnUiThread(() -> {
                if (success && data != null) {
                    cachedSuspect = data;
                    renderSuspect();
                }
            });
        });
        SupabaseHelper.fetchBlocked((success, data, message) -> {
            runOnUiThread(() -> {
                if (success && data != null) cachedBlocked = data;
            });
        });
    }

    private void renderSuspect() {
        if (suspectContainer == null) return;
        suspectContainer.removeAllViews();
        if (cachedSuspect.length() == 0) {
            suspectContainer.addView(emptyText("📂 No suspect logs"));
            return;
        }
        String query = etSearchSuspect != null ? etSearchSuspect.getText().toString().toLowerCase().trim() : "";
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
            if (!query.isEmpty() && !fp.toLowerCase().contains(query)) continue;
            boolean isBlocked = blockedFps.contains(fp);
            if (currentSuspectFilter.equals("blocked") && !isBlocked) continue;
            if (count >= 200) break;
            suspectContainer.addView(createSuspectCard(o, isBlocked));
            count++;
        }
        if (count == 0) {
            suspectContainer.addView(emptyText("🔍 No matching suspect"));
        }
    }

    private View createSuspectCard(JSONObject o, boolean isBlocked) {
        String fp = o.optString("fp", "");
        String reason = o.optString("reason", "unknown");
        String ip = o.optString("ip", "");
        long ts = parseIso(o.optString("created_at", ""));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setStroke(dp(1), isBlocked ? Color.parseColor("#EF4444") : Color.parseColor("#262630"));
        bg.setCornerRadius(dp(10));
        card.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, dp(8));
        card.setLayoutParams(p);

        TextView tvFp = new TextView(this);
        String shortFp = fp.length() > 20 ? fp.substring(0, 12) + "..." : fp;
        tvFp.setText((isBlocked ? "🚫 " : "⚠️ ") + shortFp);
        tvFp.setTextColor(isBlocked ? Color.parseColor("#EF4444") : Color.parseColor("#F59E0B"));
        tvFp.setTextSize(11f);
        tvFp.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvFp);

        TextView tvInfo = new TextView(this);
        String timeStr = ts > 0 ? new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date(ts)) : "";
        tvInfo.setText("Reason: " + reason + (ip != null && !ip.isEmpty() ? " · IP: " + ip : "") + (timeStr.isEmpty() ? "" : " · " + timeStr));
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
        LinearLayout.LayoutParams rp2 = new LinearLayout.LayoutParams(0, dp(32), 1f);
        btnBlock.setLayoutParams(rp2);

        btnCopy.setOnClickListener(v -> {
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) {
                cb.setPrimaryClip(ClipData.newPlainText("FP", fp));
                Toast.makeText(MainActivity.this, "FP copied", Toast.LENGTH_SHORT).show();
            }
        });

        btnBlock.setOnClickListener(v -> {
            if (isBlocked) {
                SupabaseHelper.unblockFp(fp, (success, msg) -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, success ? "Unblocked" : msg, Toast.LENGTH_SHORT).show();
                        if (success) loadSuspect();
                    });
                });
            } else {
                SupabaseHelper.blockFp(fp, "manual", (success, msg) -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, success ? "Blocked" : msg, Toast.LENGTH_SHORT).show();
                        if (success) loadSuspect();
                    });
                });
            }
        });

        row.addView(btnCopy);
        row.addView(btnBlock);
        card.addView(row);

        return card;
    }

    private void loadLinks() {
        SupabaseHelper.fetchLinks((success, data, message) -> {
            runOnUiThread(() -> {
                if (linksContainer == null) return;
                linksContainer.removeAllViews();
                if (!success || data == null) {
                    linksContainer.addView(emptyText("📂 Failed to load links"));
                    return;
                }
                int activeCount = 0;
                for (int i = 0; i < data.length(); i++) {
                    JSONObject o = data.optJSONObject(i);
                    if (o != null && o.optBoolean("is_active", false)) activeCount++;
                }
                tvLinksTitle.setText("🔗 MOVE2LINK (" + activeCount + "/" + data.length() + " aktif)");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject o = data.optJSONObject(i);
                    if (o == null) continue;
                    linksContainer.addView(createLinkCard(o));
                }
            });
        });
    }

    private View createLinkCard(JSONObject o) {
        String id = o.optString("id", "");
        String url = o.optString("url", "");
        boolean isActive = o.optBoolean("is_active", false);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(10), dp(14), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setStroke(dp(1), isActive ? Color.parseColor("#22C55E") : Color.parseColor("#262630"));
        bg.setCornerRadius(dp(10));
        card.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, dp(8));
        card.setLayoutParams(p);

        TextView tvUrl = new TextView(this);
        tvUrl.setText((isActive ? "✅ " : "❌ ") + url);
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
        LinearLayout.LayoutParams rp3 = new LinearLayout.LayoutParams(0, dp(32), 1f);
        btnDelete.setLayoutParams(rp3);

        btnCopy.setOnClickListener(v -> {
            ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cb != null) {
                cb.setPrimaryClip(ClipData.newPlainText("Link", url));
                Toast.makeText(MainActivity.this, "URL copied", Toast.LENGTH_SHORT).show();
            }
        });

        btnToggle.setOnClickListener(v -> {
            SupabaseHelper.toggleLink(id, !isActive, (success, msg) -> {
                runOnUiThread(() -> {
                    if (success) loadLinks();
                    else Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                });
            });
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete Link")
                    .setMessage("Hapus link ini?\n" + url)
                    .setPositiveButton("Hapus", (d, w) -> {
                        SupabaseHelper.deleteLink(id, (success, msg) -> {
                            runOnUiThread(() -> {
                                if (success) loadLinks();
                                else Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            });
                        });
                    })
                    .setNegativeButton("Batal", null).show();
        });

        row.addView(btnCopy);
        row.addView(btnToggle);
        row.addView(btnDelete);
        card.addView(row);
        return card;
    }

    private void confirmRefreshLinks() {
        new AlertDialog.Builder(this)
                .setTitle("🔄 Refresh Links")
                .setMessage("Generate 100 link Move2Link baru?\n\n" +
                        "Link lama akan dihapus permanen.\n" +
                        "Pastikan server tidak dalam kondisi sibuk.\n\n" +
                        "Proses bisa memakan 30-60 detik.")
                .setPositiveButton("REFRESH", (d, w) -> refreshLinks())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void refreshLinks() {
        Toast.makeText(this, "⏳ Generating 100 links...", Toast.LENGTH_LONG).show();
        if (tvLinksTitle != null) {
            tvLinksTitle.setText("🔗 REFRESHING... (0/100)");
        }

        SupabaseHelper.refreshLinks((success, message) -> {
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(MainActivity.this, "✅ Refresh berhasil!", Toast.LENGTH_LONG).show();
                    SupabaseHelper.addLog("Refreshed 100 links");
                    loadLinks();
                } else {
                    Toast.makeText(MainActivity.this, "❌ Gagal: " + message, Toast.LENGTH_LONG).show();
                    if (tvLinksTitle != null) {
                        tvLinksTitle.setText("🔗 MOVE2LINK");
                    }
                }
            });
        });
    }

    private void showAddLinkDialog() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(20));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setCornerRadius(dp(16));
        root.setBackground(bg);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("+ TAMBAH LINK MOVE2LINK");
        tvTitle.setTextSize(13f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#A78BFA"));
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, 0, 0, dp(12));
        root.addView(tvTitle);

        EditText etUrl = new EditText(this);
        etUrl.setHint("https://move2link.co/xxxxx");
        etUrl.setTextColor(Color.parseColor("#F8FAFC"));
        etUrl.setHintTextColor(Color.parseColor("#64748B"));
        etUrl.setTextSize(12f);
        etUrl.setBackgroundResource(R.drawable.edit_bg);
        etUrl.setPadding(dp(14), dp(14), dp(14), dp(14));
        root.addView(etUrl);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(root)
                .setPositiveButton("TAMBAH", null)
                .setNegativeButton("BATAL", null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty() || !url.startsWith("http")) {
                Toast.makeText(this, "URL tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }
            SupabaseHelper.addLink(url, (success, msg) -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, success ? "Link ditambahkan" : msg, Toast.LENGTH_SHORT).show();
                    if (success) {
                        dialog.dismiss();
                        loadLinks();
                    }
                });
            });
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#14141C")));
        }
    }

    private void showBulkImportDialog() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(20));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setCornerRadius(dp(16));
        root.setBackground(bg);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("📦 BULK IMPORT");
        tvTitle.setTextSize(13f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#A78BFA"));
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, 0, 0, dp(6));
        root.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText("Paste 1 URL per baris");
        tvSub.setTextSize(10f);
        tvSub.setTextColor(Color.parseColor("#94A3B8"));
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setPadding(0, 0, 0, dp(12));
        root.addView(tvSub);

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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(root)
                .setPositiveButton("IMPORT", null)
                .setNegativeButton("BATAL", null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String text = etUrls.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] lines = text.split("\n");
            Set<String> unique = new HashSet<>();
            JSONArray urls = new JSONArray();
            for (String line : lines) {
                String clean = line.trim();
                if (clean.startsWith("http") && !unique.contains(clean)) {
                    unique.add(clean);
                    urls.put(clean);
                }
            }
            if (urls.length() == 0) {
                Toast.makeText(this, "Tidak ada URL valid", Toast.LENGTH_SHORT).show();
                return;
            }
            SupabaseHelper.bulkAddLinks(urls, (success, msg) -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, success ? urls.length() + " link ditambahkan" : msg, Toast.LENGTH_SHORT).show();
                    if (success) {
                        dialog.dismiss();
                        loadLinks();
                    }
                });
            });
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#14141C")));
        }
    }

    private void loadMonitor() {
        SupabaseHelper.fetchOnlineCount((success, data, message) -> {
            runOnUiThread(() -> {
                if (success && data != null) {
                    long now = System.currentTimeMillis();
                    int active = 0;
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject o = data.optJSONObject(i);
                        if (o == null) continue;
                        long ts = o.optLong("last_seen", 0);
                        if (ts > 0 && (now - ts) <= 120000) active++;
                    }
                    tvLiveCount.setText("Online: " + active + " user");
                } else {
                    tvLiveCount.setText("Online: 0");
                }
            });
        });

        SupabaseHelper.fetchServerInfo((success, data, message) -> {
            runOnUiThread(() -> {
                if (success && data != null) {
                    long total = data.optLong("total_keys", 0);
                    long active = data.optLong("active_keys", 0);
                    long expired = data.optLong("expired_keys", 0);
                    tvServerUptime.setText("Total keys: " + total + " · Active: " + active + " · Expired: " + expired);
                } else {
                    tvServerUptime.setText("Stats: --");
                }
            });
        });

        SupabaseHelper.fetchRateLimit((success, data, message) -> {
            runOnUiThread(() -> {
                if (rateContainer == null) return;
                rateContainer.removeAllViews();
                if (!success || data == null || data.length() == 0) {
                    rateContainer.addView(emptyText("📂 No rate limit data"));
                    return;
                }
                for (int i = 0; i < Math.min(20, data.length()); i++) {
                    JSONObject o = data.optJSONObject(i);
                    if (o == null) continue;
                    rateContainer.addView(createSimpleRow(o.optString("fp", ""), "getkey count: " + o.optInt("count", 0)));
                }
            });
        });

        SupabaseHelper.fetchUsedLinks((success, data, message) -> {
            runOnUiThread(() -> {
                if (usedContainer == null) return;
                usedContainer.removeAllViews();
                if (!success || data == null || data.length() == 0) {
                    usedContainer.addView(emptyText("📂 No used links"));
                    return;
                }
                for (int i = 0; i < Math.min(30, data.length()); i++) {
                    JSONObject o = data.optJSONObject(i);
                    if (o == null) continue;
                    usedContainer.addView(createSimpleRow(o.optString("fp", ""), "link_id: " + o.optInt("link_id", 0)));
                }
            });
        });
    }

    private void loadSecurity() {
        SupabaseHelper.fetchBlocked((success, data, message) -> {
            runOnUiThread(() -> {
                if (blockedContainer == null) return;
                blockedContainer.removeAllViews();
                if (!success || data == null || data.length() == 0) {
                    blockedContainer.addView(emptyText("📂 No blocked FP"));
                    return;
                }
                cachedBlocked = data;
                for (int i = 0; i < data.length(); i++) {
                    JSONObject o = data.optJSONObject(i);
                    if (o == null) continue;
                    final String fp = o.optString("fp", "");
                    final String ip = o.optString("ip", "");
                    String reason = o.optString("reason", "");
                    LinearLayout row = new LinearLayout(MainActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(dp(10), dp(8), dp(10), dp(8));
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor(Color.parseColor("#1F0A0A"));
                    bg.setCornerRadius(dp(8));
                    row.setBackground(bg);
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    p.setMargins(0, 0, 0, dp(6));
                    row.setLayoutParams(p);
                    TextView tvFp = new TextView(MainActivity.this);
                    String display = "🚫 " + fp + (ip != null && !ip.isEmpty() ? "\n   IP: " + ip : "");
                    tvFp.setText(display);
                    tvFp.setTextColor(Color.parseColor("#EF4444"));
                    tvFp.setTextSize(10f);
                    tvFp.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    row.addView(tvFp);
                    TextView btnUnblock = createMiniBtn("UNBLOCK", "#22C55E", "#0A1F14");
                    LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(dp(80), dp(30));
                    btnUnblock.setLayoutParams(up);
                    btnUnblock.setTextSize(9f);
                    btnUnblock.setOnClickListener(v -> {
                        SupabaseHelper.unblockFp(fp, (success2, msg) -> {
                            runOnUiThread(() -> {
                                if (success2) loadSecurity();
                            });
                        });
                    });
                    row.addView(btnUnblock);
                    blockedContainer.addView(row);
                }
            });
        });

        SupabaseHelper.fetchHealth((success, data, message) -> {
            runOnUiThread(() -> {
                if (healthContainer == null) return;
                healthContainer.removeAllViews();
                if (!success || data == null) {
                    healthContainer.addView(emptyText("📂 Cannot check health"));
                    return;
                }
                java.util.Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                    String fn = keys.next();
                    int status = data.optInt(fn, 0);
                    boolean ok = (status >= 200 && status < 500) || status == 401 || status == 400;
                    healthContainer.addView(createSimpleRow((ok ? "✅ " : "❌ ") + fn, "HTTP " + status));
                }
            });
        });
    }

    private void blockFpManual() {
        String fp = etBlockFp.getText().toString().trim();
        if (fp.isEmpty()) {
            Toast.makeText(this, "Enter FP", Toast.LENGTH_SHORT).show();
            return;
        }
        SupabaseHelper.blockFp(fp, "manual", (success, msg) -> {
            runOnUiThread(() -> {
                Toast.makeText(this, success ? "Blocked" : msg, Toast.LENGTH_SHORT).show();
                if (success) {
                    etBlockFp.setText("");
                    loadSecurity();
                }
            });
        });
    }

    private void cleanupExpired() {
        new AlertDialog.Builder(this)
                .setTitle("Cleanup Expired Keys")
                .setMessage("Hapus semua key yang sudah expired?\n\nTidak bisa dibatalkan.")
                .setPositiveButton("HAPUS", (d, w) -> {
                    SupabaseHelper.cleanupExpired((success, msg) -> {
                        runOnUiThread(() -> {
                            Toast.makeText(this, success ? "Cleanup done" : msg, Toast.LENGTH_SHORT).show();
                            if (success) {
                                loadKeys();
                                loadStats();
                            }
                        });
                    });
                })
                .setNegativeButton("Batal", null).show();
    }

    private View createSimpleRow(String left, String right) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(12), dp(8), dp(12), dp(8));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#14141C"));
        bg.setCornerRadius(dp(8));
        row.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
    
    // ============================================
// STORE TAB
// ============================================
private void loadStore() {
    SupabaseHelper.fetchStore((success, data, message) -> {
        runOnUiThread(() -> {
            if (storeContainer == null) return;
            storeContainer.removeAllViews();
            if (!success || data == null || data.length() == 0) {
                storeContainer.addView(emptyText("📂 Belum ada produk"));
                storeContainer.addView(createAddBtn("+ TAMBAH PRODUK STORE", v -> showStoreDialog(null)));
                return;
            }
            cachedStore = data;
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                storeContainer.addView(createStoreCard(o));
            }
            storeContainer.addView(createAddBtn("+ TAMBAH PRODUK STORE", v -> showStoreDialog(null)));
        });
    });
}

private View createStoreCard(JSONObject o) {
    String id = o.optString("id", "");
    String cat = o.optString("category", "");
    String name = o.optString("name", "");
    int price = o.optInt("price", 0);
    String priceText = o.optString("price_text", price > 0 ? "Rp " + price : "Nego");
    boolean isActive = o.optBoolean("is_active", true);
    String desc = o.optString("description", "");
    String emoji = o.optString("emoji", "📦");

    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(14), dp(12), dp(14), dp(12));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setStroke(dp(1), isActive ? Color.parseColor("#A78BFA") : Color.parseColor("#262630"));
    bg.setCornerRadius(dp(10));
    card.setBackground(bg);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(8));
    card.setLayoutParams(p);

    TextView tvTop = new TextView(this);
    tvTop.setText((isActive ? "✅ " : "❌ ") + emoji + " [" + cat + "]");
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
    tvPrice.setPadding(0, dp(2), 0, 0);
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
    LinearLayout.LayoutParams r3 = new LinearLayout.LayoutParams(0, dp(32), 1f);
    btnDel.setLayoutParams(r3);

    btnEdit.setOnClickListener(v -> showStoreDialog(o));
    btnToggle.setOnClickListener(v -> {
        SupabaseHelper.toggleStore(id, !isActive, (success, msg) -> runOnUiThread(() -> {
            if (success) loadStore();
            else Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }));
    });
    btnDel.setOnClickListener(v -> {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Produk")
                .setMessage("Hapus " + name + "?")
                .setPositiveButton("Hapus", (d, w) -> {
                    SupabaseHelper.deleteStore(id, (success, msg) -> runOnUiThread(() -> {
                        if (success) loadStore();
                        else Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }));
                })
                .setNegativeButton("Batal", null).show();
    });

    row.addView(btnEdit);
    row.addView(btnToggle);
    row.addView(btnDel);
    card.addView(row);
    return card;
}

private void showStoreDialog(final JSONObject existing) {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(20), dp(20), dp(20));

    TextView tvTitle = new TextView(this);
    tvTitle.setText(existing == null ? "+ PRODUK BARU" : "EDIT PRODUK");
    tvTitle.setTextSize(13f);
    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
    tvTitle.setTextColor(Color.parseColor("#A78BFA"));
    tvTitle.setGravity(Gravity.CENTER);
    tvTitle.setPadding(0, 0, 0, dp(12));
    root.addView(tvTitle);

    EditText etCat = makeInput(existing == null ? "" : existing.optString("category", ""), "Kategori (contoh: KEY)");
    EditText etName = makeInput(existing == null ? "" : existing.optString("name", ""), "Nama produk");
    EditText etPrice = makeInput(existing == null ? "" : String.valueOf(existing.optInt("price", 0)), "Harga (angka, 0 = Nego)");
    EditText etEmoji = makeInput(existing == null ? "📦" : existing.optString("emoji", "📦"), "Emoji (contoh: 🔑)");
    EditText etDesc = makeInput(existing == null ? "" : existing.optString("description", ""), "Deskripsi");
    etDesc.setMinLines(2);

    root.addView(etCat);
    root.addView(etName);
    root.addView(etPrice);
    root.addView(etEmoji);
    root.addView(etDesc);

    AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(root)
            .setPositiveButton("SIMPAN", null)
            .setNegativeButton("BATAL", null)
            .create();
    dialog.show();

    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        String cat = etCat.getText().toString().trim();
        String name = etName.getText().toString().trim();
        int price = 0;
        try { price = Integer.parseInt(etPrice.getText().toString().trim()); } catch (Exception ignored) {}
        String emoji = etEmoji.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        if (cat.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Kategori & nama wajib", Toast.LENGTH_SHORT).show();
            return;
        }
        final int fp = price;
        if (existing == null) {
            SupabaseHelper.addStore(cat, name, fp, "", desc, emoji, 0, (success, msg) -> runOnUiThread(() -> {
                Toast.makeText(this, success ? "✅ Ditambahkan" : "❌ " + msg, Toast.LENGTH_SHORT).show();
                if (success) { dialog.dismiss(); loadStore(); }
            }));
        } else {
            try {
                JSONObject patch = new JSONObject();
                patch.put("category", cat);
                patch.put("name", name);
                patch.put("price", fp);
                patch.put("price_text", fp > 0 ? "Rp " + fp : "Hubungi Admin");
                patch.put("description", desc);
                patch.put("emoji", emoji);
                String id = existing.optString("id", "");
                SupabaseHelper.editStore(id, patch, (success, msg) -> runOnUiThread(() -> {
                    Toast.makeText(this, success ? "✅ Diupdate" : "❌ " + msg, Toast.LENGTH_SHORT).show();
                    if (success) { dialog.dismiss(); loadStore(); }
                }));
            } catch (Exception e) {}
        }
    });

    if (dialog.getWindow() != null) {
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#14141C")));
    }
}

// ============================================
// APK UPDATES TAB
// ============================================
private void loadApk() {
    SupabaseHelper.fetchApkUpdates((success, data, message) -> {
        runOnUiThread(() -> {
            if (apkContainer == null) return;
            apkContainer.removeAllViews();
            
            // TOMBOL DI PALING ATAS
            apkContainer.addView(createAddBtn("+ TAMBAH APK UPDATE", v -> showApkDialog(null)));

            if (!success || data == null || data.length() == 0) {
                apkContainer.addView(emptyText("📂 Belum ada APK"));
                return;
            }
            cachedApk = data;
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                apkContainer.addView(createApkCard(o));
            }
            // (Hapus baris createAddBtn yang di bawah sini)
        });
    });
}

private View createApkCard(JSONObject o) {
    String id = o.optString("id", "");
    String cat = o.optString("category", "");
    String name = o.optString("name", "");
    String ver = o.optString("version", "");
    String size = o.optString("size", "");
    String link = o.optString("link", "");
    String getkey = o.optString("getkey_link", "");
    String changelog = o.optString("changelog", "");
    boolean isActive = o.optBoolean("is_active", true);

    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(14), dp(12), dp(14), dp(12));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setStroke(dp(1), isActive ? Color.parseColor("#06B6D4") : Color.parseColor("#262630"));
    bg.setCornerRadius(dp(10));
    card.setBackground(bg);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(8));
    card.setLayoutParams(p);

    TextView tvTop = new TextView(this);
    tvTop.setText((isActive ? "✅ " : "❌ ") + "[" + cat + "]");
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

    TextView tvMeta = new TextView(this);
    tvMeta.setText("📦 " + ver + " · " + size);
    tvMeta.setTextColor(Color.parseColor("#A855F7"));
    tvMeta.setTextSize(10f);
    tvMeta.setPadding(0, dp(2), 0, 0);
    card.addView(tvMeta);

    if (!changelog.isEmpty()) {
        TextView tvCh = new TextView(this);
        tvCh.setText(changelog);
        tvCh.setTextColor(Color.parseColor("#94A3B8"));
        tvCh.setTextSize(9f);
        tvCh.setPadding(0, dp(4), 0, 0);
        card.addView(tvCh);
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
    LinearLayout.LayoutParams r3 = new LinearLayout.LayoutParams(0, dp(32), 1f);
    btnDel.setLayoutParams(r3);

    btnEdit.setOnClickListener(v -> showApkDialog(o));
    btnToggle.setOnClickListener(v -> {
        SupabaseHelper.toggleApkUpdate(id, !isActive, (success, msg) -> runOnUiThread(() -> {
            if (success) loadApk();
            else Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }));
    });
    btnDel.setOnClickListener(v -> {
        new AlertDialog.Builder(this)
                .setTitle("Hapus APK")
                .setMessage("Hapus " + name + "?")
                .setPositiveButton("Hapus", (d, w) -> {
                    SupabaseHelper.deleteApkUpdate(id, (success, msg) -> runOnUiThread(() -> {
                        if (success) loadApk();
                        else Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }));
                })
                .setNegativeButton("Batal", null).show();
    });

    row.addView(btnEdit);
    row.addView(btnToggle);
    row.addView(btnDel);
    card.addView(row);
    return card;
}

private void showApkDialog(final JSONObject existing) {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(20), dp(20), dp(20));

    TextView tvTitle = new TextView(this);
    tvTitle.setText(existing == null ? "+ APK BARU" : "EDIT APK");
    tvTitle.setTextSize(13f);
    tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
    tvTitle.setTextColor(Color.parseColor("#06B6D4"));
    tvTitle.setGravity(Gravity.CENTER);
    tvTitle.setPadding(0, 0, 0, dp(12));
    root.addView(tvTitle);

    final String[] selectedCategory = { existing == null ? "" : existing.optString("category", "") };
LinearLayout catContainer = new LinearLayout(this);
catContainer.setOrientation(LinearLayout.VERTICAL);
catContainer.setPadding(0, 0, 0, dp(8));

TextView catLabel = new TextView(this);
catLabel.setText("Kategori:");
catLabel.setTextColor(Color.parseColor("#64748B"));
catLabel.setTextSize(11f);
catLabel.setPadding(0, 0, 0, dp(4));
catContainer.addView(catLabel);

LinearLayout catRow = new LinearLayout(this);
catRow.setOrientation(LinearLayout.HORIZONTAL);
catRow.setPadding(0, 0, 0, dp(4));

TextView catDisplay = new TextView(this);
catDisplay.setText(selectedCategory[0].isEmpty() ? "Pilih kategori..." : selectedCategory[0]);
catDisplay.setTextColor(Color.parseColor("#A855F7"));
catDisplay.setTextSize(12f);
catDisplay.setTypeface(null, android.graphics.Typeface.BOLD);
catDisplay.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
catRow.addView(catDisplay);

TextView btnPickCat = createMiniBtn("PILIH", "#A78BFA", "#1A0E2E");
btnPickCat.setLayoutParams(new LinearLayout.LayoutParams(dp(70), dp(32)));
btnPickCat.setOnClickListener(v -> {
    String[] cats = new String[cachedCategories.length()];
    for (int i = 0; i < cachedCategories.length(); i++) {
        cats[i] = cachedCategories.optJSONObject(i).optString("name", "");
    }
    new AlertDialog.Builder(this)
        .setTitle("Pilih Kategori")
        .setItems(cats, (d, which) -> {
            selectedCategory[0] = cats[which];
            catDisplay.setText(selectedCategory[0]);
        })
        .show();
});
catRow.addView(btnPickCat);
catContainer.addView(catRow);
root.addView(catContainer);

    EditText etName = makeInput(existing == null ? "" : existing.optString("name", ""), "Nama APK");
    EditText etVer = makeInput(existing == null ? "" : existing.optString("version", ""), "Versi (contoh: v1.0)");
    EditText etSize = makeInput(existing == null ? "" : existing.optString("size", ""), "Ukuran (contoh: 15 MB)");
    EditText etCh = makeInput(existing == null ? "" : existing.optString("changelog", ""), "Changelog (fitur baru)");
    EditText etLink = makeInput(existing == null ? "" : existing.optString("link", ""), "Link download");
    EditText etGetkey = makeInput(existing == null ? "" : existing.optString("getkey_link", ""), "Link getkey (opsional)");
    final String[] selectedScreenshot = { existing == null ? "" : existing.optString("screenshot", "") };
LinearLayout ssContainer = new LinearLayout(this);
ssContainer.setOrientation(LinearLayout.VERTICAL);
ssContainer.setPadding(0, 0, 0, dp(8));

TextView ssLabel = new TextView(this);
ssLabel.setText("Screenshot APK:");
ssLabel.setTextColor(Color.parseColor("#64748B"));
ssLabel.setTextSize(11f);
ssLabel.setPadding(0, 0, 0, dp(4));
ssContainer.addView(ssLabel);

TextView ssDisplay = new TextView(this);
ssDisplay.setText(selectedScreenshot[0].isEmpty() ? "Belum ada gambar" : selectedScreenshot[0]);
if (!selectedScreenshot[0].isEmpty()) {
    ssDisplay.setTag(selectedScreenshot[0]);
}
ssDisplay.setTextColor(Color.parseColor("#A855F7"));
ssDisplay.setTextSize(11f);
ssDisplay.setPadding(dp(10), dp(10), dp(10), dp(10));
ssDisplay.setBackgroundResource(R.drawable.edit_bg);
ssContainer.addView(ssDisplay);

LinearLayout ssRow = new LinearLayout(this);
ssRow.setOrientation(LinearLayout.HORIZONTAL);
ssRow.setPadding(0, dp(6), 0, 0);

TextView btnPickImg = createMiniBtn("PILIH GAMBAR", "#A78BFA", "#1A0E2E");
LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0, dp(36), 1f);
lp1.setMargins(0, 0, dp(4), 0);
btnPickImg.setLayoutParams(lp1);

TextView btnClearImg = createMiniBtn("HAPUS", "#EF4444", "#1F0A0A");
LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(dp(80), dp(36));
btnClearImg.setLayoutParams(lp2);

btnPickImg.setOnClickListener(v -> {
    activeScreenshotDisplay = ssDisplay;
    // Hapus tag lama biar gambar lama ga kebawa
    if (ssDisplay.getTag() == null || ssDisplay.getText().toString().equals("Belum ada gambar")) {
        ssDisplay.setTag(null);
    }
    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(intent, 1001);
});

btnClearImg.setOnClickListener(v -> {
    selectedScreenshot[0] = "";
    ssDisplay.setText("Belum ada gambar");
});

ssRow.addView(btnPickImg);
ssRow.addView(btnClearImg);
ssContainer.addView(ssRow);
root.addView(ssContainer);

    etCh.setMinLines(2);

    root.addView(etName);
    root.addView(etVer);
    root.addView(etSize);
    root.addView(etCh);
    root.addView(etLink);
    root.addView(etGetkey);   

    AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(root)
            .setPositiveButton("SIMPAN", null)
            .setNegativeButton("BATAL", null)
            .create();
    dialog.show();

    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
        String cat = selectedCategory[0];
        String name = etName.getText().toString().trim();
        String ver = etVer.getText().toString().trim();
        String size = etSize.getText().toString().trim();
        String ch = etCh.getText().toString().trim();
        String link = etLink.getText().toString().trim();
        String getkey = etGetkey.getText().toString().trim();
        String screenshot = "";
if (activeScreenshotDisplay != null && activeScreenshotDisplay.getTag() != null) {
    screenshot = activeScreenshotDisplay.getTag().toString();
}
        if (cat.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Kategori & nama wajib", Toast.LENGTH_SHORT).show();
            return;
        }
        if (existing == null) {
            SupabaseHelper.addApkUpdate(cat, name, ver, size, ch, link, getkey, screenshot, 0, (success, msg) -> runOnUiThread(() -> {
                Toast.makeText(this, success ? "✅ Ditambahkan" : "❌ " + msg, Toast.LENGTH_SHORT).show();
                if (success) { dialog.dismiss(); loadApk(); }
            }));
        } else {
            try {
                JSONObject patch = new JSONObject();
                patch.put("category", cat);
                patch.put("name", name);
                patch.put("version", ver);
                patch.put("size", size);
                patch.put("changelog", ch);
                patch.put("link", link);
                patch.put("getkey_link", getkey);
                patch.put("screenshot", screenshot);
                String id = existing.optString("id", "");
                SupabaseHelper.editApkUpdate(id, patch, (success, msg) -> runOnUiThread(() -> {
                    Toast.makeText(this, success ? "✅ Diupdate" : "❌ " + msg, Toast.LENGTH_SHORT).show();
                    if (success) { dialog.dismiss(); loadApk(); }
                }));
            } catch (Exception e) {}
        }
    });

    if (dialog.getWindow() != null) {
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.parseColor("#14141C")));
    }
}

// ============================================
// HELPER — input + button
// ============================================
private EditText makeInput(String value, String hint) {
    EditText et = new EditText(this);
    et.setText(value);
    et.setHint(hint);
    et.setTextColor(Color.parseColor("#F8FAFC"));
    et.setHintTextColor(Color.parseColor("#64748B"));
    et.setTextSize(12f);
    et.setBackgroundResource(R.drawable.edit_bg);
    et.setPadding(dp(14), dp(14), dp(14), dp(14));
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(8));
    et.setLayoutParams(p);
    return et;
}

private TextView createAddBtn(String text, View.OnClickListener listener) {
    TextView btn = new TextView(this);
    btn.setText(text);
    btn.setTextSize(12f);
    btn.setTypeface(null, android.graphics.Typeface.BOLD);
    btn.setTextColor(Color.parseColor("#0A0A0F"));
    btn.setGravity(Gravity.CENTER);
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#A855F7"));
    bg.setCornerRadius(dp(12));
    btn.setBackground(bg);
    btn.setPadding(0, dp(16), 0, dp(16));
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, dp(8), 0, 0);
    btn.setLayoutParams(p);
    btn.setClickable(true);
    btn.setFocusable(true);
    btn.setOnClickListener(listener);
    return btn;
}

// ============================================
// COMMENTS TAB
// ============================================
private void loadComments() {
    SupabaseHelper.fetchComments((success, data, message) -> {
        runOnUiThread(() -> {
            if (commentsContainer == null) return;
            commentsContainer.removeAllViews();
            if (!success || data == null || data.length() == 0) {
                commentsContainer.addView(emptyText("📂 Belum ada komentar"));
                return;
            }
            cachedComments = data;
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                commentsContainer.addView(createCommentCard(o));
            }
        });
    });
}

private View createCommentCard(JSONObject o) {
    String id = o.optString("id", "");
    int apkId = o.optInt("apk_id", 0);
    String name = o.optString("name", "");
    String comment = o.optString("comment", "");
    String device = o.optString("device", "");
    String country = o.optString("country", "");
    String ip = o.optString("ip", "");
    String fp = o.optString("fp", "");
    boolean isHidden = o.optBoolean("is_hidden", false);
    long ts = parseIso(o.optString("created_at", ""));

    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(14), dp(12), dp(14), dp(12));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setStroke(dp(1), isHidden ? Color.parseColor("#EF4444") : Color.parseColor("#262630"));
    bg.setCornerRadius(dp(10));
    card.setBackground(bg);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(8));
    card.setLayoutParams(p);

    TextView tvName = new TextView(this);
    tvName.setText((isHidden ? "🚫 " : "👤 ") + name);
    tvName.setTextColor(isHidden ? Color.parseColor("#EF4444") : Color.parseColor("#A78BFA"));
    tvName.setTextSize(12f);
    tvName.setTypeface(null, android.graphics.Typeface.BOLD);
    card.addView(tvName);

    TextView tvMeta = new TextView(this);
    StringBuilder meta = new StringBuilder();
    meta.append("📱 ").append(device.isEmpty() ? "?" : device);
    if (!country.isEmpty()) meta.append(" · 🌍 ").append(country);
    if (ts > 0) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        meta.append(" · ").append(sdf.format(new Date(ts)));
    }
    if (!ip.isEmpty()) meta.append("\nIP: ").append(ip);
    if (!fp.isEmpty()) meta.append(" · FP: ").append(fp.length() > 15 ? fp.substring(0, 15) + "..." : fp);
    meta.append("\nAPK ID: ").append(apkId);
    tvMeta.setText(meta.toString());
    tvMeta.setTextColor(Color.parseColor("#94A3B8"));
    tvMeta.setTextSize(9f);
    tvMeta.setPadding(0, dp(4), 0, 0);
    card.addView(tvMeta);

    TextView tvComment = new TextView(this);
    tvComment.setText(comment);
    tvComment.setTextColor(Color.parseColor("#F8FAFC"));
    tvComment.setTextSize(11f);
    tvComment.setPadding(0, dp(8), 0, 0);
    card.addView(tvComment);

    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setPadding(0, dp(8), 0, 0);

    TextView btnHide = createMiniBtn(isHidden ? "SHOW" : "HIDE",
            isHidden ? "#22C55E" : "#F59E0B",
            isHidden ? "#0A1F14" : "#1F1A0A");
    TextView btnDel = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    TextView btnCopyIp = createMiniBtn("COPY IP", "#A78BFA", "#1A0E2E");

    LinearLayout.LayoutParams r1 = new LinearLayout.LayoutParams(0, dp(32), 1f);
    r1.setMargins(0, 0, dp(4), 0);
    btnCopyIp.setLayoutParams(r1);
    LinearLayout.LayoutParams r2 = new LinearLayout.LayoutParams(0, dp(32), 1f);
    r2.setMargins(0, 0, dp(4), 0);
    btnHide.setLayoutParams(r2);
    LinearLayout.LayoutParams r3 = new LinearLayout.LayoutParams(0, dp(32), 1f);
    btnDel.setLayoutParams(r3);

    btnCopyIp.setOnClickListener(v -> {
        if (ip.isEmpty()) return;
        ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cb != null) {
            cb.setPrimaryClip(ClipData.newPlainText("IP", ip));
            Toast.makeText(this, "IP copied", Toast.LENGTH_SHORT).show();
        }
    });

    btnHide.setOnClickListener(v -> {
        SupabaseHelper.hideComment(id, !isHidden, (success, msg) -> runOnUiThread(() -> {
            if (success) loadComments();
            else Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }));
    });

    btnDel.setOnClickListener(v -> {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Komentar")
                .setMessage("Hapus komentar dari " + name + "?")
                .setPositiveButton("Hapus", (d, w) -> {
                    SupabaseHelper.deleteComment(id, (success, msg) -> runOnUiThread(() -> {
                        if (success) loadComments();
                        else Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }));
                })
                .setNegativeButton("Batal", null).show();
    });

    row.addView(btnCopyIp);
    row.addView(btnHide);
    row.addView(btnDel);
    card.addView(row);
    return card;
}

// ============================================
// FILTER TAB (BAD WORDS)
// ============================================
private void loadBadWords() {
    SupabaseHelper.fetchBadWords((success, data, message) -> {
        runOnUiThread(() -> {
            if (badWordsContainer == null) return;
            badWordsContainer.removeAllViews();
            if (!success || data == null || data.length() == 0) {
                badWordsContainer.addView(emptyText("📂 Belum ada kata kasar"));
                return;
            }
            cachedBadWords = data;
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                badWordsContainer.addView(createBadWordRow(o));
            }
        });
    });
}

private View createBadWordRow(JSONObject o) {
    String id = o.optString("id", "");
    String word = o.optString("word", "");

    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(14), dp(10), dp(14), dp(10));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setCornerRadius(dp(10));
    row.setBackground(bg);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(6));
    row.setLayoutParams(p);

    TextView tvWord = new TextView(this);
    tvWord.setText("🚫 " + word);
    tvWord.setTextColor(Color.parseColor("#EF4444"));
    tvWord.setTextSize(12f);
    tvWord.setTypeface(null, android.graphics.Typeface.BOLD);
    tvWord.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    row.addView(tvWord);

    TextView btnDel = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(dp(85), dp(32));
    btnDel.setLayoutParams(dp2);
    btnDel.setOnClickListener(v -> {
        SupabaseHelper.deleteBadWord(id, (success, msg) -> runOnUiThread(() -> {
            if (success) loadBadWords();
            else Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }));
    });
    row.addView(btnDel);
    return row;
}

private void addBadWord() {
    if (etBadWord == null) return;
    String word = etBadWord.getText().toString().trim().toLowerCase();
    if (word.isEmpty()) {
        Toast.makeText(this, "Ketik kata dulu", Toast.LENGTH_SHORT).show();
        return;
    }
    SupabaseHelper.addBadWord(word, (success, msg) -> runOnUiThread(() -> {
        if (success) {
            etBadWord.setText("");
            Toast.makeText(this, "✅ Ditambahkan", Toast.LENGTH_SHORT).show();
            loadBadWords();
        } else {
            Toast.makeText(this, "❌ " + msg, Toast.LENGTH_SHORT).show();
        }
    }));
}

// ============================================
// KATEGORI TAB
// ============================================
private void loadCategories() {
    SupabaseHelper.fetchCategories((success, data, message) -> {
        runOnUiThread(() -> {
            if (categoriesContainer == null) return;
            categoriesContainer.removeAllViews();
            if (!success || data == null || data.length() == 0) {
                categoriesContainer.addView(emptyText("📂 Belum ada kategori"));
                return;
            }
            cachedCategories = data;
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.optJSONObject(i);
                if (o == null) continue;
                categoriesContainer.addView(createCategoryRow(o));
            }
        });
    });
}

private View createCategoryRow(JSONObject o) {
    String id = o.optString("id", "");
    String name = o.optString("name", "");

    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(14), dp(10), dp(14), dp(10));
    GradientDrawable bg = new GradientDrawable();
    bg.setColor(Color.parseColor("#14141C"));
    bg.setCornerRadius(dp(10));
    row.setBackground(bg);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    p.setMargins(0, 0, 0, dp(6));
    row.setLayoutParams(p);

    TextView tvName = new TextView(this);
    tvName.setText("📁 " + name);
    tvName.setTextColor(Color.parseColor("#F8FAFC"));
    tvName.setTextSize(12f);
    tvName.setTypeface(null, android.graphics.Typeface.BOLD);
    tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    row.addView(tvName);

    TextView btnDel = createMiniBtn("DELETE", "#EF4444", "#1F0A0A");
    LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(dp(85), dp(32));
    btnDel.setLayoutParams(dp2);
    btnDel.setOnClickListener(v -> {
        SupabaseHelper.deleteCategory(id, (success, msg) -> runOnUiThread(() -> {
            if (success) loadCategories();
            else Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }));
    });
    row.addView(btnDel);
    return row;
}

private void addCategory() {
    if (etNewCategory == null) return;
    String name = etNewCategory.getText().toString().trim().toUpperCase();
    if (name.isEmpty()) {
        Toast.makeText(this, "Ketik nama kategori", Toast.LENGTH_SHORT).show();
        return;
    }
    SupabaseHelper.addCategory(name, 0, (success, msg) -> runOnUiThread(() -> {
        if (success) {
            etNewCategory.setText("");
            Toast.makeText(this, "✅ Ditambahkan", Toast.LENGTH_SHORT).show();
            loadCategories();
        } else {
            Toast.makeText(this, "❌ " + msg, Toast.LENGTH_SHORT).show();
        }
    }));
}

    private TextView emptyText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#64748B"));
        tv.setTextSize(11f);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, dp(40), 0, dp(40));
        return tv;
    }
}