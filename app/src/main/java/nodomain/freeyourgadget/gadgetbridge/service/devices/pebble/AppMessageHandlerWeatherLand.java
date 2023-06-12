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

import android.annotation.SuppressLint;
import android.util.Pair;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.activities.SettingsActivity;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventSendBytes;
import nodomain.freeyourgadget.gadgetbridge.model.Weather;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;

public class AppMessageHandlerWeatherLand extends AppMessageHandler {
    /*
        "invert_color": 2,
        // Not in use, C code asks for weather updates.
        "request_weather": 255,
        "temperature": 1,
        "icon": 0
     */

    private static final int temperature = 1;
    private static final int icon = 0;


    public AppMessageHandlerWeatherLand(UUID uuid, PebbleProtocol pebbleProtocol) {
        super(uuid, pebbleProtocol);
    }

    public byte[] encodeWeatherMessage(WeatherSpec weatherSpec) {
        if (weatherSpec == null) {
            return null;
        }

        ArrayList<Pair<Integer, Object>> pairs = new ArrayList<>(7);
        pairs.add(new Pair<>(icon, getIcon(weatherSpec.currentConditionCode, isDay(), weatherSpec.currentTemp, weatherSpec.windSpeed)));
        pairs.add(new Pair<>(temperature, getCurrentTemperature(weatherSpec.currentTemp)));
        byte[] weatherMessage = mPebbleProtocol.encodeApplicationMessagePush(PebbleProtocol.ENDPOINT_APPLICATIONMESSAGE, mUUID, pairs, null);

        ByteBuffer buf = ByteBuffer.allocate(weatherMessage.length);

        buf.put(weatherMessage);

        return buf.array();
    }

    private int getIcon(int id, boolean dayBool, int temp, float wind) {
        switch (String.valueOf(id).charAt(0)) {
            case '2':
                return 12; //STORM
            case '3':
            case '5':
                return 8; //RAIN
            case '6':
                return 9; //SNOW
            case '7':
                return 6; //HAZE
            case '8':
                if (id == 800) {
                    if (wind > 8.9) return 2; //WINDY (8.9 m/s)
                    if (temp < 273) return 3; //COLD (0ºC)

                    return (dayBool ? 0 : 1);// CLEAR_DAY | CLEAR_NIGHT
                }
                if (id == 801 || id == 802) {
                    return (dayBool ? 4 : 5); // PARTLY_CLOUDY_DAY | PARTLY_CLOUDY_NIGHT
                }
            default:
                return 7;
        }
    }


    private String getCurrentTemperature(int kelvin) {
        boolean metric = Objects.equals(GBApplication.getPrefs().getString(SettingsActivity.PREF_MEASUREMENT_SYSTEM, "metric"), "metric");
        return Math.round(metric ? kelvin2celsius(kelvin) : kelvin2fahrenheit(kelvin)) + "\u00B0";
    }

    private double kelvin2celsius(int kelvin) {
        return kelvin - 273;
    }

    private long kelvin2fahrenheit(int kelvin) {
        return Math.round((9.0 / 5) * kelvin2celsius(kelvin) + 32);
    }

    private boolean isDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return (hour <= 19 && hour >= 7); //Between 7am and 7pm
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
