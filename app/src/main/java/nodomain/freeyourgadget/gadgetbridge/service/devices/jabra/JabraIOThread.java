package nodomain.freeyourgadget.gadgetbridge.service.devices.jabra;

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

public class JabraIOThread extends BtClassicIoThread {
    private static final Logger LOG = LoggerFactory.getLogger(JabraIOThread.class);
    private final JabraProtocol mJabraProtocol;
    public JabraIOThread(GBDevice device, Context context, JabraProtocol deviceProtocol,
                         JabraDeviceSupport jabraDeviceSupport, BluetoothAdapter bluetoothAdapter) {
        super(device, context, deviceProtocol, jabraDeviceSupport, bluetoothAdapter);
        //galaxyBudsProtocol = deviceProtocol;
        mJabraProtocol = deviceProtocol;
    }

    @Override
    protected void initialize() {
        setUpdateState(GBDevice.State.INITIALIZED);
    }

    @NonNull
    @Override
    protected UUID getUuidToConnect(@NonNull ParcelUuid[] uuids) {
        return UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }

    @Override
    protected byte[] parseIncoming(InputStream inStream) throws IOException {
        byte[] buffer = new byte[1048576]; //HUGE read
        int bytes = inStream.read(buffer);
        LOG.debug("read " + bytes + " bytes. " + hexdump(buffer, 0, bytes));
        return Arrays.copyOf(buffer, bytes);
    }
}
