package nodomain.freeyourgadget.gadgetbridge.capabilities.equalizer;

import nodomain.freeyourgadget.gadgetbridge.R;

public enum EqualizerBand {
    BAND_400(400),
    BAND_1000(1000),
    BAND_2500(2500),
    BAND_6300(6300),
    BAND_16000(16000),
    ;

    private int frequency;

    EqualizerBand(final int frequency) {
        this.frequency = frequency;
    }

    public int getFrequency() {
        return frequency;
    }
}
