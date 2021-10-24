package nodomain.freeyourgadget.gadgetbridge.service.devices.galaxy_buds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;

public class GalaxyBudsProtocol extends GBDeviceProtocol {
    private static final Logger LOG = LoggerFactory.getLogger(GalaxyBudsProtocol.class);

    protected GalaxyBudsProtocol(GBDevice device) {
        super(device);
    }
}
