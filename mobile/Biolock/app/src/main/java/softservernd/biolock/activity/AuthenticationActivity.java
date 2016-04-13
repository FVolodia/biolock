package softservernd.biolock.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import softservernd.biolock.CustomApplication;
import softservernd.biolock.R;
import softservernd.biolock.control.ECGChartSurfaceView;
import softservernd.biolock.delegate.OnECGBluetoothManagerListener;
import softservernd.biolock.dnn.ECGClassifier;
import softservernd.biolock.ecgtools.ECGTools;
import softservernd.biolock.tools.CSVFile;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 * # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 * # Created By: omatv@softserveinc.com
 * # Maintained By: tshchyb@softserveinc.com
 */
public class AuthenticationActivity extends AppCompatActivity
        implements View.OnClickListener, OnECGBluetoothManagerListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "VisualizationActivity";

    private ArrayAdapter<String> mFoundDeviceAdapter;

    private TextView mHeartRateTextView;
    private ArrayList<String> mFoundDeviceArrayList;

    private static ECGChartSurfaceView mEcgChart;

    private FileOutputStream mStreamECG;

    private ArrayList<BluetoothDevice> mBTDeviceList = new ArrayList<>();

    private int mHeartRate = -1;

    private Button mAuthenticateButton;

    private ProgressBar mProgress;

    private Handler mHandler = new Handler();
    private ECGClassifier mClassifier = new ECGClassifier();
    private float[] mBuffer = null;

    private int mDelay = 500; //milliseconds
    private int mProgressCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        mProgress = (ProgressBar) findViewById(R.id.progress);

        mEcgChart = (ECGChartSurfaceView) findViewById(R.id.ecgChartViewSurfaceView);
        mEcgChart.initializeWithSignalSize(1024);

        ((CustomApplication) getApplication()).setCurrentActivity(this);

        mHeartRateTextView = (TextView) findViewById(R.id.heartRateTextView);

        ImageView heartImageView = (ImageView) findViewById(R.id.heartAnimationECGImageView);
        ((AnimationDrawable) heartImageView.getBackground()).start();

        // Quick permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");

            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }

        mAuthenticateButton = (Button) findViewById(R.id.authenticationButton);
        mAuthenticateButton.setOnClickListener(this);

        String[] layerList = loadLayersNames();
        mClassifier.load(getBaseContext(), layerList);
    }

    private String[] loadLayersNames() {
        try {
            String layerFolder = "layers";
            String[] fileNames = getBaseContext().getAssets().list(layerFolder);
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = layerFolder + "/" + fileNames[i];
            }
            return fileNames;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Turn ON BlueTooth if it is OFF
        if (!CustomApplication.getInstance().getBluetoothManager().isEnabled())
            CustomApplication.getInstance().getBluetoothManager().enable(REQUEST_ENABLE_BT);
//        TODO: if you want to write binary data to file, init file and add data from Device.
//        initFiles();
        setup();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                setup();
            } else {
                Toast.makeText(this, "Bluetooth not enabled!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateHeartRate() {
        final Handler h = new Handler();
        final int delay = 1000; //milliseconds

        h.postDelayed(new Runnable() {
            public void run() {
                mHeartRateTextView.setText(String.valueOf(mHeartRate));
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    @Override
    public void onDeviceDiscoveryStarted() {

    }

    @Override
    public void onDeviceDiscoveryFinished() {

    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        if (device.getName() != null) {
            mFoundDeviceArrayList.add(device.getName());
            mBTDeviceList.add(device);
            mFoundDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeviceConnected(BluetoothSocket socket) {
        updateHeartRate();
    }

    @Override
    public void onNewECGData(float[] data) {
        mEcgChart.setChartData(data);
        mBuffer = data.clone();
    }

    @Override
    public void onNewHeartRate(int heartRate) {
        Log.d(TAG, "heartRate = " + heartRate);
        mHeartRate = heartRate;
    }

    private void setup() {
        CustomApplication.getInstance().getBluetoothManager().startDiscovery();

        //let's make a broadcast receiver to register our thing
        mFoundDeviceArrayList = new ArrayList<>();
        mFoundDeviceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mFoundDeviceArrayList);

        CustomApplication.getInstance().getBluetoothManager().createDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((CustomApplication) getApplication()).setCurrentActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void closeFile() {
        try {
            if (mStreamECG != null) mStreamECG.close();
        } catch (IOException e) {
//            Log.e(TAG, "Error during closing stream. " + e);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                initializeSnackBar(v);
                break;
            case R.id.authenticationButton:
                Toast.makeText(AuthenticationActivity.this, "Authentication started!", Toast.LENGTH_SHORT).show();
                authenticateUser();
                break;
        }
    }

    private void initializeSnackBar(View view) {
        final Snackbar snackbar = Snackbar.make(view, "Record user ECG data for ECG authentication!", Snackbar.LENGTH_LONG);
        snackbar.setAction("Enrol", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: add custom logic for record
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private void authenticateUser() {

        mHandler.postDelayed(new Runnable() {
            public void run() {
                //do something
                if (mProgressCount < 20) {
                    Log.e("asd", String.valueOf(mProgressCount));
                    mProgress.setVisibility(View.VISIBLE);
                    mAuthenticateButton.setVisibility(View.GONE);
                    mProgressCount++;
                    mProgress.setProgress(mProgressCount);
                    mHandler.postDelayed(this, mDelay);

                    if (mBuffer != null) {
                        float[] signal = ECGTools.getFilteredSignal(mBuffer);
                        if (signal != null) {
                            int result = mClassifier.predict(signal);
                            if (result == 1) {
                                mHandler.removeCallbacksAndMessages(null);
                                Intent i = new Intent(AuthenticationActivity.this, ECGVisualizationActivity.class);
                                startActivity(i);
                                Log.i(TAG, "LOGGED IN");
                            }
                        }
                    }
                } else {
                    mProgressCount = 0;
                    mProgress.setVisibility(View.GONE);
                    mAuthenticateButton.setVisibility(View.VISIBLE);
                    Toast.makeText(AuthenticationActivity.this, "Your ECG is not authorized please try again!", Toast.LENGTH_LONG).show();
                }
            }
        }, mDelay);
    }

    private float[] readSignalFromFIle(Context context, String fileName) throws Exception {
        InputStream stream = context.getAssets().open(fileName);
        CSVFile cvsFile = new CSVFile(stream);
        ArrayList rows = cvsFile.readLines();
        float[] signal = new float[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            String[] items = (String[]) rows.get(i);
            signal[i] = Float.parseFloat(items[0]);
        }
        return signal;
    }
}
