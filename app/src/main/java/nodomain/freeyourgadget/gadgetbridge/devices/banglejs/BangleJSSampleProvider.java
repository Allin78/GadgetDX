/*  Copyright (C) 2020-2024 Gordon Williams, José Rebelo

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.devices.banglejs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.banglejs.sleep.BangleJsSleepFeatureExtractor;
import nodomain.freeyourgadget.gadgetbridge.devices.banglejs.sleep.SleepClassificationModel;
import nodomain.freeyourgadget.gadgetbridge.devices.banglejs.sleep.SleepLabelEnum;
import nodomain.freeyourgadget.gadgetbridge.entities.BangleJSActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.BangleJSActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;

public class BangleJSSampleProvider extends AbstractSampleProvider<BangleJSActivitySample> {
    private static final Logger LOG = LoggerFactory.getLogger(BangleJSSampleProvider.class);
    private SleepClassificationModel sleepClassificationModel = null;

    public static final int TYPE_ACTIVITY = 0;

    public BangleJSSampleProvider(GBDevice device, DaoSession session) {
        super(device, session);
        try {
            this.sleepClassificationModel = new SleepClassificationModel("models/model.tflite", new BangleJsSleepFeatureExtractor(), 12, 600);
        } catch (IOException e) {
            LOG.error("Could not load Sleep classification model.");
        }
    }

    @Override
    protected List<BangleJSActivitySample> getGBActivitySamples(final int timestamp_from, final int timestamp_to, final int activityType) {
        final List<BangleJSActivitySample> samples = super.getGBActivitySamples(timestamp_from, timestamp_to, activityType);

        overlaySleep(samples, timestamp_from, timestamp_to, activityType);

        return samples;
    }

    private void overlaySleep(List<BangleJSActivitySample> samples_to_predict, int timestampFrom, int timestampTo, final int activityType) {
        List<BangleJSActivitySample> allSamples = super.getGBActivitySamples(timestampFrom - sleepClassificationModel.getInputTimeLength(), timestampFrom, activityType);
        allSamples.addAll(samples_to_predict);

        int startSleep =-1;
        for (int curTimestamp = timestampFrom; curTimestamp <= timestampTo; curTimestamp += sleepClassificationModel.getTimeSliceLength()) {


            int timestampFromNewLabel=curTimestamp - sleepClassificationModel.getTimeSliceLength();
            SleepLabelEnum label = this.sleepClassificationModel.predict(allSamples, curTimestamp);

            if(startSleep==-1 && (label == SleepLabelEnum.DEEP || label==SleepLabelEnum.LIGHT)){
                startSleep = curTimestamp-sleepClassificationModel.getTimeSliceLength();
            } else if (startSleep!=-1 && (label == SleepLabelEnum.UNKNOWN || label==SleepLabelEnum.WAKE)) {
                if(Math.abs(curTimestamp-startSleep) < 30*60) { // Min of 30 min consecutive sleep
                    timestampFromNewLabel = startSleep; // This does not work correctly and is just used for testing.
                }
                startSleep = -1;
            }

            int raw_label;
            switch (label) {
                case WAKE:
                    raw_label = TYPE_ACTIVITY;
                    break;
                case LIGHT:
                    raw_label = ActivityKind.TYPE_LIGHT_SLEEP;
                    break;
                case DEEP:
                    raw_label = ActivityKind.TYPE_DEEP_SLEEP;
                    break;
                default:
                    raw_label = ActivityKind.TYPE_UNKNOWN;
            }
            for (BangleJSActivitySample sample : samples_to_predict) {
                if (timestampFromNewLabel < sample.getTimestamp() && sample.getTimestamp() <= curTimestamp) {

                    sample.setRawKind(raw_label != ActivityKind.TYPE_UNKNOWN ? raw_label : sample.getRawKind());
                }
            }
        }



    }


    @Override
    public AbstractDao<BangleJSActivitySample, ?> getSampleDao() {
        return getSession().getBangleJSActivitySampleDao();
    }

    @Nullable
    @Override
    protected Property getRawKindSampleProperty() {
        return BangleJSActivitySampleDao.Properties.RawKind;
    }

    @NonNull
    @Override
    protected Property getTimestampSampleProperty() {
        return BangleJSActivitySampleDao.Properties.Timestamp;
    }

    @NonNull
    @Override
    protected Property getDeviceIdentifierSampleProperty() {
        return BangleJSActivitySampleDao.Properties.DeviceId;
    }

    @Override
    public int normalizeType(int rawType) {
        switch (rawType) {
            case TYPE_ACTIVITY:
                return ActivityKind.TYPE_ACTIVITY;
            case ActivityKind.TYPE_LIGHT_SLEEP:
                return ActivityKind.TYPE_LIGHT_SLEEP;
            case ActivityKind.TYPE_DEEP_SLEEP:
                return ActivityKind.TYPE_DEEP_SLEEP;
            default: // fall through
                return ActivityKind.TYPE_UNKNOWN;
        }
    }

    @Override
    public int toRawActivityKind(int activityKind) {
        return TYPE_ACTIVITY;
    }

    @Override
    public float normalizeIntensity(int rawIntensity) {
        return rawIntensity / 2048.0f;
    }

    @Override
    public BangleJSActivitySample createActivitySample() {
        return new BangleJSActivitySample();
    }

    /**
     * Upserts a sample in the database, avoiding duplicated samples if a sample already exists in a
     * close timestamp (within 2 minutes);
     */
    public void upsertSample(final BangleJSActivitySample sample) {
        final List<BangleJSActivitySample> nearSamples = getGBActivitySamples(
                sample.getTimestamp() - 60 * 2,
                sample.getTimestamp() + 60 * 2,
                normalizeType(sample.getRawKind())
        );

        if (nearSamples.isEmpty()) {
            // No nearest sample, just insert
            LOG.debug("No duplicate found at {}, inserting", sample.getTimestamp());
            addGBActivitySample(sample);
            return;
        }

        BangleJSActivitySample nearestSample = nearSamples.get(0);

        for (final BangleJSActivitySample s : nearSamples) {
            final int curDist = Math.abs(nearestSample.getTimestamp() - s.getTimestamp());
            final int newDist = Math.abs(sample.getTimestamp() - s.getTimestamp());
            if (newDist < curDist) {
                nearestSample = s;
            }
        }

        LOG.debug("Found {} duplicates for {}, updating nearest sample at {}", nearSamples.size(), sample.getTimestamp(), nearestSample.getTimestamp());

        if (sample.getHeartRate() != 0) {
            nearestSample.setHeartRate(sample.getHeartRate());
        }
        if (sample.getSteps() != 0) {
            nearestSample.setSteps(sample.getSteps());
        }
        if (sample.getRawIntensity() != 0) {
            nearestSample.setRawIntensity(sample.getRawIntensity());
        }

        addGBActivitySample(nearestSample);
    }
}
