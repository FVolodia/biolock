package softservernd.biolock.device;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

import softservernd.biolock.delegate.OnBluetoothConnectedListener;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 * # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 * # Created By: omatv@softserveinc.com
 * # Maintained By: tshchyb@softserveinc.com
 */
public class ConnectBTDeviceTask extends AsyncTask<Void, Integer, Boolean> {

    private final BluetoothDevice mBluetoothDevice;
    private int mAttempts;

    private UUID mUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    private Context mContext;
    private BluetoothSocket mBluetoothSocket;
    private OnBluetoothConnectedListener mDelegate;
    private ProgressDialog mProgressDialog;

    public ConnectBTDeviceTask(Context context, BluetoothDevice device, OnBluetoothConnectedListener listener, int mAttempts) {
        mContext = context;
        mDelegate = listener;
        //using the well-known SPP UUID
        mUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
        this.mAttempts = mAttempts;
        mBluetoothDevice = device;
        try {
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(mUUID);
            Log.d("ConnectBTDeviceTask", "Bluetooth socket: " + mBluetoothSocket);
        } catch (IOException e) {
            Log.e("ConnectBTDeviceTask", "error during creating bluetooth socket", e);
        }
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle("Connecting to " + mBluetoothDevice.getName());
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    protected void closeBluetoothSocket() {
        if (mBluetoothSocket.isConnected()) {
            try {
                mBluetoothSocket.close();
            } catch (IOException e1) {
                Log.e("ConnectBTDeviceTask", "Error during closing bluetooth socket. ", e1);
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mProgressDialog.setMessage("Attempt " + values[0] + " of 5. Processing...");
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean isConnected = false;
        int attempt = 1;
        do {
            try {
                publishProgress(attempt);
                mBluetoothSocket.connect();
                isConnected = true;
                break;
            } catch (IOException e) {
                Log.e("ConnectBTDeviceTask", "Can't connect to device " + mBluetoothDevice.getName(), e);
                closeBluetoothSocket();
                attempt++;
            }
        } while (attempt < mAttempts);
        return isConnected;
    }

    @Override
    protected void onPostExecute(Boolean isConnected) {
        super.onPostExecute(isConnected);
        mProgressDialog.dismiss();
        if (isConnected) {
            Toast.makeText(mContext,
                    "Successfully connected to " + mBluetoothDevice.getName(),
                    Toast.LENGTH_SHORT).show();
            Log.d("ConnectBTDeviceTask", "Connected to bluetooth device: " + mBluetoothDevice.getName());
            mDelegate.onBluetoothConnected(mBluetoothSocket);
        } else {
            Toast.makeText(mContext,
                    "Failed to connect to " + mBluetoothDevice.getName(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
