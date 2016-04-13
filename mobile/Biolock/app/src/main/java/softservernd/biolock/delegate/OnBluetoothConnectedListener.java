package softservernd.biolock.delegate;

import android.bluetooth.BluetoothSocket;

/**
 * Created by Oleksandr Matviishyn on 4/10/16.
 */
public interface OnBluetoothConnectedListener {
    void onBluetoothConnected(BluetoothSocket bluetoothSocket);
}
