package whirity404.arknights.notice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import whirity404.arknights_cn.BulletinListActivity;

public class NotificationBox extends AppCompatActivity {
    private ListView listView;
    private List<Map<String, String>> announcements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_box);

        listView = findViewById(R.id.listView);

        // Load announcements from cache or database
        loadAnnouncements();

        SimpleAdapter adapter = new SimpleAdapter(
            this,
            announcements,
            android.R.layout.simple_list_item_2,
            new String[]{"title", "host"},
            new int[]{android.R.id.text1, android.R.id.text2}
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, String> item = announcements.get(position);
                    String host = item.get("host");

                    Intent intent;
                    if (host.contains("hypergryph.com")) {
                        intent = new Intent(NotificationBox.this, whirity404.arknights_cn.BulletinListActivity.class);
                    } else if (host.contains("gryphline.com")) {
                        intent = new Intent(NotificationBox.this, whirity404.arknights_tw.BulletinListActivity.class);
                    } else if (host.contains("arknights.jp")) {
                        intent = new Intent(NotificationBox.this, whirity404.arknights_jp.BulletinListActivity.class);
                    } else if (host.contains("arknights.global")) {
                        intent = new Intent(NotificationBox.this, whirity404.arknights_en.BulletinListActivity.class);
                    } else if (host.contains("arknights.kr")) {
                        intent = new Intent(NotificationBox.this, whirity404.arknights_kr.BulletinListActivity.class);
                    } else {
                        intent = new Intent(NotificationBox.this, whirity404.arknights.notice.MainActivity.class);
                    }

                    startActivity(intent);
                }
            });
    }

    private void loadAnnouncements() {
        // Load announcements from cache or database
        // For example:
        announcements.add(new HashMap<String, String>() {{
                    put("title", getString(R.string.upd_CHN));
                    put("host", "hypergryph.com");
                }});
        announcements.add(new HashMap<String, String>() {{
                    put("title", getString(R.string.upd_CTW));
                    put("host", "gryphline.com");
                }});
        announcements.add(new HashMap<String, String>() {{
                    put("title", getString(R.string.upd_jp));
                    put("host", "arknights.jp");
                }});
        announcements.add(new HashMap<String, String>() {{
                    put("title", getString(R.string.upd_en));
                    put("host", "arknights.global");
                }});
        announcements.add(new HashMap<String, String>() {{
                    put("title", getString(R.string.upd_kr));
                    put("host", "arknights.kr");
                }});
    }
}

