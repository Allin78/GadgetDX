package nodomain.freeyourgadget.gadgetbridge.service.devices.msftband;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class MsftBandProtocol extends GBDeviceProtocol {

    Logger logger = LoggerFactory.getLogger(getClass());

    protected MsftBandProtocol(GBDevice device) {
        super(device);
    }

    @Override
    public GBDeviceEvent[] decodeResponse(byte[] responseData) {
        logger.debug("response: {}", StringUtils.bytesToHex(responseData));

        return super.decodeResponse(responseData);
    }
}
