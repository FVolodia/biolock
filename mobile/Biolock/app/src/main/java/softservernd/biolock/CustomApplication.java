package softservernd.biolock;

import android.app.Activity;
import android.app.Application;

import softservernd.biolock.device.ECGBluetoothManager;

/**
 * Created by Oleksandr Matviishyn on 4/13/16.
 */
public class CustomApplication extends Application {

    private static CustomApplication sInstance;
    private ECGBluetoothManager mBluetoothManager;

    public static CustomApplication getInstance() {
        return sInstance;
    }

    private Activity mCurrentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mBluetoothManager = new ECGBluetoothManager();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity currentActivity){
        boolean registerReceiver = mCurrentActivity == null;
        mCurrentActivity = currentActivity;
        if (registerReceiver) {
            mBluetoothManager.registerReceiver();
        }
    }

    public ECGBluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }
}
