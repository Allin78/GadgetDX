package nodomain.freeyourgadget.gadgetbridge.devices.banglejs.sleep;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.entities.AbstractActivitySample;

public interface SleepClassificationFeatureExtractor {

    float[] getVector(List<AbstractActivitySample> featureSamples);

    int getVectorSize();
}
