package nodomain.freeyourgadget.gadgetbridge.activities.charts;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.TimeSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.CyclingSample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.Spo2Sample;
import nodomain.freeyourgadget.gadgetbridge.model.TemperatureSample;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class CyclingChartFragment extends AbstractChartFragment<CyclingChartFragment.CyclingChartsData>{
    private LineChart cyclingHistoryChart;

    private int BACKGROUND_COLOR;
    private int DESCRIPTION_COLOR;
    private int CHART_TEXT_COLOR;
    private int LEGEND_TEXT_COLOR;
    private int CHART_LINE_COLOR;
    private final Prefs prefs = GBApplication.getPrefs();

    protected static class CyclingChartsData extends ChartsData {
        DefaultChartsData<LineData> chartsData;

        public CyclingChartsData(DefaultChartsData<LineData> chartsData) {
            this.chartsData = chartsData;
        }

        public DefaultChartsData<LineData> getChartsData() {
            return chartsData;
        }
    }

    @Override
    public String getTitle() {
        return "Cycling data";
    }

    @Override
    protected void init() {
        BACKGROUND_COLOR = GBApplication.getBackgroundColor(requireContext());
        LEGEND_TEXT_COLOR = DESCRIPTION_COLOR = GBApplication.getTextColor(requireContext());
        CHART_TEXT_COLOR = GBApplication.getSecondaryTextColor(requireContext());

        if (prefs.getBoolean("chart_heartrate_color", false)) {
            CHART_LINE_COLOR = ContextCompat.getColor(getContext(), R.color.chart_heartrate_alternative);
        } else {
            CHART_LINE_COLOR = ContextCompat.getColor(getContext(), R.color.chart_heartrate);
        }
    }

    @Override
    protected CyclingChartsData refreshInBackground(ChartsHost chartsHost, DBHandler db, GBDevice device) {
        List<CyclingSample> samples = getSamples(db, device);

        return new CyclingChartsDataBuilder(samples).build();
    }

    protected class CyclingChartsDataBuilder {
        private final List<CyclingSample> samples;

        private final TimestampTranslation tsTranslation = new TimestampTranslation();

        private final List<Entry> lineEntries = new ArrayList<>();

        long averageSum;
        long averageNumSamples;

        public CyclingChartsDataBuilder(final List<CyclingSample> samples) {
            this.samples = samples;
        }

        private void reset() {
            tsTranslation.reset();
            lineEntries.clear();

            averageSum = 0;
            averageNumSamples = 0;
        }

        private void processSamples() {
            reset();

            for (final CyclingSample sample : samples) {
                processSample(sample);
            }
        }

        private void processSample(final CyclingSample sample) {
            final int ts = tsTranslation.shorten((int) (sample.getTimestamp() / 1000L));
            lineEntries.add(new Entry(ts, sample.getDistance()));
        }

        public CyclingChartsData build() {
            processSamples();

            final List<ILineDataSet> lineDataSets = new ArrayList<>();

            lineDataSets.add(createDataSet(lineEntries));

            final LineData lineData = new LineData(lineDataSets);
            final ValueFormatter xValueFormatter = new SampleXLabelFormatter(tsTranslation);
            final DefaultChartsData<LineData> chartsData = new DefaultChartsData<>(lineData, xValueFormatter);
            return new CyclingChartsData(chartsData);
        }
    }

    protected LineDataSet createDataSet(final List<Entry> values) {
        final LineDataSet lineDataSet = new LineDataSet(values, "Cycling");
        lineDataSet.setColor(CHART_LINE_COLOR);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setLineWidth(2.2f);
        lineDataSet.setFillAlpha(255);
        lineDataSet.setValueTextColor(CHART_TEXT_COLOR);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.ROOT, "%d", (int) value);
            }
        });
        return lineDataSet;
    }

    @Override
    protected void renderCharts() {

    }

    @Override
    protected void setupLegend(final Chart<?> chart) {
        final List<LegendEntry> legendEntries = new ArrayList<>(2);

        final LegendEntry entry = new LegendEntry();
        entry.label = requireContext().getString(R.string.pref_header_spo2);
        entry.formColor = CHART_LINE_COLOR;
        legendEntries.add(entry);

        chart.getLegend().setCustom(legendEntries);
        chart.getLegend().setTextColor(LEGEND_TEXT_COLOR);
    }

    @Override
    protected void updateChartsnUIThread(CyclingChartsData cyclingData) {
        final DefaultChartsData<LineData> chartsData = cyclingData.getChartsData();
        cyclingHistoryChart.setData(null); // workaround for https://github.com/PhilJay/MPAndroidChart/issues/2317
        cyclingHistoryChart.getXAxis().setValueFormatter(chartsData.getXValueFormatter());
        cyclingHistoryChart.setData(chartsData.getData());
        cyclingHistoryChart.getAxisLeft().removeAllLimitLines();

        cyclingHistoryChart.getAxisRight().setEnabled(false);
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

    private List<CyclingSample> getSamples(final DBHandler db, final GBDevice device) {
        final int tsStart = getTSStart();
        final int tsEnd = getTSEnd();
        final DeviceCoordinator coordinator = device.getDeviceCoordinator();
        final TimeSampleProvider<CyclingSample> sampleProvider = coordinator.getCyclingSampleProvider(device, db.getDaoSession());
        return sampleProvider.getAllSamples(tsStart * 1000L, tsEnd * 1000L);
    }
}
