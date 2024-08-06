package nodomain.freeyourgadget.gadgetbridge.activities.charts;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;

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

    protected @ColorInt int color_unknown = Color.argb(25, 128, 128, 128);
    protected @ColorInt int color_active_time = Color.rgb(170, 0, 255);

    private TextView mDateView;
    private ImageView bodyEnergyGauge;

    protected int CHART_TEXT_COLOR;
    protected int LEGEND_TEXT_COLOR;
    protected int TEXT_COLOR;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_body_energy, container, false);

//        mHRVStatusLastNight = rootView.findViewById(R.id.hrv_status_last_night);
//        mHRVStatusSevenDaysAvg = rootView.findViewById(R.id.hrv_status_seven_days_avg);
//        mHRVStatusSevenDaysAvgStatus = rootView.findViewById(R.id.hrv_status_seven_days_avg_rate);
//        mHRVStatusLastNight5MinHighest = rootView.findViewById(R.id.hrv_status_last_night_highest_5);
//        mHRVStatusDayAvg = rootView.findViewById(R.id.hrv_status_day_avg);
//        mHRVStatusBaseline = rootView.findViewById(R.id.hrv_status_baseline);
        mDateView = rootView.findViewById(R.id.hrv_status_date_view);
        bodyEnergyGauge = rootView.findViewById(R.id.body_energy_gauge);

//        refresh();


        return rootView;
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
//        List<? extends BodyEnergySample> samples = getBodyEnergySamples(db, device, getTSStart(), getTSEnd());

        bodyEnergyGauge.setImageBitmap(drawGauge(300, 20, color_active_time, 0.30F));

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

    /**
     * @param width Bitmap width in pixels
     * @param barWidth Gauge bar width in pixels
     * @param filledColor Color of the filled part of the gauge
     * @param filledFactor Factor between 0 and 1 that determines the amount of the gauge that should be filled
     * @return Bitmap containing the gauge
     */
    Bitmap drawGauge(int width, int barWidth, @ColorInt int filledColor, float filledFactor) {
        int height = width;
        int barMargin = (int) Math.ceil(barWidth / 2f);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(barWidth);
        paint.setColor(color_unknown);
        canvas.drawArc(
                barMargin,
                barMargin,
                width - barMargin,
                width - barMargin,
                120,
                300,
                false,
                paint);
        paint.setStrokeWidth(barWidth);
        paint.setColor(filledColor);
        canvas.drawArc(
                barMargin,
                barMargin,
                width - barMargin,
                height - barMargin,
                120,
                300 * filledFactor,
                false,
                paint
        );

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        float textPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, requireContext().getResources().getDisplayMetrics());
        textPaint.setTextSize(textPixels);
        textPaint.setTextAlign(Paint.Align.CENTER);
        int yPos = (int) ((float) height / 2 - ((textPaint.descent() + textPaint.ascent()) / 2)) ;
        canvas.drawText("30", width / 2f, yPos, textPaint);



        return bitmap;
    }

    protected static class BodyEnergyData extends ChartsData {}

}