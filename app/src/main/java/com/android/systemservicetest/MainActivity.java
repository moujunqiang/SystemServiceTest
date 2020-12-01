package com.android.systemservicetest;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 网络状态
     */
    private Button mBtnNet;
    /**
     * WIFI状态
     */
    private Button mBtnWifi;
    private ToggleButton mBtnStatus;
    /**
     *
     */
    private TextView mTvStatus;
    WifiManager systemService;
    /**
     * 发送通知
     */
    private Button mBtnNotify;
    private SensorManager sensorManager;
    /**
     * 获取所有传感器列表
     */
    private Button mBtnSensor;
    private ImageView mImage;
    /**
     * 拍照
     */
    private Button mBtnPhoto;
    private static final int MY_ADD_CASE_CALL_PHONE = 6;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 检查wifi是否可用
     */
    public boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    private void initView() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        systemService = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mBtnNet = (Button) findViewById(R.id.btn_net);
        mBtnNet.setOnClickListener(this);
        mBtnWifi = (Button) findViewById(R.id.btn_wifi);
        mBtnWifi.setOnClickListener(this);
        mBtnStatus = (ToggleButton) findViewById(R.id.btn_status);
        mTvStatus = (TextView) findViewById(R.id.tv_status);
        mBtnStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {//android10之后 禁止直接打开关闭wifi，
                    startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
                } else {
                    if (isChecked) {
                        Toast.makeText(MainActivity.this, "it's on now", Toast.LENGTH_SHORT).show();
                        systemService.setWifiEnabled(false);
                    } else {
                        systemService.setWifiEnabled(true);

                        Toast.makeText(MainActivity.this, "it's off now", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
        mBtnNotify = (Button) findViewById(R.id.btn_notify);
        mBtnNotify.setOnClickListener(this);
        mBtnSensor = (Button) findViewById(R.id.btn_sensor);
        mBtnSensor.setOnClickListener(this);
        mImage = (ImageView) findViewById(R.id.image);
        mBtnPhoto = (Button) findViewById(R.id.btn_photo);
        mBtnPhoto.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //初始化按钮的状态
        mBtnStatus.setChecked(systemService.getWifiState() == WifiManager.WIFI_STATE_ENABLED);
    }

    /**
     * 获取所有的传感器列表
     */
    public void getAllSen() {
        List<Sensor> sensors = new ArrayList<>();
        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            Log.e("sensor", sensor.getName());
        }
        //获取传感器管理对象
        // 获取传感器的类型(TYPE_ACCELEROMETER:加速度传感器)
        Sensor mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(SensorListener, mSensor, SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消监听
        sensorManager.unregisterListener(SensorListener);
    }

    final SensorEventListener SensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {

            //当sensor的值发生变化的时候会触发这个回调方法
            StringBuilder sb = new StringBuilder();
            sb.append("X方向的加速度：");
            sb.append(sensorEvent.values[0]);
            sb.append("/nY方向的加速度：");
            sb.append(sensorEvent.values[1]);
            sb.append("/nZ方向的加速度：");
            sb.append(sensorEvent.values[2]);
            Log.e("sensor", sb.toString());
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //当精度变化的时候来处理
        }
    };

    /**
     * 发送通知
     */
    public void sendNotify() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "chat";
        //适配8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "聊天信息",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(this, MainActivity2.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        /**
         * 注意写上 channel_id，适配8.0，不用担心8.0以下的，找不到 channel_id 不影响程序
         */
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("这是标题")
                .setContentText("我是内容，我是demo")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        //通过 builder.build() 拿到 notification
        mNotificationManager.notify(1, mBuilder.build());
    }

    private static final int MY_ADD_CASE_CALL_PHONE2 = 7;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_net:
                if (isNetworkConnected(this)) {
                    mTvStatus.setText("网络可用");
                } else {
                    mTvStatus.setText("网络不可用");
                }
                break;
            case R.id.btn_wifi:
                if (isWifiConnected(this)) {
                    mTvStatus.setText("WIFI可用");
                } else {
                    mTvStatus.setText("WIFI不可用");
                }
                break;
            case R.id.btn_notify:
                sendNotify();
                break;
            case R.id.btn_sensor:
                getAllSen();
                break;
            case R.id.btn_photo:
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_ADD_CASE_CALL_PHONE);
                } else {
                    try {
                        //有权限,去打开摄像头
                        takePhoto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void takePhoto() throws IOException {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        // 获取文件
        File file = createFileIfNeed(System.currentTimeMillis()+".png");
        //拍照后原图回存入此路径下

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            uri = Uri.fromFile(file);
        } else {
            /**
             * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
             * 并且这样可以解决MIUI系统上拍照返回size为0的情况
             */
            uri = FileProvider.getUriForFile(this, "com.android.systemservicetest.provider", file);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 1);
    }

    // 在sd卡中创建一保存图片（原图和缩略图共用的）文件夹
    private File createFileIfNeed(String fileName) throws IOException {
        String fileA = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/nbinpic";
        File fileJA = new File(fileA);
        if (!fileJA.exists()) {
            fileJA.mkdirs();
        }
        File file = new File(fileA, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /**
     * 申请权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == MY_ADD_CASE_CALL_PHONE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    takePhoto();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //"权限拒绝");
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode != Activity.RESULT_CANCELED) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                mImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }
    }

}