/*  Copyright (C) 2018-2020 Carsten Pfeiffer, Felix Konstantin Maurer

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.database;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.GBPrefs;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

/**
 * Created by tjhowse on 2021-01-07.
 */

public class PeriodicUploader extends BroadcastReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(PeriodicUploader.class);

    public static void enablePeriodicUpload(Context context) {
        Prefs prefs = GBApplication.getPrefs();
        boolean autoUploadEnabled = prefs.getBoolean(GBPrefs.AUTO_UPLOAD_ENABLED, false);
        Integer autoUploadInterval = prefs.getInt(GBPrefs.AUTO_UPLOAD_INTERVAL, 0);
        sheduleAlarm(context, autoUploadInterval, autoUploadEnabled);
    }

    public static void sheduleAlarm(Context context, Integer autoUploadInterval, boolean autoUploadEnabled) {
        Intent i = new Intent(context, PeriodicUploader.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0 , i, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        if (!autoUploadEnabled) {
            return;
        }
        int uploadPeriod = autoUploadInterval * 60 * 60 * 1000;
        if (uploadPeriod == 0) {
            return;
        }
        LOG.info("Enabling periodic upload");
        am.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + uploadPeriod,
                uploadPeriod,
                pi
        );
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.info("Uploading DB");
        try (DBHandler dbHandler = GBApplication.acquireDB()) {
            DBHelper helper = new DBHelper(context);
            String server = GBApplication.getPrefs().getString(GBPrefs.AUTO_UPLOAD_SERVER, null);
            String username = GBApplication.getPrefs().getString(GBPrefs.AUTO_UPLOAD_USERNAME, null);
            String password = GBApplication.getPrefs().getString(GBPrefs.AUTO_UPLOAD_PASSWORD, null);
            if (server == null) {
                LOG.info("Unable to upload DB, upload server not set");
                return;
            }
            Uri dstUri = Uri.parse(dst);
            // Retrieve time of most recent upload
            // Read out data points generated since most recent upload
            // Store timestamp of last data point in most recent upload
            // Convert to InfluxDB line protocol:
            // measurement,tags value_name=value epoch_time_seconds
            // Example for measurement MI_BAND_ACTIVITY_SAMPLE with no tags.
            // MI_BAND_ACTIVITY_SAMPLE DEVICE_ID=1,USER_ID=1,RAW_INTENSITY=1,STEPS=-1,RAW_KIND=1,HEART_RATE=-1 1610008597
            // MI_BAND_ACTIVITY_SAMPLE DEVICE_ID=1,USER_ID=1,RAW_INTENSITY=1,STEPS=-1,RAW_KIND=1,HEART_RATE=-1 1610008598
            // etc...

            // Potentially add a method to DBHelper to generate the InfluxDB line protocol messages.

            try {
                // Check for and, if necessary, create gadgetbridge database
                // curl -i -XPOST http://localhost:8086/query --data-urlencode "q=CREATE DATABASE gadgetbridge"
                // For each set of 5k data points:
                // curl -i -XPOST 'http://localhost:8086/write?db=gadgetbridge&precision=s' --data-binary <data>
                // https://docs.influxdata.com/influxdb/v1.8/tools/api/#write-http-endpoint
        } catch (Exception ex) {
            GB.updateUploadFailedNotification(context.getString(R.string.notif_upload_failed_title), context);
            LOG.info("Exception while uploading DB: ", ex);
        }
    }
}
