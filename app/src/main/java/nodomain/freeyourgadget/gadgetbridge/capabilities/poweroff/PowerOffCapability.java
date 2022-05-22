package nodomain.freeyourgadget.gadgetbridge.capabilities.poweroff;

import nodomain.freeyourgadget.gadgetbridge.capabilities.AbstractCapability;
import nodomain.freeyourgadget.gadgetbridge.capabilities.AbstractCapabilityImpl;

public class PowerOffCapability extends AbstractCapability {
    @Override
    public AbstractCapabilityImpl getImplementation() {
        return new PowerOffCapabilityImpl();
    }
}
