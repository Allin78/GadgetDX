/*  Copyright (C) 2016-2021 Andreas Shimokawa

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

import android.util.Pair;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventSendBytes;
import nodomain.freeyourgadget.gadgetbridge.model.Weather;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;

class AppMessageHandlerSimplyWeather extends AppMessageHandler {
    private static final int KEY_TEMPERATURE = 0;
    private static final int KEY_LOCATION = 1;
    private static final int KEY_ICON = 2;
    private static final int KEY_R_TIME12 = 3;
    private static final int KEY_R_TIME24 = 4;
    private static final int KEY_LANGUAGE = 5;
    private static final int KEY_DISCONNECTED_VIBES = 6;

    AppMessageHandlerSimplyWeather(UUID uuid, PebbleProtocol pebbleProtocol) {
        super(uuid, pebbleProtocol);
    }

    private byte[] encodeWeatherMessage(WeatherSpec weatherSpec) {

        /*
            KEY_TEMPERATURE: Math.floor(json.main.temp) + (celsius ? "\u00B0C" : "\u00B0F"),
            KEY_LOCATION: json.name,
            KEY_ICON: json.weather[0].icon,
            KEY_R_TIME12: getTime(12),
            KEY_R_TIME24: getTime(24),
            KEY_LANGUAGE: options.language,
            KEY_DISCONNECTED_VIBES: "Y",
         */

        if (weatherSpec == null) {
            return null;
        }

        ArrayList<Pair<Integer, Object>> pairs = new ArrayList<>(7);
        pairs.add(new Pair<>(KEY_ICON, getIconForConditionCode(weatherSpec.currentConditionCode)));
        pairs.add(new Pair<>(KEY_TEMPERATURE, getCurrentTemperature(weatherSpec.currentTemp)));
        pairs.add(new Pair<>(KEY_LOCATION, weatherSpec.location));
        pairs.add(new Pair<>(KEY_R_TIME12, getTime(weatherSpec.timestamp, false)));
        pairs.add(new Pair<>(KEY_R_TIME24, getTime(weatherSpec.timestamp, true)));
        pairs.add(new Pair<>(KEY_LANGUAGE, "EN"));
        pairs.add(new Pair<>(KEY_DISCONNECTED_VIBES, 'Y'));
        byte[] weatherMessage = mPebbleProtocol.encodeApplicationMessagePush(PebbleProtocol.ENDPOINT_APPLICATIONMESSAGE, mUUID, pairs, null);

        ByteBuffer buf = ByteBuffer.allocate(weatherMessage.length);

        buf.put(weatherMessage);

        return buf.array();
    }

    //TODO: Fahrenheit!
    private String getCurrentTemperature(int kelvin) {
        return (kelvin - 273) + "\u00B0C";
    }

    private String getTime(int timeInMillis, Boolean h24) {
        //Calendar.getInstance().timeInMillis / 1000
        long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis * 1000);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis * 1000);

        if (!h24 && hours > 12) hours -= 12;
        if (!h24 && hours == 0) hours = 12;

        return padTime(hours) + ":" + padTime(minutes);
    }

    private String padTime(long time){
        return String.format("%2s", time).replace(' ', '0');
    }

    //Map ConditionCode to OWM Icon
    private String getIconForConditionCode(int conditionCode) {
        if (200 <= conditionCode && conditionCode <= 232)
            return "11d";
        else if (300 <= conditionCode && conditionCode <= 321)
            return "09d";
        else if (500 <= conditionCode && conditionCode <= 504)
            return "10d";
        else if (conditionCode == 511)
            return "13d";
        else if (520 <= conditionCode && conditionCode <= 531)
            return "09d";
        else if (600 <= conditionCode && conditionCode <= 622)
            return "13d";
        else if (700 <= conditionCode && conditionCode <= 781)
            return "50d";
        else if (conditionCode == 800)
            return "01d"; // 01n TODO: Check night!
        else if (conditionCode == 801)
            return "02d"; // 02n TODO: Check night!
        else if (conditionCode == 802)
            return "03d"; //03n TODO: Check night!
        else if (conditionCode == 803 || conditionCode == 804)
            return "04d"; //04n TODO: Check night!
        return "01d"; //TODO: No weather simbol!
    }

    @Override
    public GBDeviceEvent[] onAppStart() {
        WeatherSpec weatherSpec = Weather.getInstance().getWeatherSpec();
        if (weatherSpec == null) {
            return new GBDeviceEvent[]{null};
        }
        GBDeviceEventSendBytes sendBytes = new GBDeviceEventSendBytes();
        sendBytes.encodedBytes = encodeWeatherMessage(weatherSpec);
        return new GBDeviceEvent[]{sendBytes};
    }

    @Override
    public byte[] encodeUpdateWeather(WeatherSpec weatherSpec) {
        return encodeWeatherMessage(weatherSpec);
    }
}
