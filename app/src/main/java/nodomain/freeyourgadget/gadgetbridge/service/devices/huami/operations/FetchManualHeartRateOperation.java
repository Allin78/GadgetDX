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
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiSupport;

/**
 * An operation that fetches manual HR measurement data.
 */
public class FetchManualHeartRateOperation extends AbstractRepeatingFetchOperation {
    private static final Logger LOG = LoggerFactory.getLogger(FetchManualHeartRateOperation.class);

    public FetchManualHeartRateOperation(final HuamiSupport support) {
        super(support, HuamiService.COMMAND_ACTIVITY_DATA_TYPE_MANUAL_HEART_RATE, "manual hr data");
    }

    @Override
    protected boolean handleActivityData(final GregorianCalendar timestamp, final byte[] bytes) {
        if (bytes.length % 6 != 0) {
            LOG.info("Unexpected buffered manual heart rate data size {}", bytes.length);
            return false;
        }

        final ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        while (buffer.position() < bytes.length) {
            final long currentTimestamp = BLETypeConversions.toUnsigned(buffer.getInt()) * 1000;
            timestamp.setTimeInMillis(currentTimestamp);

            final byte unknown1 = buffer.get(); // always 4?
            final int hr = buffer.get() & 0xff;

            LOG.info("Manual HR at {}: {}", timestamp.getTime(), hr);

            // TODO: Save manual hr data
        }

        return true;
    }

    @Override
    protected String getLastSyncTimeKey() {
        return "lastManualHeartRateTimeMillis";
    }
}
