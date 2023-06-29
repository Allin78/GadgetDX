/*  Copyright (C) 2015-2021 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.pebble;


import android.location.Location;
import android.util.Pair;

import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventSendBytes;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.Huami2021Weather;
import nodomain.freeyourgadget.gadgetbridge.service.devices.pebble.webview.CurrentPosition;
import nodomain.freeyourgadget.gadgetbridge.util.FileUtils;
import nodomain.freeyourgadget.gadgetbridge.util.PebbleUtils;

class AppMessageHandler {
    final PebbleProtocol mPebbleProtocol;
    final UUID mUUID;
    Map<String, Integer> messageKeys;

    AppMessageHandler(UUID uuid, PebbleProtocol pebbleProtocol) {
        mUUID = uuid;
        mPebbleProtocol = pebbleProtocol;
    }

    public boolean isEnabled() {
        return true;
    }

    public UUID getUUID() {
        return mUUID;
    }

    public GBDeviceEvent[] handleMessage(ArrayList<Pair<Integer, Object>> pairs) {
        // Just ACK
        GBDeviceEventSendBytes sendBytesAck = new GBDeviceEventSendBytes();
        sendBytesAck.encodedBytes = mPebbleProtocol.encodeApplicationMessageAck(mUUID, mPebbleProtocol.last_id);
        return new GBDeviceEvent[]{sendBytesAck};
    }

    public GBDeviceEvent[] onAppStart() {
        return null;
    }

    public byte[] encodeUpdateWeather(WeatherSpec weatherSpec) {
        return null;
    }

    protected GBDevice getDevice() {
        return mPebbleProtocol.getDevice();
    }

    JSONObject getAppKeys() throws IOException, JSONException {
        File destDir = PebbleUtils.getPbwCacheDir();
        File configurationFile = new File(destDir, mUUID.toString() + ".json");
        if (configurationFile.exists()) {
            String jsonstring = FileUtils.getStringFromFile(configurationFile);
            JSONObject json = new JSONObject(jsonstring);
            return json.getJSONObject("appKeys");
        }
        throw new IOException();
    }

    protected boolean isDay() {
        GregorianCalendar date = new GregorianCalendar();
        final Location lastKnownLocation = new CurrentPosition().getLastKnownLocation();

        final GregorianCalendar[] sunriseTransitSet = SPA.calculateSunriseTransitSet(
                date,
                lastKnownLocation.getLatitude(),
                lastKnownLocation.getLongitude(),
                DeltaT.estimate(date)
        );

        long inf = sunriseTransitSet[0].getTimeInMillis();
        long sup = sunriseTransitSet[1].getTimeInMillis();
        return inf <= date.getTimeInMillis() && date.getTimeInMillis() <= sup;
    }

}