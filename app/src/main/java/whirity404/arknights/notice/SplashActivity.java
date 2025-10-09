package whirity404.arknights.notice;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;
import android.webkit.WebView;
import whirity404.arknights.notice.UpdateChecker;
import android.webkit.WebSettings;

public class SplashActivity extends AppCompatActivity {
    
    private WebView loading_webview;
    private TextView textViewLoading;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_splash);
        
        // 先初始化控件
        textViewLoading = findViewById(R.id.textview_loading);
        loading_webview = findViewById(R.id.webview_loading);

        // 调试日志
        Log.d(TAG, "textViewLoading: " + textViewLoading);
        Log.d(TAG, "loading_webview: " + loading_webview);

        if (loading_webview == null) {
            Log.e(TAG, "loading WebView is null!");
            Toast.makeText(this, "初始化失败：找不到加载控件", Toast.LENGTH_LONG).show();
            // 直接跳转，避免卡在闪屏页
            navigateToMain();
            return;
        }

        // 配置WebView
        setupWebView();
        
        // 然后加载图片
        loadLocalImage();

        // 模拟加载过程
        simulateLoading();
        
        
    }
    
    
    
    private void setupWebView() {
        try {
            WebSettings webSettings = loading_webview.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setDomStorageEnabled(true);
            
            // 设置透明背景
            loading_webview.setBackgroundColor(0x00000000);
            loading_webview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up WebView", e);
        }
    }
    
    private void loadLocalImage() {
        try {
            // 方法1：使用HTML方式加载（更可靠）
            String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "<style>" +
                "body, html { margin: 0; padding: 0; width: 100%; height: 100%; }" +
                "body { display: flex; justify-content: center; align-items: center; background: transparent; }" +
                "img { max-width: 100%; max-height: 100%; object-fit: contain; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<img src=\"file:///android_asset/raw/loading.jpg\" alt=\"Loading\">" +
                "</body>" +
                "</html>";
            
            loading_webview.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
            
            Log.d(TAG, "WebView loadData called");
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading image in WebView", e);
            // 备用方案：直接加载URL
            try {
                loading_webview.loadUrl("file:///android_asset/raw/loading.jpg");
            } catch (Exception e2) {
                Log.e(TAG, "Error loading URL in WebView", e2);
            }
        }
    }

    private void simulateLoading() {
        new UpdateChecker(SplashActivity.this).checkForUpdate();
        Log.d(TAG, "开始模拟加载");

        // 显示加载提示
        Toast.makeText(this, "正在关闭全舰防御系统...", Toast.LENGTH_SHORT).show();
        
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 隐藏WebView
                if (loading_webview != null) {
                    loading_webview.setVisibility(android.view.View.GONE);
                }
                if (textViewLoading != null) {
                    textViewLoading.setText("加载完成");
                }

                // 跳转到 MainActivity
                navigateToMain();
            }
        }, 8000); // 8秒后完成
    }

    private void navigateToMain() {
        try {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // 关闭当前的 SplashActivity
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to MainActivity", e);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理WebView资源
        if (loading_webview != null) {
            loading_webview.destroy();
        }
    }
}
