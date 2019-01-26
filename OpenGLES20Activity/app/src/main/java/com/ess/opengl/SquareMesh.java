package com.ess.opengl;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class SquareMesh {

    private static final String TAG = FrameGLRenderer.class.getName();

    boolean use_strip = true;
    boolean use_points = true;

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // The matrix must be included as a modifier of gl_Position.
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String vertexShaderCodePoints =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // The matrix must be included as a modifier of gl_Position.
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  gl_PointSize = 0.5;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final String fragmentShaderCodePoints =
            "precision mediump float;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(1, 0, 1, 1);" +
                    "}";

    private FloatBuffer vertexBuffer = null;
    private ShortBuffer drawListBuffer = null;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    private short drawOrder[] = null;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    private final short ROWS = 720;
    private final short COLS = 1280;

    private float mPeak = 0.0f;

    float[] vertices = null;
    short[] indices = null;


    public SquareMesh() {

        vertices = getVertices(COLS, ROWS);
        indices = getIndices(COLS, ROWS);

        // initialize vertex byte buffer for shape coordinates
        // (# of coordinate values * 4 bytes per float)
        ByteBuffer bb = ByteBuffer.allocateDirect(getVerticesCount(COLS, ROWS) * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer dlb = null;
        // initialize byte buffer for the draw list
        if (use_points == false) {

            // (# of coordinate values * 2 bytes per short)
            dlb = ByteBuffer.allocateDirect(indices.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(indices);
            drawListBuffer.position(0);
        }

        int vertexShader;
        if (use_points) {
            // prepare shaders and OpenGL program
            vertexShader = MyGLRenderer.loadShader(
                    GLES20.GL_VERTEX_SHADER,
                    vertexShaderCodePoints);
        } else {
            // prepare shaders and OpenGL program
            vertexShader = MyGLRenderer.loadShader(
                    GLES20.GL_VERTEX_SHADER,
                    vertexShaderCode);
        }

        int fragmentShader;
        if (use_points) {
            fragmentShader = MyGLRenderer.loadShader(
                    GLES20.GL_FRAGMENT_SHADER,
                    fragmentShaderCodePoints);
        } else {
            fragmentShader = MyGLRenderer.loadShader(
                    GLES20.GL_FRAGMENT_SHADER,
                    fragmentShaderCode);
        }

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);
    }


    int getVerticesCount(int width, int height) {
        return width * height * 3;
    }


    int getIndicesCount(int width, int height) {
        return (width*height) + (width-1)*(height-2);
    }


    public void setPeak(float peak) {

        Log.d(TAG, "peak: " + peak);

        if (vertices != null) {

            if (peak != mPeak) {
                mPeak = peak;

                for (int row = ROWS / 4; row < 3 * ROWS / 4; row++) {
                    for (int col = COLS / 4; col < 3 * COLS / 4; col++) {
                        vertexBuffer.put(2 + 3*(row*COLS + col), mPeak);
                    }
                }
            }
        }
    }


    public float getPeak() {
        return mPeak;
    }


    float[] getVertices( int width, int height ) {

        if (vertices != null) return vertices;

        vertices = new float[getVerticesCount(width, height)];
        int i = 0;

        for (int row=0; row<height; row++) {
            for (int col=0; col<width; col++) {
                vertices[i++] = ((float) (col) - width/2);
                vertices[i++] = ((float) (row) - height/2);
                vertices[i++] = 0.0f;
            }
        }

        return vertices;
    }


    short[] getIndices( short width, short height ) {

        if (indices != null) return indices;

        int numIndicies = getIndicesCount(width, height);
        indices = new short[numIndicies];
        int i = 0;

        for ( short row=0; row<(height-1); row++ ) {
            if ( (row&1)==0 ) { // even rows
                for (short col=0; col<width; col++ ) {
                    indices[i++] = (short)(col + row * width);
                    indices[i++] = (short)(col + (row+1) * width);
                }
            } else { // odd rows
                for ( int col=width-1; col>0; col-- ) {
                    indices[i++] = (short)(col + (row+1) * width);
                    indices[i++] = (short)(col - 1 + + row * width);
                }
            }
        }
        if ( ((height & 1) != 0) && (height > 2) ) {
            indices[i++] = (short)((height-1) * width);
        }

        return indices;
    }


    public void draw(float[] mvpMatrix) {

        if (use_strip) {

            // Add program to OpenGL environment
            GLES20.glUseProgram(mProgram);

            // get handle to vertex shader's vPosition member
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

            // Enable a handle to the shapes vertices
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Prepare the shape coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);

            if (use_points == false) {
                // get handle to fragment shader's vColor member
                mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

                // Set color for drawing the shape
                GLES20.glUniform4fv(mColorHandle, 1, color, 0);
            }

            // get handle to shape's transformation matrix
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            //        MyGLRenderer.checkGlError("glGetUniformLocation");

            // Pass the projection and view transformation to the shader
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
            //        MyGLRenderer.checkGlError("glUniformMatrix4fv");

            if (use_points == false) {
                // Draw the strip
                GLES20.glDrawElements(
                        GLES20.GL_TRIANGLE_STRIP, indices.length,
                        GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
            } else {
                // Draw points
                GLES20.glDrawArrays(GLES20.GL_POINTS,0, ROWS*COLS);
            }

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);

        } else {

            // Add program to OpenGL environment
            GLES20.glUseProgram(mProgram);

            // get handle to vertex shader's vPosition member
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

            // Enable a handle to the shapes vertices
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Prepare the shape coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);

            // get handle to fragment shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

            // Set color for drawing the shape
            GLES20.glUniform4fv(mColorHandle, 1, color, 0);

            // get handle to shape's transformation matrix
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            //        MyGLRenderer.checkGlError("glGetUniformLocation");

            // Pass the projection and view transformation to the shader
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
            //        MyGLRenderer.checkGlError("glUniformMatrix4fv");

            // Draw the square mesh
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES, drawOrder.length,
                    GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
    }
}
