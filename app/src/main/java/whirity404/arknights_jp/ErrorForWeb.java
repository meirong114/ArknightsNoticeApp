package whirity404.arknights_jp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import whirity404.arknights.notice.MainActivity;
import whirity404.arknights.notice.R;

public class ErrorForWeb extends AppCompatActivity {
    private WebView webView;
    private ValueCallback<Uri[]> uploadMessage;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    private static final int DOWNLOAD_PERMISSION_REQUEST_CODE = 2;
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 3;

    private String selectedImageUrl;
    private ActionMode actionMode;
    private List<String> adBlockRules = new ArrayList<>();
    private String blockedPageHtml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jp_errorweb);

        webView = findViewById(R.id.webView);

        // 初始化广告拦截系统
        initAdBlockSystem();

        // 配置WebView设置
        configureWebView();

        // 加载指定网页
        //webView.loadUrl("https://www.himcbbs.com");
        loadMainPage();

        // 检查并请求存储权限
        checkAndRequestPermissions();
    }

    private void initAdBlockSystem() {
        // 加载广告拦截规则
        loadAdBlockRules();

        // 加载拦截提示页面
        loadBlockedPage();
    }

    private void loadAdBlockRules() {
        try {
            InputStream is = getAssets().open("adlist.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    adBlockRules.add(line);
                }
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "加载广告拦截列表失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMainPage() {
        webView.loadUrl("https://ak-webview.arknights.jp");
    }
    private void loadBlockedPage() {
        try {
            InputStream is = getAssets().open("blocked.html");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            blockedPageHtml = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            blockedPageHtml = "<html><body><h1>广告已被拦截</h1></body></html>";
        }
    }

    private void configureWebView() {
        WebSettings webSettings = webView.getSettings();

        // 启用JavaScript
        webSettings.setJavaScriptEnabled(true);

        // 启用DOM存储
        webSettings.setDomStorageEnabled(true);

        // 启用数据库
        webSettings.setDatabaseEnabled(true);

        // 设置缓存模式
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // 启用缩放控制
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // 启用广域视图
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // 其他设置
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        // 处理文件上传
        webView.setWebChromeClient(new WebChromeClient() {
                // 对于Android 5.0+
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                    }
                    uploadMessage = filePathCallback;

                    Intent intent = fileChooserParams.createIntent();
                    try {
                        startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
                    } catch (Exception e) {
                        uploadMessage = null;
                        Toast.makeText(ErrorForWeb.this, "无法打开文件选择器", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return true;
                }
            });

        // 处理页面导航和广告拦截
        webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // 处理特殊URL方案
                    if (url.startsWith("tel:") || url.startsWith("sms:") || url.startsWith("mailto:")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }

                    // 让WebView处理其他URL
                    return false;
                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                    if (shouldBlockRequest(url)) {
                        return createBlockedResponse();
                    }
                    return super.shouldInterceptRequest(view, url);
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    if (shouldBlockRequest(url)) {
                        return createBlockedResponse();
                    }
                    return super.shouldInterceptRequest(view, request);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    // 页面开始加载时清除选中的图片URL
                    selectedImageUrl = null;
                }
            });

        // 处理下载
        webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                    // 检查是否有存储权限
                    if (ContextCompat.checkSelfPermission(ErrorForWeb.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                        != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ErrorForWeb.this, 
                                                          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                                                          DOWNLOAD_PERMISSION_REQUEST_CODE);
                        return;
                    }

                    // 开始下载
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                    // 设置cookie
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);

                    // 设置用户代理
                    request.addRequestHeader("User-Agent", userAgent);

                    // 设置下载描述
                    request.setDescription("正在下载文件");

                    // 设置下载标题
                    String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
                    request.setTitle(fileName);

                    // 设置下载后通知显示
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                    // 设置下载位置
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

                    // 获取下载服务并加入下载队列
                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    downloadManager.enqueue(request);

                    Toast.makeText(ErrorForWeb.this, "开始下载: " + fileName, Toast.LENGTH_LONG).show();
                }
            });

        // 添加长按图片处理
        webView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    WebView.HitTestResult result = webView.getHitTestResult();
                    if (result.getType() == WebView.HitTestResult.IMAGE_TYPE || 
                        result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

                        selectedImageUrl = result.getExtra();
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                        actionMode = startActionMode(new ImageActionModeCallback());
                        return true;
                    }
                    return false;
                }
            });
    }

    private boolean shouldBlockRequest(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        for (String rule : adBlockRules) {
            if (matchesAdRule(url, rule)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesAdRule(String url, String rule) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            String path = uri.getPath();

            // 处理 ||domain/path* 格式
            if (rule.startsWith("||") && rule.contains("*")) {
                String pattern = rule.substring(2); // 移除开头的 ||

                // 将 * 转换为正则表达式 .*
                String regex = pattern.replace("*", ".*")
                    .replace(".", "\\.");

                // 构建完整URL匹配模式
                String fullPattern = "^(http|https)://" + regex;

                return url.matches(fullPattern);
            }

            // 处理 ||domain^ 格式
            if (rule.startsWith("||") && rule.endsWith("^")) {
                String domain = rule.substring(2, rule.length() - 1);
                return host != null && (host.equals(domain) || host.endsWith("." + domain));
            }

            // 处理 |http://example.com| 精确匹配
            if (rule.startsWith("|") && rule.endsWith("|")) {
                String exactUrl = rule.substring(1, rule.length() - 1);
                return url.equals(exactUrl);
            }

            // 处理简单域名匹配
            return url.contains(rule);
        } catch (Exception e) {
            return false;
        }
    }
    private WebResourceResponse createBlockedResponse() {
        return new WebResourceResponse(
            "text/html",
            "utf-8",
            new ByteArrayInputStream(blockedPageHtml.getBytes())
        );
    }

    // 图片操作菜单回调
    private class ImageActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.image_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.save_image) {
                saveImageToGallery();
                mode.finish();
                return true;
            } else if (id == R.id.share_image) {
                shareImage();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    }

    private void saveImageToGallery() {
        if (selectedImageUrl == null || selectedImageUrl.isEmpty()) {
            Toast.makeText(this, "没有选中的图片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                                              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                              SAVE_IMAGE_PERMISSION_REQUEST_CODE);
            return;
        }

        // 在新线程中下载并保存图片
        new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 下载图片
                        Bitmap bitmap = new android.graphics.BitmapFactory().decodeStream(
                            new java.net.URL(selectedImageUrl).openStream());

                        // 保存到相册
                        String fileName = "IMG_" + UUID.randomUUID().toString() + ".jpg";
                        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        if (!directory.exists()) {
                            directory.mkdirs();
                        }
                        File file = new File(directory, fileName);
                        FileOutputStream fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();

                        // 通知系统相册更新
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(Uri.fromFile(file));
                        sendBroadcast(mediaScanIntent);

                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ErrorForWeb.this, 
                                                   "图片已保存到相册", Toast.LENGTH_LONG).show();
                                }
                            });
                    } catch (IOException e) {
                        Log.e("SaveImage", "保存图片失败", e);
                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ErrorForWeb.this, 
                                                   "保存图片失败!", Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                }
            }).start();
    }

    private void shareImage() {
        if (selectedImageUrl == null || selectedImageUrl.isEmpty()) {
            Toast.makeText(this, "没有选中的图片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 在新线程中下载并分享图片
        new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 下载图片
                        Bitmap bitmap = new android.graphics.BitmapFactory().decodeStream(
                            new java.net.URL(selectedImageUrl).openStream());

                        // 保存到缓存目录
                        File cachePath = new File(getCacheDir(), "images");
                        String fileSharedName = "shared_image" + UUID.randomUUID().toString() + ".jpg";
                        cachePath.mkdirs();
                        File file = new File(cachePath, fileSharedName);
                        FileOutputStream fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();

                        // 创建分享Intent
                        Uri contentUri = FileProvider.getUriForFile(
                            ErrorForWeb.this,
                            getPackageName() + ".fileprovider",
                            file);

                        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/jpeg");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(Intent.createChooser(shareIntent, "分享图片"));
                                }
                            });
                    } catch (IOException e) {
                        Log.e("ShareImage", "分享图片失败", e);
                        runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ErrorForWeb.this, 
                                                   "分享图片失败!", Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                }
            }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == DOWNLOAD_PERMISSION_REQUEST_CODE || 
            requestCode == SAVE_IMAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "存储权限被拒绝，相关功能可能无法使用", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (uploadMessage == null) return;

            Uri[] results = null;
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            uploadMessage.onReceiveValue(results);
            uploadMessage = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("不要再按了！");
            builder.setMessage("再按就要跟爆裂黎明说去了！");

            // 设置三个按钮
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 取消操作
                        dialog.dismiss();
                    }
                });

            builder.setNeutralButton("刷新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 刷新操作
                        loadMainPage();
                    }
                });

            builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 退出操作
                        finish();
                    }
                });

            AlertDialog dialog = builder.create();
            dialog.show();


        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                                                  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                                                  DOWNLOAD_PERMISSION_REQUEST_CODE);
            }
        }
    }
}

