package com.ess.opengl;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class FrameGLRenderer implements IFrameGLRenderer, GLSurfaceView.Renderer {

    private static final String TAG = FrameGLRenderer.class.getName();

    private float mAngleX = 0;
    private float mAngleY = 0;

    int width;
    int height;

    protected long frameAccessHandle;
    protected long rendererHandle;

    public FrameGLRenderer(long frameAccessHandle, int height, int width) {
        this.width = width;
        this.height = height;
        this.frameAccessHandle = frameAccessHandle;
        rendererHandle = nativeCreate(frameAccessHandle, height, width);
    }

    // IFrameRenderer3D interface methods
    public synchronized void start() {
        nativeStart(rendererHandle);
    }

    public synchronized void stop() {
        nativeStop(rendererHandle);
    }

    public void setAngleX(float angle) {

        if (angle < 0.0) {
            mAngleX = 0.0f;
        } else if (angle > 90.0f) {
            mAngleX = 90.0f;
        } else {
            mAngleX = angle;
        }
        nativeSetAngleX(rendererHandle, mAngleX);
    }

    public void setAngleY(float angle) {
        if (angle < 0.0) {
            mAngleY = 0.0f;
        } else if(angle > 90.0f) {
            mAngleY = 90.0f;
        } else {
            mAngleY = angle;
        }

        nativeSetAngleY(rendererHandle, mAngleY);
    }

    public float getAngleX() {
        return nativeGetAngleX(rendererHandle);
    }

    public float getAngleY() {
        return nativeGetAngleY(rendererHandle);
    }

    public void setPeak(float peak) {
        nativeSetPeak(rendererHandle, peak);
    }

    public float getPeak() {
        return nativeGetPeak(rendererHandle);
    }

    // GLSurfaceView.Renderer interface methods

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        nativeOnSurfaceCreated(rendererHandle);
        start();
    }

    public void onSurfaceChanged(GL10 unused, int screen_width, int screen_height) {

        nativeOnSurfaceChanged(rendererHandle, screen_width, screen_height);
    }

    public void onDrawFrame(GL10 unused) {
        nativeOnDrawFrame(rendererHandle);
    }


    // IFrameRenderer3D native
    private final native long nativeCreate(long frame_access_handle, int height, int width);
    private static final native int nativeStart(long renderer_handle);
    private static final native int nativeStop(long renderer_handle);
    public static native void nativeSetAngleX(long renderer_handle, float mAngleX);
    public static native void nativeSetAngleY(long renderer_handle, float mAngleY);
    public static native float nativeGetAngleX(long renderer_handle);
    public static native float nativeGetAngleY(long renderer_handle);
    public static native void nativeSetPeak(long renderer_handle, float peak);
    public static native float nativeGetPeak(long renderer_handle);

    // GLSurfaceView.Renderer native
    public static native void nativeOnSurfaceCreated(long rendererHandle);
    public static native void nativeOnSurfaceChanged(long rendererHandle, int width, int height);
    public static native void nativeOnDrawFrame(long rendererHandle);
}
