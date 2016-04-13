package softservernd.biolock.device;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import softservernd.biolock.CustomApplication;
import softservernd.biolock.R;
import softservernd.biolock.delegate.OnBluetoothConnectedListener;
import softservernd.biolock.delegate.OnBluetoothDataReceiver;
import softservernd.biolock.delegate.OnECGBluetoothManagerListener;


/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 * # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 * # Created By: omatv@softserveinc.com
 * # Maintained By: tshchyb@softserveinc.com
 */
public class ECGBluetoothManager implements OnBluetoothConnectedListener, OnBluetoothDataReceiver {
    private static final String TAG = "ECGBluetoothManager";
    private final String BT_MODULE_NAME = "sichiray";
    private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private final IntentFilter mFilter = new IntentFilter();
    private ArrayList<String> mFoundDeviceArrayList = new ArrayList<>();
    private ArrayList<BluetoothDevice> mBTDeviceList = new ArrayList<>();
    private ArrayAdapter<String> mFoundDeviceAdapter;

    public ECGBluetoothManager() {
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();

        mFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    public void registerReceiver() {
        CustomApplication.getInstance().getCurrentActivity().registerReceiver(mReceiver, mFilter);
        mFoundDeviceAdapter = new ArrayAdapter<>(CustomApplication.getInstance().getCurrentActivity(),
                android.R.layout.simple_list_item_1, mFoundDeviceArrayList);
    }

    @Override
    protected void finalize() throws Throwable {
        CustomApplication.getInstance().getCurrentActivity().unregisterReceiver(mReceiver);
        super.finalize();
    }

    public boolean isEnabled() {
        return mAdapter.isEnabled();
    }

    public boolean enable(int requestCode) {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        CustomApplication.getInstance().getCurrentActivity().startActivityForResult(enableIntent, requestCode);
        return true;
    }

    public void startDiscovery() {
        if (isDiscovering())
            stopDiscovery();
        mAdapter.startDiscovery();
    }

    public void stopDiscovery() {
        mAdapter.cancelDiscovery();
    }

    public boolean isDiscovering() {
        return mAdapter.isDiscovering();
    }

    public boolean isDesiredDeviceName(String deviceName) {
        return deviceName.endsWith(BT_MODULE_NAME);
    }

    public void connectToDevice(Context context, BluetoothDevice device, int attempts) {
        ConnectBTDeviceTask task = new ConnectBTDeviceTask(context, device, this, attempts);
        task.execute();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                ((OnECGBluetoothManagerListener) CustomApplication.getInstance().getCurrentActivity()).onDeviceDiscoveryStarted();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                ((OnECGBluetoothManagerListener) CustomApplication.getInstance().getCurrentActivity()).onDeviceDiscoveryFinished();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                ((OnECGBluetoothManagerListener) CustomApplication.getInstance().getCurrentActivity()).onDeviceFound(device);
                if (device.getName() != null) {
                    mFoundDeviceArrayList.add(device.getName());
                    mBTDeviceList.add(device);
                    mFoundDeviceAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public void onBluetoothConnected(BluetoothSocket bluetoothSocket) {
        try {
            InputStream input = bluetoothSocket.getInputStream();
            ECGBluetoothReader device = new ECGBluetoothReader(input, this);
            device.start();
            ((OnECGBluetoothManagerListener) CustomApplication.getInstance().getCurrentActivity()).onDeviceConnected(bluetoothSocket);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onSetECGData(float[] data) {
        ((OnECGBluetoothManagerListener) CustomApplication.getInstance().getCurrentActivity()).onNewECGData(data);
    }

    @Override
    public void onSetHeartRate(int heartRate) {
        ((OnECGBluetoothManagerListener) CustomApplication.getInstance().getCurrentActivity()).onNewHeartRate(heartRate);
    }

    public void createDialog() {
        final Activity currentActivity = CustomApplication.getInstance().getCurrentActivity();
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(currentActivity);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setCancelable(false);
        builderSingle.setTitle(R.string.selectECGDevice);

        builderSingle.setNegativeButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                mFoundDeviceAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final BluetoothDevice device = mBTDeviceList.get(which);

                        if (CustomApplication.getInstance().getBluetoothManager().isDesiredDeviceName(device.getName())) {
                            CustomApplication.getInstance().getBluetoothManager().stopDiscovery();
                            CustomApplication.getInstance().getBluetoothManager().connectToDevice(currentActivity, device, 5);
                        } else {
                            Toast.makeText(currentActivity,
                                    "Selected Device [" + device.getName() + "] not compatible with this software.",
                                    Toast.LENGTH_SHORT).show();
                            builderSingle.show();
                        }
                    }
                });
        builderSingle.show();
    }
}
