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
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 # Created By: omatv@softserveinc.com
 # Maintained By: tshchyb@softserveinc.com
 */
public class CSVFileReader {
    private static final String TAG = "CSVFileReader";
    private InputStream mInputStream = null;

    public CSVFileReader(InputStream inputStream) {
        this.mInputStream = inputStream;
    }

    public ArrayList readLines() {
        if (mInputStream == null)
            return null;
        ArrayList<String[]> rows = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mInputStream));
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
                mInputStream.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return rows;
    }
}
