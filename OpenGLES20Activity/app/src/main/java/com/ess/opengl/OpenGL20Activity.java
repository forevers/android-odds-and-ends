package com.ess.opengl;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class OpenGL20Activity extends AppCompatActivity {

    private MyGLSurfaceView mGLView;
    private FrameGLSurfaceView frameGLSurfaceView;


    // load the 'native-lib' library
    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_open_gl20);

        mGLView = (MyGLSurfaceView) findViewById(R.id.surfaceView);
        frameGLSurfaceView = (FrameGLSurfaceView) findViewById(R.id.frameGLSurfaceView);
    }


    @Override
    protected void onResume() {

        super.onResume();

        if (frameGLSurfaceView != null) {
            frameGLSurfaceView.onResume();
        }
    }


    @Override
    protected void onPause() {

        super.onPause();

        if (frameGLSurfaceView != null) {
            frameGLSurfaceView.onPause();
        }
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

}




