/*  Copyright (C) 2019-2024 Andreas Shimokawa, Carsten Pfeiffer, Daniel
    Dakhno, José Rebelo, Petr Vaněk

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
package nodomain.freeyourgadget.gadgetbridge.model;

import android.content.Context;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.activities.charts.ActivityAnalysis;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.AbstractActivitySample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;


public class DailyTotals implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(DailyTotals.class);

    private final long steps;
    private final long distance;
    private final long[] sleep;  // light deep rem awake

    public DailyTotals() {
        this(0, 0, new long[]{0, 0, 0 ,0});
    }

    public DailyTotals(final long steps, final long distance, final long[] sleep) {
        this.steps = steps;
        this.distance = distance;
        this.sleep = sleep;
    }

    public long getSteps() {
        return steps;
    }

    public long getDistance() {
        return distance;
    }

    public long getSleep() {
        return (long) Arrays.stream(sleep).asDoubleStream().sum();
    }

    public static DailyTotals getDailyTotalsForDevice(GBDevice device, Calendar day) {

        try (DBHandler handler = GBApplication.acquireDB()) {
            return getDailyTotalsForDevice(device, day, handler);
        } catch (Exception e) {
            //GB.toast("Error loading sleep/steps widget data for device: " + device, Toast.LENGTH_SHORT, GB.ERROR, e);
            return new DailyTotals();
        }
    }

    public static DailyTotals getDailyTotalsForDevice(GBDevice device, Calendar day, DBHandler handler) {
        ActivityAnalysis analysis = new ActivityAnalysis();
        ActivityAmounts amountsSteps;
        ActivityAmounts amountsSleep;

        amountsSteps = analysis.calculateActivityAmounts(getSamplesOfDay(handler, day, 0, device));
        amountsSleep = analysis.calculateActivityAmounts(getSamplesOfDay(handler, day, -12, device));

        long[] sleep = getTotalsSleepForActivityAmounts(amountsSleep);
        Pair<Long, Long> stepsDistance = getTotalsStepsForActivityAmounts(amountsSteps);

        // Purposely not including awake sleep
        return new DailyTotals(stepsDistance.getLeft(), stepsDistance.getRight(), sleep);
    }

    private static long[] getTotalsSleepForActivityAmounts(ActivityAmounts activityAmounts) {
        long totalSecondsDeepSleep = 0;
        long totalSecondsLightSleep = 0;
        long totalSecondsRemSleep = 0;
        long totalSecondsAwakeSleep = 0;
        for (ActivityAmount amount : activityAmounts.getAmounts()) {
            if (amount.getActivityKind() == ActivityKind.DEEP_SLEEP) {
                totalSecondsDeepSleep += amount.getTotalSeconds();
            } else if (amount.getActivityKind() == ActivityKind.LIGHT_SLEEP) {
                totalSecondsLightSleep += amount.getTotalSeconds();
            } else if (amount.getActivityKind() == ActivityKind.REM_SLEEP) {
                totalSecondsRemSleep += amount.getTotalSeconds();
            } else if (amount.getActivityKind() == ActivityKind.AWAKE_SLEEP) {
                totalSecondsAwakeSleep += amount.getTotalSeconds();
            }
        }
        long totalMinutesDeepSleep = (totalSecondsDeepSleep / 60);
        long totalMinutesLightSleep = (totalSecondsLightSleep / 60);
        long totalMinutesRemSleep = (totalSecondsRemSleep / 60);
        long totalMinutesAwakeSleep = (totalSecondsAwakeSleep / 60);
        return new long[]{totalMinutesLightSleep, totalMinutesDeepSleep, totalMinutesRemSleep, totalMinutesAwakeSleep};
    }

    public static Pair<Long, Long> getTotalsStepsForActivityAmounts(ActivityAmounts activityAmounts) {
        long totalSteps = 0;
        long totalDistance = 0;

        for (ActivityAmount amount : activityAmounts.getAmounts()) {
            totalSteps += amount.getTotalSteps();
            totalDistance += amount.getTotalDistance();
        }
        return Pair.of(totalSteps, totalDistance);
    }

    private static List<? extends ActivitySample> getSamplesOfDay(DBHandler db, Calendar day, int offsetHours, GBDevice device) {
        int startTs;
        int endTs;

        day = (Calendar) day.clone(); // do not modify the caller's argument
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.add(Calendar.HOUR, offsetHours);

        startTs = (int) (day.getTimeInMillis() / 1000);
        endTs = startTs + 24 * 60 * 60 - 1;

        return getSamples(db, device, startTs, endTs);
    }

    public static List<? extends ActivitySample> getSamples(DBHandler db, GBDevice device, int tsFrom, int tsTo) {
        return getAllSamples(db, device, tsFrom, tsTo);
    }

    protected static SampleProvider<? extends AbstractActivitySample> getProvider(DBHandler db, GBDevice device) {
        DeviceCoordinator coordinator = device.getDeviceCoordinator();
        return coordinator.getSampleProvider(device, db.getDaoSession());
    }

    protected static List<? extends ActivitySample> getAllSamples(DBHandler db, GBDevice device, int tsFrom, int tsTo) {
        SampleProvider<? extends ActivitySample> provider = getProvider(db, device);
        return provider.getAllActivitySamples(tsFrom, tsTo);
    }

    public static ActivitySample getFirstSample(DBHandler db, GBDevice device) {
        SampleProvider<? extends ActivitySample> provider = getProvider(db, device);
        return provider.getFirstActivitySample();
    }

}
