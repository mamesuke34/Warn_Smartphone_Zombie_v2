package com.example.kan.warn_smartphone_zombie;


import android.app.Activity;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/*
 @relation "データセットの名前"
 @attribute 属性名を列挙（名義属性は{,,}、数値属性realは実数、integerは整数）
 @data ここ以降がCSVデータ

 String write = ("@relation file\n\n"+
                    "@attribute acceleration real\n"+
                    "@attribute state {standing,walking,running}\n\n"+
                    "@data\n");
 */

public abstract class weka extends Activity implements SensorEventListener, View.OnClickListener {

  float gx, gy, gz;
  public static  double accel;

  @Override
  public void onSensorChanged(SensorEvent event) {
    gx = event.values[0];
    gy = event.values[1];
    gz = event.values[2];
    accel = Math.sqrt((gx*gx) + (gy*gy) + (gz*gz));
  }

  public static void main(String[] args) {

      try {
        //学習データと分類機を生成
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("weka.arff");
        Instances instances = source.getDataSet();
        instances.setClassIndex(2); //setClassIndexは分類したい属性の番号（ここではstateを判別するので2）
        Classifier classifier = new J48(); //J48の分類器を指定
        classifier.buildClassifier(instances);

        //評価
        Evaluation eval = new Evaluation(instances);
        eval.evaluateModel(classifier, instances);
        System.out.println(eval.toSummaryString());

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
        System.out.println(result);

        if (result == 0.0){
          //running

        } else if (result == 1.0){
          //walking

        } else if (result == 2.0){
          //standing

        }


      } catch (Exception e) {
        e.printStackTrace();
      }
  }

}


