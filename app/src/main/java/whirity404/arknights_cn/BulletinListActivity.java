package whirity404.arknights_cn;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import whirity404.arknights.notice.R;
import whirity404.arknights.notice.models.BulletinItem;
import whirity404.arknights.notice.utils.HttpUtil;
import whirity404.arknights.notice.utils.JsonUtil;

public class BulletinListActivity extends AppCompatActivity {
    private ListView listView;
    private ProgressBar progressBar;
    private List<BulletinItem> originalItems = new ArrayList<>(); // 保存原始数据
    private long firstBackTime;
    
    // 是否已经弹出过提示，避免重复弹窗
    private boolean hasShownHintDialog = false;

// 关键词列表
    private static final String[] HIT_KEYWORDS = new String[]{
        "未成年人",
        "限时",
        "防沉迷",
        "网信办",
        "国家新闻出版署",
        "国务院办公厅"
    };
    
    
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstBackTime > 0) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.exit_notice);
            builder.setMessage(R.string.exit_content);

            // 设置三个按钮
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 取消操作
                        dialog.dismiss();
                    }
                });

            builder.setNeutralButton(R.string.refresh, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 刷新操作
                        new LoadBulletinTask().execute();
                    }
                });

            builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 退出操作
                        finish();
                    }
                });

            AlertDialog dialog = builder.create();
            dialog.show();

            firstBackTime = System.currentTimeMillis();
            return;
        }

        super.onBackPressed();
    }
    
    private boolean containsHitKeyword(String title) {
        if (title == null) return false;
        for (String keyword : HIT_KEYWORDS) {
            if (title.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private void showHintDialog() {
        if (hasShownHintDialog) return;
        hasShownHintDialog = true;

        new AlertDialog.Builder(this)
            .setTitle("友情提示")
            .setMessage(
            "检测到有游玩限制。\n\n" +
            "你可能需要翻墙来获取外服，" +
            "可点击下方按钮查看相关页面。\n\n广告：宝可梦机场，超低费率，低延迟大带宽，翻墙看片首选！"
        )
        .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 退出操作
                    finish();
                }
            });
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin_list);

        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);

        new LoadBulletinTask().execute();
    }

    private class LoadBulletinTask extends AsyncTask<Void, Void, List<BulletinItem>> {
        private String error;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<BulletinItem> doInBackground(Void... voids) {
            try {
                String json = HttpUtil.get("https://ak-webview.hypergryph.com/api/game/bulletinList?target=Android");
                return JsonUtil.parseBulletinList(json);
            } catch (Exception e) {
                error = e.getMessage();
                return null;
            }
        }
        
        private void showRecommendDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(BulletinListActivity.this);
            builder.setTitle("提示");
            builder.setCancelable(false);
            builder.setMessage(
                "检测到有游玩限制。\n\n" +
                "你可能需要翻墙来获得外服，" +
                "可点击下方按钮查看相关页面。"
            );

            // 按钮 1：官网
            builder.setPositiveButton("\n广告:宝可梦加速器(超低费率，极低延迟，超高速度，看片不卡！)\n", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(android.net.Uri.parse(
                                           "https://love.p6m6.com/#/register?code=dWeTh0cI"   // ← 网站 A
                                       ));
                        startActivity(intent);
                    }
                });

            // 按钮 2：TG / 备用站
            builder.setNeutralButton("\n推荐机场的TG频道\n", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(android.net.Uri.parse(
                                           "https://t.me/pokemon_love"  // ← 网站 B
                                       ));
                        startActivity(intent);
                    }
                });

            builder.setNegativeButton("\n谢谢，我不要。\n", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplication(), "你可以在菜单中刷新以重新显示此内容。", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

            builder.create().show();
        }
        

        @Override
        protected void onPostExecute(List<BulletinItem> result) {
            progressBar.setVisibility(View.GONE);
            
            
            

            if (result != null) {
                originalItems = result; // 保存原始数据
                boolean hit = false;

                for (BulletinItem item : originalItems) {
                    if (containsHitKeyword(item.getTitle())) {
                        hit = true;
                        break;
                    }
                }

                if (hit) {
                    showRecommendDialog(); // 每次加载，只弹一个
                }
                

                // 创建SimpleAdapter需要的数据结构
                List<Map<String, String>> data = new ArrayList<>();
                for (BulletinItem item : originalItems) {
                    Map<String, String> map = new HashMap<>();
                    map.put("title", item.getCid() + " | " + item.getTitle());
                    map.put("time", item.getDisplayTime());
                    data.add(map);
                }

                SimpleAdapter adapter = new SimpleAdapter(
                    BulletinListActivity.this,
                    data,
                    android.R.layout.simple_list_item_2,
                    new String[]{"title", "time"},
                    new int[]{android.R.id.text1, android.R.id.text2}
                );

                listView.setAdapter(adapter);

                // 设置点击监听器
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            // 从原始数据列表获取对象
                            BulletinItem item = originalItems.get(position);

                            // 添加空检查
                            if (item == null || item.getCid() == null) {
                                Toast.makeText(BulletinListActivity.this, 
                                               "无法获取公告详情", 
                                               Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Intent intent = new Intent(BulletinListActivity.this, NoticeDetailActivity.class);
                            intent.putExtra("cid", item.getCid());
                            startActivity(intent);
                        }
                    });

            } else {
                Toast.makeText(BulletinListActivity.this, 
                               "加载失败: " + error, 
                               Toast.LENGTH_LONG).show();
            }
        }
    }
}

