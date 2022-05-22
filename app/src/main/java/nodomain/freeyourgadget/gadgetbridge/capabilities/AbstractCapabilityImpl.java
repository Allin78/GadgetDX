package nodomain.freeyourgadget.gadgetbridge.capabilities;

import android.content.Context;
import android.view.ViewGroup;

import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsHandler;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public abstract class AbstractCapabilityImpl<C extends AbstractCapability, P extends AbstractCapabilityPrefs> {
    public abstract int[] getSupportedDeviceSpecificSettings(final GBDevice device);

    public abstract void registerPreferences(final Context context,
                                             final C capability,
                                             final DeviceSpecificSettingsHandler handler);

    public abstract void configureCardShortcuts(final ViewGroup infos,
                                                final GBDevice device);
}
