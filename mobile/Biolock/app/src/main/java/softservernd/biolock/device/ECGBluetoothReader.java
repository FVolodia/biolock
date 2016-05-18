package softservernd.biolock.device;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import softservernd.biolock.delegate.OnBluetoothDataReceiver;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 # Created By: omatv@softserveinc.com
 # Maintained By: tshchyb@softserveinc.com
 */
public class ECGBluetoothReader {

    private static final String TAG = "ECGBluetoothReader";
    private static final short SYNC_START = (short) 0xaaaa;
    private static final int BUFFER_SIZE = 1024*2;

    private OnBluetoothDataReceiver mDelegate;

    private ScheduledFuture mExecutorTask = null;
    private ScheduledExecutorService mExecutor;

    private double[] mEcgBuffer;

    private InputStream mStream;

    private byte[] mSyncStart = new byte[100];
    private byte[] mPayloadLength = new byte[1];
    private byte[] mData = new byte[256];
    private byte[] mSyncEnd = new byte[1];

    private float[] mECGData = new float[BUFFER_SIZE];
    private float[] mHRVData = new float[64];

    ByteBuffer syncStartBuffer = ByteBuffer.wrap(mSyncStart).order(ByteOrder.LITTLE_ENDIAN);
    ByteBuffer payloadLengthBuffer = ByteBuffer.wrap(mPayloadLength).order(ByteOrder.LITTLE_ENDIAN);
    ByteBuffer syncEndBuffer = ByteBuffer.wrap(mSyncEnd).order(ByteOrder.LITTLE_ENDIAN);

    public ECGBluetoothReader(InputStream stream, OnBluetoothDataReceiver delegate) {
        super();
        mDelegate = delegate;
        mStream = stream;
        mExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void readPackage(InputStream stream) throws IOException {
        syncStartBuffer.clear();
        syncEndBuffer.clear();
        payloadLengthBuffer.clear();
        readFromStream(stream, mSyncStart, 2);
        syncStartBuffer.position(2);
        while (syncStartBuffer.position() < mSyncStart.length - 1 && syncStartBuffer.getShort(syncStartBuffer.position() - 2) != SYNC_START) {
            syncStartBuffer.put((byte) stream.read());
        }
        readFromStream(stream, mPayloadLength, 1);
        int length = (int) payloadLengthBuffer.get(0);
        if (length < 0) {
            Log.e(TAG, "length < 0");
            return;
        }
        readFromStream(stream, mData, length);

        ByteBuffer dataBuffer = ByteBuffer.wrap(mData, 0, length).order(ByteOrder.BIG_ENDIAN);

        readFromStream(stream, mSyncEnd, 1);
        byte crc = (byte) (~sumByteArray(mData, length) & 0xFF);
        if (syncEndBuffer.get(0) != crc) {
            Log.e(TAG, String.format("Checksum if wrong. Actually/Calculated = %02X / %02X. Length = %d", syncEndBuffer.get(0), crc, length));
            return;
        }
        int excodeCount = 1;
        while (dataBuffer.hasRemaining()) {
            byte code;
            while ((code = dataBuffer.get()) == 0x55) {
                excodeCount++;
            }
            parseDataRow(dataBuffer, code);
        }

    }

    private void parseDataRow(ByteBuffer dataBuffer, byte code) {
        switch (code) {
            case 0x02:
                int signalQualityValue = (int) dataBuffer.get();
                if (signalQualityValue == 0) {
                    Log.d(TAG, "quality: sensor off");
                } else if (signalQualityValue == 200) {
                    Log.d(TAG, "quality: sensor on");
                }
                break;
            case 0x03:
                int heartRate = (int) dataBuffer.get();
//                Log.d(TAG, "heartRate = " + heartRate);
                System.arraycopy(mHRVData, 1, mHRVData, 0, 63);
                mHRVData[63] = (float) heartRate;
//                mDelegate.OnSetHRVData(mHRVData);
                mDelegate.onSetHeartRate(heartRate);

                break;
            case (byte) 0x80:
                int length = (int) dataBuffer.get();
                int ecgValue = (int) dataBuffer.getShort();
                Log.d(TAG, "ecg = " + ecgValue);
                System.arraycopy(mECGData, 1, mECGData, 0, BUFFER_SIZE-1);
                mECGData[BUFFER_SIZE-1] = (float) ecgValue * -1;
                mDelegate.onSetECGData(mECGData);
                break;
            default:
                Log.d(TAG, "buffer" + Arrays.toString(dataBuffer.array()));
                Log.d(TAG, "code = " + code);
                if (code < 0) {
                    int packageLength = (int) dataBuffer.get();
                    Log.d(TAG, "len = " + packageLength);
                    dataBuffer.position(dataBuffer.position() + packageLength);
                } else {
                    dataBuffer.get();
                }
                break;
        }
    }

    static void readFromStream(InputStream stream, byte[] buffer, int length) throws IOException {
        int read = 0;
        while (read < length) {
            int n = stream.read(buffer, read, length - read);
            if (n < 0) throw new IOException("Stream ended");
            read += n;
        }
    }

    public static byte sumByteArray(byte[] bytes, int length) {
        byte sum = 0;
        for (int i = 0; i < length; i++) {
            sum += bytes[i];
        }
        return sum;
    }

    public void start() {
        mExecutorTask = mExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    readPackage(mStream);
                } catch (IOException e) {
                    Log.e(TAG, "Fail read package from BT", e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }, 1, 1, TimeUnit.MILLISECONDS);
    }
}
