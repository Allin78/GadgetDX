package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr.WithingsSteelHRSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;

/**
 * This class is need for sleep tracking as the wthings steel HR sends heartrate while sleeping in an extra activity.
 * This leads to breaking the sleep session in the sleep calculation of GB.
 */
public class SleepActivitySampleHelper {

    private static Logger logger = LoggerFactory.getLogger(SleepActivitySampleHelper.class);

    public static WithingsSteelHRActivitySample mergeIfNecessary(WithingsSteelHRSampleProvider provider, WithingsSteelHRActivitySample sample) {
        WithingsSteelHRActivitySample sleepSample = getLastSleepSample(provider, sample.getTimestamp());
        if (sleepSample != null && shouldMerge(sample)) {
            sample = doMerge(sleepSample, sample);
        }

        return sample;
    }

    private static WithingsSteelHRActivitySample getLastSleepSample(WithingsSteelHRSampleProvider provider, long timestamp) {
        List<WithingsSteelHRActivitySample> samples = provider.getActivitySamples((int)timestamp - 500, (int)timestamp);
        if (samples.isEmpty()) {
            return null;
        }

        WithingsSteelHRActivitySample lastSample = samples.get(samples.size() - 1);
        if (lastSample.getRawKind() == ActivityKind.TYPE_LIGHT_SLEEP
                || lastSample.getRawKind() == ActivityKind.TYPE_DEEP_SLEEP
                || lastSample.getRawKind() == ActivityKind.TYPE_REM_SLEEP) {
            return lastSample;
        } else {
            return null;
        }
    }

    private static boolean shouldMerge(WithingsSteelHRActivitySample sample) {
        return sample.getSteps() == 0
                && sample.getDistance() == 0
                && sample.getRawKind() < 1
                && sample.getCalories() == 0
                && sample.getHeartRate() > 1
                && sample.getRawIntensity() == 0;
    }

    private static WithingsSteelHRActivitySample doMerge(WithingsSteelHRActivitySample origin, WithingsSteelHRActivitySample update) {
        WithingsSteelHRActivitySample mergeResult = origin;
        mergeResult.setHeartRate(update.getHeartRate());
        mergeResult.setTimestamp(update.getTimestamp());
        return mergeResult;
    }
}
