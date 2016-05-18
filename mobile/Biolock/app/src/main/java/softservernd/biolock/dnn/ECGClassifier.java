package softservernd.biolock.dnn;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import softservernd.biolock.tools.CSVFileReader;


/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 * # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 * # Created By: omatv@softserveinc.com
 * # Maintained By: tshchyb@softserveinc.com
 */
public class ECGClassifier {

    private float[][][] mLayerWeights = null;

    public void load(Context context, String[] layerFiles) {
        // Allocate memory n layers
        mLayerWeights = new float[layerFiles.length][][];
        int layerIndex = 0;
        for (String file : layerFiles) {
            try {
                InputStream stream = context.getAssets().open(file);
                CSVFileReader cvsFile = new CSVFileReader(stream);
                ArrayList rows = cvsFile.readLines();

                // Create matrix for layer weights
                int columnNumber = ((String[]) rows.get(0)).length;
                float[][] matrix = new float[rows.size()][columnNumber];

                // Fill matrix
                for (int i = 0; i < rows.size(); ++i) {
                    String[] columns = (String[]) rows.get(i);
                    for (int j = 0; j < columns.length; ++j) {
                        matrix[i][j] = Float.parseFloat(columns[j]);
                    }
                }

                // Store weights
                mLayerWeights[layerIndex++] = matrix;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int predict(float[] input) {
        // Check if model have enough layers
        if (input.length <= 1)
            throw new RuntimeException("Not enough layers to work with (should be more than 1).");

        float[] y = input.clone();

        // process input data with n-1 layers, last layer will be used differently
        for (int i = 0; i < mLayerWeights.length - 1; i++) {
            float[] yDot = multiply(addBias(y), mLayerWeights[i]);
            float[] yRELU = activationRELU(yDot);

            y = new float[yRELU.length];
            System.arraycopy(yRELU, 0, y, 0, yRELU.length);
        }

        // process data with last layer
        float[] yDot = multiply(addBias(y), mLayerWeights[mLayerWeights.length - 1]);
        float[] ySoftMax = activationSoftMax(yDot);

        Log.e("SOFTMAX", "" + ySoftMax[0] + ", " + ySoftMax[1]);

        if (ySoftMax[1] < 0.999)
            ySoftMax[1] = 0;

        Log.e("SOFTMAX after", "" + ySoftMax[0] + ", " + ySoftMax[1]);

        return argMax(ySoftMax);
    }

    private static float[] addBias(float[] x) {
        float bias = 1.0f;
        // Create array with place for bias
        float[] out = new float[x.length + 1];
        System.arraycopy(x, 0, out, 1, x.length);

        // Set bias to the beginning of array
        out[0] = bias;
        return out;
    }

    // vector-matrix multiplication y = x*A
    private static float[] multiply(float[] x, float[][] A) {
        int rows = A.length;
        int columns = A[0].length;
        if (x.length != rows)
            throw new RuntimeException("Illegal matrix dimensions.");
        float[] c = new float[columns];
        for (int i = 0; i < columns; i++) {
            for (int k = 0; k < rows; k++) {
                c[i] += x[k] * A[k][i];
            }
        }
        return c;
    }

    private static float[] activationRELU(float[] x) {
        for (int i = 0; i < x.length; i++) {
            if (x[i] < 0) x[i] = 0;
        }
        return x;
    }

    private static float[] activationSoftMax(float[] x) {
        float[] res = new float[x.length];
        float sum = 0;
        for (int i = 0; i < res.length; i++) {
            res[i] = (float) Math.exp(x[i] - max(x));
            sum += res[i];
        }
        for (int i = 0; i < res.length; i++) {
            res[i] /= sum;
        }
        return res;
    }

    private static float max(float[] x) {
        int indexOfMaximum = argMax(x);
        return x[indexOfMaximum];
    }

    private static int argMax(float[] x) {
        int index = 0;
        float maxValue = x[index];

        for (int i = 0; i < x.length; i++) {
            if (x[i] > maxValue) {
                maxValue = x[i];
                index = i;
            }
        }
        return index;
    }
}
