package nodomain.freeyourgadget.gadgetbridge.capabilities;

import java.util.Set;

import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public abstract class AbstractCapabilityPrefs {
    public abstract <T extends AbstractCapabilityPrefs> T getPrefs(final Prefs devicePrefs);
}
