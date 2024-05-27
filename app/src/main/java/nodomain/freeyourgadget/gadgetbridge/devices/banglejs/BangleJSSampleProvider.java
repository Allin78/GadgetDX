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

import android.content.res.AssetFileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.BangleJSActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.BangleJSActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;

public class BangleJSSampleProvider extends AbstractSampleProvider<BangleJSActivitySample> {
    private static final Logger LOG = LoggerFactory.getLogger(BangleJSSampleProvider.class);
    public static final int TEN_MINUTES = 600;
    private Interpreter sleepClassificationModel = null;

    public static final int TYPE_ACTIVITY = 0;

    public BangleJSSampleProvider(GBDevice device, DaoSession session) {
        super(device, session);

        try {
            this.sleepClassificationModel = new Interpreter(loadModelFile()
                    );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected List<BangleJSActivitySample> getGBActivitySamples(final int timestamp_from, final int timestamp_to, final int activityType) {
        final List<BangleJSActivitySample> samples = super.getGBActivitySamples(timestamp_from, timestamp_to, activityType);

        overlaySleep(samples, timestamp_from, timestamp_to, activityType);

        return samples;
    }

    private void overlaySleep(List<BangleJSActivitySample> samples_to_predict, int timestampFrom, int timestampTo, final int activityType) {
        final List<BangleJSActivitySample> allSamples = super.getGBActivitySamples(timestampFrom - 12 * TEN_MINUTES, timestampFrom, activityType);
        allSamples.addAll(samples_to_predict);

        for (int curTimestamp = timestampFrom; curTimestamp <= timestampTo; curTimestamp += TEN_MINUTES) {
            float[][] output = new float[1][3];
            float[][][] input = this.createModelInput(allSamples, curTimestamp);
            sleepClassificationModel.allocateTensors();
            sleepClassificationModel.run(input, output);
            int label = getSleepLabel(output[0]);

            for (BangleJSActivitySample sample : samples_to_predict) {
                if (curTimestamp - TEN_MINUTES < sample.getTimestamp() && sample.getTimestamp() <= curTimestamp) {
                    sample.setRawKind(label != ActivityKind.TYPE_UNKNOWN ? label : sample.getRawKind());
                }
            }
        }

    }

    private int getSleepLabel(float[] prediction) {
        int label_idx = -1;
        float max_val = 0;
        for (int i = 0; i < prediction.length; i++) {
            if (prediction[i] > max_val) {
                max_val = prediction[i];
                label_idx = i;
            }
        }

        switch (label_idx) {
            case 0:
                return ActivityKind.TYPE_ACTIVITY;
            case 1:
                return ActivityKind.TYPE_LIGHT_SLEEP;
            case 2:
                return ActivityKind.TYPE_DEEP_SLEEP;
            default:
                return ActivityKind.TYPE_UNKNOWN;
        }
    }

    private float[][][] createModelInput(List<BangleJSActivitySample> allSamples, int timestampTo) {
        float[][][] result = new float[1][12][6];
        int timestampFrom = timestampTo - 12 * TEN_MINUTES;
        for (int i = 0; i < 12; i++) {
            int curTimestamp = timestampFrom + i * TEN_MINUTES;
            result[0][i] = this.createFeature(allSamples, curTimestamp, curTimestamp + TEN_MINUTES);
        }
        return result;
    }

    private float[] createFeature(List<BangleJSActivitySample> allSamples, int timestampFrom, int timestampTo) {
        ArrayList<Double> heartrates = new ArrayList<>();
        double hrSum = 0;
        double hr_min = Double.MAX_VALUE;
        double hr_max = Double.MIN_VALUE;
        int step_count = 0;
        int movement=0;
        for (int i = 0; i < allSamples.size(); i++) {
            BangleJSActivitySample entry = allSamples.get(i);
            if (entry.getTimestamp() <= timestampTo && entry.getTimestamp() >= timestampFrom) {
                double currentHr = entry.getHeartRate();
                hrSum += currentHr;
                heartrates.add(currentHr);
                step_count+=entry.getSteps();
                movement+=entry.getRawIntensity();


                if (currentHr < hr_min) {
                    hr_min = currentHr;
                }
                if (currentHr > hr_max) {
                    hr_max = currentHr;
                }
            }
        }


        double hr_mean = hrSum / heartrates.size();
        double hr_std = 0;
        for (double currentHeartrate : heartrates) {
            hr_std += Math.pow(currentHeartrate - hr_mean, 2);
        }
        hr_std = Math.sqrt(hr_std / heartrates.size());


        float[] features = new float[6];

        features[0] = (float) step_count/1500;
        features[1] = this.normalizeIntensity(movement);
        features[2] = (float) (hr_mean-30) / 185;
        features[3] = (float) (hr_min -30) / 185;
        features[4] = (float) (hr_max -30) / 185;
        features[5] = (float) (hr_std / 60);

        return features;
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        try(AssetFileDescriptor fileDescriptor = GBApplication.getContext().getAssets().openFd("models/model.tflite")) {
            try(FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
                FileChannel fileChannel = inputStream.getChannel();
                long startOffset = fileDescriptor.getStartOffset();
                long declareLength = fileDescriptor.getDeclaredLength();
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength);
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
