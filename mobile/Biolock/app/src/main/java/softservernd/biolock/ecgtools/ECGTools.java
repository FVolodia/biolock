package softservernd.biolock.ecgtools;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignExstrom;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 * # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 * # Created By: omatv@softserveinc.com
 * # Maintained By: tshchyb@softserveinc.com
 */

class Filter {
    public Filter() {
        double fs = 277;
        double fcf1 = 4.0/fs;
        double fcf2 = 35.0/fs;
        IirFilterCoefficients iirFilterCoefficients = IirFilterDesignExstrom.design(FilterPassType.bandpass, 4, fcf1, fcf2);
        filter = new IirFilter(iirFilterCoefficients);
    }

    IirFilter filter;
}

public class ECGTools {

    // Recommended ECG segment length with one R-peak
    public static int mSegmentLength = 270;
    // Length before R-peak that should be taken into ECG segment
    public static int mLengthFromBeginningToRPeak = 80;
    public static float mRPeakThreshold = 0.4f;

    public static Filter filter = new Filter();

    // kaiser FIR filter coefficients
    public static double[] b = {
            0.124843,
            0.129979,
            0.124843,
            0.110082,
            0.087538,
            0.059976,
            0.030676,
            0.002958,
            -0.020282,
            -0.036963,
            -0.046057,
            -0.047671,
            -0.042941,
            -0.033773,
            -0.022487,
            -0.011417,
            -0.002538,
            0.002815,
            0.004104,
            0.001597,
            -0.003767,
            -0.010608,
            -0.017404,
            -0.022777,
            -0.025748,
            -0.025877,
            -0.023310};


    public static float[] getFilteredSignal(float[] signal, int chunks) {
        float[] preprocessed = preprocess(signal);
        ECGPeaks peaks = detectRPeaks(preprocessed, mRPeakThreshold);
        List<float[]> segments = segment(preprocessed, peaks, mSegmentLength);
        return calculate(segments);
    }

    public static float[] iirFilter(float[] signal) {
        float[] filteredData = new float[signal.length];

        for (int i = 0; i < signal.length; ++i) {
            filteredData[i] = (float)filter.filter.step((double)signal[i]);
        }

        return filteredData;
    }


    public static float[] filter(float[] signal, double[] b) {
        float[] filterData = new float[signal.length];
        int sampleLength = signal.length;
        int filterLength = b.length;
        float[] temp = new float[sampleLength + filterLength];
        for (int i = 0; i < temp.length; i++) {
            if (i < filterLength) {
                temp[i] = signal[i];
            } else {
                temp[i] = signal[i - filterLength];
            }
        }
        for (int i = 1; i < sampleLength; i++) {
            for (int j = 0; j < filterLength; j++) {
                filterData[i] += temp[i - j + filterLength] * b[j];
            }
        }
        return filterData;
    }

    public static float[] normalize(float[] signal) {
        float gain = (getMax(signal) - getMin(signal)) / 2;
        float mean = 0;
        for (double d : signal) {
            mean += d;
        }
        mean /= signal.length;
        float[] normalized = new float[signal.length];
        for (int i = 0; i < signal.length; i++) {
            normalized[i] = (signal[i] - mean) / gain;
        }
        return normalized;
    }

    public static float[] preprocess(float[] signal) {
        float[] filtered = filter(signal, b);
        return normalize(filtered);
    }

    public static ECGPeaks detectRPeaks(float[] signal, float threshold) {
        float[] temp = signal.clone();
        // zero data, which is less than threshold
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] < threshold) {
                temp[i] = 0;
            }
        }

        // first index for each segment above threshold
        List<Integer> segmentFirst = new ArrayList<>();
        // last index for each segment above threshold
        List<Integer> segmentLast = new ArrayList<>();

        for (int i = 0; i < temp.length - 1; i++) {
            if (temp[i] == 0 && temp[i + 1] != 0) {
                segmentFirst.add(i);
            }
            if (temp[i] != 0 && temp[i + 1] == 0) {
                segmentLast.add(i);
            }
        }

        int nPeaks = Math.min(segmentFirst.size(), segmentLast.size());
        int nSamples = signal.length;

        float[] rPeakValues = new float[nPeaks];
        int[] rPeakIndexes = new int[nPeaks];

        // locals maximum search
        for (int i = 0; i < nPeaks; i++) {
            int indexFirst = segmentFirst.get(i);
            int indexLast = segmentLast.get(i);
            int[] segmentMask = new int[nSamples];
            for (int n = indexFirst; n <= indexLast; n++) {
                segmentMask[n] = 1;
            }
            float[] masked = setMask(signal, segmentMask);

            // R-peak value
            rPeakValues[i] = getMax(masked);
            rPeakIndexes[i] = getMaxIndex(masked);
        }

        return new ECGPeaks(rPeakValues, rPeakIndexes);
    }

    private static List<float[]> segment(float[] signal, ECGPeaks peaks, int segmentLength) {
        List<float[]> ecgSegment = new ArrayList<>();
        int[] peakIndexes = peaks.getPeaksIndexes();
        int[] peakDistances = getDiff(peakIndexes);

        for (int i = 0; i < peakIndexes.length - 1; i++) {
            int start = peakIndexes[i] - mLengthFromBeginningToRPeak;
            int stop = start + Math.min(peakDistances[i], segmentLength);
            if (start < 0) continue;

            ecgSegment.add(slice(signal, start, stop, segmentLength));
        }
        return ecgSegment;
    }

    private static float[] getSegmentMean(List<float[]> ecg) {
        if (ecg.size() == 0) return null;

        float[] resultArray = new float[ecg.get(0).length];
        Arrays.fill(resultArray, Float.MAX_VALUE);

        for (int j = 0; j < ecg.get(0).length; j++) {
            for (int i = 0; i < ecg.size(); i++) {
                if (ecg.get(i)[j] < resultArray[j])
                    resultArray[j] = ecg.get(i)[j];
            }
        }
        return resultArray;
    }

    private static float[] calculate(List<float[]> ecg) {

        float[] segmentMean = getSegmentMean(ecg);
        if (segmentMean == null) return null;

        float normGain = (getMax(segmentMean) - getMin(segmentMean)) / 2;
        for (int i = 0; i < segmentMean.length; i++) {
            segmentMean[i] /= normGain;
        }
        return segmentMean;
    }

    private static float getMin(float[] data) {
        float min = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] < min) {
                min = data[i];
            }
        }
        return min;
    }

    private static float getMax(float[] data) {
        float max = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] > max) {
                max = data[i];
            }
        }
        return max;
    }

    private static int getMaxIndex(float[] data) {
        int maxIndex = 0;
        float max = data[maxIndex];
        for (int i = maxIndex + 1; i < data.length; i++) {
            if (data[i] > max) {
                max = data[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private static float[] setMask(float[] data, int[] segmentMask) {
        float[] resultMask = new float[data.length];
        for (int i = 0; i < data.length; i++) {
            resultMask[i] = data[i] * segmentMask[i];
        }
        return resultMask;
    }

    private static int[] getDiff(int[] peakIndexes) {
        int[] diffResult = new int[peakIndexes.length - 1];
        for (int i = 0; i < peakIndexes.length - 1; i++) {
            diffResult[i] = peakIndexes[i + 1] - peakIndexes[i];
        }
        return diffResult;
    }

    private static float[] slice(float[] data, int start, int stop, int length) {
        float[] result = new float[length];
        for (int i = 0; i < result.length; i++) {
            if (i < stop - start) {
                result[i] = data[i + start];
            } else {
                result[i] = 0f;
            }
        }
        return result;
    }
}
