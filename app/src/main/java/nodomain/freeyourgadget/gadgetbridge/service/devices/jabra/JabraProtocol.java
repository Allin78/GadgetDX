package nodomain.freeyourgadget.gadgetbridge.service.devices.jabra;

import static nodomain.freeyourgadget.gadgetbridge.util.GB.hexdump;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.galaxy_buds.GalaxyBudsProtocol;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;

public class JabraProtocol extends GBDeviceProtocol {
    private static final Logger LOG = LoggerFactory.getLogger(JabraProtocol.class);

    private boolean isFirstExchange = true;
    protected JabraProtocol(GBDevice device) {
        super(device);
    }

    @Override
    public GBDeviceEvent[] decodeResponse(byte[] responseData) {
        List<GBDeviceEvent> devEvts = new ArrayList<>();
        LOG.debug("received data: " + hexdump(responseData));
        LOG.debug("received data length: " + responseData.length);

        if (isFirstExchange) {
            isFirstExchange = false;
            devEvts.add(new GBDeviceEventVersionInfo()); //TODO: this is a weird hack to make the DBHelper happy. Replace with proper firmware detection
        }

        ByteBuffer incoming = ByteBuffer.wrap(responseData);
        incoming.order(ByteOrder.LITTLE_ENDIAN);


        byte sof = incoming.get();

        return devEvts.toArray(new GBDeviceEvent[devEvts.size()]);
    }
}
