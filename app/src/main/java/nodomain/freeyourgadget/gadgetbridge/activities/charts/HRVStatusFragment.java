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

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;


public class HRVStatusFragment extends AbstractChartFragment {
    protected static final Logger LOG = LoggerFactory.getLogger(HRVStatusFragment.class);

//    private HorizontalBarChart mStatsChart;

//    @Override
//    protected ChartsData refreshInBackground(ChartsHost chartsHost, DBHandler db, GBDevice device) {
//        List<? extends ActivitySample> samples = getSamples(db, device);
//
//        MyHeartRateVariabilityData myHeartRateVariabilityData = refreshStats(samples);
//
//        return new MyChartsData(myHeartRateVariabilityData);
//    }

//    private MyHeartRateVariabilityData refreshStats(List<? extends ActivitySample> samples) {
//        ActivityAnalysis analysis = new ActivityAnalysis();
//        analysis.calculateActivityAmounts(samples);
//        BarData data = new BarData();
//        data.setValueTextColor(CHART_TEXT_COLOR);
//        List<BarEntry> entries = new ArrayList<>();
//
//        ActivityUser user = new ActivityUser();
//        /*double distanceFactorCm;
//        if (user.getGender() == user.GENDER_MALE){
//            distanceFactorCm = user.getHeightCm() * user.GENDER_MALE_DISTANCE_FACTOR / 1000;
//        } else {
//            distanceFactorCm = user.getHeightCm() * user.GENDER_FEMALE_DISTANCE_FACTOR / 1000;
//        }*/
//
////        for (Map.Entry<Integer, Long> entry : analysis.stats.entrySet()) {
////            entries.add(new BarEntry(entry.getKey(), entry.getValue() / 60));
////        }
////
////        BarDataSet set = new BarDataSet(entries, "");
////        set.setValueTextColor(CHART_TEXT_COLOR);
////        set.setColors(getColorFor(ActivityKind.TYPE_ACTIVITY));
////        //set.setDrawValues(false);
////        //data.setBarWidth(0.1f);
////        data.addDataSet(set);
////
//        return new MyHeartRateVariabilityData(data);
//    }

//    @Override
//    protected void updateChartsnUIThread(ChartsData chartsData) {
////        MyChartsData mcd = (MyChartsData) chartsData;
////        mStatsChart.setData(mcd.getChartsData().getBarData());
//    }

    private LineChart mWeeklyHRVStatusChart;
    private TextView mHRVStatusSevenDaysAvg;
    private TextView mHRVStatusSevenDaysAvgRate; // Balanced, Unbalanced, Low
    private TextView mHRVStatusLastNight;
    private TextView mDateView;

    private int BACKGROUND_COLOR;
    private int DESCRIPTION_COLOR;
    private int CHART_TEXT_COLOR;
    private int LEGEND_TEXT_COLOR;


    @Override
    public String getTitle() {
        return getString(R.string.pref_header_hrv_status);
    }

    @Override
    protected void init() {
        BACKGROUND_COLOR = GBApplication.getBackgroundColor(requireContext());
        LEGEND_TEXT_COLOR = DESCRIPTION_COLOR = GBApplication.getTextColor(requireContext());
        CHART_TEXT_COLOR = GBApplication.getSecondaryTextColor(requireContext());
    }

    @Override
    protected ChartsData refreshInBackground(ChartsHost chartsHost, DBHandler db, GBDevice device) {
        return null;
    }

    @Override
    protected void renderCharts() {
        char x = 'a';
    }

//    @Override
//    protected void setupLegend(Chart chart) {
//
//    }

    @Override
    protected void updateChartsnUIThread(ChartsData chartsData) {
        mWeeklyHRVStatusChart.setData(null); // workaround for https://github.com/PhilJay/MPAndroidChart/issues/2317
//        mWeeklyHRVStatusChart.getXAxis().setValueFormatter(chartsData.getXValueFormatter());
//        mWeeklyHRVStatusChart.setData(chartsData.getData());
        mWeeklyHRVStatusChart.getAxisRight().removeAllLimitLines();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hrv_status, container, false);



        mWeeklyHRVStatusChart = rootView.findViewById(R.id.hrv_weekly_line_chart);
        mHRVStatusLastNight = rootView.findViewById(R.id.hrv_status_last_night);
        mHRVStatusSevenDaysAvg = rootView.findViewById(R.id.hrv_status_seven_days_avg);
        mHRVStatusSevenDaysAvgRate = rootView.findViewById(R.id.hrv_status_seven_days_avg_rate);
        mDateView = rootView.findViewById(R.id.hrv_status_date_view);
        setupHRVStatusData();
//        setupWeeklyHRVStatusChart();

        mDateView.setText("July 28");

        // refresh immediately instead of use refreshIfVisible(), for perceived performance
//        refresh();

