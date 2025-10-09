package whirity404.arknights.notice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class StartupReceiver extends BroadcastReceiver {

    private Context contexu;
    @Override
    public void onReceive(Context context, Intent intent) {
        Uri data = intent.getData();
        if (data != null && "arknights".equals(data.getScheme())) {
            // 创建要启动的目标Intent
            Intent targetIntent = new Intent();

            // 设置要启动的应用程序的主类（完整类名）
            targetIntent.setClassName("com.hypergryph.arknights", "com.u8.sdk.U8UnityContext");

            // 添加FLAG_ACTIVITY_NEW_TASK标志，因为从BroadcastReceiver启动Activity需要这个标志
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                context.startActivity(targetIntent);
                Toast.makeText(contexu, "启动", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "无法启动: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
