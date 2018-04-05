package com.mantarakk.mphilproject;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.DataProducer;
import com.mbientlab.metawear.DataToken;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.data.AngularVelocity;
import com.mbientlab.metawear.data.MagneticField;
import com.mbientlab.metawear.data.Quaternion;
import com.mbientlab.metawear.module.GyroBmi160;
import com.mbientlab.metawear.module.Led;
import com.mbientlab.metawear.builder.filter.Comparison;
import com.mbientlab.metawear.builder.filter.ThresholdOutput;
import com.mbientlab.metawear.builder.function.Function1;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Debug;
import com.mbientlab.metawear.module.Logging;
import com.mbientlab.metawear.AsyncDataProducer;
import com.mbientlab.metawear.module.AccelerometerBmi160;
import com.mbientlab.metawear.module.AccelerometerBosch.NoMotionDataProducer;
import com.mbientlab.metawear.module.SensorFusionBosch;
import com.mbientlab.metawear.module.MagnetometerBmm150;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import android.os.Environment;
import android.widget.TextView;
import com.mbientlab.metawear.builder.RouteComponent.AccountType;
import com.mbientlab.metawear.module.Settings;

import android.os.Build;
import android.widget.Toast;


import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import bolts.Continuation;
import bolts.Task;

public class MainActivity extends AppCompatActivity implements ServiceConnection {


    private final String LIFT_SQUAT = "SQUAT";
    private final String LIFT_DEADLIFT = "DEADLIFT";
    private final String LIFT_BENCH = "BENCH";
    private final String LIFT_OHP = "OHP";
    private final String LIFT_SNATCH = "SNATCH";

    private final String PLACEMENT_WRIST = "WRIST";
    private final String PLACEMENT_BAR = "BAR";

    private String selectedPlacement;
    private String selectedLift;

    private float samplePeriod;

    private Timer timer = new Timer();

    private Switch accelToggle;
    private Switch gyroToggle;
    private Switch quaternionToggle;
    private Switch magToggle;

    private TextView accelRecived;
    private TextView gyroRecived;
    private TextView magRecived;
    private TextView quatRecived;

    private Spinner placement;
    private Spinner lift;

    private boolean recordAccel;
    private boolean recordGyro;
    private boolean recordQuant;
    private boolean recordMag;

    private static final String LOG_TAG = "freefall";

    private MetaWearBoard mwBoard;
    private AccelerometerBmi160  accelerometer;
    private Led led;
    private GyroBmi160 gyroscope;
    private MagnetometerBmm150 magnetometer;
    private Debug debug;
    private Logging logging;

    private ArrayList<Double> currentSessionTimes;
    private ArrayList<Double> currentSessionElapsedTimes;
    private LiftData currentSessionLiftData;
    private ArrayList<AccelData> currentSessionAccel;
    private ArrayList<GyroData> currentSessionGyro;
    private ArrayList<MagData> currentSessionMag;

    private long currentSessionStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getApplicationContext().bindService(new Intent(this, BtleService.class), this, Context.BIND_AUTO_CREATE);

        selectedLift = this.LIFT_SQUAT;
        selectedPlacement = this.PLACEMENT_WRIST;

        placement = findViewById(R.id.placement);
        ArrayAdapter<CharSequence> adapterplacement = ArrayAdapter.createFromResource(this,
                R.array.placements, android.R.layout.simple_spinner_item);
        adapterplacement.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        placement.setAdapter(adapterplacement);

