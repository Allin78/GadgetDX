package nodomain.freeyourgadget.gadgetbridge.service.devices.galaxy_buds;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btclassic.BtClassicIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.serial.AbstractSerialDeviceSupport;

public class GalaxyBudsIOThread extends BtClassicIoThread {
    private static final Logger LOG = LoggerFactory.getLogger(GalaxyBudsIOThread.class);

    private final GalaxyBudsProtocol galaxyBudsProtocol;

    public GalaxyBudsIOThread(GBDevice gbDevice, Context context, GalaxyBudsProtocol deviceProtocol, AbstractSerialDeviceSupport deviceSupport, BluetoothAdapter btAdapter) {
        super(gbDevice, context, deviceProtocol, deviceSupport, btAdapter);
        galaxyBudsProtocol = deviceProtocol;
    }

    @NonNull
    protected UUID getUuidToConnect(@NonNull ParcelUuid[] uuids) {
        return UUID.fromString("00001102-0000-1000-8000-00805f9b34fd");
    }


    @Override
    protected byte[] parseIncoming(InputStream inStream) throws IOException {
        return new byte[0];
    }
}
