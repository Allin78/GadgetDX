/*  Copyright (C) 2021 - 2022 José Rebelo, Ngô Minh Quang

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btclassic.BtClassicIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.headphones.protocol.Message;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class SonyHeadphonesIoThread extends BtClassicIoThread {
    private static final Logger LOG = LoggerFactory.getLogger(SonyHeadphonesIoThread.class);

    private final SonyHeadphonesProtocol mProtocol;

    public SonyHeadphonesIoThread(
        GBDevice gbDevice,
        Context context,
        SonyHeadphonesProtocol protocol,
        SonyHeadphonesSupport support,
        BluetoothAdapter btAdapter
    ) {
        super(gbDevice, context, protocol, support, btAdapter);
        mProtocol = protocol;
    }

    @Override
    protected synchronized void initialize() {
        setUpdateState(GBDevice.State.INITIALIZING);
        mProtocol.initialize(this);
    }

    @Override
    public synchronized void quit() {
        mProtocol.quit();
        super.quit();
    }

    @Override
    public synchronized void write(byte[] bytes) {
        // Log the human-readable message, for debugging
        LOG.info("> {}", Message.fromBytes(bytes));
        super.write(bytes);
    }

    @Override
    protected byte[] parseIncoming(InputStream inputStream) throws IOException {
        final ByteArrayOutputStream msgStream = new ByteArrayOutputStream();
        final byte[] incoming = new byte[1];

        do {
            inputStream.read(incoming);

            if (incoming[0] == Message.MESSAGE_HEADER) {
                msgStream.reset();
            }

            msgStream.write(incoming);
        } while (incoming[0] != Message.MESSAGE_TRAILER);

        LOG.trace("Raw message: {}", GB.hexdump(msgStream.toByteArray()));

        return msgStream.toByteArray();
    }

    @NonNull
    @Override
    protected UUID getUuidToConnect(@NonNull ParcelUuid[] uuids) {
        return UUID.fromString("96CC203E-5068-46ad-B32D-E316F5E069BA");
    }
}
