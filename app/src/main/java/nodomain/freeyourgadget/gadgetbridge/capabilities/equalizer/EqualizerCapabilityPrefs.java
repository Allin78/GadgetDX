package nodomain.freeyourgadget.gadgetbridge.capabilities.equalizer;

import java.util.Map;
import java.util.Set;

import nodomain.freeyourgadget.gadgetbridge.capabilities.AbstractCapabilityPrefs;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class EqualizerCapabilityPrefs extends AbstractCapabilityPrefs {
    public static final String PREF_EQUALIZER = "pref_sony_equalizer";
    public static final String PREF_EQUALIZER_MODE = "pref_sony_equalizer_mode";
    public static final String PREF_EQUALIZER_BAND_TEMPLATE = "pref_sony_equalizer_band_%d";
    public static final String PREF_EQUALIZER_BAND_400 = "pref_sony_equalizer_band_400";
    public static final String PREF_EQUALIZER_BAND_1000 = "pref_sony_equalizer_band_1000";
    public static final String PREF_EQUALIZER_BAND_2500 = "pref_sony_equalizer_band_2500";
    public static final String PREF_EQUALIZER_BAND_6300 = "pref_sony_equalizer_band_6300";
    public static final String PREF_EQUALIZER_BAND_16000 = "pref_sony_equalizer_band_16000";
    public static final String PREF_EQUALIZER_BASS = "pref_sony_equalizer_bass";

    public EqualizerPreset preset;
    public Map<EqualizerBand, Integer> bands;
    public int bass;

    @Override
    public EqualizerCapabilityPrefs getPrefs(final Prefs devicePrefs) {
        return null;
    }
}
