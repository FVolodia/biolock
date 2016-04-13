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
 * Created by tarasshchybovyk on 4/12/16.
 */
public class ECGBluetoothManager implements OnBluetoothConnectedListener, OnBluetoothDataReceiver {
    private static final String TAG = "ECGBluetoothManager";
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private final String BT_MODULE_NAME = "sichiray";
    private final IntentFilter filter = new IntentFilter();
    private ArrayList<String> foundDeviceArrayList = new ArrayList<>();
    private ArrayList<BluetoothDevice> btDeviceList = new ArrayList<>();
    private ArrayAdapter<String> foundDeviceAdapter;

    public ECGBluetoothManager() {
        this.adapter = BluetoothAdapter.getDefaultAdapter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    public void registerReceiver() {
        CustomApplication.getInstance().getCurrentActivity().registerReceiver(mReceiver, filter);
        foundDeviceAdapter = new ArrayAdapter<>(CustomApplication.getInstance().getCurrentActivity(),
                android.R.layout.simple_list_item_1, foundDeviceArrayList);
    }

    @Override
    protected void finalize() throws Throwable {
        CustomApplication.getInstance().getCurrentActivity().unregisterReceiver(mReceiver);
        super.finalize();
    }

    public boolean isEnabled() {
        return adapter.isEnabled();
    }

    public boolean enable(int requestCode) {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        CustomApplication.getInstance().getCurrentActivity().startActivityForResult(enableIntent, requestCode);
        return true;
    }

    public void startDiscovery() {
        if (isDiscovering())
            stopDiscovery();
        adapter.startDiscovery();
    }

    public void stopDiscovery() {
        adapter.cancelDiscovery();
    }

    public boolean isDiscovering() {
        return adapter.isDiscovering();
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
                ((OnECGBluetoothManagerListener)CustomApplication.getInstance().getCurrentActivity()).onDeviceDiscoveryStarted();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                ((OnECGBluetoothManagerListener)CustomApplication.getInstance().getCurrentActivity()).onDeviceDiscoveryFinished();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                ((OnECGBluetoothManagerListener)CustomApplication.getInstance().getCurrentActivity()).onDeviceFound(device);
                if (device.getName() != null) {
                    foundDeviceArrayList.add(device.getName());
                    btDeviceList.add(device);
                    foundDeviceAdapter.notifyDataSetChanged();
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
            ((OnECGBluetoothManagerListener)CustomApplication.getInstance().getCurrentActivity()).onDeviceConnected(bluetoothSocket);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void OnSetECGData(float[] data) {
        ((OnECGBluetoothManagerListener)CustomApplication.getInstance().getCurrentActivity()).onNewECGData(data);
    }

    @Override
    public void OnSetHeartRate(int heartRate) {
        ((OnECGBluetoothManagerListener)CustomApplication.getInstance().getCurrentActivity()).onNewHeartRate(heartRate);
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
                foundDeviceAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final BluetoothDevice device = btDeviceList.get(which);

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
