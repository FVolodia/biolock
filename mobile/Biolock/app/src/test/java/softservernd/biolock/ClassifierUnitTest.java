package softservernd.biolock;

import android.content.Context;
import android.test.InstrumentationTestCase;

import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;

import softservernd.biolock.tools.CSVFileReader;
import softservernd.biolock.dnn.ECGClassifier;
import softservernd.biolock.ecgtools.ECGTools;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ClassifierUnitTest extends InstrumentationTestCase {

    @Test
    public void testPrediction() throws Exception {
        Context context = getInstrumentation().getContext();

        String[] fileNames = {"layers/bias_weights_0.csv", "layers/bias_weights_1.csv", "layers/bias_weights_2.csv", "layers/bias_weights_3.csv"};
        ECGClassifier classifier = new ECGClassifier();
        classifier.load(context, fileNames);

        String fileName = "20160411_163402_ecg.csv";
        float[] signal = readSignal(context, fileName);
        float[] data = ECGTools.getFilteredSignal(signal);

        int value = classifier.predict(data);
        assertEquals(value, 1);

        fileName = "20160411_182039_ecg.csv";
        signal = readSignal(context, fileName);
        data = ECGTools.getFilteredSignal(signal);
        value = classifier.predict(data);
        assertEquals(value, 0);

        fileName = "20160411_181942_ecg.csv";
        signal = readSignal(context, fileName);
        data = ECGTools.getFilteredSignal(signal);
        value = classifier.predict(data);
        assertEquals(value, 1);
    }

    private float[] readSignal(Context context, String fileName) throws Exception {
        InputStream stream = context.getAssets().open(fileName);
        CSVFileReader cvsFile = new CSVFileReader(stream);
        ArrayList rows = cvsFile.readLines();
        float[] signal = new float[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            String[] items = (String[])rows.get(i);
            signal[i] = Float.parseFloat(items[0]);
        }
        return signal;
    }
}