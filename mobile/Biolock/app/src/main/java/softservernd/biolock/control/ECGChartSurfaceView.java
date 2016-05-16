package softservernd.biolock.control;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 # Created By: omatv@softserveinc.com
 # Maintained By: tshchyb@softserveinc.com
 */
public class ECGChartSurfaceView extends GLSurfaceView {

    private ECGChartRenderer mChartRenderer;
    private float[] mDataPoints;
    private boolean mIsUpdating = false;

    public ECGChartSurfaceView(Context context) {
        super(context);
    }
    public ECGChartSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRGB(float[] rgb) {
        mChartRenderer.setChartColor(rgb);
    }

    public void initializeWithSignalSize(int ECGSignalSize) {
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.setZOrderOnTop(true); //necessary
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // Set the Renderer for drawing on the GLSurfaceView
        mChartRenderer = new ECGChartRenderer(ECGSignalSize);
        mDataPoints = new float[ECGSignalSize];
        setRenderer(mChartRenderer);

        setChartData(mDataPoints);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        new Thread(new Task()).start();
    }

    public void setChartData(float[] datapoints) {
        if (datapoints.length > 0) {
            mIsUpdating = true;
            mDataPoints = datapoints.clone();
            float mMaxValue = getMax(datapoints);
            float mMinValue = getMin(datapoints);
            for (int i = 0; i < mDataPoints.length; i++) {
                mDataPoints[i] = (((datapoints[i] - mMinValue) * (1.0f - (-1.0f)) / (mMaxValue - mMinValue)) + (-1));
            }
            mIsUpdating = false;
        }
    }

    private class Task implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (!mIsUpdating) {
                    mChartRenderer.setChartData(mDataPoints);
                    requestRender();
                }
            }
        }
    }

    private float getMax(float[] array) {
        if (array.length > 0) {
            float max = array[0];
            for (int i = 1; i < array.length; i++) {
                if (array[i] > max) {
                    max = array[i];
                }
            }
            return max;
        } else {
            return 0f;
        }
    }

    private float getMin(float[] array) {
        if (array.length > 0) {
            float min = array[0];
            for (int i = 1; i < array.length; i++) {
                if (array[i] < min) {
                    min = array[i];
                }
            }
            return min;
        } else {
            return 0f;
        }
    }
}
