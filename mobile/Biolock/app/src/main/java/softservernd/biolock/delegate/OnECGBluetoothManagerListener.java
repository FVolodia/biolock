package softservernd.biolock.delegate;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 * # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 * # Created By: omatv@softserveinc.com
 * # Maintained By: tshchyb@softserveinc.com
 */
public interface OnECGBluetoothManagerListener {
    void onDeviceDiscoveryStarted();

    void onDeviceDiscoveryFinished();

    void onDeviceFound(BluetoothDevice device);

    void onDeviceConnected(BluetoothSocket socket);

    void onNewECGData(float[] data);

    void onNewHeartRate(int heartRate);
}
