package sensor.shibataku.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.view.View;

import android.widget.Button;

import android.widget.TextView;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.IOException;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;


import weka.core.DenseInstance;
import weka.core.Instances;


import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.converters.ConverterUtils.DataSource;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView textView,textInfo,text;
    private float sensorX;
    private float sensorY;
    private float sensorZ;
    private float sensorXYZ;
    private  int count=0;

    private String fileName1 = "walk.csv";
    private String fileName2 = "stand.csv";
    private String fileName3 = "run.csv";
    private String fileName4 = "acceleration.arff";
    private Button buttonSave1,button_reset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        textView = (TextView) findViewById(R.id.text_view);

        textInfo = (TextView) findViewById(R.id.textInfo);

        text = (TextView) findViewById(R.id.text);

        buttonSave1 = (Button) findViewById(R.id.button_save1);

        button_reset =(Button) findViewById(R.id.button_reset);


        button_reset.setOnClickListener(new View.OnClickListener() {
                                            @Override

                                            public void onClick(View v) {
                        count=0;
                                                textInfo.setText("RESET");
                                                text.setText("");

                                            }
                                        });



        buttonSave1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // エディットテキストのテキストを取得


                if (count == 0) {
                    String text = "walk,"+(float) (Math.sqrt(sensorX * sensorX + sensorY * sensorY + sensorZ * sensorZ)) + "\n";;
                    saveFile(fileName1, text);
                    textInfo.setText("Saving_walk");

                }

                if (count == 1) {

                    textInfo.setText("Saved_walk\n next stand ");

                }

                if (count == 2) {
                    String text = "stand,"+ (float) (Math.sqrt(sensorX * sensorX + sensorY * sensorY + sensorZ * sensorZ)) + "\n";;
                    saveFile(fileName2, text);
                    textInfo.setText("Saving_stand");

                }

                if (count == 3) {

                    textInfo.setText("Saved_stand\n next run");


                }

                if (count == 4) {
                    String text = "run,"+(float) (Math.sqrt(sensorX * sensorX + sensorY * sensorY + sensorZ * sensorZ)) + "\n";;
                    saveFile(fileName3, text);
                    textInfo.setText("Saving_run");

                }

                if (count == 5) {

                    textInfo.setText("Saved_run\n finished");

                }


                if (count == 6) {

                    textInfo.setText("writing");
                    String text = "@RELATION acceleration\n\n" +
                            "@ATTRIBUTE state {walk,stand,run}\n" +
                            "@ATTRIBUTE value real\n\n" +
                            "@DATA\n";
                    saveFile(fileName4, text);

                    try {

                        FileInputStream fileInputStream;
                       fileInputStream = openFileInput("walk.csv");
                       byte[] readBytes = new byte[fileInputStream.available()];
                          fileInputStream.read(readBytes);
                          String readString = new String(readBytes);
                        saveFile2(fileName4,readString);

                        fileInputStream.close();

                        fileInputStream = openFileInput("stand.csv");
                        byte[] readBytes2 = new byte[fileInputStream.available()];
                        fileInputStream.read(readBytes2);
                        String readString2 = new String(readBytes2);
                        saveFile2(fileName4,readString2);

                        fileInputStream.close();


                        fileInputStream = openFileInput("run.csv");
                        byte[] readBytes3 = new byte[fileInputStream.available()];
                        fileInputStream.read(readBytes3);
                        String readString3 = new String(readBytes3);
                        saveFile2(fileName4,readString3);

                        fileInputStream.close();




                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    textInfo.setText("writing_finished");

                }




                count++;
            }

        });
    }

        @Override
        protected void onResume(){
            super.onResume();
            Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        }


            @Override
            protected void onPause() {
                super.onPause();
                sensorManager.unregisterListener(this);
            }


            @Override
            public void onSensorChanged(SensorEvent event) {

                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    sensorX = event.values[0];
                    sensorY = event.values[1];
                    sensorZ = event.values[2];
                    sensorXYZ = (float) Math.sqrt(sensorX * sensorX + sensorY * sensorY + sensorZ * sensorZ);

                    String strTmp = "加速度センサー\n"
                            + " X: " + sensorX + "\n"
                            + " Y: " + sensorY + "\n"
                            + " Z: " + sensorZ + "\n"
                            + " SUM" + sensorXYZ;
                    textView.setText(strTmp);


                    if (count == 1) {
                        String text = "walk," + sensorXYZ + "\n";
                        saveFile2(fileName1, text);


                    }

                    if (count == 3) {


                        String text = "stand," + sensorXYZ + "\n";
                        saveFile2(fileName2, text);


                    }

                    if (count == 5) {
                        String text = "run," + sensorXYZ + "\n";
                        saveFile2(fileName3, text);

                    }

                    if (count >= 7) {


                        textInfo.setText("weka_run");


                        try {


                            DataSource source = new DataSource("/data/data/sensor.shibataku.myapplication/files/acceleration.arff");
                            Instances instances = source.getDataSet();
                            instances.setClassIndex(0);
                            J48 j48 = new J48();
                            j48.buildClassifier(instances);


                            Evaluation eval = new Evaluation(instances);
                            eval.evaluateModel(j48, instances);
                            //   textView.setText(eval.toSummaryString());
                            System.out.println(eval.toSummaryString());


                            textInfo.setText("weka_finish");


                            FastVector out = new FastVector(3);
                            out.addElement("walk");
                            out.addElement("stand");
                            out.addElement("run");
                            Attribute state = new Attribute("state", out, 0);
                            Attribute value = new Attribute("value", 1);


                            Instance instance = new DenseInstance(2);
                            instance.setDataset(instances);
                            instance.setValue(value, sensorXYZ);

                            double result = j48.classifyInstance(instance);

                            System.out.println(result);


                            if (result == 0) {
                                text.setText("WALKING");

                            }

                            if (result == 1) {
                                text.setText("STANDING");

                            }

                            if (result == 2) {
                                text.setText("RUNNING");

                            }


                        } catch (Exception e) {
                            e.printStackTrace();

                        }


                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }


            // ファイルを保存
            public void saveFile(String file, String str) {
                FileOutputStream fileOutputstream = null;

                try {
                    fileOutputstream = openFileOutput(file, Context.MODE_PRIVATE);
                    fileOutputstream.write(str.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }


            public void saveFile2(String file, String str) {
                FileOutputStream fileOutputstream = null;

                try {
                    fileOutputstream = openFileOutput(file, Context.MODE_APPEND);
                    fileOutputstream.write(str.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }





            }

        }




