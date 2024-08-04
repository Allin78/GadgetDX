/*  Copyright (C) 2017-2024 Andreas Shimokawa, Daniele Gobbetti, José Rebelo

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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.TimeSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.HrvSummarySample;
import nodomain.freeyourgadget.gadgetbridge.util.DateTimeUtils;


public class HRVStatusFragment extends AbstractChartFragment<HRVStatusFragment.HRVStatusWeeklyData> {
    protected static final Logger LOG = LoggerFactory.getLogger(HRVStatusFragment.class);
    protected final int TOTAL_DAYS = 7;

    private LineChart mWeeklyHRVStatusChart;
    private TextView mHRVStatusSevenDaysAvg;
    private TextView mHRVStatusSevenDaysAvgStatus; // Balanced, Unbalanced, Low
    private TextView mHRVStatusLastNight;
    private TextView mHRVStatusLastNight5MinHighest;
    private TextView mDateView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hrv_status, container, false);

        mWeeklyHRVStatusChart = rootView.findViewById(R.id.hrv_weekly_line_chart);
        mHRVStatusLastNight = rootView.findViewById(R.id.hrv_status_last_night);
        mHRVStatusSevenDaysAvg = rootView.findViewById(R.id.hrv_status_seven_days_avg);
        mHRVStatusSevenDaysAvgStatus = rootView.findViewById(R.id.hrv_status_seven_days_avg_rate);
        mHRVStatusLastNight5MinHighest = rootView.findViewById(R.id.hrv_status_last_night_highest_5);
        mDateView = rootView.findViewById(R.id.hrv_status_date_view);

        setupLineChart();
        refresh();

        return rootView;
    }

    @Override
    public String getTitle() {
        return getString(R.string.pref_header_hrv_status);
    }

    @Override
    protected void init() {}

    @Override
    protected HRVStatusWeeklyData refreshInBackground(ChartsHost chartsHost, DBHandler db, GBDevice device) {
        Calendar day = Calendar.getInstance();
        Date tsEnd = getChartsHost().getEndDate();
        day.setTime(tsEnd);
        String formattedDate = new SimpleDateFormat("E, MMM dd").format(tsEnd);
        mDateView.setText(formattedDate);
        List<HRVStatusDayData> weeklyData = getWeeklyData(db, day, device);
        return new HRVStatusWeeklyData(weeklyData);
    }

    @Override
    protected void renderCharts() {
        mWeeklyHRVStatusChart.invalidate();
    }

    protected LineDataSet createDataSet(final List<Entry> values) {
        final LineDataSet lineDataSet = new LineDataSet(values, getString(R.string.hrv_status_seven_days_avg));
        lineDataSet.setColor(getResources().getColor(R.color.hrv_status_char_line_color));
        lineDataSet.setDrawCircles(false);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setFillAlpha(255);
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setCircleColor(getResources().getColor(R.color.hrv_status_char_line_color));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setDrawValues(true);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.ROOT, "%d", (int) value);
            }
        });
        return lineDataSet;
    }

    @Override
    protected void updateChartsnUIThread(HRVStatusWeeklyData weeklyData) {
        mWeeklyHRVStatusChart.setData(null); // workaround for https://github.com/PhilJay/MPAndroidChart/issues/2317
        List<Entry> lineEntries = new ArrayList<>();
        final List<ILineDataSet> lineDataSets = new ArrayList<>();
        weeklyData.getDaysData().forEach((HRVStatusDayData day) -> {
            if (day.status.getNum() > 0) {
                lineEntries.add(new Entry(day.i, day.weeklyAvg));
            } else {
                if (!lineEntries.isEmpty()) {
                    lineDataSets.add(createDataSet(lineEntries));
                    lineEntries.clear();
                }
            }
        });
        if (!lineEntries.isEmpty()) {
            lineDataSets.add(createDataSet(lineEntries));
        }

        List<LegendEntry> legendEntries = new ArrayList<>(1);
        LegendEntry activityEntry = new LegendEntry();
        activityEntry.label = getString(R.string.hrv_status_seven_days_avg);
        activityEntry.formColor = getResources().getColor(R.color.hrv_status_char_line_color);
        legendEntries.add(activityEntry);
        mWeeklyHRVStatusChart.getLegend().setCustom(legendEntries);

        final LineData lineData = new LineData(lineDataSets);
        mWeeklyHRVStatusChart.setData(lineData);

        final XAxis x = mWeeklyHRVStatusChart.getXAxis();
        x.setValueFormatter(getHRVStatusChartDayValueFormatter(weeklyData));

        HRVStatusDayData today = weeklyData.getCurrentDay();
        mHRVStatusSevenDaysAvg.setText(today.weeklyAvg > 0 ? today.weeklyAvg.toString() : "-");
        mHRVStatusLastNight.setText(today.lastNight > 0 ? today.lastNight.toString() : "-");
        mHRVStatusLastNight5MinHighest.setText(today.lastNight5MinHigh > 0 ? today.lastNight5MinHigh.toString() : "-");
        switch (today.status.getNum()) {
            case 0:
                mHRVStatusSevenDaysAvgStatus.setText("-");
                mHRVStatusSevenDaysAvgStatus.setTextColor(0);
                break;
            case 1:
                mHRVStatusSevenDaysAvgStatus.setText(getString(R.string.hrv_status_poor));
                mHRVStatusSevenDaysAvgStatus.setTextColor(getResources().getColor(R.color.hrv_status_poor));
                break;
            case 2:
                mHRVStatusSevenDaysAvgStatus.setText(getString(R.string.hrv_status_low));
                mHRVStatusSevenDaysAvgStatus.setTextColor(getResources().getColor(R.color.hrv_status_low));
                break;
            case 3:
                mHRVStatusSevenDaysAvgStatus.setText(getString(R.string.hrv_status_unbalanced));
                mHRVStatusSevenDaysAvgStatus.setTextColor(getResources().getColor(R.color.hrv_status_unbalanced));
                break;
            case 4:
                mHRVStatusSevenDaysAvgStatus.setText(getString(R.string.hrv_status_balanced));
                mHRVStatusSevenDaysAvgStatus.setTextColor(getResources().getColor(R.color.hrv_status_balanced));
                break;
        }
    }

    private List<HRVStatusDayData> getWeeklyData(DBHandler db, Calendar day, GBDevice device) {
        day = (Calendar) day.clone(); // do not modify the caller's argument
        day.add(Calendar.DATE, -TOTAL_DAYS);

        List<HRVStatusDayData> weeklyData = new ArrayList<>();;
        for (int counter = 0; counter < TOTAL_DAYS; counter++) {
            int startTs = (int) (day.getTimeInMillis() / 1000);
            int endTs = startTs + 24 * 60 * 60 - 1;
            day.add(Calendar.DATE, 1);
            List<? extends HrvSummarySample> samples = getSamples(db, device, startTs, endTs);
            if (!samples.isEmpty()) {
                int finalCounter = counter;
                Calendar finalDay = (Calendar) day.clone();
                samples.forEach(sample -> {
                    weeklyData.add(new HRVStatusDayData(finalDay, finalCounter, sample.getTimestamp(), sample.getWeeklyAverage(), sample.getLastNightAverage(), sample.getLastNight5MinHigh(), sample.getStatus()));
                });
            } else {
                HRVStatusDayData d = new HRVStatusDayData(
                        (Calendar) day.clone(),
                        counter,
                        0,
                        0,
                        0,
                        0,
                        HrvSummarySample.Status.NONE
                );
                weeklyData.add(d);
            }
        }
        return weeklyData;
    }

    private List<? extends HrvSummarySample> getSamples(final DBHandler db, final GBDevice device, int tsFrom, int tsTo) {
        final DeviceCoordinator coordinator = device.getDeviceCoordinator();
        final TimeSampleProvider<? extends HrvSummarySample> sampleProvider = coordinator.getHrvSummarySampleProvider(device, db.getDaoSession());
        return sampleProvider.getAllSamples(tsFrom * 1000L, tsTo * 1000L);
    }

    private void setupLineChart() {
        mWeeklyHRVStatusChart.getDescription().setEnabled(false);

        final XAxis xAxisBottom = mWeeklyHRVStatusChart.getXAxis();
        xAxisBottom.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisBottom.setDrawLabels(true);
        xAxisBottom.setDrawGridLines(false);
        xAxisBottom.setEnabled(true);
        xAxisBottom.setDrawLimitLinesBehindData(true);
        xAxisBottom.setSpaceMin(0.5f);
        xAxisBottom.setSpaceMax(0.5f);
        xAxisBottom.setAxisMaximum(6);
        xAxisBottom.setAxisMinimum(0);

        final YAxis yAxisLeft = mWeeklyHRVStatusChart.getAxisLeft();
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setAxisMaximum(120);
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setDrawTopYLabelEntry(false);
        yAxisLeft.setEnabled(true);

        final YAxis yAxisRight = mWeeklyHRVStatusChart.getAxisRight();
        yAxisRight.setDrawGridLines(false);
        yAxisRight.setEnabled(false);
        yAxisRight.setDrawLabels(false);
    }

    ValueFormatter getHRVStatusChartDayValueFormatter(HRVStatusWeeklyData weeklyData) {
        return new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatHRVStatusChartValue((long) value, weeklyData);
            }
        };
    }

    protected String formatHRVStatusChartValue(long value, HRVStatusWeeklyData weeklyData) {
        HRVStatusDayData day = weeklyData.getDay((int) value);
        SimpleDateFormat formatLetterDay = new SimpleDateFormat("EEEEE", Locale.getDefault());
        return formatLetterDay.format(new Date(day.day.getTimeInMillis()));
    }

    protected void setupLegend(Chart chart) {}

    protected static class HRVStatusWeeklyData extends ChartsData {
        private final List<HRVStatusDayData> data;

        public HRVStatusWeeklyData(final List<HRVStatusDayData> chartsData) {
            this.data = chartsData;
        }

        public HRVStatusDayData getDay(int i) {
            return this.data.get(i);
        }

        public HRVStatusDayData getCurrentDay() {
            return this.data.get(this.data.size() - 1);
        }

        public List<HRVStatusDayData> getDaysData() {
            return data;
        }
    }

    protected static class HRVStatusDayData {
        public Integer i;
        public long timestamp;
        public Integer weeklyAvg;
        public Integer lastNight;
        public Integer lastNight5MinHigh;
        public HrvSummarySample.Status status;
        public Calendar day;

        public HRVStatusDayData(Calendar day, int i, long timestamp, Integer weeklyAvg, Integer lastNight, Integer lastNight5MinHigh, HrvSummarySample.Status status) {
            this.lastNight = lastNight;
            this.weeklyAvg = weeklyAvg;
            this.lastNight5MinHigh = lastNight5MinHigh;
            this.i = i;
            this.timestamp = timestamp;
            this.status = status;
            this.day = day;
        }
    }
}