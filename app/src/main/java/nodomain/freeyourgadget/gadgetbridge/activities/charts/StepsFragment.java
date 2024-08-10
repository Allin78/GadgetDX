package nodomain.freeyourgadget.gadgetbridge.activities.charts;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityAmount;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityAmounts;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityUser;
import nodomain.freeyourgadget.gadgetbridge.util.LimitedQueue;

public class StepsFragment extends AbstractChartFragment<StepsFragment.StepsData> {
    protected static final Logger LOG = LoggerFactory.getLogger(BodyEnergyFragment.class);

    private TextView mDateView;
    private ImageView stepsGauge;
    private TextView steps;
    private TextView distance;
    private TextView stepsAvg;
    private TextView stepsTotal;
    private TextView distanceAvg;
    private TextView distanceTotal;
    private TextView stepsChartTitle;
    private BarChart stepsChart;

    protected int CHART_TEXT_COLOR;
    protected int TEXT_COLOR;
    protected int STEPS_GOAL;
    protected int TOTAL_DAYS = 7;

    protected int BACKGROUND_COLOR;
    protected int DESCRIPTION_COLOR;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_steps, container, false);

        mDateView = rootView.findViewById(R.id.steps_date_view);
        stepsGauge = rootView.findViewById(R.id.steps_gauge);
        steps = rootView.findViewById(R.id.steps_count);
        distance = rootView.findViewById(R.id.steps_distance);
        stepsChart = rootView.findViewById(R.id.steps_chart);
        stepsAvg = rootView.findViewById(R.id.steps_avg);
        distanceAvg = rootView.findViewById(R.id.distance_avg);
        stepsChartTitle = rootView.findViewById(R.id.steps_chart_title);
        stepsTotal = rootView.findViewById(R.id.steps_total);
        distanceTotal = rootView.findViewById(R.id.distance_total);
        STEPS_GOAL = GBApplication.getPrefs().getInt(ActivityUser.PREF_USER_STEPS_GOAL, ActivityUser.defaultUserStepsGoal);
        setupStepsChart();
        refresh();

        if (GBApplication.getPrefs().getBoolean("charts_range", true)) {
            stepsChartTitle.setText(getString(R.string.weekstepschart_steps_a_month));
            TOTAL_DAYS = 30;
        } else {
            stepsChartTitle.setText(getString(R.string.weekstepschart_steps_a_week));
            TOTAL_DAYS = 7;
        }


        return rootView;
    }

    protected void setupStepsChart() {
        stepsChart.getDescription().setEnabled(false);
        stepsChart.setTouchEnabled(false);
        stepsChart.setPinchZoom(false);
        stepsChart.setDoubleTapToZoomEnabled(false);
        stepsChart.getLegend().setEnabled(false);

        final XAxis xAxisBottom = stepsChart.getXAxis();
        xAxisBottom.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisBottom.setDrawLabels(true);
        xAxisBottom.setDrawGridLines(false);
        xAxisBottom.setEnabled(true);
        xAxisBottom.setDrawLimitLinesBehindData(true);
        xAxisBottom.setTextColor(CHART_TEXT_COLOR);

        final YAxis yAxisLeft = stepsChart.getAxisLeft();
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setDrawTopYLabelEntry(true);
        yAxisLeft.setEnabled(true);
        yAxisLeft.setTextColor(CHART_TEXT_COLOR);
        yAxisLeft.setAxisMinimum(0f);

        final YAxis yAxisRight = stepsChart.getAxisRight();
        yAxisRight.setEnabled(true);
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawGridLines(false);
        yAxisRight.setDrawAxisLine(true);
    }

        @Override
    public String getTitle() {
        return getString(R.string.steps);
    }

    @Override
    protected void init() {
        TEXT_COLOR = GBApplication.getTextColor(requireContext());
        CHART_TEXT_COLOR = GBApplication.getSecondaryTextColor(requireContext());
        BACKGROUND_COLOR = GBApplication.getBackgroundColor(getContext());
        DESCRIPTION_COLOR = GBApplication.getTextColor(getContext());
        CHART_TEXT_COLOR = GBApplication.getSecondaryTextColor(getContext());
    }

    @Override
    protected StepsFragment.StepsData refreshInBackground(ChartsHost chartsHost, DBHandler db, GBDevice device) {
        Calendar day = Calendar.getInstance();
        day.setTime(chartsHost.getEndDate());
        String formattedDate = new SimpleDateFormat("E, MMM dd").format(chartsHost.getEndDate());
        mDateView.setText(formattedDate);
        List<StepsDay> stepsDaysData = getMyStepsDaysData(db, day, device);
        return new StepsFragment.StepsData(stepsDaysData);
    }

    @Override
    protected void updateChartsnUIThread(StepsFragment.StepsData stepsData) {
        stepsChart.setData(null);

        List<BarEntry> entries = new ArrayList<>();
        int counter = 0;
        for(StepsDay day : stepsData.days) {
            entries.add(new BarEntry(counter, day.steps));
            counter++;
        }
        BarDataSet set = new BarDataSet(entries, "Steps");
        set.setDrawValues(false);
        set.setColors(getResources().getColor(R.color.steps_color));
        final XAxis x = stepsChart.getXAxis();
        x.setValueFormatter(getStepsChartDayValueFormatter(stepsData));
        if (TOTAL_DAYS > 7) {
            x.setLabelCount(2, true);
        }

        BarData barData = new BarData(set);
        barData.setValueTextColor(Color.GRAY); //prevent tearing other graph elements with the black text. Another approach would be to hide the values cmpletely with data.setDrawValues(false);
        barData.setValueTextSize(10f);
        stepsChart.setData(barData);

        stepsGauge.setImageBitmap(drawGauge(
                300,
                20,
                getResources().getColor(R.color.steps_color),
                (int) stepsData.todayStepsDay.steps,
                STEPS_GOAL
        ));

        steps.setText(String.format(String.valueOf(stepsData.todayStepsDay.steps)));
        distance.setText(getString(R.string.steps_distance_unit, stepsData.todayStepsDay.distance));
        stepsAvg.setText(String.format(String.valueOf(stepsData.stepsDailyAvg)));
        distanceAvg.setText(getString(R.string.steps_distance_unit, stepsData.distanceDailyAvg));
        stepsTotal.setText(String.format(String.valueOf(stepsData.totalSteps)));
        distanceTotal.setText(getString(R.string.steps_distance_unit, stepsData.totalDistance));
    }

    ValueFormatter getStepsChartDayValueFormatter(StepsFragment.StepsData stepsData) {
        return new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                StepsFragment.StepsDay day = stepsData.days.get((int) value);
                String pattern = TOTAL_DAYS > 7 ? "dd/MM" : "EEE";
                SimpleDateFormat formatLetterDay = new SimpleDateFormat(pattern, Locale.getDefault());
                return formatLetterDay.format(new Date(day.day.getTimeInMillis()));
            }
        };
    }

    @Override
    protected void renderCharts() {
        stepsChart.invalidate();
    }

    protected void setupLegend(Chart<?> chart) {}

    private List<StepsFragment.StepsDay> getMyStepsDaysData(DBHandler db, Calendar day, GBDevice device) {
        day = (Calendar) day.clone(); // do not modify the caller's argument
        day.add(Calendar.DATE, -TOTAL_DAYS + 1);

        List<StepsDay> daysData = new ArrayList<>();;
        for (int counter = 0; counter < TOTAL_DAYS; counter++) {
            long totalSteps = 0;
            ActivityAmounts amounts = getActivityAmountsForDay(db, day, device);
            for (ActivityAmount amount : amounts.getAmounts()) {
                if (amount.getTotalSteps() > 0) {
                    totalSteps += amount.getTotalSteps();
                }
            }
            double distance = 0;
            if (totalSteps > 0) {
                double avgStep = (0.67+0.762)/2; // https://marathonhandbook.com/average-stride-length/  (female+male)/2
                distance = avgStep * totalSteps / 1000;
            }
            Calendar d = (Calendar) day.clone();
            daysData.add(new StepsDay(d, totalSteps, distance));
            day.add(Calendar.DATE, 1);
        }
        return daysData;
    }

    protected ActivityAmounts getActivityAmountsForDay(DBHandler db, Calendar day, GBDevice device) {
        LimitedQueue<Integer, ActivityAmounts> activityAmountCache = null;
        ActivityAmounts amounts = null;

        Activity activity = getActivity();
        int key = (int) (day.getTimeInMillis() / 1000);
        if (activity != null) {
            activityAmountCache = ((ActivityChartsActivity) activity).mActivityAmountCache;
            amounts = activityAmountCache.lookup(key);
        }

        if (amounts == null) {
            ActivityAnalysis analysis = new ActivityAnalysis();
            amounts = analysis.calculateActivityAmounts(getSamplesOfDay(db, day, 0, device));
            if (activityAmountCache != null) {
                activityAmountCache.add(key, amounts);
            }
        }

        return amounts;
    }

    private List<? extends ActivitySample> getSamplesOfDay(DBHandler db, Calendar day, int offsetHours, GBDevice device) {
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

    protected List<? extends ActivitySample> getSamples(DBHandler db, GBDevice device, int tsFrom, int tsTo) {
        SampleProvider<? extends ActivitySample> provider = device.getDeviceCoordinator().getSampleProvider(device, db.getDaoSession());
        return provider.getAllActivitySamples(tsFrom, tsTo);
    }

    Bitmap drawGauge(int width, int barWidth, @ColorInt int filledColor, int value, int maxValue) {
        int height = width;
        int barMargin = (int) Math.ceil(barWidth / 2f);
        float filledFactor = (float) value / maxValue;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(barWidth);
        paint.setColor(getResources().getColor(R.color.gauge_line_color));
        canvas.drawArc(
                barMargin,
                barMargin,
                width - barMargin,
                width - barMargin,
                90,
                360,
                false,
                paint);
        paint.setStrokeWidth(barWidth);
        paint.setColor(filledColor);
        canvas.drawArc(
                barMargin,
                barMargin,
                width - barMargin,
                height - barMargin,
                90,
                360 * filledFactor,
                false,
                paint
        );

        Paint textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        float textPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, requireContext().getResources().getDisplayMetrics());
        textPaint.setTextSize(textPixels);
        textPaint.setTextAlign(Paint.Align.CENTER);
        int yPos = (int) ((float) height / 2 - ((textPaint.descent() + textPaint.ascent()) / 2)) ;
        canvas.drawText(String.valueOf(value), width / 2f, yPos, textPaint);
        Paint textLowerPaint = new Paint();
        textLowerPaint.setColor(TEXT_COLOR);
        textLowerPaint.setTextAlign(Paint.Align.CENTER);
        float textLowerPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, requireContext().getResources().getDisplayMetrics());
        textLowerPaint.setTextSize(textLowerPixels);
        int yPosLowerText = (int) ((float) height / 2 - textPaint.ascent()) ;
        canvas.drawText(String.valueOf(maxValue), width / 2f, yPosLowerText, textLowerPaint);

        return bitmap;
    }

    protected static class StepsDay {
        public long steps;
        public double distance;
        public Calendar day;

        protected StepsDay(Calendar day, long steps, double distance) {
            this.steps = steps;
            this.distance = distance;
            this.day = day;
        }
    }

    protected static class StepsData extends ChartsData {
        List<StepsDay> days;
        long stepsDailyAvg = 0;
        double distanceDailyAvg = 0;
        long totalSteps = 0;
        double totalDistance = 0;
        StepsDay todayStepsDay;
        protected StepsData(List<StepsDay> days) {
            this.days = days;
            int daysCounter = 0;
            for(StepsDay day : days) {
                this.totalSteps += day.steps;
                this.totalDistance += day.distance;
                if (day.steps > 0) {
                    daysCounter++;
                }
            }
            if (daysCounter > 0) {
                this.stepsDailyAvg = this.totalSteps / daysCounter;
                this.distanceDailyAvg = this.totalDistance / daysCounter;
            }
            this.todayStepsDay = days.get(days.size() - 1);
        }
    }
}
