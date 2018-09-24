package com.example.kan.warn_smartphone_zombie;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;



public class MainActivity extends Activity implements Runnable, SensorEventListener, OnClickListener {

    SensorManager sm;
    TextView tv;
    Handler h;
    float gx, gy, gz;
    double accel;

    boolean d_flag = false;

    private static final int REQUEST_EXTERNAL_STORAGE_CODE = 0x01;
    private static String[] mPermissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
    boolean stand = false;
    boolean walking = false;
    boolean running = false;
    private Button button1;
    private Button button2;
    private Button button3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tv = (TextView)findViewById(R.id.accel_value);

        h = new Handler();
        h.postDelayed(this, 500);

        button1 = (Button)findViewById(R.id.button1);
        button1.setText("stand");
        button1.setOnClickListener(this);

        button2 = (Button)findViewById(R.id.button2);
        button2.setText("walking");
        button2.setOnClickListener(this);

        button3 = (Button)findViewById(R.id.button3);
        button3.setText("running");
        button3.setOnClickListener(this);
    }

    //3状態のボタン
    public void onClick(View v) {
        if (v == button1){
            stand = true;
            Dialog();
        }else if (v == button2){
            walking = true;
            Dialog();
        }else if (v == button3){
            running = true;
            Dialog();
        }
    }

    //ファイルの書き込みパーミッション許可
    private static void verifyStoragePermissions(Activity activity) {
        int readPermission = ContextCompat.checkSelfPermission(activity, mPermissions[0]);
        int writePermission = ContextCompat.checkSelfPermission(activity, mPermissions[1]);

        if (writePermission != PackageManager.PERMISSION_GRANTED ||
                readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, mPermissions, REQUEST_EXTERNAL_STORAGE_CODE);
        }
    }

    //ファイルを保存
    public void saveFile(String file, Double str) {
        verifyStoragePermissions(this);
        try {
            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath()+"/smartphonezombie/"+file, true);
            fw.write(String.valueOf(str));
            fw.write("\n");
            fw.close();
            System.out.println("CSVファイルの出力が完了しました");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("CSVファイルの出力に失敗しました");
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

    //x,y,z,合成の加速度を取得
    //3状態学習のボタンを押したら、ファイル書き込みを呼び出し
    @Override
    public void onSensorChanged(SensorEvent event) {
        gx = event.values[0];
        gy = event.values[1];
        gz = event.values[2];
        accel = Math.sqrt((gx*gx) + (gy*gy) + (gz*gz));

        if (stand == true){
            if (d_flag == true) {
                saveFile("stand.csv", accel);
            }
        }else if (walking == true){
            if (d_flag == true) {
                saveFile("walking.csv", accel);
            }
        }else if (running == true){
            if (d_flag == true) {
                saveFile("running.csv", accel);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    //ダイアログの表示
    public void Dialog () {
        d_flag = true;
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Measuring Now");
        builder.setMessage("Please Wait 10 Seconds.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // OK button pressed
                d_flag = false;
                stand = false;
                walking = false;
                running = false;
                }});
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                d_flag = false;
                stand = false;
                walking = false;
                running = false;
                }});
        builder.show();
    }
}