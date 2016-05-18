package softservernd.biolock.control;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * # # Copyright (C) 2016 SoftServe Inc., or its affiliates. All Rights Reserved.
 # Licensed under http://www.apache.org/licenses/LICENSE-2.0 <see LICENSE file>
 # Created By: omatv@softserveinc.com
 # Maintained By: tshchyb@softserveinc.com
 */
class ECGChartRenderer implements GLSurfaceView.Renderer {

    private final ECGSignalChart mECGLineChart;
    private volatile float[] mChartData;
    private int mWidth;
    private int mHeight;

    public ECGChartRenderer(int ECGSignalSize) {
        mChartData = new float[ECGSignalSize];
        mECGLineChart = new ECGSignalChart(ECGSignalSize);
    }

    public void setChartColor(float[] rgb) {
        mECGLineChart.rgb = rgb;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mWidth = width;
        mHeight = height;

        if (height == 0) {                       //Prevent A Divide By Zero By
            height = 1;                         //Making Height Equal One
        }
        gl.glViewport(0, 0, width, height);     //Reset The Current Viewport
        gl.glMatrixMode(GL10.GL_PROJECTION);    //Select The Projection Matrix
        gl.glLoadIdentity();                    //Reset The Projection Matrix

        //Calculate The Aspect Ratio Of The Window
        GLU.gluPerspective(gl, 45.0f, (float) height / (float) width, 0.1f, 100.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);     //Select The Modelview Matrix
        gl.glLoadIdentity();                    //Reset The Modelview Matrix
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // clear Screen and Depth Buffer
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        // Reset the Modelview Matrix
        gl.glLoadIdentity();
        // Drawing
        gl.glScalef(0.3f, 1.0f, 1.0f);
        gl.glTranslatef(0.0f, 0.0f, -3.0f);     // move 5 units INTO the screen
        // is the same as moving the camera 5 units away
        mECGLineChart.setResolution(mWidth, mHeight);
        mECGLineChart.setChartData(mChartData);

        mECGLineChart.draw(gl);
    }

    public void setChartData(float[] chartData) {
        mChartData = chartData;
    }
}

class ECGSignalChart {
    private final float CHART_POINT;
    private float mChartData[];
    private int mWidth;
    private int mHeight;
    private FloatBuffer mVertexBuffer;
    private final float[] mVertices;

    public float[] rgb = {0.21f, 0.86f, 0.68f};

    public ECGSignalChart(int ecgSignalSize) {
        mChartData = new float[ecgSignalSize];
        CHART_POINT = ecgSignalSize;
        mVertices = new float[(int) (CHART_POINT * 3.0f)];
        drawRealtimeChart();
        vertexGenerate();
    }

    public void setResolution(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void setChartData(float[] chartData) {
        mChartData = chartData;
        drawRealtimeChart();
        vertexGenerate();
    }

    public void draw(GL10 gl) {
        gl.glViewport(0, 0, mWidth, mHeight);
        // bind the previously generated texture
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // set the color for the triangle
//        gl.glColor4f(0.21f, 0.86f, 0.68f, 1f);
        gl.glColor4f(rgb[0], rgb[1], rgb[2], 1f);
        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        // Line width
        gl.glLineWidth(5.0f);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, mVertices.length / 3);
        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);


    }

    private void drawRealtimeChart() {
        float verticeInc = 2.0f / CHART_POINT;
        // update x vertrices
        for (int i = 0; i < CHART_POINT * 3; i = i + 3) {
            if (i < CHART_POINT * 3) {
                mVertices[i] = -1 + (i * verticeInc) / 3;
            }
        }
        // update y vertrices
        int k = 0;
        for (int i = 1; i < CHART_POINT * 3; i = i + 3) {
            if (i < CHART_POINT * 3) {
                mVertices[i] = mChartData[k++];
                if (k >= mChartData.length)
                    break;
            }
        }
    }

    private void vertexGenerate() {
        // a float has 4 bytes so we allocate for each coordinate 4 bytes
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(mVertices.length * 4);

        vertexByteBuffer.order(ByteOrder.nativeOrder());
        // allocates the memory from the byte buffer
        mVertexBuffer = vertexByteBuffer.asFloatBuffer();
        // fill the vertexBuffer with the vertices
        mVertexBuffer.put(mVertices);
        // set the cursor position to the beginning of the buffer
        mVertexBuffer.position(0);
    }
}