        placement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = adapterView.getItemAtPosition(i).toString().toUpperCase();
                Log.i("MainActivty", selected);
                if(selected.equals(PLACEMENT_BAR)){
                    selectedPlacement = PLACEMENT_BAR;
                } else if(selected.equals(PLACEMENT_WRIST)) {
                    selectedPlacement = PLACEMENT_WRIST;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lift = findViewById(R.id.lift);
        ArrayAdapter<CharSequence> adapterlifts = ArrayAdapter.createFromResource(this,
                R.array.lifts, android.R.layout.simple_spinner_item);
        adapterlifts.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        lift.setAdapter(adapterlifts);

        lift.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = adapterView.getItemAtPosition(i).toString().toUpperCase();
                Log.i("MainActivty", selected);
                if(selected.equals(LIFT_BENCH)){
                    selectedLift = LIFT_BENCH;
                } else if(selected.equals(LIFT_DEADLIFT)) {
                    selectedLift = LIFT_DEADLIFT;
                } else if(selected.equals(LIFT_OHP)) {
                    selectedLift = LIFT_OHP;
                } else if(selected.equals(LIFT_SQUAT)) {
                    selectedLift = LIFT_SQUAT;
                } else if(selected.equals(LIFT_SNATCH)) {
                    selectedLift = LIFT_SNATCH;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        accelRecived = findViewById(R.id.accelRecieved);
        gyroRecived = findViewById(R.id.gyroRecieved);
        magRecived = findViewById(R.id.magRecieved);


        accelToggle = findViewById(R.id.accelToggle);
        if(accelToggle != null) {
            accelToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        //do stuff when Switch is ON
                        recordAccel = true;
                        Log.i(LOG_TAG, "Accel ON");
                    } else {
                        //do stuff when Switch if OFF
                        recordAccel = false;
                        Log.i(LOG_TAG, "Accel OFF");
                    }
                }
            });
        }

        gyroToggle = findViewById(R.id.gyroToggle);
        if(gyroToggle != null) {
            gyroToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        //do stuff when Switch is ON
                        recordGyro = true;
                        Log.i(LOG_TAG, "Gyro ON");
                    } else {
                        //do stuff when Switch if OFF
                        recordGyro = false;
                        Log.i(LOG_TAG, "Gyro OFF");
                    }
                }
            });
        }

        magToggle = findViewById(R.id.magToggle);
        if(magToggle != null) {
            magToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        //do stuff when Switch is ON
                        recordMag = true;
                        Log.i(LOG_TAG, "Mag ON");
                    } else {
                        //do stuff when Switch if OFF
                        recordMag = false;
                        Log.i(LOG_TAG, "Mag OFF");
                    }
                }
            });
        }


        findViewById(R.id.start).setOnClickListener(v -> {

            led.stop(true);
            currentSessionStartTime = Calendar.getInstance().getTimeInMillis();

            startStream();

        });
        findViewById(R.id.stop).setOnClickListener(v -> {

            endStream("tbd");

            Log.i(LOG_TAG, "Stopped Streaming");

        });
        findViewById(R.id.reset).setOnClickListener(v -> {
            mwBoard.tearDown();
            Log.i(LOG_TAG, "Tear Down Initiated");
            accelRecived.setText("0");
            gyroRecived.setText("0");
            quatRecived.setText("0");
            magRecived.setText("0");
        });


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        123);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        BtleService.LocalBinder serviceBinder = (BtleService.LocalBinder) service;

        String mwMacAddress= "F2:A3:66:B9:5E:16";   ///< Put your board's MAC address here
        BluetoothManager btManager= (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothDevice btDevice= btManager.getAdapter().getRemoteDevice(mwMacAddress);

        mwBoard= serviceBinder.getMetaWearBoard(btDevice);
        mwBoard.connectAsync().onSuccessTask(task -> {

            led = mwBoard.getModule(Led.class);
            led.editPattern(Led.Color.GREEN, Led.PatternPreset.BLINK)
                    .commit();
            led.play();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    led.stop(true);
                }
            }, 2*1000);

            return null;

        }).continueWith((Continuation<Object, Void>) task -> {
            if (task.isFaulted()) {
                Log.e(LOG_TAG, mwBoard.isConnected() ? "Error setting up route" : "Error connecting", task.getError());
            } else {
                Log.i(LOG_TAG, "Connected");
                debug = mwBoard.getModule(Debug.class);
                logging= mwBoard.getModule(Logging.class);
            }

            return null;
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    public void streamAccelData() {
        Log.i(LOG_TAG, "Streaming Accel");
        accelerometer.packedAcceleration().addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(new Subscriber() {
                    @Override
                    public void apply(Data data, Object... env) {
                        Log.i("MainActivity", "realtime: " + data.formattedTimestamp() + " Acceleration:" + data.value(Acceleration.class).toString());
                        currentSessionAccel.add(new AccelData(data.value(Acceleration.class)));
                        accelRecived.setText(Integer.toString(currentSessionAccel.size()));
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                accelerometer.packedAcceleration().start();
                accelerometer.start();
                return null;
            }
        });
    }

    public void streamGyroData() {
        Log.i(LOG_TAG, "Streaming Gyro");
        gyroscope.packedAngularVelocity().addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(new Subscriber() {
                    @Override
                    public void apply(Data data, Object... env) {
                        Log.i("MainActivity", "realtime: " + data.formattedTimestamp() +  data.value(AngularVelocity.class).toString());
                        currentSessionGyro.add(new GyroData(data.value(AngularVelocity.class)));
                        gyroRecived.setText(Integer.toString(currentSessionGyro.size()));
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                gyroscope.packedAngularVelocity().start();
                gyroscope.start();
                return null;
            }
        });
    }

    public void streamMagData() {
        Log.i(LOG_TAG, "Streaming Mag");
        magnetometer.packedMagneticField().addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(new Subscriber() {
                    @Override
                    public void apply(Data data, Object... env) {
                        Log.i("MainActivity", "realtime: " + data.formattedTimestamp() + data.value(MagneticField.class).toString());
                        currentSessionMag.add(new MagData(data.value(MagneticField.class)));
                        magRecived.setText(Integer.toString(currentSessionMag.size()));
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                magnetometer.packedMagneticField().start();
                magnetometer.start();
                return null;
            }
        });
    }

    public void startStream() {

        if(recordAccel) {
            accelerometer = mwBoard.getModule(AccelerometerBmi160.class);
            accelerometer.configure()
                    .odr(AccelerometerBmi160.OutputDataRate.ODR_25_HZ)
                    .commit();
            currentSessionAccel = new ArrayList<>();
            accelerometer.packedAcceleration().start();
            accelerometer.start();
            streamAccelData();
        }
        if(recordGyro) {
            gyroscope = mwBoard.getModule(GyroBmi160.class);
            gyroscope.configure()
                    .odr(GyroBmi160.OutputDataRate.ODR_25_HZ)
                    .range(GyroBmi160.Range.FSR_2000)
                    .commit();
            currentSessionGyro = new ArrayList<>();
            gyroscope.packedAngularVelocity().start();
            gyroscope.start();
            streamGyroData();
        }
        if(recordMag) {
            magnetometer = mwBoard.getModule(MagnetometerBmm150.class);
            magnetometer.configure().outputDataRate(MagnetometerBmm150.OutputDataRate.ODR_25_HZ).commit();
            currentSessionMag = new ArrayList<>();
            magnetometer.packedMagneticField().start();
            magnetometer.start();
            streamMagData();
        }

    }

    public void endStream(String form) {

        if(recordAccel) {
            accelerometer.stop();
            accelerometer.acceleration().stop();
        }
        if(recordGyro) {
            gyroscope.stop();
            gyroscope.angularVelocity().stop();
        }
        if(recordMag) {
            magnetometer.stop();
            magnetometer.magneticField().stop();
        }

        int totalSamples = 0;
        int totalMagSamples = currentSessionMag.size();
        int totalGyroSamples = currentSessionGyro.size();
        int totalAccelSamples = currentSessionAccel.size();

        if(totalMagSamples <= totalGyroSamples || totalMagSamples <= totalAccelSamples) {
            totalSamples = totalMagSamples;
        } else if(totalGyroSamples <= totalMagSamples || totalGyroSamples <= totalAccelSamples) {
            totalSamples = totalGyroSamples;
        } else if(totalAccelSamples <= totalMagSamples || totalAccelSamples <= totalGyroSamples) {
            totalSamples = totalAccelSamples;
        }

        float incremeant = 1 / accelerometer.getOdr();
        Log.i("MainActivty", Integer.toString(totalSamples));


        ArrayList<Double> elapsedTimes = new ArrayList<>();
        for(int i = 0; i < totalSamples; i++) {
            elapsedTimes.add(new Double((currentSessionStartTime + incremeant * i) * 1000));
        }

        currentSessionLiftData = new LiftData(selectedPlacement, selectedLift, form, elapsedTimes, currentSessionAccel, currentSessionGyro, currentSessionMag);


        createCSV(currentSessionLiftData, totalSamples);

    }

    public void createCSV(LiftData liftData, int totalSamples) {

        FileWriter writer;

        File root = Environment.getExternalStorageDirectory();
        File gpxfile = new File(root, "project/test.csv");

        try {

            writer = new FileWriter(gpxfile);

            writeCsvHeader(writer,"Elapsed Time","Accel X","Accel Y", "Accel Z", "Ang Vel X","Ang Vel Y", "Ang Z", "Mag X","Mag Y", "Mag Z");
            for(int i = 0 ; i < totalSamples; i++) {
                writeCsvData(writer, liftData.getElapsedTimes().get(i), liftData.getAccelData().get(i).getxVal(), liftData.getAccelData().get(i).getyVal(), liftData.getAccelData().get(i).getzVal(),
                        liftData.getGyroData().get(i).getxVal(), liftData.getGyroData().get(i).getyVal(), liftData.getGyroData().get(i).getzVal(),
                        liftData.getMagData().get(i).getxVal(),  liftData.getMagData().get(i).getyVal(), liftData.getGyroData().get(i).getzVal());
            }
            Toast toast = Toast.makeText(this.getApplicationContext(), "Saved CSV", Toast.LENGTH_SHORT);
            toast.show();

            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void writeCsvHeader(Writer writer, String times, String accelx, String accely, String accelz, String gyrox, String gyroy, String gyroz, String magx, String magy, String magz) throws IOException {
        String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", times, accelx, accely, accelz, gyrox, gyroy, gyroz, magx, magy, magz);
        writer.write(line);
    }


    private void writeCsvData(Writer writer, double times, float accelx, float accely, float accelz, float gyrox, float gyroy, float gyroz, float magx, float magy, float magz) throws IOException {
        String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", times, accelx, accely, accelz, gyrox, gyroy, gyroz, magx, magy, magz);
        writer.write(line);
    }

}
