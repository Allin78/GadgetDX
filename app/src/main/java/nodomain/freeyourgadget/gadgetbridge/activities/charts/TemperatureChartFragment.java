/*  Copyright (C) 2023 José Rebelo

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.activities.charts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.TimeSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.TemperatureSample;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class TemperatureChartFragment extends AbstractChartFragment<TemperatureChartFragment.TemperatureChartsData> {
    protected static final Logger LOG = LoggerFactory.getLogger(ActivitySleepChartFragment.class);

    private LineChart mStressChart;
    private int BACKGROUND_COLOR;
    private int DESCRIPTION_COLOR;
    private int CHART_TEXT_COLOR;
//    private int LEGEND_TEXT_COLOR;

    private final Prefs prefs = GBApplication.getPrefs();

    private final boolean CHARTS_SLEEP_RANGE_24H = prefs.getBoolean("chart_sleep_range_24h", false);
    private final boolean SHOW_CHARTS_AVERAGE = prefs.getBoolean("charts_show_average", true);

    @Override
    protected void init() {
        BACKGROUND_COLOR = GBApplication.getBackgroundColor(requireContext());
//        LEGEND_TEXT_COLOR = DESCRIPTION_COLOR = GBApplication.getTextColor(requireContext());
        CHART_TEXT_COLOR = GBApplication.getSecondaryTextColor(requireContext());

    }

    @Override
    protected TemperatureChartsData refreshInBackground(final ChartsHost chartsHost, final DBHandler db, final GBDevice device) {
        final List<? extends TemperatureSample> samples = getSamples(db, device);

        LOG.info("Got {} temperature samples", samples.size());

        ensureStartAndEndSamples((List<TemperatureSample>) samples);

        return new TemperatureChartsDataBuilder(samples).build();
    }


    @Override
    protected void updateChartsnUIThread(final TemperatureChartsData temperatureData) {
        mStressChart.setData(null); // workaround for https://github.com/PhilJay/MPAndroidChart/issues/2317
        mStressChart.getXAxis().setValueFormatter(temperatureData.getXValueFormatter());
        mStressChart.setData(temperatureData.getData());
        mStressChart.getAxisRight().removeAllLimitLines();

//        if (temperatureData.getAverage() > 0) {
//            final LimitLine averageLine = new LimitLine(temperatureData.getAverage());
//            averageLine.setLineColor(Color.RED);
//            averageLine.setLineWidth(0.1f);
//            mStressChart.getAxisRight().addLimitLine(averageLine);
//        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.menuitem_temperature);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_stresschart, container, false);

        mStressChart = rootView.findViewById(R.id.stress_line_chart);

        setupLineChart();

        // refresh immediately instead of use refreshIfVisible(), for perceived performance
        refresh();

        return rootView;
    }

    private void setupLineChart() {
        mStressChart.setBackgroundColor(BACKGROUND_COLOR);
        mStressChart.getDescription().setTextColor(DESCRIPTION_COLOR);
        configureBarLineChartDefaults(mStressChart);

        final XAxis x = mStressChart.getXAxis();
        x.setDrawLabels(true);
        x.setDrawGridLines(false);
        x.setEnabled(true);
        x.setTextColor(CHART_TEXT_COLOR);
        x.setDrawLimitLinesBehindData(true);

        final YAxis yAxisLeft = mStressChart.getAxisLeft();
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setAxisMaximum(100f);
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setDrawTopYLabelEntry(false);
        yAxisLeft.setTextColor(CHART_TEXT_COLOR);
        yAxisLeft.setEnabled(true);

        final YAxis yAxisRight = mStressChart.getAxisRight();
        yAxisRight.setDrawGridLines(false);
        yAxisRight.setEnabled(true);
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawTopYLabelEntry(true);
        yAxisRight.setTextColor(CHART_TEXT_COLOR);
        yAxisRight.setAxisMaximum(100f);
        yAxisRight.setAxisMinimum(0);
    }

    @Override
    protected void setupLegend(final Chart<?> chart) {
    }

    @Override
    protected void renderCharts() {
        mStressChart.animateX(ANIM_TIME, Easing.EaseInOutQuart);
    }

    private List<? extends TemperatureSample> getSamples(final DBHandler db, final GBDevice device) {
        final int tsStart = getTSStart();
        final int tsEnd = getTSEnd();
        final DeviceCoordinator coordinator = device.getDeviceCoordinator();
        final TimeSampleProvider<? extends TemperatureSample> sampleProvider = coordinator.getTemperatureSampleProvider(device, db.getDaoSession());
        return sampleProvider.getAllSamples(tsStart * 1000L, tsEnd * 1000L);
    }

    protected void ensureStartAndEndSamples(final List<TemperatureSample> samples) {
        if (samples == null || samples.isEmpty()) {
            return;
        }

        final long tsEndMillis = getTSEnd() * 1000L;
        final long tsStartMillis = getTSStart() * 1000L;

        final TemperatureSample lastSample = samples.get(samples.size() - 1);
        if (lastSample.getTimestamp() < tsEndMillis) {
            samples.add(new EmptyTemperatureSample(tsEndMillis));
        }

        final TemperatureSample firstSample = samples.get(0);
        if (firstSample.getTimestamp() > tsStartMillis) {
            samples.add(0, new EmptyTemperatureSample(tsStartMillis));
        }
    }

    protected static final class EmptyTemperatureSample implements TemperatureSample {
        private final long ts;

        public EmptyTemperatureSample(final long ts) {
            this.ts = ts;
        }


        @Override
        public long getTimestamp() {
            return ts;
        }

        @Override
        public float getTemperature() {
            return 0;
        }

        @Override
        public int getTemperatureType() {
            return 0;
        }
    }

    protected class TemperatureChartsDataBuilder {
        private static final int UNKNOWN_VAL = 2;

        private final List<? extends TemperatureSample> samples;

        private final TimestampTranslation tsTranslation = new TimestampTranslation();

        int previousTs;
        int currentTypeStartTs;
        long averageSum;
        long averageNumSamples;

        public TemperatureChartsDataBuilder(final List<? extends TemperatureSample> samples) {
            this.samples = samples;
        }
//
//        private void reset() {
//            tsTranslation.reset();
//            lineEntriesPerLevel.clear();
//            accumulator.clear();
//            previousTs = 0;
//            currentTypeStartTs = 0;
//        }

//        private void processSamples() {
//            reset();
//
//            for (final TemperatureSample sample : samples) {
//                processSample(sample);
//            }
//
//            // Add the last block, if any
//            if (currentTypeStartTs != previousTs) {
//                set(previousTs, previousStressType, samples.get(samples.size() - 1).getStress());
//            }
//        }
//
//        private void processSample(final StressSample sample) {
//            //LOG.debug("Processing sample {} {}", sdf.format(new Date(sample.getTimestamp())), sample.getStress());
//
//            final StressType stressType = StressType.fromStress(sample.getStress());
//            final int ts = tsTranslation.shorten((int) (sample.getTimestamp() / 1000L));
//
//            if (ts == 0) {
//                // First sample
//                previousTs = ts;
//                currentTypeStartTs = ts;
//                previousStressType = stressType;
//                set(ts, stressType, sample.getStress());
//                return;
//            }
//
//            if (ts - previousTs > 60 * 10) {
//                // More than 15 minutes since last sample
//                // Set to unknown right after the last sample we got until the current time
//                int lastEndTs = Math.min(previousTs + 60 * 5, ts - 1);
//                set(lastEndTs, StressType.UNKNOWN, UNKNOWN_VAL);
//                set(ts - 1, StressType.UNKNOWN, UNKNOWN_VAL);
//            }
//
//            if (!stressType.equals(previousStressType)) {
//                currentTypeStartTs = ts;
//            }
//
//            set(ts, stressType, sample.getStress());
//
//            accumulator.put(stressType, accumulator.get(stressType) + 60);
//
//            if (stressType != StressType.UNKNOWN) {
//                averageSum += sample.getStress();
//                averageNumSamples++;
//            }
//
//            previousStressType = stressType;
//            previousTs = ts;
//        }
//
//        private void set(final int ts, final StressType stressType, final int stress) {
//            for (final Map.Entry<StressType, List<Entry>> stressTypeListEntry : lineEntriesPerLevel.entrySet()) {
//                if (stressTypeListEntry.getKey() == stressType) {
//                    stressTypeListEntry.getValue().add(new Entry(ts, stress));
//                } else {
//                    stressTypeListEntry.getValue().add(new Entry(ts, 0));
//                }
//            }
//        }

        public TemperatureChartsData build() {
            TimestampTranslation tsTranslation = new TimestampTranslation();
            List<Entry> entries = new ArrayList<Entry>();
            long firstTs = 0;

            for (TemperatureSample sample : samples) {
                int timestamp_in_seconds = (int) (sample.getTimestamp() / 1000L);
                entries.add(new Entry(tsTranslation.shorten(timestamp_in_seconds), sample.getTemperature()));
                if (firstTs == 0) {
                    firstTs = sample.getTimestamp();
                }
            }

            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.temperature));
            dataSet.setLineWidth(2.2f);
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            dataSet.setCubicIntensity(0.1f);
            dataSet.setDrawCircles(false);
            dataSet.setCircleRadius(2f);
            dataSet.setDrawValues(true);
            dataSet.setValueTextColor(CHART_TEXT_COLOR);
            dataSet.setHighlightEnabled(true);
            dataSet.setHighlightEnabled(true);
            LineData lineData = new LineData(dataSet);

            return new TemperatureChartsData(lineData, tsTranslation);
        }
    }

    protected static class TemperatureChartsData extends DefaultChartsData<LineData> {
        public TemperatureChartsData(LineData lineData, TimestampTranslation tsTranslation) {
            super(lineData, new customFormatter(tsTranslation));
        }
    }


    protected static class customFormatter extends ValueFormatter {
        private final TimestampTranslation tsTranslation;
        SimpleDateFormat annotationDateFormat = new SimpleDateFormat("dd.MM.");
        Calendar cal = GregorianCalendar.getInstance();

        public customFormatter(TimestampTranslation tsTranslation) {
            this.tsTranslation = tsTranslation;
        }

        @Override
        public String getFormattedValue(float value) {
            cal.clear();
            int ts = (int) value;
            cal.setTimeInMillis(tsTranslation.toOriginalValue(ts) * 1000L);
            Date date = cal.getTime();
            return annotationDateFormat.format(date);
        }
    }

}
