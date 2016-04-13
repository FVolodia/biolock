package softservernd.biolock.ecgtools;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 # Created By: omatv@softserveinc.com
 # Maintained By: tshchyb@softserveinc.com
 */
public class ECGPeaks {
    private float[] peakValues = null;
    private int[] peaksIndexes = null;

    public ECGPeaks(float[] peakValues, int[] maxInd) {
        this.peakValues = peakValues;
        this.peaksIndexes = maxInd;
    }

    public float[] getPeakValues() {
        return peakValues;
    }

    public int[] getPeaksIndexes() {
        return peaksIndexes;
    }
}