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
 * Created by Oleksandr Matviishyn on 4/12/16.
 */
public class ConnectBTDeviceTask extends AsyncTask<Void, Integer, Boolean> {

    private final BluetoothDevice bluetoothDevice;
    private int attempts;

    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    private Context mContext;
    private BluetoothSocket mBluetoothSocket;
    private OnBluetoothConnectedListener delegate;

    private ProgressDialog mProgressDialog;

    public ConnectBTDeviceTask(Context context, BluetoothDevice device, OnBluetoothConnectedListener listener, int attempts) {
        mContext = context;
        delegate = listener;
        //using the well-known SPP UUID
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);
        this.attempts = attempts;
        bluetoothDevice = device;
        try {
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
            Log.d("ConnectBTDeviceTask", "Bluetooth socket: " + mBluetoothSocket);
        } catch (IOException e) {
            Log.e("ConnectBTDeviceTask", "error during creating bluetooth socket", e);
        }
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle("Connecting to " + bluetoothDevice.getName());
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
                Log.e("ConnectBTDeviceTask", "Can't connect to device " + bluetoothDevice.getName(), e);
                closeBluetoothSocket();
                attempt++;
            }
        } while (attempt < attempts);
        return isConnected;
    }

    @Override
    protected void onPostExecute(Boolean isConnected) {
        super.onPostExecute(isConnected);
        mProgressDialog.dismiss();
        if (isConnected) {
            Toast.makeText(mContext,
                    "Successfully connected to " + bluetoothDevice.getName(),
                    Toast.LENGTH_SHORT).show();
            Log.d("ConnectBTDeviceTask", "Connected to bluetooth device: " + bluetoothDevice.getName());
            delegate.onBluetoothConnected(mBluetoothSocket);
        } else {
            Toast.makeText(mContext,
                    "Failed to connect to " + bluetoothDevice.getName(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
