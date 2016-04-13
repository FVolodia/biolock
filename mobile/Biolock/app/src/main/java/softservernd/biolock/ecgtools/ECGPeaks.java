package softservernd.biolock.ecgtools;

/**
 * Created by tarasshchybovyk on 4/12/16.
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