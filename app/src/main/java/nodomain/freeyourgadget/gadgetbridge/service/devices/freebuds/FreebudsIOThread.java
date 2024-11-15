package nodomain.freeyourgadget.gadgetbridge.service.devices.freebuds;

import static nodomain.freeyourgadget.gadgetbridge.util.GB.hexdump;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btclassic.BtClassicIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.devices.freebuds.FreebudsDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.freebuds.FreebudsIOThread;
import nodomain.freeyourgadget.gadgetbridge.service.devices.freebuds.FreebudsProtocol;

public class FreebudsIOThread extends BtClassicIoThread {

    private static final Logger LOG = LoggerFactory.getLogger(FreebudsIOThread.class);

    private final FreebudsProtocol mFreebudsProtocol;

    @NonNull
    protected UUID getUuidToConnect(@NonNull ParcelUuid[] uuids) {
        return mFreebudsProtocol.UUID_DEVICE_CTRL;
    }

    @Override
    protected void initialize() {
        write(mFreebudsProtocol.encodeBatteryStatusReq());
        write(mFreebudsProtocol.encodeDeviceInfoReq());
        write(mFreebudsProtocol.encodeAudioModeStatusReq());
        write(mFreebudsProtocol.encodeInEarStateReq());
        setUpdateState(GBDevice.State.INITIALIZED);
    }

    public FreebudsIOThread(GBDevice device, Context context, FreebudsProtocol deviceProtocol,
                            FreebudsDeviceSupport freebudsSupport, BluetoothAdapter bluetoothAdapter) {
        super(device, context, deviceProtocol, freebudsSupport, bluetoothAdapter);
        mFreebudsProtocol = deviceProtocol;
    }

    @Override
    protected byte[] parseIncoming(InputStream inStream) throws IOException {
        byte[] buffer = new byte[1048576]; //HUGE read
        int bytes = inStream.read(buffer);
        LOG.debug("read " + bytes + " bytes. " + hexdump(buffer, 0, bytes));
        return Arrays.copyOf(buffer, bytes);
    }

}
