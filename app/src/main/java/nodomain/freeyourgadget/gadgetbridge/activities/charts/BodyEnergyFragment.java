package nodomain.freeyourgadget.gadgetbridge.activities.charts;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.TimeSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.BodyEnergySample;


public class BodyEnergyFragment extends AbstractChartFragment<BodyEnergyFragment.BodyEnergyData> {
    protected static final Logger LOG = LoggerFactory.getLogger(BodyEnergyFragment.class);
    protected final int TOTAL_DAYS = 7;

    private PieChart mBodyEnergyLevelChart;
//    private TextView mHRVStatusSevenDaysAvg;
//    private TextView mHRVStatusSevenDaysAvgStatus; // Balanced, Unbalanced, Low
//    private TextView mHRVStatusLastNight;
//    private TextView mHRVStatusLastNight5MinHighest;
//    private TextView mHRVStatusDayAvg;
//    private TextView mHRVStatusBaseline;
    private TextView mDateView;
    protected int CHART_TEXT_COLOR;
    protected int LEGEND_TEXT_COLOR;
    protected int TEXT_COLOR;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_body_energy, container, false);

        mBodyEnergyLevelChart = rootView.findViewById(R.id.body_energy_level_chart);
//        mHRVStatusLastNight = rootView.findViewById(R.id.hrv_status_last_night);
//        mHRVStatusSevenDaysAvg = rootView.findViewById(R.id.hrv_status_seven_days_avg);
//        mHRVStatusSevenDaysAvgStatus = rootView.findViewById(R.id.hrv_status_seven_days_avg_rate);
//        mHRVStatusLastNight5MinHighest = rootView.findViewById(R.id.hrv_status_last_night_highest_5);
//        mHRVStatusDayAvg = rootView.findViewById(R.id.hrv_status_day_avg);
//        mHRVStatusBaseline = rootView.findViewById(R.id.hrv_status_baseline);
        mDateView = rootView.findViewById(R.id.hrv_status_date_view);

        setupBodyEnergyLevelChart();
//        refresh();


        return rootView;
    }


    private void setupBodyEnergyLevelChart() {
        final PieData data = new PieData();
        final List<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(60, "y"));
        entries.add(new PieEntry(40, "x"));

        final PieDataSet pieDataSet = new PieDataSet(entries, "");

        pieDataSet.setColors(Color.GREEN, Color.GRAY);

        data.setDataSet(pieDataSet);
        //Get the chart
        mBodyEnergyLevelChart.setData(data);
        mBodyEnergyLevelChart.invalidate();
        mBodyEnergyLevelChart.setCenterText("30");
        mBodyEnergyLevelChart.setDrawEntryLabels(false);
        mBodyEnergyLevelChart.setContentDescription("");
        //pieChart.setDrawMarkers(true);
        //pieChart.setMaxHighlightDistance(34);
        mBodyEnergyLevelChart.setEntryLabelTextSize(28f);
        mBodyEnergyLevelChart.setHoleRadius(75);
        mBodyEnergyLevelChart.setMaxAngle(270f);
        mBodyEnergyLevelChart.setRotationAngle(-135f);
        mBodyEnergyLevelChart.setDrawRoundedSlices(true);
        mBodyEnergyLevelChart.setBackgroundColor(GBApplication.getBackgroundColor(getContext()));
        mBodyEnergyLevelChart.getDescription().setTextColor(GBApplication.getTextColor(getContext()));
        mBodyEnergyLevelChart.setNoDataText("-");
        mBodyEnergyLevelChart.setHoleColor(getContext().getResources().getColor(R.color.transparent));
        mBodyEnergyLevelChart.getLegend().setEnabled(false);


        //legend attributes
//        Legend legend = pieChart.getLegend();
//        legend.setForm(Legend.LegendForm.CIRCLE);
//        legend.setTextSize(12);
//        legend.setFormSize(20);
//        legend.setFormToTextSpace(2);
    }

    @Override
    public String getTitle() {
        return getString(R.string.pref_header_body_energy);
    }

    @Override
    protected void init() {
        TEXT_COLOR = GBApplication.getTextColor(requireContext());
        LEGEND_TEXT_COLOR = GBApplication.getTextColor(requireContext());
        CHART_TEXT_COLOR = GBApplication.getSecondaryTextColor(requireContext());
    }

    @Override
    protected BodyEnergyData refreshInBackground(ChartsHost chartsHost, DBHandler db, GBDevice device) {
        String formattedDate = new SimpleDateFormat("E, MMM dd").format(getTSEnd());
        mDateView.setText(formattedDate);
        List<? extends BodyEnergySample> samples = getBodyEnergySamples(db, device, getTSStart(), getTSEnd());

        return null;
    }


    @Override
    protected void renderCharts() {
//        mWeeklyHRVStatusChart.invalidate();
    }


    public List<? extends BodyEnergySample> getBodyEnergySamples(final DBHandler db, final GBDevice device, int tsFrom, int tsTo) {
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(tsTo * 1000L); //we need today initially, which is the end of the time range
        day.set(Calendar.HOUR_OF_DAY, 0); //and we set time for the start and end of the same day
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        tsFrom = (int) (day.getTimeInMillis() / 1000);
        tsTo = tsFrom + 24 * 60 * 60 - 1;

        final DeviceCoordinator coordinator = device.getDeviceCoordinator();
        final TimeSampleProvider<? extends BodyEnergySample> sampleProvider = coordinator.getBodyEnergySampleProvider(device, db.getDaoSession());
        return sampleProvider.getAllSamples(tsFrom * 1000L, tsTo * 1000L);
    }

    protected void setupLegend(Chart<?> chart) {}

    @Override
    protected void updateChartsnUIThread(BodyEnergyData chartsData) {

    }

    protected static class BodyEnergyData extends ChartsData {}

}