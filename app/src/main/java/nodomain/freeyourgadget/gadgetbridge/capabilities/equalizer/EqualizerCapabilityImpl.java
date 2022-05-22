package nodomain.freeyourgadget.gadgetbridge.capabilities.equalizer;

import android.content.Context;
import android.view.ViewGroup;

import androidx.preference.ListPreference;
import androidx.preference.SeekBarPreference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSpecificSettingsHandler;
import nodomain.freeyourgadget.gadgetbridge.capabilities.AbstractCapabilityImpl;
import nodomain.freeyourgadget.gadgetbridge.devices.lenovo.watchxplus.WatchXPlusSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class EqualizerCapabilityImpl extends AbstractCapabilityImpl<EqualizerCapability, EqualizerCapabilityPrefs> {
    private static final Logger LOG = LoggerFactory.getLogger(EqualizerCapabilityImpl.class);

    public int[] getSupportedDeviceSpecificSettings(final GBDevice device) {
        return new int[]{
                R.xml.devicesettings_headphones_equalizer
        };
    }

    @Override
    public void registerPreferences(final Context context, final EqualizerCapability config, final DeviceSpecificSettingsHandler handler) {
        final ListPreference mode = handler.findPreference(EqualizerCapabilityPrefs.PREF_EQUALIZER_MODE);
        if (mode != null) {
            handler.addPreferenceHandlerFor(EqualizerCapabilityPrefs.PREF_EQUALIZER_MODE);

            final String[] entries = new String[config.supportedPresets.size()];
            final String[] entryValues = new String[config.supportedPresets.size()];
            int i = 0;
            for (EqualizerPreset preset : config.supportedPresets) {
                entries[i] = context.getResources().getString(preset.getName());
                entryValues[i] = preset.getValue();

                i++;
            }
            mode.setEntries(entries);
            mode.setEntryValues(entryValues);
        }

        for (final EqualizerBand band : EqualizerBand.values()) {
            final String bandPrefKey = String.format(EqualizerCapabilityPrefs.PREF_EQUALIZER_BAND_TEMPLATE, band.getFrequency());
            final SeekBarPreference bandPreference = handler.findPreference(bandPrefKey);

            if (bandPreference == null) {
                continue;
            }

            if (config.supportedBands.contains(band)) {
                handler.addPreferenceHandlerFor(bandPrefKey);
            } else {
                bandPreference.setVisible(false);
            }
        }

        if (!config.supportsBass) {
            final SeekBarPreference bassPreference = handler.findPreference(EqualizerCapabilityPrefs.PREF_EQUALIZER_BASS);

            if (bassPreference != null) {
                handler.addPreferenceHandlerFor(EqualizerCapabilityPrefs.PREF_EQUALIZER_BASS);

                bassPreference.setVisible(false);
            }
        }
    }

    @Override
    public void configureCardShortcuts(final ViewGroup infos,
                                       final GBDevice device) {
    }
}
