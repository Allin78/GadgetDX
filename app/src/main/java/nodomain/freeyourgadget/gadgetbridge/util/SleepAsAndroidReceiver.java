package nodomain.freeyourgadget.gadgetbridge.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SleepAsAndroidReceiver extends BroadcastReceiver {

    boolean sendData;
    float[] accelData = new float[5];
    long batchSize=12;

    @Override
    public void onReceive(Context context, Intent intent) {
        switch(intent.getAction()) {
            case "com.urbandroid.sleep.watch.CHECK_CONNECTED":
                context.sendBroadcast(new Intent("com.urbandroid.sleep.watch.CONFIRM_CONNECTED"));
                break;
            case "com.urbandroid.sleep.watch.START_TRACKING":
                sendData=true;
                Intent dataIntent = new Intent("com.urbandroid.sleep.watch.DATA_UPDATE");
                dataIntent.setPackage("com.urbandroid.sleep");

                accelData[0] = 10.7f;
                accelData[1] = 30.3f;
                accelData[2] = 40.60f;

                dataIntent.putExtra("MAX_RAW_DATA", accelData);
                context.sendBroadcast(dataIntent);
                break;
            case "com.urbandroid.sleep.watch.STOP_TRACKING":
                sendData=false;
                break;
            case "com.urbandroid.sleep.watch.SET_BATCH_SIZE":
                batchSize=intent.getLongExtra("SIZE",12);
                break;
            case "com.urbandroid.sleep.watch.HINT":
                // TODO: Make wrist band vibrate
                boolean dd =false;
                dd=true;
                break;
            case "":
                break;
            default:
                boolean dummy =false;
                dummy=true;
        }
    }
}
