package nodomain.freeyourgadget.gadgetbridge.service.devices.msftband;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.ParcelUuid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import androidx.annotation.NonNull;
import nodomain.freeyourgadget.gadgetbridge.devices.msftband.MsftBandConstants;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btclassic.BtClassicIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.serial.AbstractSerialDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class MsftBandIOThread extends BtClassicIoThread {
    /**
     * Returns the uuid to connect to.
     * Default implementation returns the first of the given uuids that were
     * read from the remote device.
     *
     * @param uuids
     * @return
     */
    @NonNull
    @Override
    protected UUID getUuidToConnect(@NonNull ParcelUuid[] uuids) {
        return MsftBandConstants.UUID_1P_COMMAND_SERVICE_01_BT;
    }

    MsftBandProtocol protocol;
    byte[] buffer = new byte[1024];

    private Logger logger = LoggerFactory.getLogger(getClass());

    public MsftBandIOThread(GBDevice gbDevice, Context context, MsftBandProtocol deviceProtocol, AbstractSerialDeviceSupport deviceSupport, BluetoothAdapter btAdapter) {
        super(gbDevice, context, deviceProtocol, deviceSupport, btAdapter);
        this.protocol = deviceProtocol;
    }

    /**
     * Returns an incoming message for consuming by the GBDeviceProtocol
     *
     * @param inStream
     * @return
     * @throws IOException
     */
    @Override
    protected byte[] parseIncoming(InputStream inStream) throws IOException {
        int size = inStream.read(buffer);
        logger.debug("read bytes: {}", size);
        byte[] actual = new byte[size];
        System.arraycopy(buffer, 0, actual, 0, size);
        logger.debug("parseIncoming: {}", StringUtils.bytesToHex(actual));
        return actual;
    }
}
