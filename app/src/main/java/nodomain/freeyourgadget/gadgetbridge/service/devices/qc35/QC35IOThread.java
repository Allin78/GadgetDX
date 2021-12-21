package nodomain.freeyourgadget.gadgetbridge.service.devices.qc35;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btclassic.BtClassicIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.serial.AbstractSerialDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;

public class QC35IOThread extends BtClassicIoThread {
    QC35Protocol protocol;
    byte[] buffer = new byte[1024];

    private Logger logger = LoggerFactory.getLogger(getClass());

    public QC35IOThread(GBDevice gbDevice, Context context, QC35Protocol deviceProtocol, AbstractSerialDeviceSupport deviceSupport, BluetoothAdapter btAdapter) {
        super(gbDevice, context, deviceProtocol, deviceSupport, btAdapter);
        this.protocol = deviceProtocol;
    }

    @NonNull
    @Override
    protected UUID getUuidToConnect(@NonNull ParcelUuid[] uuids) {
        return UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected void initialize() {
        super.initialize();

        write(protocol.encodeSendConfiguration(DeviceSettingsPreferenceConst.PREF_QC35_NOISE_CANCELLING_LEVEL));
    }
    @Override
    protected byte[] parseIncoming(InputStream inStream) throws IOException {
        int size = inStream.read(buffer);
        logger.debug("read bytes: {}", size);
        byte[] actual = new byte[size];
        System.arraycopy(buffer, 0, actual, 0, size);
        return actual;
    }
}
