package nodomain.freeyourgadget.gadgetbridge.capabilities;

import nodomain.freeyourgadget.gadgetbridge.capabilities.equalizer.EqualizerCapabilityImpl;

public abstract class AbstractCapability<C extends AbstractCapabilityImpl> {
    public abstract C getImplementation();
}
