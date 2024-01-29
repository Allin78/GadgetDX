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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.util.HealthUtils;

/**
 * A simple {@link AbstractDashboardWidget} subclass.
 * Use the {@link DashboardSleepWidget#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardSleepWidget extends AbstractDashboardWidget {
    private static final Logger LOG = LoggerFactory.getLogger(DashboardSleepWidget.class);
    private TextView sleepAmount;
    private ImageView sleepGauge;

    public DashboardSleepWidget() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param timeFrom Start time in seconds since Unix epoch.
     * @param timeTo End time in seconds since Unix epoch.
     * @return A new instance of fragment DashboardSleepWidget.
     */
    public static DashboardSleepWidget newInstance(int timeFrom, int timeTo) {
        DashboardSleepWidget fragment = new DashboardSleepWidget();
        Bundle args = new Bundle();
        args.putInt(ARG_TIME_FROM, timeFrom);
        args.putInt(ARG_TIME_TO, timeTo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.dashboard_widget_sleep, container, false);
        sleepAmount = fragmentView.findViewById(R.id.sleep_text);
        sleepGauge = fragmentView.findViewById(R.id.sleep_gauge);

        fillData();

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sleepAmount != null && sleepGauge != null) fillData();
    }

    @Override
    protected void fillData() {
        // Update text representation
        long totalSleepMinutes = HealthUtils.getSleepMinutesTotal(timeTo);
        String sleepHours = String.format("%d", (int) Math.floor(totalSleepMinutes / 60f));
        String sleepMinutes = String.format("%02d", (int) (totalSleepMinutes % 60f));
        sleepAmount.setText(sleepHours + ":" + sleepMinutes);

        // Draw gauge
        sleepGauge.setImageBitmap(drawGauge(200, 15, color_light_sleep, HealthUtils.getSleepMinutesGoalFactor(timeTo)));
    }
}