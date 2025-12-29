package whirity404.arknights.notice;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Prite extends Service {

    private Dialog customDialog;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showCustomDialog();
        return START_NOT_STICKY;
    }

    private void showCustomDialog() {
        // 创建自定义 Dialog
        customDialog = new Dialog(this);
        customDialog.setContentView(R.layout.dialog_interface_prite);
        customDialog.setTitle("还记得我吗？");

        // 获取布局中的控件
        ImageView imageView = customDialog.findViewById(R.id.dialog_image);
        Button buttonOk = customDialog.findViewById(R.id.dialog_button_ok);

        // 设置图片（这里假设你有一个图片资源 ic_launcher_foreground）
        imageView.setImageResource(R.drawable.a);

        // 设置按钮点击事件
        buttonOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                    stopSelf(); // 停止服务
                }
            });

        // 显示 Dialog
        customDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
        }
    }
}

