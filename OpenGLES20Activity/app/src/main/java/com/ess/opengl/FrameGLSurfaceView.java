package com.ess.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class FrameGLSurfaceView extends GLSurfaceView {

    private static final String TAG = FrameGLSurfaceView.class.getName();

    private FrameGLRenderer mRendererNative;

    public FrameGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public FrameGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        // TODO fix orientation handling in native code
        mRendererNative = new FrameGLRenderer(1, 1280, 720);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRendererNative);

        // Render the view only when there is a change in the drawing data
        // GLSurfaceView.requestRender() is required if this is enables
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;
    private final float PEAK_SCALE_FACTOR = 1.0f / 10000;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {

            case MotionEvent.ACTION_MOVE:

                if ( (y > getHeight() / 2) && (x < getWidth() / 2) ) {

                    float dx = x - mPreviousX;
                    mRendererNative.setAngleY(mRendererNative.getAngleY() +  (dx * TOUCH_SCALE_FACTOR));

                } else if ( (y < getHeight() / 2) && (x > getWidth() / 2)) {
                    float angleX = mRendererNative.getAngleX();
                    float dy = y - mPreviousY;
                    Log.d(TAG, "angleX : " + angleX + " dy : " + dy * TOUCH_SCALE_FACTOR);
                    Log.d(TAG, "new angleX : " + (angleX -  (dy * TOUCH_SCALE_FACTOR)));
                    mRendererNative.setAngleX(angleX -  (dy * TOUCH_SCALE_FACTOR));
                } else if ( (y < getHeight() / 2) && (x < getWidth() / 2)) {
                    float peak = mRendererNative.getPeak();
                    float dy = y - mPreviousY;
                    Log.d(TAG, "peak : " + peak + " dPeak : " + dy * PEAK_SCALE_FACTOR);
                    mRendererNative.setPeak(peak + (dy * PEAK_SCALE_FACTOR));
                }

                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;

        return true;
    }
}
