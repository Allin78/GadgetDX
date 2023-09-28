package nodomain.freeyourgadget.gadgetbridge.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiSupport;

public class SleepAsAndroidReceiver extends BroadcastReceiver {

    private static final ArrayList<Float> accData = new ArrayList<Float>();
    private static Timer maxAccTimer = new Timer();
    private static HuamiSupport deviceSupport;
    private static boolean trackingOngoing = false;
    private static Timer accTimer = new Timer();
    private static long lastAccCollection;
    private static long batchSize = 2;
    private static float maxAccelData =0;

    public static void setDeviceSupport(HuamiSupport deviceSupport) {
        SleepAsAndroidReceiver.deviceSupport = deviceSupport;
    }

    // For some reason an empty constructor is necessary

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "com.urbandroid.sleep.watch.CHECK_CONNECTED":
                context.sendBroadcast(new Intent("com.urbandroid.sleep.watch.CONFIRM_CONNECTED"));
                break;
            case "com.urbandroid.sleep.watch.START_TRACKING":
                // Start Continous HR-Data gathering
                //deviceSupport.onEnableRealtimeHeartRateMeasurement(true);

                // TODO: Add logic to send updates periodically
                trackingOngoing = true;
                deviceSupport.setRawSensor(true);
                accTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (trackingOngoing) {
                            if ((System.currentTimeMillis()-lastAccCollection)>6000) {
                                deviceSupport.setRawSensor(false);
                                deviceSupport.setRawSensor(true);
                            }
                        }
                    }
                }, 5000, 5000);
                maxAccTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(trackingOngoing){
                        collectAccelMax();}
                    }
                }, 0, 10000);
                break;
            case "com.urbandroid.sleep.watch.STOP_TRACKING":
                trackingOngoing = false;
                accTimer.cancel();
                accTimer = new Timer();
                maxAccTimer.cancel();
                maxAccTimer =new Timer();
                deviceSupport.setRawSensor(false);
                break;
            case "com.urbandroid.sleep.watch.SET_BATCH_SIZE":
                batchSize = intent.getLongExtra("SIZE", 12);
                break;
            case "com.urbandroid.sleep.watch.HINT":
                deviceSupport.sendFindDeviceCommand(true);
                break;
            case "com.urbandroid.sleep.watch.UPDATE_ALARM":
                // TODO: Update watch alarm
                break;
            default:
                boolean dummy;
                dummy = false;
        }
    }

    private float[] convertToFloatArray(ArrayList<Float> list) {
        float[] result = new float[list.size()];
        int i = 0;
        for (float f : list) {
            result[i++] = f;
        }
        return result;
    }

    private void sendToSleepAsAndroid(ArrayList<Float> values, Context context, String action, String extra) {
        Intent intent = new Intent(action);
        intent.setPackage("com.urbandroid.sleep");
        intent.putExtra(extra, convertToFloatArray(values));
        context.sendBroadcast(intent);
    }

    public void onAccelDataChange(float gx, float gy, float gz) {
lastAccCollection=System.currentTimeMillis();
        float accelData = (float) Math.sqrt((gx * gx) + (gy * gy) + (gz * gz));
        if (accelData > maxAccelData) {
            maxAccelData = accelData;
        }
    }

    public void triggerAccelTransfer() {
        sendToSleepAsAndroid(accData, GBApplication.getContext(), "com.urbandroid.sleep.watch.DATA_UPDATE", "MAX_RAW_DATA");
        accData.clear();
    }

    public void collectAccelMax() {
        accData.add(maxAccelData);
        maxAccelData = 0;
        if (accData.size() >= batchSize) {
            triggerAccelTransfer();
        }
    }
}
