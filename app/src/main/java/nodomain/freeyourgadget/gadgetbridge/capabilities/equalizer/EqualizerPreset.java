package nodomain.freeyourgadget.gadgetbridge.capabilities.equalizer;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;

public enum EqualizerPreset {
    OFF(R.string.sony_equalizer_preset_off, "off"),
    BRIGHT(R.string.sony_equalizer_preset_bright, "bright"),
    EXCITED(R.string.sony_equalizer_preset_excited, "excited"),
    MELLOW(R.string.sony_equalizer_preset_mellow, "mellow"),
    RELAXED(R.string.sony_equalizer_preset_relaxed, "relaxed"),
    VOCAL(R.string.sony_equalizer_preset_vocal, "vocal"),
    TREBLE_BOOST(R.string.sony_equalizer_preset_treble_boost, "treble_boost"),
    BASS_BOOST(R.string.sony_equalizer_preset_bass_boost, "bass_boost"),
    SPEECH(R.string.sony_equalizer_preset_speech, "speech"),
    MANUAL(R.string.sony_equalizer_preset_manual, "manual"),
    CUSTOM_1(R.string.sony_equalizer_preset_custom_1, "custom_1"),
    CUSTOM_2(R.string.sony_equalizer_preset_custom_2, "custom_2"),
    ;

    private final int name;
    private final String value;

    EqualizerPreset(final int name, final String value) {
        this.name = name;
        this.value = value;
    }

    public int getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }
}
