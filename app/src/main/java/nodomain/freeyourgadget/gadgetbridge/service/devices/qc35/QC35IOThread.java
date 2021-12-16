package nodomain.freeyourgadget.gadgetbridge.service.devices.qc35;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btclassic.BtClassicIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.serial.AbstractSerialDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;

public class QC35IOThread extends BtClassicIoThread {
    public QC35IOThread(GBDevice gbDevice, Context context, GBDeviceProtocol deviceProtocol, AbstractSerialDeviceSupport deviceSupport, BluetoothAdapter btAdapter) {
        super(gbDevice, context, deviceProtocol, deviceSupport, btAdapter);
    }

    @NonNull
    @Override
    protected UUID getUuidToConnect(@NonNull ParcelUuid[] uuids) {
        return UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected byte[] parseIncoming(InputStream inStream) throws IOException {
        byte[] buffer = new byte[inStream.available()];
        inStream.read(buffer);
        return buffer;
    }
}
