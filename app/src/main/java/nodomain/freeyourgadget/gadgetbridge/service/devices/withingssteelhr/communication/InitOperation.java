package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication;

import java.io.IOException;

import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEOperation;
import nodomain.freeyourgadget.gadgetbridge.service.btle.Transaction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;

public class InitOperation extends AbstractBTLEOperation<WithingsSteelHRDeviceSupport> {

    public InitOperation(WithingsSteelHRDeviceSupport support) {
        super(support);
    }

    @Override
    protected void doPerform() throws IOException {
    }
}
