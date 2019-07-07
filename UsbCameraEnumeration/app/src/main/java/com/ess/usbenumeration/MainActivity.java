package com.ess.usbenumeration;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ess.util.UsbMonitor;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /* USB monitor for attach/detach and permission management */
    private UsbMonitor usbMonitor;
    UsbManager usbManager = null;
    private static int vendorId;
    private static int productId;
    private UsbDevice usbDevice;

    enum UsbState
    {
        DEVICE_CONNECTION_REQUIRED,
        PERMISSION_REQUIRED,
        PERMISSION_GRANTED,
        DEVICE_OPENED
    }

    UsbState usbState = UsbState.PERMISSION_REQUIRED;

    /* async thread processing */
    HandlerThread handlerThread;
    Handler handler;
    Looper looper;
    private volatile boolean handlerThreadDestroyed;

    /* ui widgets */
    Button usbButton;
    TextView usbText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        usbButton = (Button)findViewById(R.id.usb_button);
        usbButton.setOnClickListener(onClickUsbButtonListener);
        usbButton.setBackgroundColor(Color.RED);
        usbText = (TextView)findViewById(R.id.usb_textview);

        /* usb detection */
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        usbMonitor = new UsbMonitor(this, onUsbDeviceConnectListener);

        /* obtain first USB of device registered in the device_filter.xml list if it is inserted */
        usbDevice = usbMonitor.getDevice(this, R.xml.device_filter);

        /* was app launched by USB hotplug or launcher ? */
        Intent intent = getIntent();
        if (intent != null) {
            Log.i(TAG, "launched by intent");
        }

        if (usbDevice == null) {

            /* no supported usb device inserted */
            Log.i(TAG, "usbDevice == null");
            usbButton.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "Connect USB Device", Toast.LENGTH_SHORT).show();

        } else {

            Log.i(TAG, "usbDevice != null");

            /* supported usb device inserted */
            productId = usbDevice.getProductId();
            vendorId = usbDevice.getVendorId();
            Log.d(TAG, usbDevice.getDeviceName() + productId + ":" + vendorId);

            if (usbMonitor.hasPermission(usbDevice)) {
                Log.i(TAG, "usbDevice has permission");
                usbButton.setText("Request Device Open");
                usbText.setText("device connected with permission");
                usbState = UsbState.PERMISSION_GRANTED;
            } else {
                Log.i(TAG, "usbDevice requires permission");
                usbText.setText("device connected ... request permission");
                usbButton.setText("Request Permission");
            }
        }

        /* async processing thread */
        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        looper = handlerThread.getLooper();
        /* post async Runnables to handler */
        handler = new Handler(looper);
        handlerThreadDestroyed = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* If the activity has already been created/instantiated, the ACTION_USB_DEVICE_ATTACHED event will
       arrive through the 'onNewIntent()' method.
     */
    @Override
    protected void onNewIntent(Intent intent) {

        Log.v(TAG, "onNewIntent() enter");

        super.onNewIntent(intent);

        if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){

            Log.v(TAG, "ACTION_USB_DEVICE_ATTACHED");

            /* obtain first USB of device registered in the device_filter.xml list */
            usbDevice = usbMonitor.getDevice(this, R.xml.device_filter);

            if (usbDevice == null) {

                Log.i(TAG, "usbDevice == null");
                usbButton.setVisibility(View.GONE);

            } else {

                Log.i(TAG, "usbDevice != null");

                /* supported usb device inserted */
                productId = usbDevice.getProductId();
                vendorId = usbDevice.getVendorId();
                Log.d(TAG, usbDevice.getDeviceName() + productId + ":" + vendorId);

                usbButton.setVisibility(View.VISIBLE);

                if (usbMonitor.hasPermission(usbDevice)) {

                    Log.i(TAG, "usbDevice has permission");

                    usbText.setText("intent device connected with permission");
                    usbState = UsbState.PERMISSION_GRANTED;
                    if (null != usbMonitor.openDevice(usbDevice)) {
                        Log.i(TAG, "intent based USB device open success");
                        usbText.setText("USB openDevice success");
                        usbButton.setVisibility(View.GONE);
                    } else {
                        Log.i(TAG, "intent based USB device open failure");
                        usbText.setText("USB openDevice failure");
                    }

                } else {
                    Log.i(TAG, "usbDevice requires permission");
                    usbText.setText("device connected ... request permission");
                    usbButton.setText("Request Permission");
                }

            }
        }

        Log.v(TAG, "onNewIntent() exit");
    }

    private final View.OnClickListener onClickUsbButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(final View view) {

            Log.i(TAG, "onClickUsbButtonListener::onClick() entry");

            switch (usbState) {

                case DEVICE_CONNECTION_REQUIRED:

                    Log.i(TAG, "DEVICE_CONNECTION_REQUIRED");
                    Toast.makeText(MainActivity.this, "Connect USB Device", Toast.LENGTH_SHORT).show();
                    break;

                case PERMISSION_REQUIRED:

                    Log.i(TAG, "PERMISSION_REQUIRED");

                    if (usbDevice != null) {

                        boolean permission = usbMonitor.requestPermission(MainActivity.this, usbDevice);
                        if (permission) {
                            Log.i(TAG, "usbDevice has permission");

                            usbText.setText("USB Permission Obtained");
                            usbButton.setText("OPEN Device");
                            usbState = UsbState.PERMISSION_GRANTED;

                        } else {
                            Log.i(TAG, "usbDevice DOES NOT have usb permission");
                        }
                    } else {
                        Log.i(TAG, "usbDevice == null");
                    }
                    break;

                case PERMISSION_GRANTED:

                    Log.i(TAG, "PERMISSION_GRANTED");

                    postRunnable(new Runnable() {

                        @Override
                        public void run() {

                            UsbMonitor.UsbConnectionData usbConnectionData = usbMonitor.openDevice(usbDevice);
                            if (usbConnectionData != null) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        usbText.setText("USB openDevice success");
                                        usbButton.setVisibility(View.GONE);
                                    }
                                });

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        usbText.setText("USB openDevice failure");
                                    }
                                });
                                Log.i(TAG, "openDevice() failure");
                            }
                        }
                    });
                    break;

                case DEVICE_OPENED:

                    Log.i(TAG, "DEVICE_OPENED");
                    break;

                default:
                    Log.i(TAG, "invalid usb state transition");
                    break;
            }

            Log.i(TAG, "onClickUsbButtonListener::onClick() exit");
        }
    };

    private final UsbMonitor.OnUsbDeviceConnectListener onUsbDeviceConnectListener = new UsbMonitor.OnUsbDeviceConnectListener() {

        @Override
        public void onAttach(final UsbDevice device) {

            Log.v(TAG, "onAttach()");

            Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();

            if (usbDevice == null) {

                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                if (!deviceList.isEmpty()) {

                    for (UsbDevice allowedDevice : deviceList.values()) {

                        if (device.getVendorId() == vendorId && device.getProductId() == productId) {

                            if (usbManager.hasPermission(allowedDevice)) {

                                Log.i("UsbStateReceiver", "We have the USB permission! ");
                                Toast.makeText(MainActivity.this, "UVC Camera Attached", Toast.LENGTH_SHORT).show();
                                usbDevice = allowedDevice;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onDetach(final UsbDevice device) {

            Log.v(TAG, "onDetach()");
            Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
            usbDevice = null;

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    usbButton.setText("waiting for USB connection ...");
                    usbText.setText("Connect USB Device");
                    usbButton.setVisibility(View.GONE);                }
            });
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbMonitor.UsbConnectionData usbConnectionData) {

            Log.v(TAG, "onConnect() entry");
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbMonitor.UsbConnectionData usbConnectionData) {

            Log.v(TAG, "onDisconnect()");
        }

        @Override
        public void onCancel(UsbDevice device) {

            Log.v(TAG, "onCancel()");
        }
    };

    @Override
    protected void onStart() {

        Log.v(TAG, "onStart:");

        super.onStart();

        usbMonitor.register();
    }

    @Override
    protected void onStop() {

        Log.v(TAG, "onStop() entry");

        if (usbMonitor != null) {
            usbMonitor.unregister();
        }

        super.onStop();

        Log.v(TAG, "onStop() exit");
    }

    @Override
    protected void onDestroy() {

        Log.v(TAG, "onDestroy:");

        if (usbMonitor != null) {
            usbMonitor.destroy();
            usbMonitor = null;
        }

        super.onDestroy();
    }


    protected final synchronized void postRunnable(final Runnable runnable) {

        if ((runnable == null) || (handler == null)) return;

        try {
            handler.removeCallbacks(runnable);
            handler.post(runnable);
        } catch (final Exception e) {
            Log.e(TAG, e.toString());
        }
    }

}
