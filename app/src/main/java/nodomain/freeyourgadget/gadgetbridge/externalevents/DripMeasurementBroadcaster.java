/*  Copyright (C) 2023 Alicia Hormann

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package nodomain.freeyourgadget.gadgetbridge.externalevents;

import static nodomain.freeyourgadget.gadgetbridge.GBApplication.getContext;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Locale;

import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.healthThermometer.TemperatureInfo;


public class DripMeasurementBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(DripMeasurementBroadcaster.class);
    private static final String ACTION_NEW_MEASUREMENT = "nodomain.freeyourgadget.gadgetbridge.action.temperatureMeasurement";

    /**
     * Sends an intent with the measurement data out to other apps.
     * Currently only sends one datapoint at a time
     * and therefore relies on the assumption, that the measurements are fetched chronologically.
     */
    public static void sendMeasurement(TemperatureInfo info) {
        //todo: create a setting and check if it is enabled here
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm");
        try {
            String measurement_json = new JSONObject()
                    .put("Date", dateformat.format(info.getTimestamp()))
                    .put("Time", timeformat.format(info.getTimestamp()))
                    .put("Temperature", String.format(Locale.US, "%.2f", info.getTemperature()))
                    .toString();
            Intent intent = new Intent();
            intent.setAction(ACTION_NEW_MEASUREMENT);
            intent.putExtra(Intent.EXTRA_TEXT, measurement_json);
            intent.setType("text/json");
            intent.setPackage("com.drip"); // Hardcoding target app / package
            getContext().sendBroadcast(intent);
            LOG.debug("Sent Broadcast to com.drip: " + intent.getAction() + ", " + intent.getExtras());
        } catch (JSONException e) {
            LOG.error("Failed to build intent", e);
        }
    }
}
