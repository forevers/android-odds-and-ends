package com.ess.threadingactvity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "thread_demo";

    private Button start_stop_button;
    private boolean running = false;

    AndroidThread androidThread;
    AndroidThreadPool androidThreadPool;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);

        start_stop_button = (Button)findViewById(R.id.button);

        start_stop_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!running) {

                    start_stop_button.setText("Stop");

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        naThreadConstruction(true);
//                    }
//                });

                    // construct joinable threads
                    naThreadConstruction();

                    androidThread = new AndroidThread(10, 20);
                    androidThread.start();

                    androidThreadPool.execute_now(1,1);
                    running = true;

                } else {
                    start_stop_button.setText("Start");

                    // a blocking call to join the native threads
                    naThreadDeconstruction();

                    androidThread.stop_();
                    running = false;
                }
            }
        });

        /*
         * Gets the number of available cores
         * (not always the same as the maximum number of cores)
         */
        int numberCores = Runtime.getRuntime().availableProcessors();
        // queueCapacity defaults to Integer#MAX_VALUE if not specified
        int queueCapacity = 10;
        androidThreadPool = new AndroidThreadPool(numberCores, numberCores, 2, SECONDS, queueCapacity);
    }


    // native thread abstraction
    class AndroidThread extends Thread {

        private int id;
        private int msec;
        Object lock = new Integer(1);
        boolean run_state;

        AndroidThread(int id, int msec) {

            this.id = id;
            this.msec = msec;
            run_state = true;
        }

        public void run() {

            while (is_running()) {
                try {
                    Thread.sleep(msec);
                    Log.i(TAG, "AndroidThread id : " + id);
                } catch (InterruptedException e) {
                    Log.i(TAG, "AndroidThread id : " + id + " interrupted");
                }
            }

            Log.i(TAG, "AndroidThread id : " + id + "exiting");
        }

        public void stop_() {
            synchronized(lock) {
                run_state = false;
            }
        }

        public boolean is_running() {
            boolean running;
            synchronized(lock) {
                running = run_state;
            }
            return running;
        }
    }

    // native library method
    private native void naThreadConstruction();
    private native void naThreadDeconstruction();
}
