package com.android.sensormanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Button mBtnNet;
    private Button mBtnWifi;
    private ToggleButton mBtnStatus;
    private TextView mTvStatus;
    WifiManager systemService;
    private Button mBtnNotify;
    private SensorManager sensorManager;
    private Button mBtnSensor;
    private ImageView mImage;


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
        String CHANNEL_ID = "test";
        //适配8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "测试消息",
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
                .setContentTitle("通知标题")
                .setContentText("通知内容")
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

        }
    }


}