package nodomain.freeyourgadget.gadgetbridge.capabilities.equalizer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import nodomain.freeyourgadget.gadgetbridge.capabilities.AbstractCapability;
import nodomain.freeyourgadget.gadgetbridge.capabilities.AbstractCapabilityImpl;

public class EqualizerCapability extends AbstractCapability {
    public List<EqualizerPreset> supportedPresets = Collections.emptyList();
    public Set<EqualizerPreset> customPresets = Collections.emptySet();

    public int bandMin = 0;
    public int bandMax = 0;
    public List<EqualizerBand> supportedBands = Collections.emptyList();

    public boolean supportsBass = false;
    public int bassMin = 0;
    public int bassMax = 0;

    @Override
    public AbstractCapabilityImpl getImplementation() {
        return new EqualizerCapabilityImpl();
    }
}
