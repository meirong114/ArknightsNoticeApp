package whirity404.arknights.notice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private long firstBackTime;
    private static final String BROWSER_SWITCH_FILE = ".openInBrowser.switch";
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
                        Intent jumpIntent = new Intent(MainActivity.this,SplashActivity.class);
                        startActivity(jumpIntent);
                        finish();
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
        new UpdateChecker(MainActivity.this).checkForUpdate();
        setContentView(R.layout.activity_main);
        
        checkBrowserSetting();

        // 简中服按钮
        Button btnCn = (Button) findViewById(R.id.btn_cn);
        btnCn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBulletinListActivity("cn");
            }
        });

        // 繁中服按钮
        Button btnTw = (Button) findViewById(R.id.btn_tw);
        btnTw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBulletinListActivity("tw");
            }
        });

        // 日服按钮
        Button btnJp = (Button) findViewById(R.id.btn_jp);
        btnJp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBulletinListActivity("jp");
            }
        });

        // 国际服按钮
        Button btnEn = (Button) findViewById(R.id.btn_en);
        btnEn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBulletinListActivity("en");
            }
        });

        // 韩服按钮
        Button btnKr = (Button) findViewById(R.id.btn_kr);
        btnKr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBulletinListActivity("kr");
            }
        });
    }
    
    private void checkBrowserSetting() {
        String browserSetting = readBrowserSetting();
        
        // 如果文件不存在、为空或为"0"，则显示选择对话框
        if (browserSetting == null || browserSetting.isEmpty() || "0".equals(browserSetting)) {
            showBrowserChoiceDialog();
        }
        // 如果已经是"1"或"2"，则不提醒
    }

    /**
     * 显示浏览器选择对话框
     */
    private void showBrowserChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择浏览器打开方式");
        builder.setMessage("请选择默认的链接打开方式：");
        
        builder.setPositiveButton("内置浏览器", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 写入"1"表示内置浏览器
                writeBrowserSetting("1");
                dialog.dismiss();
            }
        });
        
        builder.setNegativeButton("系统浏览器", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 写入"2"表示系统浏览器
                writeBrowserSetting("2");
                dialog.dismiss();
            }
        });
        
        builder.setCancelable(false); // 禁止点击外部取消
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 读取浏览器设置
     */
    private String readBrowserSetting() {
        File file = new File(getFilesDir(), BROWSER_SWITCH_FILE);
        if (!file.exists()) {
            return "0"; // 文件不存在，返回0
        }
        
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            try {
                fis.close();
            } catch (IOException e) {}
            return new String(data).trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "0";
        }
    }

    /**
     * 写入浏览器设置
     */
    private void writeBrowserSetting(String setting) {
        try {
            FileOutputStream fos = openFileOutput(BROWSER_SWITCH_FILE, MODE_PRIVATE);
            fos.write(setting.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startBulletinListActivity(String languageCode) {
        try {
            // 使用您预留的语言代码格式
            String packageName = "whirity404.arknights_" + languageCode;
            String className = packageName + ".BulletinListActivity";
            
            Class<?> clazz = Class.forName(className);
            Intent intent = new Intent(this, clazz);
            
            // 可以传递语言代码给目标Activity
            intent.putExtra("language_code", languageCode);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            // 处理找不到类的情况，可以显示Toast提示用户
        }
    }
}
