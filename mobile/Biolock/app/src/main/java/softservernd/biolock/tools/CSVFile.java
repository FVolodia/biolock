package softservernd.biolock.tools;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by tarasshchybovyk on 4/11/16.
 */
public class CSVFile {
    private static final String TAG = "CSVFile";
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    public static CSVFile createFile(String folder, String name) {
        File file = new File(folder, name + ".csv");
        try {
            OutputStream stream = new FileOutputStream(file);
            return new CSVFile(stream);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public CSVFile(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public CSVFile(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public ArrayList readLines() {
        if (inputStream == null)
            return null;
        ArrayList<String[]> rows = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] row = line.split(",");
                rows.add(row);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return rows;
    }

    public boolean writeLines(String lines) {
        if (outputStream == null)
            return false;

        try {
            outputStream.write(lines.getBytes());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return true;
    }
}
