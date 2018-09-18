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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class StudyActivity extends Activity implements Runnable, SensorEventListener, OnClickListener {

    SensorManager sm;
    TextView tv;
    Handler h;
    float gx, gy, gz;
    double accel;
    TextView message2;

    boolean d_flag = false;

    private static final int REQUEST_EXTERNAL_STORAGE_CODE = 0x01;
    private static String[] mPermissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    String csv_stand = "/storage/emulated/0/smartphonezombie/stand.csv";
    String csv_walk = "/storage/emulated/0/smartphonezombie/walking.csv";
    String csv_run = "/storage/emulated/0/smartphonezombie/running.csv";
    String arfffile = "/storage/emulated/0/smartphonezombie/weka.arff";

    private final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
    boolean stand = false;
    boolean walking = false;
    boolean running = false;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button Finish;
    private Button Delete_Data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_study);

        tv = (TextView)findViewById(R.id.accel_value);

        h = new Handler();
        h.postDelayed(this, 0);

        button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(this);

        button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(this);

        button3 = (Button)findViewById(R.id.button3);
        button3.setOnClickListener(this);

        Finish = (Button)findViewById(R.id.finish);
        Finish.setOnClickListener(this);

        Delete_Data = (Button)findViewById(R.id.delete_data);
        Delete_Data.setOnClickListener(this);

        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/smartphonezombie/");
        dir.mkdir(); //ディレクトリ生成
        System.out.println("ディレクトリを生成しました");

        message2= (TextView)findViewById(R.id.message2);
        message2.setText("立ち状態・歩き状態・走り状態のあなたの行動を学習します。" +
                "3つの状態で<MEASURE NOW>から10秒以上測定を行ってください。" +
                "3状態の学習が完了したら<FINISH STUDY>を押して学習完了です。");
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
        }else if (v == Delete_Data){
            //ファイル削除
            File file1 = new File("/storage/emulated/0/smartphonezombie/stand.csv");
            file1.delete();
            File file2 = new File("/storage/emulated/0/smartphonezombie/walking.csv");
            file2.delete();
            File file3 = new File("/storage/emulated/0/smartphonezombie/running.csv");
            file3.delete();
            File file4 = new File("/storage/emulated/0/smartphonezombie/weka.arff");
            file4.delete();
            new AlertDialog.Builder(this)
                    .setMessage("Complete Deleting.")
                    .setPositiveButton("Close", null)
                    .show();
        }else if (v == Finish){
            makearff();
            finish();
        }
    }

    //ARFFファイル生成
    public void makearff () {
        System.out.println("ARFFファイルを生成します");
        FileOutputStream files;
        FileInputStream fileInputStream;
        String text_stand = null;
        String text_walk = null;
        String text_run = null;
        try {
            files = new FileOutputStream(new File("/storage/emulated/0/smartphonezombie/weka.arff"));
            // @relation "データセットの名前"
            // @attribute 属性名を列挙（名義属性は{,,}、数値属性realは実数、integerは整数）
            // @data ここ以降がCSVデータ
            String write = ("@relation file\n\n"+


                    "@attribute acceleration real\n"+
                    "@attribute state {standing,walking,running}\n\n"+

                    "@data\n");
            files.write(write.getBytes());
            System.out.println("ARFFファイル生成完了");

            //stand.csv
            try {
                fileInputStream = new FileInputStream(new File("/storage/emulated/0/smartphonezombie/stand.csv"));
                byte[] readBytes = new byte[fileInputStream.available()];
                fileInputStream.read(readBytes);
                text_stand = new String(readBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            files.write(text_stand.getBytes());
            System.out.println("stand.csv出力完了");

            //walikng.csv
            try {
                fileInputStream = new FileInputStream(new File("/storage/emulated/0/smartphonezombie/walking.csv"));
                byte[] readBytes = new byte[fileInputStream.available()];
                fileInputStream.read(readBytes);
                text_walk = new String(readBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            files.write(text_walk.getBytes());
            System.out.println("walking.csv出力完了");

            //running.csv
            try {
                fileInputStream = new FileInputStream(new File("/storage/emulated/0/smartphonezombie/running.csv"));
                byte[] readBytes = new byte[fileInputStream.available()];
                fileInputStream.read(readBytes);
                text_run = new String(readBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            files.write(text_run.getBytes());
            System.out.println("running.csv出力完了");

            files.flush();
            files.close();
            System.out.println("ARFFファイルへの書き込みが完了しました");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //ファイルの書き込みパーミッション許可・ディレクトリ生成
    private static void verifyStoragePermissions(Activity activity) {
        int readPermission = ContextCompat.checkSelfPermission(activity, mPermissions[0]);
        int writePermission = ContextCompat.checkSelfPermission(activity, mPermissions[1]);
        if (writePermission != PackageManager.PERMISSION_GRANTED ||
                readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, mPermissions, REQUEST_EXTERNAL_STORAGE_CODE);
        }
    }

    //ファイルを保存
    public void saveFile(String file, Double str, String status) {
        verifyStoragePermissions(this);

        try {
            FileWriter fw = new FileWriter(Environment.getExternalStorageDirectory().getPath()+"/smartphonezombie/"+file, true);
            fw.write(String.valueOf(str));
            fw.write(", ");
            fw.write(status);
            fw.write("\n");
            fw.close();
            System.out.println("CSVファイルの出力が完了しました");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("CSVファイルの出力に失敗しました");
        }
    }

    //現在の加速度を表示
    @Override
    public void run() {
        tv.setText("X-axis : " + gx + "\n"
                + "Y-axis : " + gy + "\n"
                + "Z-axis : " + gz + "\n"
                + "acceleration : " + accel + "\n");
        h.postDelayed(this, 0);
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
                saveFile("stand.csv", accel, "standing");
            }
        }else if (walking == true){
            if (d_flag == true) {
                saveFile("walking.csv", accel, "walking");
            }
        }else if (running == true){
            if (d_flag == true) {
                saveFile("running.csv", accel, "running");
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
        builder = new AlertDialog.Builder(StudyActivity.this);
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
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                // キャンセルされたときの処理
                d_flag = false;
                stand = false;
                walking = false;
                running = false;
            }});
        builder.show();
    }
}