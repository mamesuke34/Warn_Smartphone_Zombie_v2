package com.example.kan.warn_smartphone_zombie;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import android.os.Environment;
import android.view.View.OnClickListener;
import java.util.List;


public class MainActivity extends Activity implements Runnable, SensorEventListener {

    SensorManager sm;
    TextView tv;
    Handler h;
    float gx, gy, gz;
    double accel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LinearLayout ll;
        ll = new LinearLayout(this);
        setContentView(ll);

        tv = new TextView(this);
        ll.addView(tv);

        h = new Handler();
        h.postDelayed(this, 500);

        try {
            //出力先を作成する
            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath()+"/test.csv", false);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
            //内容を指定する
            pw.print("running");
            pw.print(",");
            pw.print(accel);
            pw.println();
            //ファイルに書き出す
            pw.close();
        } catch (IOException ex) {
            //例外時処理
            ex.printStackTrace();
        }
    }

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