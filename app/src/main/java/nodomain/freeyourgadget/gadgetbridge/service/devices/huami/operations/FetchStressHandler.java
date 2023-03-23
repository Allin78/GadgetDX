/*  Copyright (C) 2016-2021 Andreas Shimokawa, Carsten Pfeiffer, Daniele
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
package nodomain.freeyourgadget.gadgetbridge.service.devices.huami.operations;

import android.content.Context;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiExtendedSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiService;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.HuamiExtendedActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.MiBandActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.User;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.RecordedDataTypes;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.util.DateTimeUtils;
import nodomain.freeyourgadget.gadgetbridge.util.DeviceHelper;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

/**
 * An operation that fetches activity data. For every fetch, a new operation must
 * be created, i.e. an operation may not be reused for multiple fetches.
 */
public class FetchStressHandler implements FetchHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FetchStressHandler.class);

    private final GBDevice gbDevice;
    private final Context context;

    private final boolean manual;


    public FetchStressHandler(boolean manual_, GBDevice device_, Context context_) {
        manual = manual_;
        gbDevice = device_;
        context = context_;
    }

    @Override
    public boolean handleActivityData(final GregorianCalendar timestamp, final byte[] bytes) {
        if (bytes.length % 5 != 0) {
            LOG.info("Unexpected buffered stress data size {}", bytes.length);
            return false;
        }

        final ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        final GregorianCalendar lastSyncTimestamp = new GregorianCalendar();
        ArrayList<HuamiExtendedActivitySample> samples = new ArrayList<>();

        while (buffer.position() < bytes.length) {
            final long currentTimestamp = BLETypeConversions.toUnsigned(buffer.getInt()) * 1000;

            // 0-39 = relaxed
            // 40-59 = mild
            // 60-79 = moderate
            // 80-100 = high
            final long stress = buffer.get();
            timestamp.setTimeInMillis(currentTimestamp);

            LOG.info("Stress (manual) at {}: {}", timestamp, stress);

            HuamiExtendedActivitySample sample = getSample(timestamp, (int) stress);

            samples.add(sample);

        }


        try (DBHandler handler = GBApplication.acquireDB()) {
            DaoSession session = handler.getDaoSession();

            DeviceCoordinator coordinator = DeviceHelper.getInstance().getCoordinator(gbDevice);
            HuamiExtendedSampleProvider sampleProvider = (HuamiExtendedSampleProvider) coordinator.getSampleProvider(gbDevice, session);
            Device device = DBHelper.getDevice(gbDevice, session);
            User user = DBHelper.getUser(session);

            for (MiBandActivitySample sample : samples) {
                sample.setDevice(device);
                sample.setUser(user);
                sample.setProvider(sampleProvider);
            }
            sampleProvider.addGBActivitySamples(samples.toArray(new HuamiExtendedActivitySample[0]));

            LOG.info("Huami activity data: last sample timestamp: {}", DateTimeUtils.formatDateTime(timestamp.getTime()));
            return true;
        } catch (Exception ex) {
            GB.toast(context, "Error saving activity samples", Toast.LENGTH_LONG, GB.ERROR);
            LOG.error("Error saving activity samples", ex);
            return false;
        }
    }
    private HuamiExtendedActivitySample getSample(GregorianCalendar timestamp, int stress) {
        HuamiExtendedActivitySample sample = new HuamiExtendedActivitySample();
        sample.setTimestamp((int) (timestamp.getTimeInMillis() / 1000));
        sample.setRawKind(HuamiExtendedSampleProvider.TYPE_CUSTOM_UNSET);
        sample.setStress(stress);
        return sample;
    }


    @Override
    public String getLastSyncTimeKey() {
        return "lastSyncStressTimeMillis";
    }

    @Override
    public byte getCommandDataType() {
        return HuamiService.COMMAND_ACTIVITY_DATA_TYPE_STRESS_MANUAL;
    }

    public int getDataType() {
        return RecordedDataTypes.TYPE_STRESS;
    }

    @Override
    public String getDataName() {
        return "stress data";
    }
}
