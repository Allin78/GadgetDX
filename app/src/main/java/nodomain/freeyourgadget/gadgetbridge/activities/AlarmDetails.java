/*  Copyright (C) 2015-2024 Andreas Shimokawa, Carsten Pfeiffer, Daniel
    Dakhno, Daniele Gobbetti, Dmitry Markin, Lem Dulfo, Taavi Eomäe

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
package nodomain.freeyourgadget.gadgetbridge.activities;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TimePicker;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.entities.Alarm;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.util.AlarmUtils;
import nodomain.freeyourgadget.gadgetbridge.util.DeviceHelper;

public class AlarmDetails extends AbstractGBActivity {

    private Alarm alarm;
    private TimePicker timePicker;
    private CheckedTextView cbSmartWakeup;
    private CheckedTextView cbSnooze;
    private CheckedTextView cbMonday;
    private CheckedTextView cbTuesday;
    private CheckedTextView cbWednesday;
    private CheckedTextView cbThursday;
    private CheckedTextView cbFriday;
    private CheckedTextView cbSaturday;
    private CheckedTextView cbSunday;
    private EditText title;
    private EditText description;
    private GBDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_details);

        alarm = (Alarm) getIntent().getSerializableExtra(nodomain.freeyourgadget.gadgetbridge.model.Alarm.EXTRA_ALARM);
        device = getIntent().getParcelableExtra(GBDevice.EXTRA_DEVICE);

        title = findViewById(R.id.alarm_title);
        description = findViewById(R.id.alarm_description);

        timePicker = findViewById(R.id.alarm_time_picker);
        cbSmartWakeup = findViewById(R.id.alarm_cb_smart_wakeup);
        cbSnooze = findViewById(R.id.alarm_cb_snooze);
        cbMonday = findViewById(R.id.alarm_cb_monday);
        cbTuesday = findViewById(R.id.alarm_cb_tuesday);
        cbWednesday = findViewById(R.id.alarm_cb_wednesday);
        cbThursday = findViewById(R.id.alarm_cb_thursday);
        cbFriday = findViewById(R.id.alarm_cb_friday);
        cbSaturday = findViewById(R.id.alarm_cb_saturday);
        cbSunday = findViewById(R.id.alarm_cb_sunday);


        cbSmartWakeup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
            }
        });
        cbSnooze.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
            }
        });
        cbMonday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
            }
        });
        cbTuesday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
            }
        });
        cbWednesday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
            }
        });
        cbThursday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
            }
        });
        cbFriday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
            }
        });
        cbSaturday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
            }
        });
        cbSunday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
            }
        });

        timePicker.setIs24HourView(DateFormat.is24HourFormat(GBApplication.getContext()));
        timePicker.setCurrentHour(alarm.getHour());
        timePicker.setCurrentMinute(alarm.getMinute());

        boolean smartAlarmForced = forcedSmartWakeup(alarm.getPosition());
        cbSmartWakeup.setChecked(alarm.getSmartWakeup() || smartAlarmForced);
        int smartAlarmVisibility = supportsSmartWakeup(alarm.getPosition()) ? View.VISIBLE : View.GONE;
        cbSmartWakeup.setVisibility(smartAlarmVisibility);
        cbSmartWakeup.setEnabled(!smartAlarmForced);

        cbSnooze.setChecked(alarm.getSnooze());
        int snoozeVisibility = supportsSnoozing() ? View.VISIBLE : View.GONE;
        cbSnooze.setVisibility(snoozeVisibility);

        title.setVisibility(supportsTitle() ? View.VISIBLE : View.GONE);
        title.setText(alarm.getTitle());

        final int titleLimit = getAlarmTitleLimit();
        if (titleLimit > 0) {
            title.setFilters(new InputFilter[]{new InputFilter.LengthFilter(titleLimit)});
        }

        description.setVisibility(supportsDescription() ? View.VISIBLE : View.GONE);
        description.setText(alarm.getDescription());

        cbMonday.setChecked(alarm.getRepetition(Alarm.ALARM_MON));
        cbTuesday.setChecked(alarm.getRepetition(Alarm.ALARM_TUE));
        cbWednesday.setChecked(alarm.getRepetition(Alarm.ALARM_WED));
        cbThursday.setChecked(alarm.getRepetition(Alarm.ALARM_THU));
        cbFriday.setChecked(alarm.getRepetition(Alarm.ALARM_FRI));
        cbSaturday.setChecked(alarm.getRepetition(Alarm.ALARM_SAT));
        cbSunday.setChecked(alarm.getRepetition(Alarm.ALARM_SUN));
    }

    private boolean supportsSmartWakeup(int position) {
        if (device != null) {
            DeviceCoordinator coordinator = device.getDeviceCoordinator();
            return coordinator.supportsSmartWakeup(device, position);
        }
        return false;
    }

    /**
     * The alarm at this position *must* be a smart alarm
     */
    private boolean forcedSmartWakeup(int position) {
        if (device != null) {
            DeviceCoordinator coordinator = device.getDeviceCoordinator();
            return coordinator.forcedSmartWakeup(device, position);
        }
        return false;
    }

    private boolean supportsTitle() {
        if (device != null) {
            DeviceCoordinator coordinator = device.getDeviceCoordinator();
            return coordinator.supportsAlarmTitle(device);
        }
        return false;
    }

    private int getAlarmTitleLimit() {
        if (device != null) {
            DeviceCoordinator coordinator = device.getDeviceCoordinator();
            return coordinator.getAlarmTitleLimit(device);
        }
        return -1;
    }

    private boolean supportsDescription() {
        if (device != null) {
            DeviceCoordinator coordinator = device.getDeviceCoordinator();
            return coordinator.supportsAlarmDescription(device);
        }
        return false;
    }

    private boolean supportsSnoozing() {
        if (device != null) {
            DeviceCoordinator coordinator = device.getDeviceCoordinator();
            return coordinator.supportsAlarmSnoozing();
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // back button
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateAlarm() {
        // Set alarm as used and enabled if time has changed
        if (alarm.getUnused() && alarm.getHour() != timePicker.getCurrentHour() || alarm.getMinute() != timePicker.getCurrentMinute()) {
            alarm.setUnused(false);
            alarm.setEnabled(true);
        }
        alarm.setSmartWakeup(supportsSmartWakeup(alarm.getPosition()) && cbSmartWakeup.isChecked());
        alarm.setSnooze(supportsSnoozing() && cbSnooze.isChecked());
        int repetitionMask = AlarmUtils.createRepetitionMask(cbMonday.isChecked(), cbTuesday.isChecked(), cbWednesday.isChecked(), cbThursday.isChecked(), cbFriday.isChecked(), cbSaturday.isChecked(), cbSunday.isChecked());
        alarm.setRepetition(repetitionMask);
        alarm.setHour(timePicker.getCurrentHour());
        alarm.setMinute(timePicker.getCurrentMinute());
        alarm.setTitle(title.getText().toString());
        alarm.setDescription(description.getText().toString());
        DBHelper.store(alarm);
    }

    @Override
    protected void onPause() {
        updateAlarm();
        super.onPause();
    }
}
