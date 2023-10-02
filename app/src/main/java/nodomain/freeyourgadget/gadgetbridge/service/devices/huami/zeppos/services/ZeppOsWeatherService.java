/*  Copyright (C) 2023 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huami.zeppos.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.Huami2021Support;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.zeppos.AbstractZeppOsService;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class ZeppOsWeatherService extends AbstractZeppOsService {
    private static final Logger LOG = LoggerFactory.getLogger(ZeppOsWeatherService.class);

    private static final short ENDPOINT = 0x000e;

    public static final byte CMD_CAPABILITIES_REQUEST = 0x01;
    public static final byte CMD_CAPABILITIES_RESPONSE = 0x02;
    public static final byte CMD_LIST_GET = 0x05;
    public static final byte CMD_LIST_RET = 0x06;
    public static final byte CMD_SET = 0x07;
    public static final byte CMD_SET_ACK = 0x08;
    public static final byte CMD_SET_DEFAULT = 0x09;
    public static final byte CMD_SET_DEFAULT_ACK = 0x0a;
    public static final byte CMD_CURRENT_GET = 0x0b;
    public static final byte CMD_CURRENT_RET = 0x0c;

    public static final byte FLAG_ALARMS = 1;
    public static final byte FLAG_CURRENT_LOCATION = 2;
    public static final byte FLAG_DEFAULT_LOCATION = 4;

    private int version = -1;

    // keep track fo the secondary weathers returned by the band, so that we can correctly
    // delete them if needed
    private final Map<String, String> secondaryWeathers = new LinkedHashMap<>();

    public ZeppOsWeatherService(final Huami2021Support support) {
        super(support);
    }

    @Override
    public short getEndpoint() {
        return ENDPOINT;
    }

    @Override
    public boolean isEncrypted() {
        return false;
    }

    @Override
    public void handlePayload(final byte[] payload) {
        switch (payload[0]) {
            case CMD_CAPABILITIES_RESPONSE:
                version = payload[1] & 0xff;
                if (version != 2) {
                    LOG.warn("Unsupported weather service version {}", version);
                }
                LOG.info("Weather service version={}", version);
                break;
            case CMD_SET_ACK:
                LOG.info("Weather set ACK, status = {}", payload[1]);
                return;
            case CMD_SET_DEFAULT_ACK:
                LOG.info("Weather default location ACK, status = {}", payload[1]);
                return;
            case CMD_LIST_RET:
                parseWeatherLocationList(payload);
                return;
            case CMD_CURRENT_RET:
                parseCurrentWeather(payload);
                return;
            default:
                LOG.warn("Unexpected weather byte {}", String.format("0x%02x", payload[0]));
        }
    }

    @Override
    public void initialize(final TransactionBuilder builder) {
        requestCapabilities(builder);
        requestList(builder);
        requestCurrent(builder);
    }

    public void requestCapabilities(final TransactionBuilder builder) {
        write(builder, CMD_CAPABILITIES_REQUEST);
    }

    public void requestList(final TransactionBuilder builder) {
        write(builder, CMD_LIST_GET);
    }

    public void requestCurrent(final TransactionBuilder builder) {
        write(builder, CMD_CURRENT_GET);
    }

    public void onSendWeather(List<WeatherSpec> weatherSpecs) {
        if (weatherSpecs.size() > 5) {
            weatherSpecs = weatherSpecs.subList(0, 5);
        }
        WeatherSpec currentLocation = null;
        WeatherSpec defaultLocation = weatherSpecs.get(0);

        for (final WeatherSpec weatherSpec : weatherSpecs) {
            if (weatherSpec.isCurrentLocation == 1) {
                currentLocation = weatherSpec;
                LOG.debug("Sending ('{}', '{}') as current weather", weatherKey(currentLocation), currentLocation.location);
                break;
            }
        }

        LOG.debug("Sending ('{}', '{}') as default weather", weatherKey(defaultLocation), defaultLocation.location);

        // Weather is not sent directly to the bands, they send HTTP requests for each location.
        // When we have a weather update, set the default location to that location on the band.

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(CMD_SET_DEFAULT);

            byte flags = FLAG_ALARMS | FLAG_DEFAULT_LOCATION;

            if (currentLocation != null) {
                flags |= FLAG_CURRENT_LOCATION;
            }

            baos.write(flags);
            baos.write((byte) 0x01); // alarm ?

            // Current location
            if (currentLocation != null) {
                baos.write(weatherKey(currentLocation).getBytes(StandardCharsets.UTF_8));
                baos.write((byte) 0x00);
                baos.write(currentLocation.location.getBytes(StandardCharsets.UTF_8));
                baos.write((byte) 0x00);
            } else {
                baos.write((byte) 0x00);
                baos.write((byte) 0x00);
            }

            // Default location
            baos.write(weatherKey(defaultLocation).getBytes(StandardCharsets.UTF_8));
            baos.write((byte) 0x00);
            baos.write(defaultLocation.location.getBytes(StandardCharsets.UTF_8));
            baos.write((byte) 0x00);

            write("set weather current and default", baos.toByteArray());
        } catch (final Exception e) {
            LOG.error("Failed to set weather location", e);
        }

        final Set<String> knownWeatherKeys = new HashSet<>();
        for (final WeatherSpec weatherSpec : weatherSpecs) {
            knownWeatherKeys.add(weatherKey(weatherSpec));
        }

        // Delete secondary weather locations that were not sent
        final Iterator<String> secondaryWeathersIter = secondaryWeathers.keySet().iterator();
        while (secondaryWeathersIter.hasNext()) {
            final String key = secondaryWeathersIter.next();
            if (!knownWeatherKeys.contains(key)) {
                deleteSecondaryWeather(key, secondaryWeathers.get(key));
                secondaryWeathersIter.remove();
            }
        }

        // Create new secondary weather locations that do not yet exist on the watch
        for (final WeatherSpec weatherSpec : weatherSpecs) {
            if (weatherSpec.equals(currentLocation) || weatherSpec.equals(defaultLocation)) {
                continue;
            }
            createSecondaryWeather(weatherSpec);
            secondaryWeathers.put(weatherKey(weatherSpec), weatherSpec.location);
        }
    }

    private void createSecondaryWeather(final WeatherSpec weatherSpec) {
        LOG.debug("Sending ('{}', '{}') as secondary weather", weatherKey(weatherSpec), weatherSpec.location);
        setSecondaryWeather(weatherKey(weatherSpec), weatherSpec.location, (byte) 0x01);
    }

    private void deleteSecondaryWeather(final String key, final String name) {
        LOG.debug("Deleting ('{}', '{}') as secondary weather", key, name);
        setSecondaryWeather(key, name, (byte) 0x00);
    }

    private void setSecondaryWeather(final String key, final String name, final byte op) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(CMD_SET);

            // Current location
            baos.write(key.getBytes(StandardCharsets.UTF_8));
            baos.write((byte) 0x00);
            baos.write(name.getBytes(StandardCharsets.UTF_8));
            baos.write((byte) 0x00);
            baos.write(op);

            write("set secondary weather", baos.toByteArray());
        } catch (final Exception e) {
            LOG.error("Failed to set secondary weather", e);
        }
    }

    public static String weatherKey(final WeatherSpec weatherSpec) {
        if (weatherSpec.latitude != 0 && weatherSpec.longitude != 0) {
            return String.format(Locale.ROOT, "%.3f,%.3f,xiaomi-accu:1234567890", weatherSpec.latitude, weatherSpec.longitude);
        } else {
            return "1.234,-5.678,xiaomi_accu:1234567890";
        }
    }

    private void parseWeatherLocationList(final byte[] payload)  {
        final ByteBuffer buf = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);
        buf.get(); // discard first byte

        final int status = buf.get() & 0xff;
        if (status != 1) {
            LOG.warn("Unexpected weather list status {}", status);
            return;
        }

        secondaryWeathers.clear();

        final int numLocations = buf.get() & 0xff;
        LOG.info("Got weather list from device, {} locations", numLocations);

        for (int i = 0; i < numLocations; i++) {
            final String key = StringUtils.untilNullTerminator(buf);
            final String name = StringUtils.untilNullTerminator(buf);

            LOG.debug("Weather[{}]: key={}, name={}", i, key, name);
            secondaryWeathers.put(key, name);
        }

        if (buf.position() < buf.limit()) {
            LOG.warn("There are {} data bytes still in the buffer", (buf.limit() - buf.position()));
        }
    }

    private void parseCurrentWeather(final byte[] payload)  {
        LOG.info("Got current weather location from device");
        // TODO do we need this?
    }
}
