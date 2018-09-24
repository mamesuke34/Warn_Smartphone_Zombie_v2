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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;


public class MainActivity extends Activity implements Runnable, SensorEventListener, OnClickListener {

    SensorManager sm;
    TextView tv;
    Handler h;
    float gx, gy, gz;
    static double accel;//合成加速度
    int flag = 0;
    int count = 0;
    TextView status_view;
    TextView message;

    Instances instances;
    Classifier classifier;

    private final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
    private Button Study_Mode;
    private Button START;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.accel_value);

        h = new Handler();
        h.postDelayed(this, 0);

        START = findViewById(R.id.start);
        START.setOnClickListener(this);

        Study_Mode = findViewById(R.id.study_mode);
        Study_Mode.setOnClickListener(this);

        message = findViewById(R.id.status2);
        message.setText("あなたの動きを学習して、歩きスマホかどうか判定します。<STUDY MODE>から学習を開始してください。");

    }

    //ボタンクリック
    public void onClick(View v) {
        if (v == START){
            flag = 1;

            //WEKAの学習データと分類機を生成
            ConverterUtils.DataSource source = null;
            try {
                source = new ConverterUtils.DataSource("/storage/emulated/0/smartphonezombie/weka.arff");
                instances = source.getDataSet();
                instances.setClassIndex(1); //setClassIndexは分類したい属性の番号（ここではstateを判別するので1）
                classifier = new J48(); //J48の分類器を指定
                classifier.buildClassifier(instances);
            } catch (Exception e) {
                e.printStackTrace();
            }
            weka();
        }else if( v == Study_Mode){
            Intent intent = new Intent(getApplication(), StudyActivity.class);
            startActivity(intent);
        }
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
    public void run() {
    }

    //x,y,z,合成の加速度を取得
    @Override
    public void onSensorChanged(SensorEvent event) {
        count++;
        gx = event.values[0];
        gy = event.values[1];
        gz = event.values[2];
        accel = Math.sqrt((gx*gx) + (gy*gy) + (gz*gz));
        if( flag == 1 && count%5 == 0 ){
            weka();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void weka() {
        try {
            //評価
            Evaluation eval = new Evaluation(instances);
            eval.evaluateModel(classifier, instances);

            //事例データ
            FastVector out = new FastVector(2);
            out.addElement("standing");
            out.addElement("walking");
            out.addElement("running");
            Attribute acceleration = new Attribute("acceleration", 0);
            Attribute state = new Attribute("state", out, 1);
            FastVector win = new FastVector(2);

            //データセット指定
            Instance instance = new DenseInstance(3);
            instance.setValue(acceleration, accel);
            instance.setDataset(instances);

            double result = classifier.classifyInstance(instance);
            //System.out.println("wekaで結果出ました:");
            //System.out.println(result);

            status_view = findViewById(R.id.status);
            if (result == 0.0){
                //standing
                status_view.setText("You are Standing. | "+result);
                final ImageView image = findViewById(R.id.status_image);
                image.setImageResource(R.drawable.asset1);

            } else if (result == 1.0){
                //walking
                status_view.setText("You are Walking. | "+result);
                final ImageView image = findViewById(R.id.status_image);
                image.setImageResource(R.drawable.asset3);
            } else if (result == 2.0){
                //running
                status_view.setText("You are Running. | "+result);
                final ImageView image = findViewById(R.id.status_image);
                image.setImageResource(R.drawable.asset5);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}