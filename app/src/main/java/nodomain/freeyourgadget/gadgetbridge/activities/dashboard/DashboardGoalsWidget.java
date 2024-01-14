/*  Copyright (C) 2023-2024 Arjan Schrijver

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
package nodomain.freeyourgadget.gadgetbridge.activities.dashboard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;

/**
 * A simple {@link AbstractDashboardWidget} subclass.
 * Use the {@link DashboardGoalsWidget#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardGoalsWidget extends AbstractDashboardWidget {
    private static final Logger LOG = LoggerFactory.getLogger(DashboardGoalsWidget.class);
    private ImageView goalsChart;

    public DashboardGoalsWidget() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param timeFrom Start time in seconds since Unix epoch.
     * @param timeTo End time in seconds since Unix epoch.
     * @return A new instance of fragment DashboardGoalsWidget.
     */
    public static DashboardGoalsWidget newInstance(int timeFrom, int timeTo) {
        DashboardGoalsWidget fragment = new DashboardGoalsWidget();
        Bundle args = new Bundle();
        args.putInt(ARG_TIME_FROM, timeFrom);
        args.putInt(ARG_TIME_TO, timeTo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View todayView = inflater.inflate(R.layout.dashboard_widget_goals, container, false);
        goalsChart = todayView.findViewById(R.id.dashboard_goals_chart);

        // Initialize legend
        TextView legend = todayView.findViewById(R.id.dashboard_goals_legend);
        SpannableString l_steps = new SpannableString("■ " + getString(R.string.steps));
        l_steps.setSpan(new ForegroundColorSpan(color_activity), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString l_distance = new SpannableString("■ " + getString(R.string.distance));
        l_distance.setSpan(new ForegroundColorSpan(color_distance), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString l_active_time = new SpannableString("■ " + getString(R.string.activity_list_summary_active_time));
        l_active_time.setSpan(new ForegroundColorSpan(color_active_time), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString l_sleep = new SpannableString("■ " + getString(R.string.menuitem_sleep));
        l_sleep.setSpan(new ForegroundColorSpan(color_light_sleep), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableStringBuilder legendBuilder = new SpannableStringBuilder();
        legend.setText(legendBuilder.append(l_steps).append(" ").append(l_distance).append("\n").append(l_active_time).append(" ").append(l_sleep));

        fillData();

        return todayView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (goalsChart != null) fillData();
    }

    protected void fillData() {
        int width = 230;
        int height = 230;
        int barWidth = 10;
        int barMargin = (int) Math.ceil(barWidth / 2f);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(barWidth);

        paint.setColor(color_activity);
        canvas.drawArc(barMargin, barMargin, width - barMargin, height - barMargin, 270, 360 * getStepsGoalFactor(), false, paint);

        barMargin += barWidth * 1.5;
        paint.setColor(color_distance);
        canvas.drawArc(barMargin, barMargin, width - barMargin, height - barMargin, 270, 360 * getDistanceGoalFactor(), false, paint);

        barMargin += barWidth * 1.5;
        paint.setColor(color_active_time);
        canvas.drawArc(barMargin, barMargin, width - barMargin, height - barMargin, 270, 360 * getActiveMinutesGoalFactor(), false, paint);

        barMargin += barWidth * 1.5;
        paint.setColor(color_light_sleep);
        canvas.drawArc(barMargin, barMargin, width - barMargin, height - barMargin, 270, 360 * getSleepMinutesGoalFactor(), false, paint);

        goalsChart.setImageBitmap(bitmap);
    }
}