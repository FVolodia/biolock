package softservernd.biolock.delegate;

/**
 * Created by Oleksandr Matviishyn on 4/10/16.
 */
public interface OnBluetoothDataReceiver {
    void OnSetECGData(float[] data);

    void OnSetHeartRate(int heartRate);
}
