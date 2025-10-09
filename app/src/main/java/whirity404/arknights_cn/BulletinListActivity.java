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

        @Override
        protected void onPostExecute(List<BulletinItem> result) {
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                originalItems = result; // 保存原始数据

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

