package nodomain.freeyourgadget.gadgetbridge.activities.charts;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.zetime.ZeTimeSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.ZeTimeActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SleepAnalysisTest {

    private class TestActivitySample implements ActivitySample {
        final int heartRate;
        final int kind;
        final long timestamp;

        TestActivitySample(int heartRate, int kind, long timestamp) {
            this.heartRate = heartRate;
            this.kind = kind;
            this.timestamp = timestamp;
        }

        @Override
        public int getHeartRate() {
            return heartRate;
        }

        @Override
        public float getIntensity() {
            return 0;
        }

        @Override
        public int getKind() {
            return kind;
        }

        @Override
        public SampleProvider getProvider() {
            return null;
        }

        @Override
        public int getRawIntensity() {
            return 0;
        }

        @Override
        public int getRawKind() {
            return 0;
        }


        @Override
        public int getSteps() {
            return 0;
        }

        @Override
        public int getTimestamp() {
            return (int) timestamp;
        }

        @Override
        public void setHeartRate(int value) {

        }
    }

    @Test
    public void testEmptySleepSession() {
        final SleepAnalysis sleepAnalysis = new SleepAnalysis();
        final List<ActivitySample> fixture = Collections.emptyList();
        assertTrue(sleepAnalysis.calculateSleepSessions(fixture).isEmpty());
    }

    @Test
    public void testSingleSleepSession() {
        final SleepAnalysis sleepAnalysis = new SleepAnalysis();
        final ZeTimeActivitySample[] fixture = {
                new ZeTimeActivitySample(1597440540, 1, 1, 0, 1, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597441830, 1, 1, 0, 0, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597442010, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597444470, 1, 1, 0, 0, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597444770, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597445490, 1, 1, 0, 4, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597446630, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597447170, 1, 1, 0, 0, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597447307, 1, 1, 0, 0, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597447310, 1, 1, 0, 0, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597447683, 1, 1, 0, 0, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597448523, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597449243, 1, 1, 0, 4, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597452063, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597452783, 1, 1, 0, 4, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597453323, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597454043, 1, 1, 0, 4, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597456323, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597457043, 1, 1, 0, 4, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597458243, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597458963, 1, 1, 0, 4, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597460763, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597464783, 1, 1, 0, 4, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597466343, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597467483, 1, 1, 0, 4, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597468863, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597469823, 1, 1, 0, 4, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597471143, 1, 1, 0, 2, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597476603, 1, 1, 0, 0, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597476750, 1, 1, 0, 0, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597476759, 1, 1, 0, 0, 0, 0, 0, 0, 0),
                new ZeTimeActivitySample(1597478340, 1, 1, 0, 1, 0, 0, 0, 0, 0),
        };
        final ZeTimeSampleProvider provider = new ZeTimeSampleProvider(null, null);
        for (final ZeTimeActivitySample sample : fixture) {
            sample.setProvider(provider);
        }
        final List<SleepAnalysis.SleepSession> result = sleepAnalysis.calculateSleepSessions(Arrays.asList(fixture));
        assertEquals("Failed size: " + result.toString(), 1, sleepAnalysis.calculateSleepSessions(Arrays.asList(fixture)).size());
        assertEquals(1597441830, result.get(0).getSleepStart().getTime()/1000);
        assertEquals(1597476759, result.get(0).getSleepEnd().getTime()/1000);
        assertEquals(18900, result.get(0).getLightSleepDuration());
        assertEquals(14040, result.get(0).getDeepSleepDuration());
    }
}