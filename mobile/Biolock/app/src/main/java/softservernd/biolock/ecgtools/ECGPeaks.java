package softservernd.biolock.ecgtools;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 * # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 * # Created By: omatv@softserveinc.com
 * # Maintained By: tshchyb@softserveinc.com
 */
public class ECGPeaks {
    private float[] mPeakValues = null;
    private int[] mPeaksIndexes = null;

    public ECGPeaks(float[] peakValues, int[] maxInd) {
        this.mPeakValues = peakValues;
        this.mPeaksIndexes = maxInd;
    }

    public float[] getPeakValues() {
        return mPeakValues;
    }

    public int[] getPeaksIndexes() {
        return mPeaksIndexes;
    }
}