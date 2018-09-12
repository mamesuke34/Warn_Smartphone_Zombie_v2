package com.example.kan.warn_smartphone_zombie;


import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;


public class SubActivity extends Activity implements Runnable, SensorEventListener, OnClickListener {

    SensorManager sm;
    TextView tv;
    Handler h;
    float gx, gy, gz;
    double accel;

    private final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
    private Button Re_Study;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tv = (TextView)findViewById(R.id.accel_value);

        h = new Handler();
        h.postDelayed(this, 500);

        Re_Study = (Button)findViewById(R.id.re_study);
        Re_Study.setOnClickListener(this);
    }

    //ボタンクリック
    public void onClick(View v) {
        if( v == Re_Study){
            Intent intent = new Intent(getApplication(), SubActivity.class);
            startActivity(intent);
        }
    }

    //現在の加速度を表示
    @Override
    public void run() {
        tv.setText("X-axis : " + gx + "\n"
                + "Y-axis : " + gy + "\n"
                + "Z-axis : " + gz + "\n"
                + "Synthetic acceleration : " + accel + "\n");
        h.postDelayed(this, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors =
                sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (0 < sensors.size()) {
            sm.registerListener(this, sensors.get(0),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        h.removeCallbacks(this);
    }

    //x,y,z,合成の加速度を取得
    @Override
    public void onSensorChanged(SensorEvent event) {
        gx = event.values[0];
        gy = event.values[1];
        gz = event.values[2];
        accel = Math.sqrt((gx*gx) + (gy*gy) + (gz*gz));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}