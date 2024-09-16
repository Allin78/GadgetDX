/*  Copyright (C) 2023-2024 Daniel Dakhno, José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.activities.charts;

import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.HeartRateUtils;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.AbstractActivitySample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public abstract class AbstractActivityChartFragment<D extends ChartsData> extends AbstractChartFragment<D>  {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractActivityChartFragment.class);

    public static final float Y_VALUE_DEEP_SLEEP = 0.01f;

    public boolean supportsHeartrate(GBDevice device) {
        DeviceCoordinator coordinator = device.getDeviceCoordinator();
        return coordinator.supportsHeartRateMeasurement(device);
    }

    public boolean supportsRemSleep(GBDevice device) {
        DeviceCoordinator coordinator = device.getDeviceCoordinator();
        return coordinator.supportsRemSleep();
    }

    public boolean supportsAwakeSleep(GBDevice device) {
        DeviceCoordinator coordinator = device.getDeviceCoordinator();
        return coordinator.supportsAwakeSleep();
    }

    protected static final class ActivityConfig {
        public final ActivityKind type;
        public final String label;
        public final Integer color;

        public ActivityConfig(ActivityKind kind, String label, Integer color) {
            this.type = kind;
            this.label = label;
            this.color = color;
        }
    }

    protected ActivityConfig akActivity;
    protected ActivityConfig akLightSleep;
    protected ActivityConfig akDeepSleep;
    protected ActivityConfig akRemSleep;
    protected ActivityConfig akAwakeSleep;
    protected ActivityConfig akNotWorn;

    protected int BACKGROUND_COLOR;
    protected int DESCRIPTION_COLOR;
    protected int CHART_TEXT_COLOR;
    protected int LEGEND_TEXT_COLOR;
    protected int HEARTRATE_COLOR;
    protected int HEARTRATE_FILL_COLOR;
    protected int AK_ACTIVITY_COLOR;
    protected int AK_DEEP_SLEEP_COLOR;
    protected int AK_REM_SLEEP_COLOR;
    protected int AK_AWAKE_SLEEP_COLOR;
    protected int AK_LIGHT_SLEEP_COLOR;
    protected int AK_NOT_WORN_COLOR;

    protected String HEARTRATE_LABEL;
    protected String HEARTRATE_AVERAGE_LABEL;

    @Override
    protected void init() {
        Prefs prefs = GBApplication.getPrefs();
        TypedValue runningColor = new TypedValue();
        BACKGROUND_COLOR = GBApplication.getBackgroundColor(getContext());
        LEGEND_TEXT_COLOR = DESCRIPTION_COLOR = GBApplication.getTextColor(getContext());
        CHART_TEXT_COLOR = GBApplication.getSecondaryTextColor(getContext());
        if (prefs.getBoolean("chart_heartrate_color", false)) {
            HEARTRATE_COLOR = ContextCompat.getColor(getContext(), R.color.chart_heartrate_alternative);
        }else{
            HEARTRATE_COLOR = ContextCompat.getColor(getContext(), R.color.chart_heartrate);
        }
        HEARTRATE_FILL_COLOR = ContextCompat.getColor(getContext(), R.color.chart_heartrate_fill);

        getContext().getTheme().resolveAttribute(R.attr.chart_activity, runningColor, true);
        AK_ACTIVITY_COLOR = runningColor.data;
        getContext().getTheme().resolveAttribute(R.attr.chart_deep_sleep, runningColor, true);
        AK_DEEP_SLEEP_COLOR = runningColor.data;
        getContext().getTheme().resolveAttribute(R.attr.chart_light_sleep, runningColor, true);
        AK_LIGHT_SLEEP_COLOR = runningColor.data;
        getContext().getTheme().resolveAttribute(R.attr.chart_rem_sleep, runningColor, true);
        AK_REM_SLEEP_COLOR = runningColor.data;
        getContext().getTheme().resolveAttribute(R.attr.chart_awake_sleep, runningColor, true);
        AK_AWAKE_SLEEP_COLOR = runningColor.data;
        getContext().getTheme().resolveAttribute(R.attr.chart_not_worn, runningColor, true);
        AK_NOT_WORN_COLOR = runningColor.data;

        HEARTRATE_LABEL = getContext().getString(R.string.charts_legend_heartrate);
        HEARTRATE_AVERAGE_LABEL = getContext().getString(R.string.charts_legend_heartrate_average);

        akActivity = new ActivityConfig(ActivityKind.ACTIVITY, getString(R.string.abstract_chart_fragment_kind_activity), AK_ACTIVITY_COLOR);
        akLightSleep = new ActivityConfig(ActivityKind.LIGHT_SLEEP, getString(R.string.abstract_chart_fragment_kind_light_sleep), AK_LIGHT_SLEEP_COLOR);
        akDeepSleep = new ActivityConfig(ActivityKind.DEEP_SLEEP, getString(R.string.abstract_chart_fragment_kind_deep_sleep), AK_DEEP_SLEEP_COLOR);
        akRemSleep = new ActivityConfig(ActivityKind.REM_SLEEP, getString(R.string.abstract_chart_fragment_kind_rem_sleep), AK_REM_SLEEP_COLOR);
        akAwakeSleep = new ActivityConfig(ActivityKind.REM_SLEEP, getString(R.string.abstract_chart_fragment_kind_awake_sleep), AK_AWAKE_SLEEP_COLOR);
        akNotWorn = new ActivityConfig(ActivityKind.NOT_WORN, getString(R.string.abstract_chart_fragment_kind_not_worn), AK_NOT_WORN_COLOR);
    }

    protected Integer getColorFor(ActivityKind activityKind) {
        switch (activityKind) {
            case DEEP_SLEEP:
                return akDeepSleep.color;
            case LIGHT_SLEEP:
                return akLightSleep.color;
            case REM_SLEEP:
                return akRemSleep.color;
            case AWAKE_SLEEP:
                return akAwakeSleep.color;
            case ACTIVITY:
                return akActivity.color;
        }
        return akActivity.color;
    }

    protected SampleProvider<? extends AbstractActivitySample> getProvider(DBHandler db, GBDevice device) {
        DeviceCoordinator coordinator = device.getDeviceCoordinator();
        return coordinator.getSampleProvider(device, db.getDaoSession());
    }

    /**
     * Returns all kinds of samples for the given device.
     * To be called from a background thread.
     *
     * @param device
     * @param tsFrom
     * @param tsTo
     */
    protected List<? extends ActivitySample> getAllSamples(DBHandler db, GBDevice device, int tsFrom, int tsTo) {
        SampleProvider<? extends ActivitySample> provider = getProvider(db, device);
        return provider.getAllActivitySamples(tsFrom, tsTo);
    }

    protected List<? extends AbstractActivitySample> getActivitySamples(DBHandler db, GBDevice device, int tsFrom, int tsTo) {
        SampleProvider<? extends AbstractActivitySample> provider = getProvider(db, device);
        return provider.getActivitySamples(tsFrom, tsTo);
    }

    public DefaultChartsData<LineData> refresh(GBDevice gbDevice, List<? extends ActivitySample> samples) {
        TimestampTranslation tsTranslation = new TimestampTranslation();
        LOG.info("" + getTitle() + ": number of samples:" + samples.size());
        LineData lineData;

        if (samples.size() == 0) {
            lineData = new LineData();
            ValueFormatter xValueFormatter = new SampleXLabelFormatter(tsTranslation);
            return new DefaultChartsData(lineData, xValueFormatter);
        }

        boolean annotate = true;
        boolean use_steps_as_movement;

        ActivityKind last_type = ActivityKind.UNKNOWN;

        int numEntries = samples.size();
        List<List<Entry>> entries = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            entries.add(new ArrayList<>());
        }
        
        boolean hr = supportsHeartrate(gbDevice);
        List<Entry> heartrateEntries = hr ? new ArrayList<Entry>(numEntries) : null;

        int lastHrSampleIndex = -1;
        HeartRateUtils heartRateUtilsInstance = HeartRateUtils.getInstance();

        float last_value = 0;
        for (int i = 0; i < numEntries; i++) {
            ActivitySample sample = samples.get(i);
            ActivityKind type = sample.getKind();
            int ts = tsTranslation.shorten(sample.getTimestamp());
            float value = sample.getIntensity();

            // filled charts
            int index = getIndexOfActivity(type);
            int last_index = getIndexOfActivity(last_type);
            if (index >= 0 && last_index >= 0) {
                if (last_type != type) {
                    entries.get(index).add(createLineEntry(0, ts));
                    entries.get(last_index).add(createLineEntry(last_value, ts));
                    entries.get(last_index).add(createLineEntry(0, ts));
                }
                if (type == ActivityKind.NOT_WORN) {
                    entries.get(index).add(createLineEntry(Y_VALUE_DEEP_SLEEP, ts));
                } else {
                    entries.get(index).add(createLineEntry(value, ts));
                }
            } else if (last_index >= 0) {
                entries.get(last_index).add(createLineEntry(0, ts));
            } else if (index >= 0) {
                entries.get(index).add(createLineEntry(0, ts));
            }

            // heart rate line graph
            if (hr && type != ActivityKind.NOT_WORN && heartRateUtilsInstance.isValidHeartRateValue(sample.getHeartRate())) {
                if (lastHrSampleIndex > -1 && ts - lastHrSampleIndex > 1800*HeartRateUtils.MAX_HR_MEASUREMENTS_GAP_MINUTES) {
                    heartrateEntries.add(createLineEntry(0, lastHrSampleIndex + 1));
                    heartrateEntries.add(createLineEntry(0, ts - 1));
                }
                heartrateEntries.add(createLineEntry(sample.getHeartRate(), ts));
                lastHrSampleIndex = ts;
            }
            last_value = value;
            last_type = type;
        }

        // convert Entry Lists to Datasets
        List<ILineDataSet> lineDataSets = new ArrayList<>();

        lineDataSets.add(createDataSet(
            entries.get(getIndexOfActivity(ActivityKind.ACTIVITY)), akActivity.color, "Activity"
        ));
        lineDataSets.add(createDataSet(
            entries.get(getIndexOfActivity(ActivityKind.DEEP_SLEEP)), akDeepSleep.color, "Deep Sleep"
        ));
        lineDataSets.add(createDataSet(
            entries.get(getIndexOfActivity(ActivityKind.LIGHT_SLEEP)), akLightSleep.color, "Light Sleep"
        ));
        lineDataSets.add(createDataSet(
            entries.get(getIndexOfActivity(ActivityKind.NOT_WORN)), akActivity.color, "Not worn"
        ));

        if (supportsRemSleep(gbDevice)) {
            lineDataSets.add(createDataSet(
                entries.get(getIndexOfActivity(ActivityKind.REM_SLEEP)), akRemSleep.color, "REM Sleep"
            ));
        }
        if (supportsAwakeSleep(gbDevice)) {
            lineDataSets.add(createDataSet(
                entries.get(getIndexOfActivity(ActivityKind.AWAKE_SLEEP)), akActivity.color, "Awake Sleep"
            ));
        }
        if (hr && heartrateEntries.size() > 0) {
            LineDataSet heartrateSet = createHeartrateSet(heartrateEntries, "Heart Rate");
            lineDataSets.add(heartrateSet);
        }

        lineData = new LineData(lineDataSets);

        ValueFormatter xValueFormatter = new SampleXLabelFormatter(tsTranslation);
        return new DefaultChartsData(lineData, xValueFormatter);
    }

    protected Entry createLineEntry(float value, int xValue) {
        return new Entry(xValue, value);
    }

    protected LineDataSet createDataSet(List<Entry> values, Integer color, String label) {
        LineDataSet set1 = new LineDataSet(values, label);
        set1.setColor(color);
//        set1.setDrawCubic(true);
//        set1.setCubicIntensity(0.2f);
        set1.setDrawFilled(true);
        set1.setDrawCircles(false);
//        set1.setLineWidth(2f);
//        set1.setCircleSize(5f);
        set1.setFillColor(color);
        set1.setFillAlpha(255);
        set1.setDrawValues(false);
//        set1.setHighLightColor(Color.rgb(128, 0, 255));
//        set1.setColor(Color.rgb(89, 178, 44));
        set1.setValueTextColor(CHART_TEXT_COLOR);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set1;
    }

    protected LineDataSet createHeartrateSet(List<Entry> values, String label) {
        LineDataSet set1 = new LineDataSet(values, label);
        set1.setLineWidth(2.2f);
        set1.setColor(HEARTRATE_COLOR);
//        set1.setDrawCubic(true);
        set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set1.setCubicIntensity(0.1f);
        set1.setDrawCircles(false);
//        set1.setCircleRadius(2f);
//        set1.setDrawFilled(true);
//        set1.setColor(getResources().getColor(android.R.color.background_light));
//        set1.setCircleColor(HEARTRATE_COLOR);
//        set1.setFillColor(ColorTemplate.getHoloBlue());
//        set1.setHighLightColor(Color.rgb(128, 0, 255));
//        set1.setColor(Color.rgb(89, 178, 44));
        set1.setDrawValues(true);
        set1.setValueTextColor(CHART_TEXT_COLOR);
        set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
        return set1;
    }

    /**
     * Implement this to supply the samples to be displayed.
     *
     * @param db
     * @param device
     * @param tsFrom
     * @param tsTo
     * @return
     */
    protected abstract List<? extends ActivitySample> getSamples(DBHandler db, GBDevice device, int tsFrom, int tsTo);

    protected List<? extends ActivitySample> getSamples(DBHandler db, GBDevice device) {
        int tsStart = getTSStart();
        int tsEnd = getTSEnd();
        List<ActivitySample> samples = (List<ActivitySample>) getSamples(db, device, tsStart, tsEnd);
        ensureStartAndEndSamples(samples, tsStart, tsEnd);
//        List<ActivitySample> samples2 = new ArrayList<>();
//        int min = Math.min(samples.size(), 10);
//        int min = Math.min(samples.size(), 10);
//        for (int i = 0; i < min; i++) {
//            samples2.add(samples.get(i));
//        }
//        return samples2;
        return samples;
    }

    protected List<? extends ActivitySample> getSamplesofSleep(DBHandler db, GBDevice device) {
        int SLEEP_HOUR_LIMIT = 12;

        int tsStart = getTSStart();
        Calendar day = GregorianCalendar.getInstance();
        day.setTimeInMillis(tsStart * 1000L);
        day.set(Calendar.HOUR_OF_DAY, SLEEP_HOUR_LIMIT);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        tsStart = toTimestamp(day.getTime());

        int tsEnd = getTSEnd();
        day.setTimeInMillis(tsEnd* 1000L);
        day.set(Calendar.HOUR_OF_DAY, SLEEP_HOUR_LIMIT);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        tsEnd = toTimestamp(day.getTime());

        List<ActivitySample> samples = (List<ActivitySample>) getSamples(db, device, tsStart, tsEnd);
        ensureStartAndEndSamples(samples, tsStart, tsEnd);
        return samples;
    }

    protected void ensureStartAndEndSamples(List<ActivitySample> samples, int tsStart, int tsEnd) {
        if (samples == null || samples.isEmpty()) {
            return;
        }
        ActivitySample lastSample = samples.get(samples.size() - 1);
        if (lastSample.getTimestamp() < tsEnd) {
            samples.add(createTrailingActivitySample(lastSample, tsEnd));
        }

        ActivitySample firstSample = samples.get(0);
        if (firstSample.getTimestamp() > tsStart) {
            samples.add(createTrailingActivitySample(firstSample, tsStart));
        }
    }

    private ActivitySample createTrailingActivitySample(ActivitySample referenceSample, int timestamp) {
        TrailingActivitySample sample = new TrailingActivitySample();
        if (referenceSample instanceof AbstractActivitySample) {
            AbstractActivitySample reference = (AbstractActivitySample) referenceSample;
            sample.setUserId(reference.getUserId());
            sample.setDeviceId(reference.getDeviceId());
            sample.setProvider(reference.getProvider());
        }
        sample.setTimestamp(timestamp);
        return sample;
    }

    private int getIndexOfActivity(ActivityKind kind) {
        switch (kind) {
            case UNKNOWN: return -1;
            case DEEP_SLEEP: return 0;
            case LIGHT_SLEEP: return 1;
            case REM_SLEEP: return 2;
            case AWAKE_SLEEP: return 3;
            case NOT_WORN: return 4;
            default: return 5; // ActivityKind.ACTIVITY
        }
    }
}

