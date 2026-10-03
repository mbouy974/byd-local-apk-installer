package com.gbox.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final String TAG = "BackupInstaller";
    private static final int STORAGE = 11, INSTALL = 12;
    private File downloads, folder;
    private LinearLayout files;
    private TextView path, status;
    private boolean busy;
    private Uri incoming;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        folder = downloads;
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(32), dp(24), dp(32), dp(24));
        page.setBackgroundColor(Color.rgb(245, 249, 250));
        TextView title = text("APK Installer", 30);
        title.setTextColor(Color.rgb(21, 94, 117));
        page.addView(title);
        page.addView(text("Choose an APK from Downloads.", 19));
        path = text("Downloads", 17);
        page.addView(path);
        LinearLayout actions = new LinearLayout(this);
        Button refresh = button("Refresh");
        refresh.setOnClickListener(v -> { if (!busy) refresh(); });
        actions.addView(refresh);
        Button home = button("Downloads");
        home.setOnClickListener(v -> { if (!busy) { folder = downloads; refresh(); } });
        actions.addView(home);
        page.addView(actions);
        ScrollView scroll = new ScrollView(this);
        files = new LinearLayout(this);
        files.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(files);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        status = text("Select a file to review it before installing.", 17);
        page.addView(status);
        setContentView(page);
        incoming = Intent.ACTION_VIEW.equals(getIntent().getAction()) ? getIntent().getData() : null;
        if (incoming != null && "content".equals(incoming.getScheme())) {
            Uri uri = incoming;
            incoming = null;
            stage(null, uri);
        } else permissionAndRefresh();
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private TextView text(String value, int size) {
        TextView text = new TextView(this);
        text.setText(value); text.setTextSize(size);
        text.setPadding(0, dp(8), 0, dp(8));
        return text;
    }
    private Button button(String value) {
        Button button = new Button(this);
        button.setText(value); button.setTextSize(19); button.setMinHeight(dp(60));
        return button;
    }
    private void permissionAndRefresh() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE);
        else refresh();
    }
    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == STORAGE) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) refresh();
            else {
                status.setText("Allow file access to browse Downloads, then tap Retry.");
                Button retry = button("Retry file access");
                retry.setOnClickListener(v -> permissionAndRefresh());
                files.removeAllViews(); files.addView(retry);
            }
        }
    }
    private void refresh() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionAndRefresh(); return;
        }
        files.removeAllViews();
        path.setText(folder.equals(downloads) ? "Downloads" : folder.getAbsolutePath());
        if (!folder.equals(downloads)) {
            Button parent = button("← Back to parent folder");
            parent.setOnClickListener(v -> { folder = folder.getParentFile(); refresh(); });
            files.addView(parent);
        }
        File[] entries = folder.listFiles(file -> file.isDirectory() || file.getName().toLowerCase(Locale.ROOT).endsWith(".apk"));
        if (entries == null) {
            status.setText("Downloads could not be read. Check file access in Settings.");
            Log.w(TAG, "Unable to list " + folder);
            return;
        }
        Arrays.sort(entries, Comparator.comparing((File f) -> !f.isDirectory()).thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File entry : entries) {
            Button row = button((entry.isDirectory() ? "Folder: " : "") + entry.getName());
            row.setAllCaps(false);
            row.setOnClickListener(v -> {
                if (busy) return;
                try {
                    String root = downloads.getCanonicalPath();
                    String candidate = entry.getCanonicalPath();
                    if (!candidate.startsWith(root + File.separator)) throw new SecurityException("Outside Downloads");
                    if (entry.isDirectory()) { folder = entry; refresh(); }
                    else stage(entry, null);
                } catch (Exception error) { fail(error); }
            });
            files.addView(row, new LinearLayout.LayoutParams(-1, -2));
        }
        status.setText(entries.length == 0 ? "No APK files here. Save or copy an APK into Downloads, then tap Refresh." : "Select an APK to review it before installing.");
    }
    private void stage(File source, Uri uri) {
        if (busy) return;
        busy = true;
        status.setText("Preparing APK…");
        new Thread(() -> {
            File temporary = new File(getCacheDir(), "selected.tmp");
            File staged = new File(getCacheDir(), "selected.apk");
            try {
                try (InputStream in = source != null ? new FileInputStream(source) : getContentResolver().openInputStream(uri);
                     FileOutputStream out = new FileOutputStream(temporary)) {
                    if (in == null) throw new IllegalArgumentException("Cannot open this file");
                    byte[] buffer = new byte[65536];
                    long total = 0; int count;
                    while ((count = in.read(buffer)) != -1) {
                        total += count;
                        if (total > 2L * 1024 * 1024 * 1024) throw new IllegalArgumentException("APK is too large");
                        out.write(buffer, 0, count);
                    }
                    if (total == 0) throw new IllegalArgumentException("This file is empty");
                }
                PackageInfo info = getPackageManager().getPackageArchiveInfo(temporary.getAbsolutePath(), 0);
                if (info == null) throw new IllegalArgumentException("This is not a readable APK. Select a single .apk file.");
                if (staged.exists() && !staged.delete()) throw new IllegalStateException("Cannot replace staged APK");
                if (!temporary.renameTo(staged)) throw new IllegalStateException("Cannot prepare APK");
                info.applicationInfo.sourceDir = staged.getAbsolutePath();
                info.applicationInfo.publicSourceDir = staged.getAbsolutePath();
                String label = info.applicationInfo.loadLabel(getPackageManager()).toString();
                String details = label + "\nVersion " + info.versionName + "\n" + info.packageName;
                runOnUiThread(() -> {
                    busy = false;
                    if (isFinishing() || isDestroyed()) return;
                    status.setText("Ready: " + label);
                    new AlertDialog.Builder(this).setTitle("Install this app?").setMessage(details)
                        .setNegativeButton("Cancel", (dialog, which) -> cleanup())
                        .setOnCancelListener(dialog -> cleanup())
                        .setPositiveButton("Continue", (dialog, which) -> install()).show();
                });
            } catch (Exception error) {
                temporary.delete();
                runOnUiThread(() -> { busy = false; fail(error); });
            }
        }, "PrepareApk").start();
    }
    @SuppressWarnings("deprecation") private void install() {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setDataAndType(ApkProvider.URI, "application/vnd.android.package-archive");
        intent.setClipData(ClipData.newRawUri("APK", ApkProvider.URI));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        intent.setComponent(new ComponentName("com.android.packageinstaller", "com.android.packageinstaller.InstallStart"));
        try {
            busy = true;
            Log.i(TAG, "Opening system installer as " + getPackageName());
            status.setText("Complete the installation on the next screen.");
            startActivityForResult(intent, INSTALL);
        } catch (Exception error) { busy = false; fail(error); }
    }
    @Override public void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != INSTALL) return;
        busy = false;
        int detail = data != null ? data.getIntExtra("android.intent.extra.INSTALL_RESULT", Integer.MIN_VALUE) : Integer.MIN_VALUE;
        Log.i(TAG, "Installer returned result=" + result + " installResult=" + detail);
        if (result == RESULT_OK) status.setText("Installation completed.");
        else if (detail != Integer.MIN_VALUE) status.setText("Installation did not complete. Installer result: " + detail);
        else status.setText("Installation cancelled or blocked by the car.");
        cleanup();
    }
    private void cleanup() {
        revokeUriPermission(ApkProvider.URI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        new File(getCacheDir(), "selected.apk").delete();
    }
    private void fail(Exception error) {
        Log.e(TAG, "APK operation failed", error);
        status.setText("Could not continue: " + (error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage()));
    }
}
