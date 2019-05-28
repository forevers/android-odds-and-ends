package com.ess.threadingactvity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class AndroidThreadPool {

    private static final String TAG = "thread_demo";

    ThreadPoolExecutor threadPoolExecutor;

    // A queue of Runnables
    private LinkedBlockingQueue<Runnable> threadPoolWorkQueue;

    Handler handler;

    AndroidThreadPool(int corePoolSize, int maximumPoolSize, int keepAliveTime, TimeUnit keepAliveTimeUnit, int queueCapacity) {

        // Instantiates the queue of Runnables as a LinkedBlockingQueue
        threadPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

        threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,           // Initial pool size
                maximumPoolSize,        // Max pool size
                keepAliveTime,
                keepAliveTimeUnit,
                threadPoolWorkQueue);

//        // Defines a Handler object that's attached to the UI thread
//        handler = new Handler(Looper.getMainLooper()) {
//            /*
//             * handleMessage() defines the operations to perform when
//             * the Handler receives a new Message to process.
//             */
//            @Override
//            public void handleMessage(Message inputMessage) {
////                ...
//            }
////        ...
//        }
    }

    public void execute_now(int id, int msec) {

        threadPoolExecutor.execute(new Runnable() {
            // todo need to subclass Runnable to pass params

            @Override
            public void run() {
//                    Log.i(TAG, "AndroidThread id : " + id + " msec : " + msec);
                for (int i = 0; i < 20; i++) {
                    Log.i(TAG, "execute_now() : " + i);
                    try {
//                        Thread.sleep(msec);
                        Thread.sleep(1000);
//                        Log.i(TAG, "AndroidThread id : " + id);

                    } catch (InterruptedException e) {
                        Log.i(TAG, "execute_now() interrupted");
                    }
                }
            }
        });
    }

    public void execute_future(int id, int msec) {

        // can be used to retrieve the result from the callable by calling future.get()
        // or cancel the task by calling future.cancel(boolean mayInterruptIfRunning).

        Future future = threadPoolExecutor.submit(new Callable() {
            // todo need to subclass Runnable to pass params
            @Override
            public Object call() throws Exception {
                for (int i = 0; i < 20; i++) {
                    Log.i(TAG, "execute_future() : " + i);

                    try {
//                        Thread.sleep(msec);
                        Thread.sleep(1000);
//                        Log.i(TAG, "AndroidThread id : " + id);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "execute_now() interrupted");
                    }
                }

                return null;
            }
        });
    }
}