package softservernd.biolock.delegate;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public interface OnECGBluetoothManagerListener {
    void onDeviceDiscoveryStarted();
    void onDeviceDiscoveryFinished();
    void onDeviceFound(BluetoothDevice device);
    void onDeviceConnected(BluetoothSocket socket);
    void onNewECGData(float[] data);
    void onNewHeartRate(int heartRate);
}
