package whirity404.arknights.notice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import whirity404.arknights_cn.BulletinListActivity;
import whirity404.arknights.notice.models.BulletinItem;
import whirity404.arknights.notice.utils.HttpUtil;
import whirity404.arknights.notice.utils.JsonUtil;

public class BulletinCheckService extends Service {
    private static final String TAG = "BulletinCheckService";
    private static final String CHANNEL_ID = "BulletinChannel";
    private static final int NOTIFICATION_ID = 1;

    private List<BulletinItem> cachedItems = new ArrayList<>();
    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        // Create notification channel for Android Oreo and above
        createNotificationChannel();

        // Start the timer to check for updates every 3 hours
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForUpdates();
            }
        }, 0, 10800000); // 10800000 milliseconds = 3 hours

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    String.valueOf(R.string.notification_Name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void checkForUpdates() {
        List<String> urls = new ArrayList<>();
        urls.add("https://ak-webview.hypergryph.com/api/game/bulletinList?target=Android");
        urls.add("https://ak-webview.arknights.global/api/game/bulletinList?target=Android");
        urls.add("https://ak-webview.arknights.jp/api/game/bulletinList?target=Android");
        urls.add("https://ak-webview.arknights.kr/api/game/bulletinList?target=Android");
        urls.add("https://ak-webview-tw.gryphline.com/api/game/bulletinList?target=Android");

        for (String url : urls) {
            try {
                String json = HttpUtil.get(url);
                List<BulletinItem> newItems = JsonUtil.parseBulletinList(json);

                if (newItems != null) {
                    List<String> newAnnouncements = getNewAnnouncements(cachedItems, newItems);
                    if (!newAnnouncements.isEmpty()) {
                        sendNotification(newAnnouncements, url);
                    }
                    cachedItems = newItems; // Update cached items
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching updates: " + e.getMessage());
            }
        }
    }

    private List<String> getNewAnnouncements(List<BulletinItem> cachedItems, List<BulletinItem> newItems) {
        List<String> newAnnouncements = new ArrayList<>();
        for (BulletinItem newItem : newItems) {
            boolean isNew = true;
            for (BulletinItem cachedItem : cachedItems) {
                if (cachedItem.getCid().equals(newItem.getCid())) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                newAnnouncements.add(newItem.getTitle().substring(0, Math.min(30, newItem.getTitle().length())));
            }
        }
        return newAnnouncements;
    }
    
    /*

    private void sendNotification(List<String> announcements, String url) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent;
        if (url.contains("hypergryph.com")) {
            intent = new Intent(this, whirity404.arknights_cn.BulletinListActivity.class);
        } else if (url.contains("gryphline.com")) {
            intent = new Intent(this, whirity404.arknights_tw.BulletinListActivity.class);
        } else if (url.contains("arknights.jp")) {
            intent = new Intent(this, whirity404.arknights_jp.BulletinListActivity.class);
        } else if (url.contains("arknights.global")) {
            intent = new Intent(this, whirity404.arknights_en.BulletinListActivity.class);
        } else if (url.contains("arknights.kr")) {
            intent = new Intent(this, whirity404.arknights_kr.BulletinListActivity.class);
        } else {
            intent = new Intent(this, whirity404.arknights.notice.MainActivity.class);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Announcements")
                .setContentText(String.join(", ", announcements))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(NOTIFICATION_ID, builder.build());
    }
    
    */
    
    private void sendNotification(List<String> announcements, String url) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, NotificationBox.class);
        intent.putExtra("announcements", announcements.toArray(new String[0]));
        intent.putExtra("url", url);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            //.setContentTitle("New Announcements")
            .setContentTitle(String.valueOf(R.string.upd_Title))
            .setContentText(String.join(", ", announcements))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        manager.notify(NOTIFICATION_ID, builder.build());
    }
    

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        Log.d(TAG, "Service stopped");
    }
}

