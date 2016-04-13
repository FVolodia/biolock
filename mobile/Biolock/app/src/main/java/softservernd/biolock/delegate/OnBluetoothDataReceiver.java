package softservernd.biolock.delegate;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 # Created By: omatv@softserveinc.com
 # Maintained By: tshchyb@softserveinc.com
 */
public interface OnBluetoothDataReceiver {
    void onSetECGData(float[] data);

    void onSetHeartRate(int heartRate);
}