        return rootView;
    }

    private void setupHRVStatusData() {
        mHRVStatusLastNight.setText("55 ms");
        mHRVStatusSevenDaysAvg.setText("50 ms");
        mHRVStatusSevenDaysAvgRate.setText("Balanced");
        mHRVStatusSevenDaysAvgRate.setTextColor(Color.GREEN);
    }

    protected void setupLegend(Chart chart) {
        final List<LegendEntry> legendEntries = new ArrayList<>(StressChartFragment.StressType.values().length + 1);

        final LegendEntry balancedEntry = new LegendEntry();
        balancedEntry.label = "Balanced";
        balancedEntry.formColor = Color.GREEN;
        legendEntries.add(balancedEntry);

        final LegendEntry unbalancedEntry = new LegendEntry();
        unbalancedEntry.label = "Unbalanced";
        unbalancedEntry.formColor = Color.BLUE;
        legendEntries.add(unbalancedEntry);

        final LegendEntry lowEntry = new LegendEntry();
        lowEntry.label = "Low";
        lowEntry.formColor = Color.RED;
        legendEntries.add(lowEntry);

        chart.getLegend().setCustom(legendEntries);
        chart.getLegend().setTextColor(LEGEND_TEXT_COLOR);
    }

    private void setupLineChart() {
        mWeeklyHRVStatusChart.setBackgroundColor(BACKGROUND_COLOR);
        mWeeklyHRVStatusChart.getDescription().setTextColor(DESCRIPTION_COLOR);
        configureBarLineChartDefaults(mWeeklyHRVStatusChart);

        final XAxis x = mWeeklyHRVStatusChart.getXAxis();
        x.setDrawLabels(true);
        x.setDrawGridLines(false);
        x.setEnabled(true);
        x.setTextColor(CHART_TEXT_COLOR);
        x.setDrawLimitLinesBehindData(true);

        final YAxis yAxisLeft = mWeeklyHRVStatusChart.getAxisLeft();
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setAxisMaximum(100f);
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setDrawTopYLabelEntry(false);
        yAxisLeft.setTextColor(CHART_TEXT_COLOR);
        yAxisLeft.setEnabled(true);

        final YAxis yAxisRight = mWeeklyHRVStatusChart.getAxisRight();
        yAxisRight.setDrawGridLines(false);
        yAxisRight.setEnabled(true);
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawTopYLabelEntry(true);
        yAxisRight.setTextColor(CHART_TEXT_COLOR);
        yAxisRight.setAxisMaximum(100f);
        yAxisRight.setAxisMinimum(0);
    }


//    @Override
//    protected StressChartFragment.StressChartsData refreshInBackground(final ChartsHost chartsHost, final DBHandler db, final GBDevice device) {
//        final List<? extends StressSample> samples = getSamples(db, device);
//
//        LOG.info("Got {} stress samples", samples.size());
//
//        ensureStartAndEndSamples((List<StressSample>) samples);
//
//        return new StressChartFragment.StressChartsDataBuilder(samples, device.getDeviceCoordinator().getStressRanges()).build();
//    }


//    private void setupStatsChart() {
//        mStatsChart.setBackgroundColor(BACKGROUND_COLOR);
//        mStatsChart.getDescription().setTextColor(DESCRIPTION_COLOR);
//        mStatsChart.setNoDataText("");
//        mStatsChart.getLegend().setEnabled(false);
//        mStatsChart.setTouchEnabled(false);
//        mStatsChart.getDescription().setText("");
//
//        XAxis right = mStatsChart.getXAxis(); //believe it or not, the X axis is vertical for HorizontalBarChart
//        right.setTextColor(CHART_TEXT_COLOR);
//
//        YAxis bottom = mStatsChart.getAxisRight();
//        bottom.setTextColor(CHART_TEXT_COLOR);
//        bottom.setGranularity(1f);
//
//        YAxis top = mStatsChart.getAxisLeft();
//        top.setTextColor(CHART_TEXT_COLOR);
//        top.setGranularity(1f);
//    }

//    @Override
//    protected List<? extends ActivitySample> getSamples(DBHandler db, GBDevice device, int tsFrom, int tsTo) {
//        return super.getAllSamples(db, device, tsFrom, tsTo);
//    }
//
//
//    @Override
//    protected void renderCharts() {
////        mStatsChart.invalidate();
//    }
//
//    private static class MyHeartRateVariabilityData extends ChartsData {
//        private final BarData barData;
//
//        MyHeartRateVariabilityData(BarData barData) {
//            this.barData = barData;
//        }
//
//        BarData getBarData() {
//            return barData;
//        }
//    }
//
//    private static class MyChartsData extends ChartsData {
//        private final MyHeartRateVariabilityData chartsData;
//
//        MyChartsData(MyHeartRateVariabilityData chartsData) {
//            this.chartsData = chartsData;
//        }
//
//        MyHeartRateVariabilityData getChartsData() {
//            return chartsData;
//        }
//    }
}