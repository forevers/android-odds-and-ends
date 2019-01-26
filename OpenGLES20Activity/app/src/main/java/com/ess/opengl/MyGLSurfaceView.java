package com.ess.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {

    private static final String TAG = MyGLSurfaceView.class.getName();

    private MyGLRenderer mRenderer;


    public MyGLSurfaceView(Context context) {
        super(context);
        init(context);
    }


    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;
    private final float PEAK_SCALE_FACTOR = 1.0f / 10;


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
                    mRenderer.setAngleY(mRenderer.getAngleY() +  (dx * TOUCH_SCALE_FACTOR));

                } else if ( (y > getHeight() / 2) && (x > getWidth() / 2)) {

                    float angleX = mRenderer.getAngleX();
                    float dy = y - mPreviousY;
                    Log.d(TAG, "angleX : " + angleX + " dy : " + dy * TOUCH_SCALE_FACTOR);
                    mRenderer.setAngleX(angleX -  (dy * TOUCH_SCALE_FACTOR));

                } else if ( (y < getHeight() / 2) ) {

                    float peak = mRenderer.getPeak();
                    float dx = x - mPreviousX;
                    Log.d(TAG, "peak : " + peak + " dPeak : " + dx * PEAK_SCALE_FACTOR);
                    mRenderer.setPeak(peak + (dx * PEAK_SCALE_FACTOR));
                }

                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;

        return true;
    }
}
