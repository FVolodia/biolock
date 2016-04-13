package softservernd.biolock;

import android.app.Activity;
import android.app.Application;

import softservernd.biolock.device.ECGBluetoothManager;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 * # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 * # Created By: omatv@softserveinc.com
 * # Maintained By: tshchyb@softserveinc.com
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

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
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
