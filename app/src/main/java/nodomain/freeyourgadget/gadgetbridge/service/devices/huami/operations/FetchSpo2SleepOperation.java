/*  Copyright (C) 2022 José Rebelo

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.GregorianCalendar;

import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiService;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiSupport;

/**
 * An operation that fetches SPO2 data for sleep measurements.
 */
public class FetchSpo2SleepOperation extends AbstractRepeatingFetchOperation {
    private static final Logger LOG = LoggerFactory.getLogger(FetchSpo2SleepOperation.class);

    public FetchSpo2SleepOperation(final HuamiSupport support) {
        super(support, HuamiService.COMMAND_ACTIVITY_DATA_TYPE_SPO2_SLEEP, "spo2 sleep data");
    }

    @Override
    protected boolean handleActivityData(final GregorianCalendar timestamp, final byte[] bytes) {
        if ((bytes.length - 1) % 30 != 0) {
            LOG.error("Unexpected length for sleep spo2 data {}, not divisible by 30", bytes.length);
            return false;
        }

        final ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        final int version = buf.get() & 0xff;
        if (version != 2) {
            LOG.error("Unknown sleep spo2 data version {}", version);
            return false;
        }

        while (buf.position() < bytes.length) {
            final long timestampSeconds = buf.getInt();
            final int spo2 = buf.get() & 0xff;
            final int unknown1 = buf.get() & 0xff; // ?

            // These seem to be 6 pairs of max/min (10s intervals?)
            final byte[] spo2max = new byte[6];
            final byte[] spo2min = new byte[6];

            final byte[] unknown2 = new byte[12]; // ? always 4 zeroes at the end?

            buf.get(spo2max);
            buf.get(spo2min);
            buf.get(unknown2);

            timestamp.setTimeInMillis(timestampSeconds * 1000L);

            LOG.info("SPO2 (sleep) at {}: {}", timestamp.getTime(), spo2);
            // TODO save
        }

        return true;
    }

    @Override
    protected String getLastSyncTimeKey() {
        return "lastSpo2sleepTimeMillis";
    }
}
