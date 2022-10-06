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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import nodomain.freeyourgadget.gadgetbridge.Logging;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiCoordinator;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiSupport;
import nodomain.freeyourgadget.gadgetbridge.util.CheckSums;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

/**
 * A repeating fetch operation (TODO: Improve docs). For every fetch, a new operation must
 * * be created, i.e. an operation may not be reused for multiple fetches.
 */
public abstract class AbstractRepeatingFetchOperation extends AbstractFetchOperation {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRepeatingFetchOperation.class);

    private final ByteArrayOutputStream byteStreamBuffer = new ByteArrayOutputStream(140);

    protected final byte dataType;

    public AbstractRepeatingFetchOperation(final HuamiSupport support, final byte dataType, final String dataName) {
        super(support);
        this.dataType = dataType;
        setName("fetching " + dataName);
    }

    @Override
    protected void startFetching(final TransactionBuilder builder) {
        LOG.info("start {}", getName());
        final GregorianCalendar sinceWhen = getLastSuccessfulSyncTime();
        startFetching(builder, dataType, sinceWhen);
    }

    /**
     * Handle the buffered activity data.
     *
     * @param timestamp The timestamp of the first sample. This function should update this to the
     *                  timestamp of the last processed sample.
     * @param bytes     the buffered bytes
     * @return true on success
     */
    protected abstract boolean handleActivityData(final GregorianCalendar timestamp, final byte[] bytes);

    @Override
    protected boolean handleActivityFetchFinish(final boolean success) {
        LOG.info("{} has finished round {}: {}, got {} bytes in buffer", getName(), fetchCount, success, byteStreamBuffer.size());

        if (!success) {
            super.handleActivityFetchFinish(false);
            return false;
        }

        if (byteStreamBuffer.size() == 0) {
            return super.handleActivityFetchFinish(true);
        }

        final byte[] bytes = byteStreamBuffer.toByteArray();
        final GregorianCalendar timestamp = (GregorianCalendar) this.startTimestamp.clone();

        final boolean handleSuccess = handleActivityData(timestamp, bytes);

        if (!handleSuccess) {
            super.handleActivityFetchFinish(false);
            return false;
        }

        timestamp.add(Calendar.MINUTE, 1);
        saveLastSyncTimestamp(timestamp);

        if (needsAnotherFetch(timestamp)) {
            byteStreamBuffer.reset();

            try {
                final boolean keepActivityDataOnDevice = HuamiCoordinator.getKeepActivityDataOnDevice(getDevice().getAddress());
                sendAck2021(keepActivityDataOnDevice);
                startFetching();
                return true;
            } catch (final IOException ex) {
                LOG.error("Error starting another round of " + getName(), ex);
                super.handleActivityFetchFinish(false);
                return false;
            }
        }

        return super.handleActivityFetchFinish(true);
    }

    @Override
    protected boolean validChecksum(final int crc32) {
        return crc32 == CheckSums.getCRC32(byteStreamBuffer.toByteArray());
    }

    @Override
    public boolean onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
        LOG.debug("characteristic read: {}: {}", characteristic.getUuid(), Logging.formatBytes(characteristic.getValue()));
        return super.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    protected void handleActivityNotif(final byte[] value) {
        LOG.debug("{} data: {}", getName(), Logging.formatBytes(value));

        if (!isOperationRunning()) {
            LOG.error("ignoring {} notification because operation is not running. Data length: {}", getName(), value.length);
            getSupport().logMessageContent(value);
            return;
        }

        if ((byte) (lastPacketCounter + 1) == value[0]) {
            // TODO we should handle skipped or repeated bytes more gracefully
            lastPacketCounter++;
            bufferActivityData(value);
        } else {
            GB.toast("Error " + getName() + ", invalid package counter: " + value[0] + ", last was: " + lastPacketCounter, Toast.LENGTH_LONG, GB.ERROR);
            handleActivityFetchFinish(false);
        }
    }

    @Override
    protected void bufferActivityData(final byte[] value) {
        byteStreamBuffer.write(value, 1, value.length - 1); // skip the counter
    }

    private boolean needsAnotherFetch(final GregorianCalendar lastSyncTimestamp) {
        final long lastFetchRange = lastSyncTimestamp.getTimeInMillis() - startTimestamp.getTimeInMillis();
        if (lastFetchRange < 1000L) {
            LOG.warn("Fetch round {} of {} got {} ms of data, stopping to avoid infinite loop", fetchCount, getName(), lastFetchRange);
            return false;
        }

        if (fetchCount > 10) {
            LOG.warn("Already have {} fetch rounds for {}, not doing another one", fetchCount, getName());
            return false;
        }

        if (lastSyncTimestamp.getTimeInMillis() >= System.currentTimeMillis()) {
            LOG.warn("Not doing another fetch since last synced timestamp is in the future: {}", lastSyncTimestamp.getTime());
            return false;
        }

        LOG.info("Doing another fetch since last sync timestamp is still too old: {}", lastSyncTimestamp.getTime());
        return true;
    }
}
