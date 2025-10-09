package whirity404.arknights_jp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import whirity404.arknights.notice.R;

public class NoticeWebActivity extends AppCompatActivity {
    
    private WebView webView;
    private String currentUrl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        destroyWebView();
        setContentView(R.layout.activity_notice_web);
        
        // 初始化工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // 显示返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        webView = findViewById(R.id.webView);
        
        // 配置WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 页面加载完成后更新标题
                if (getSupportActionBar() != null) {
                    String title = view.getTitle();
                    if (title != null && !title.isEmpty()) {
                        getSupportActionBar().setTitle(title);
                    } else {
                        getSupportActionBar().setTitle("组件默认页面");
                    }
                }
            }
        });
        
        // 获取传递的URL
        currentUrl = getIntent().getStringExtra("url");
        if (currentUrl != null && !currentUrl.isEmpty()) {
            webView.loadUrl(currentUrl);
            // 设置初始标题
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("加载中...");
            }
        } else {
            Toast.makeText(this, "无效网页", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 创建菜单
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            // 返回按钮
            onBackPressed();
            return true;
        } else if (id == R.id.menu_refresh) {
            // 刷新
            refreshWebView();
            return true;
        } else if (id == R.id.menu_open_in_system) {
            // 系统默认方式打开
            openInSystemBrowser();
            return true;
        } else if (id == R.id.menu_destroy_webview) {
            // 强行结束WebView实例
            destroyWebView();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 刷新WebView
     */
    private void refreshWebView() {
        if (webView != null) {
            webView.reload();
            Toast.makeText(this, "正在刷新", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 使用系统浏览器打开
     */
    private void openInSystemBrowser() {
        if (currentUrl != null && !currentUrl.isEmpty()) {
            try {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, 
                        android.net.Uri.parse(currentUrl));
                startActivity(intent);
                Toast.makeText(this, "尝试在系统浏览器打开", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * 强行结束WebView实例
     */
    private void destroyWebView() {
        if (webView != null) {
            // 停止加载
            webView.stopLoading();
            
            // 从父视图移除
            if (webView.getParent() != null) {
                ((android.view.ViewGroup) webView.getParent()).removeView(webView);
            }
            
            // 清理WebView
            webView.clearHistory();
            webView.clearCache(true);
            webView.clearFormData();
            webView.clearMatches();
            webView.clearSslPreferences();
            
            // 销毁WebView
            webView.destroy();
            webView = null;
            
            Toast.makeText(this, "正在尝试重新启动组件，请稍候", Toast.LENGTH_SHORT).show();
            
            // 重新创建WebView
            recreateWebView();
        }
    }
    
    /**
     * 重新创建WebView
     */
    private void recreateWebView() {
        webView = new WebView(this);
        webView.setId(R.id.webView);
        
        // 重新配置WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (getSupportActionBar() != null) {
                    String title = view.getTitle();
                    if (title != null && !title.isEmpty()) {
                        getSupportActionBar().setTitle(title);
                    } else {
                        getSupportActionBar().setTitle("PRTS Analysis OS");
                    }
                }
            }
        });
        
        // 重新加载URL
        if (currentUrl != null && !currentUrl.isEmpty()) {
            webView.loadUrl(currentUrl);
        }
        
        // 添加到布局
        android.widget.LinearLayout layout = findViewById(R.id.webview_container);
        layout.addView(webView, new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT));
    }
    
    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
