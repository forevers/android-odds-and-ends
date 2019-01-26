package com.ess.opengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG =  MyGLRenderer.class.getName();;

    private SquareMesh mSquareMesh;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private float mAngle;
    private float mAngleX = 0;
    private float mAngleY = 0;
    private float mAngleZ = 0;

    final int far = 4*1280;


    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // initialize a geometry
        mSquareMesh = new SquareMesh();
    }


    public void setPeak(float peak) {
        mSquareMesh.setPeak(peak);
    }


    public float getPeak() {
        return mSquareMesh.getPeak();
    }


    public void onDrawFrame(GL10 unused) {

        float[] scratch = new float[16];

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -far/2, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // create rotate matrix in degrees
        Matrix.setRotateM(mRotationMatrix, 0, mAngleX, 1.0f, 0, 0);
        Matrix.rotateM(mRotationMatrix, 0, mAngleY, 0, 1.0f, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Draw shape
        mSquareMesh.draw(scratch);
    }


    public void onSurfaceChanged(GL10 unused, int width, int height) {

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, far);

    }


    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER) or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }


    public float getAngle() {
        return mAngle;
    }


    public void setAngle(float angle) {
        mAngle = angle;
    }


    public float getAngleX() {
        return mAngleX;
    }


    public void setAngleX(float angle) {
        if (angle < 0.0) {
            mAngleX = 0.0f;
        } else if(angle > 90.0f) {
            mAngleX = 90.0f;
        } else {
            mAngleX = angle;
        }
    }


    public float getAngleY() {
        return mAngleY;
    }


    public void setAngleY(float angle) {
        if (angle < 0.0) {
            mAngleY = 0.0f;
        } else if(angle > 90.0f) {
            mAngleY = 90.0f;
        } else {
            mAngleY = angle;
        }
    }


    public float getAngleZ() {
        return mAngleZ;
    }


    public void setAngleZ(float angle) {
        if (angle >= 0.0 && angle <= 30.0)  mAngleZ = angle;
    }

}

