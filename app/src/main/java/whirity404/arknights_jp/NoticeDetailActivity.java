package whirity404.arknights_jp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import whirity404.arknights.notice.R;
import whirity404.arknights.notice.models.BulletinDetail;
import whirity404.arknights.notice.utils.HttpUtil;
import whirity404.arknights.notice.utils.JsonUtil;

public class NoticeDetailActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private WebView bannerWebView;
    private TextView titleView;
    private TextView headerView;
    private TextView contentView;
    private TextView timeView;
    private Button jumpButton;
    private String dT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        // 初始化视图
        progressBar = findViewById(R.id.progressBar);
        bannerWebView = findViewById(R.id.bannerWebView);
        titleView = findViewById(R.id.title);
        headerView = findViewById(R.id.header);
        contentView = findViewById(R.id.content);
        timeView = findViewById(R.id.time);
        jumpButton = findViewById(R.id.jumpButton);

        // 配置WebView
        bannerWebView.getSettings().setLoadWithOverviewMode(true);
        bannerWebView.getSettings().setUseWideViewPort(true);

        // 获取传递的cid并检查有效性
        String cid = getIntent().getStringExtra("cid");
        if (cid == null || cid.isEmpty()) {
            Toast.makeText(this, R.string.uncid, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new LoadDetailTask().execute(cid);
    }

    private class LoadDetailTask extends AsyncTask<String, Void, BulletinDetail> {
        private String error;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected BulletinDetail doInBackground(String... params) {
            try {
                String json = HttpUtil.get("https://ak-webview.arknights.jp/api/game/bulletin/" + params[0]);
                return JsonUtil.parseBulletinDetail(json);
            } catch (Exception e) {
                error = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final BulletinDetail detail) {
            progressBar.setVisibility(View.GONE);

            if (detail == null) {
                Toast.makeText(NoticeDetailActivity.this, error, Toast.LENGTH_LONG).show();
                Intent errorIntent = new Intent(NoticeDetailActivity.this, whirity404.arknights_jp.ErrorForWeb.class);
                startActivity(errorIntent);
                return;
            }

            // 显示banner图片（使用WebView）
            if (detail.getBannerImageUrl() != null && !detail.getBannerImageUrl().isEmpty()) {
                String html = "<html><body style='margin:0;padding:0;'>" +
                    "<img src='" + detail.getBannerImageUrl() + "' " +
                    "style='width:100%;height:auto;'/></body><title>Arknights</title></html>";
                bannerWebView.loadData(html, "text/html", "UTF-8");
                bannerWebView.setVisibility(View.VISIBLE);
            }

            // 显示标题
            if (detail.getTitle() != null) {
                titleView.setText(detail.getTitle());
                titleView.setVisibility(View.VISIBLE);
            }

            // 显示header
            if (detail.getHeader() != null) {
                headerView.setText(detail.getHeader());
                headerView.setVisibility(View.VISIBLE);
            }

            // 显示内容
            if (detail.getContent() != null && !detail.getContent().isEmpty()) {
                String content = detail.getContent().replace("\\u003E", ">");
                contentView.setText(content);
                contentView.setVisibility(View.VISIBLE);
            } else {
                contentView.setText(R.string.notContent);
                contentView.setVisibility(View.VISIBLE);
            }

            // 设置跳转按钮（始终显示，但根据有无链接设置不同行为）
            jumpButton.setVisibility(View.VISIBLE);
            if (detail.getJumpLink() != null && !detail.getJumpLink().isEmpty()) {
                jumpButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(detail.getJumpLink()));
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(NoticeDetailActivity.this, R.string.notopen_1, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            } else {
                jumpButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(NoticeDetailActivity.this, R.string.notopen_0, Toast.LENGTH_SHORT).show();
                        }
                    });
            }

            // 显示时间
            if (detail.getDisplayTime() != null && !detail.getDisplayTime().isEmpty()) {
                timeView.setText(detail.getDisplayTime());
            } else {
                timeView.setText("Non-get-displayTime");
            }
        }
    }
}

