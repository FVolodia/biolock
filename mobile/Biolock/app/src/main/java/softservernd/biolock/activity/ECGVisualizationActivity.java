package softservernd.biolock.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import softservernd.biolock.CustomApplication;
import softservernd.biolock.R;
import softservernd.biolock.control.ECGChartSurfaceView;
import softservernd.biolock.delegate.OnECGBluetoothManagerListener;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 # Created By: omatv@softserveinc.com
 # Maintained By: tshchyb@softserveinc.com
 */
public class ECGVisualizationActivity extends AppCompatActivity
        implements OnECGBluetoothManagerListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "VisualizationActivity";

    private ArrayAdapter<String> mFoundDeviceAdapter;

    private TextView mHeartRateTextView;
    private ArrayList<String> mFoundDeviceArrayList;

    public static ECGChartSurfaceView ecgChart;

    private int mHeartRate = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgvisualization);

        ecgChart = (ECGChartSurfaceView) findViewById(R.id.ecgChartViewSurfaceView);
        ecgChart.initializeWithSignalSize(1024);

        mHeartRateTextView = (TextView) findViewById(R.id.heartRateTextView);

        ImageView heartImageView = (ImageView) findViewById(R.id.heartAnimationECGImageView);
        ((AnimationDrawable) heartImageView.getBackground()).start();

        // Quick permission check
        int permissionCheck = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");

            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }
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

        Toast.makeText(ECGVisualizationActivity.this, "Logged in as John Doe!", Toast.LENGTH_LONG).show();
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

    private void setup() {
        CustomApplication.getInstance().getBluetoothManager().startDiscovery();
        mFoundDeviceArrayList = new ArrayList<>();
        mFoundDeviceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mFoundDeviceArrayList);
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
            mFoundDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeviceConnected(BluetoothSocket socket) {
        updateHeartRate();
    }

    @Override
    public void onNewECGData(float[] data) {
        ecgChart.setChartData(data);
    }

    @Override
    public void onNewHeartRate(int heartRate) {
        Log.d(TAG, "heartRate = " + heartRate);
        mHeartRate = heartRate;
    }
}
