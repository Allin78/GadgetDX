package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;

public abstract class AbstractResponseHandler implements ResponseHandler {
    protected GBDevice device;
    protected WithingsSteelHRDeviceSupport support;

    public AbstractResponseHandler(WithingsSteelHRDeviceSupport support) {
        this.support = support;
        this.device = support.getDevice();
    }
}
