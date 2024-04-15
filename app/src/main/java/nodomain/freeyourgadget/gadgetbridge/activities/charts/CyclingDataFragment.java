package nodomain.freeyourgadget.gadgetbridge.activities.charts;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.TimeSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.cycling_sensor.db.CyclingSensorActivitySampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.CyclingSensorActivitySample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.TemperatureSample;

public class CyclingDataFragment extends AbstractChartFragment<CyclingDataFragment.CyclingChartsData>{
    private LineChart cyclingHistoryChart;

    protected static class CyclingChartsData extends DefaultChartsData<LineData> {
        public CyclingChartsData(LineData lineData, TimestampTranslation tsTranslation) {
            super(lineData, new TemperatureChartFragment.dateFormatter(tsTranslation));
        }
    }

    @Override
    public String getTitle() {
        return "Cycling data";
    }

    @Override
    protected void init() {

    }

    @Override
    protected CyclingChartsData refreshInBackground(ChartsHost chartsHost, DBHandler db, GBDevice device) {
        List<CyclingSensorActivitySample> samples = getSamples(db, device);
        return null;
    }

    @Override
    protected void renderCharts() {

    }

    @Override
    protected void setupLegend(Chart chart) {

    }

    @Override
    protected void updateChartsnUIThread(CyclingChartsData chartsData) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cycling, container, false);

        cyclingHistoryChart = rootView.findViewById(R.id.chart_cycling_history);
        cyclingHistoryChart.setBackgroundColor(GBApplication.getBackgroundColor(requireContext()));
        cyclingHistoryChart.getDescription().setTextColor(GBApplication.getTextColor(requireContext()));

        XAxis xAxis = cyclingHistoryChart.getXAxis();
        xAxis.setTextColor(GBApplication.getTextColor(requireContext()));
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(true);
        xAxis.setEnabled(true);

        YAxis yAxis = cyclingHistoryChart.getAxisRight();
        yAxis.setEnabled(true);
        yAxis.setDrawGridLines(true);
        yAxis.setTextColor(GBApplication.getTextColor(requireContext()));

        return rootView;
    }

    private List<? extends TemperatureSample> getSamples(final DBHandler db, final GBDevice device) {
        final int tsStart = getTSStart();
        final int tsEnd = getTSEnd();
        final DeviceCoordinator coordinator = device.getDeviceCoordinator();
        final TimeSampleProvider<? extends CyclingSensorActivitySample> sampleProvider = coordinator.getCyclingSensorActivityProvider(device, db.getDaoSession());
        return sampleProvider.getAllSamples(tsStart * 1000L, tsEnd * 1000L);
    }
}
