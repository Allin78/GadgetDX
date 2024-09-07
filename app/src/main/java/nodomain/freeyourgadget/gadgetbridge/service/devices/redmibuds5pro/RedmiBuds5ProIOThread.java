package nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro;

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

public class RedmiBuds5ProIOThread extends BtClassicIoThread {
    private static final Logger LOG = LoggerFactory.getLogger(RedmiBuds5ProIOThread.class);
    private final RedmiBuds5ProProtocol redmiProtocol;

    public RedmiBuds5ProIOThread(GBDevice gbDevice, Context context, RedmiBuds5ProProtocol redmiProtocol, RedmiBuds5ProDeviceSupport deviceSupport, BluetoothAdapter btAdapter) {
        super(gbDevice, context, redmiProtocol, deviceSupport, btAdapter);
        this.redmiProtocol = redmiProtocol;
    }

    @Override
    protected byte[] parseIncoming(InputStream stream) throws IOException {
        byte[] buffer = new byte[1048576];
        int bytes = stream.read(buffer);
        LOG.debug("read " + bytes + " bytes. " + hexdump(buffer, 0, bytes));
        return Arrays.copyOf(buffer, bytes);
    }

    @NonNull
    @Override
    protected UUID getUuidToConnect(@NonNull ParcelUuid[] uuids) {
        return this.redmiProtocol.UUID_DEVICE_CTRL;
    }

    @Override
    protected void initialize() {
        write(redmiProtocol.encodeStartAuthentication());
        setUpdateState(GBDevice.State.INITIALIZING);
    }
}
