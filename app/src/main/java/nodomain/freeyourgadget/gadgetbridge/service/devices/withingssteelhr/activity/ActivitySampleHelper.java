package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.activity;

import static nodomain.freeyourgadget.gadgetbridge.model.ActivityKind.TYPE_NOT_MEASURED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr.WithingsSteelHRSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;

public class ActivitySampleHelper {

    private static Logger logger = LoggerFactory.getLogger(ActivitySampleHelper.class);

    public static WithingsSteelHRActivitySample mergeIfNecessary(WithingsSteelHRSampleProvider provider, WithingsSteelHRActivitySample sample) {
        WithingsSteelHRActivitySample lastSample = getLastSample(provider, sample.getTimestamp());
        if (lastSample != null) {
            sample = doMerge(lastSample, sample);
        }

        return sample;
    }

    private static WithingsSteelHRActivitySample getLastSample(WithingsSteelHRSampleProvider provider, long timestamp) {
        List<WithingsSteelHRActivitySample> samples = provider.getActivitySamples((int)timestamp - 10000, (int)timestamp);
        if (samples.isEmpty()) {
            return null;
        }

        return samples.get(samples.size() - 1);
    }

    private static WithingsSteelHRActivitySample doMerge(WithingsSteelHRActivitySample origin, WithingsSteelHRActivitySample update) {
        WithingsSteelHRActivitySample mergeResult = origin;
        if (update.getDuration() != 0) {
            mergeResult.setDuration(update.getDuration());
        }

        if (update.getRawKind() != -1) {
            mergeResult.setRawKind(update.getRawKind());
        }

        if (update.getSteps() != 0) {
            mergeResult.setSteps(update.getSteps());
        }

        if (update.getDistance() != 0) {
            mergeResult.setDistance(update.getDistance());
        }

        if (update.getHeartRate() != 0) {
            mergeResult.setHeartRate(update.getHeartRate());
        }

        if (update.getRawIntensity() != 0) {
            mergeResult.setRawIntensity(update.getRawIntensity());
        }

        mergeResult.setTimestamp(update.getTimestamp());

        return mergeResult;
    }
}
