/*  Copyright (C) 2019-2020 Andreas Shimokawa, Cre3per

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
package nodomain.freeyourgadget.gadgetbridge.activities.devicesettings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.CalBlacklistActivity;
import nodomain.freeyourgadget.gadgetbridge.activities.SettingsActivity;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceManager;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst;
import nodomain.freeyourgadget.gadgetbridge.devices.makibeshr3.MakibesHR3Constants;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst;
import nodomain.freeyourgadget.gadgetbridge.devices.qhybrid.ConfigActivity;
import nodomain.freeyourgadget.gadgetbridge.devices.zetime.ZeTimeConstants;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityUser;
import nodomain.freeyourgadget.gadgetbridge.model.CannedMessagesSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationType;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import nodomain.freeyourgadget.gadgetbridge.util.XTimePreference;
import nodomain.freeyourgadget.gadgetbridge.util.XTimePreferenceFragment;

import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_ALTITUDE_CALIBRATE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_AMPM_ENABLED;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_ANTILOST_ENABLED;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BUTTON_1_FUNCTION_SHORT;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BUTTON_2_FUNCTION_SHORT;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BUTTON_3_FUNCTION_SHORT;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BUTTON_1_FUNCTION_LONG;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BUTTON_2_FUNCTION_LONG;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BUTTON_3_FUNCTION_LONG;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BUTTON_1_FUNCTION_DOUBLE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BUTTON_2_FUNCTION_DOUBLE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BUTTON_3_FUNCTION_DOUBLE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_BUTTON_BP_CALIBRATE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DATEFORMAT;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DISCONNECTNOTIF_NOSHED;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_NOAUTO;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_FIND_PHONE_ENABLED;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_HYBRID_HR_DRAW_WIDGET_CIRCLES;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_HYBRID_HR_FORCE_WHITE_COLOR;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_HYBRID_HR_SAVE_RAW_ACTIVITY_FILES;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_HYDRATION_PERIOD;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_HYDRATION_SWITCH;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_LANGUAGE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_LEFUN_INTERFACE_LANGUAGE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_LIFTWRIST_NOSHED;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_LONGSIT_PERIOD;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_LONGSIT_SWITCH;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_POWER_MODE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SCREEN_ORIENTATION;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SONYSWR12_LOW_VIBRATION;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SONYSWR12_SMART_INTERVAL;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_SONYSWR12_STAMINA;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_TIMEFORMAT;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_VIBRATION_STRENGH_PERCENTAGE;
import static nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst.PREF_WEARLOCATION;
import static nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst.PREF_ACTIVATE_DISPLAY_ON_LIFT;
import static nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst.PREF_DISCONNECT_NOTIFICATION;
import static nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst.PREF_DISCONNECT_NOTIFICATION_END;
import static nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst.PREF_DISCONNECT_NOTIFICATION_START;
import static nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst.PREF_DISPLAY_ITEMS;
import static nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst.PREF_DISPLAY_ON_LIFT_END;
import static nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst.PREF_DISPLAY_ON_LIFT_START;
import static nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst.PREF_EXPOSE_HR_THIRDPARTY;
import static nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst.PREF_SHORTCUTS;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_DO_NOT_DISTURB;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_DO_NOT_DISTURB_END;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_DO_NOT_DISTURB_OFF;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_DO_NOT_DISTURB_SCHEDULED;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_DO_NOT_DISTURB_START;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MI2_DATEFORMAT;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MI2_GOAL_NOTIFICATION;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MI2_INACTIVITY_WARNINGS;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MI2_INACTIVITY_WARNINGS_DND;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MI2_INACTIVITY_WARNINGS_DND_END;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MI2_INACTIVITY_WARNINGS_DND_START;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MI2_INACTIVITY_WARNINGS_END;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MI2_INACTIVITY_WARNINGS_START;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MI2_INACTIVITY_WARNINGS_THRESHOLD;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MI2_ROTATE_WRIST_TO_SWITCH_INFO;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_MIBAND_USE_HR_FOR_SLEEP_DETECTION;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_NIGHT_MODE;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_NIGHT_MODE_END;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_NIGHT_MODE_OFF;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_NIGHT_MODE_SCHEDULED;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_NIGHT_MODE_START;
import static nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandConst.PREF_SWIPE_UNLOCK;

public class DeviceSpecificSettingsFragment extends PreferenceFragmentCompat {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceSpecificSettingsFragment.class);

    static final String FRAGMENT_TAG = "DEVICE_SPECIFIC_SETTINGS_FRAGMENT";

    private void setSettingsFileSuffix(String settingsFileSuffix, @NonNull int[] supportedSettings) {
        Bundle args = new Bundle();
        args.putString("settingsFileSuffix", settingsFileSuffix);
        args.putIntArray("supportedSettings", supportedSettings);
        setArguments(args);
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        String settingsFileSuffix = arguments.getString("settingsFileSuffix", null);
        int[] supportedSettings = arguments.getIntArray("supportedSettings");
        if (settingsFileSuffix == null || supportedSettings == null) {
            return;
        }

        getPreferenceManager().setSharedPreferencesName("devicesettings_" + settingsFileSuffix);

        if (rootKey == null) {
            // we are the main preference screen
            boolean first = true;
            for (int setting : supportedSettings) {
                if (first) {
                    setPreferencesFromResource(setting, null);
                    first = false;
                } else {
                    addPreferencesFromResource(setting);
                }
            }
        } else {
            // Now, this is ugly: search all the xml files for the rootKey
            for (int setting : supportedSettings) {
                try {
                    setPreferencesFromResource(setting, rootKey);
                } catch (Exception ignore) {
                    continue;
                }
                break;
            }
        }
        setChangeListener();
    }

    /*
     * delayed execution so that the preferences are applied first
     */
    private void invokeLater(Runnable runnable) {
        getListView().post(runnable);
    }

    private void setChangeListener() {
        final Prefs prefs = new Prefs(getPreferenceManager().getSharedPreferences());
        String disconnectNotificationState = prefs.getString(PREF_DISCONNECT_NOTIFICATION, PREF_DO_NOT_DISTURB_OFF);
        boolean disconnectNotificationScheduled = disconnectNotificationState.equals(PREF_DO_NOT_DISTURB_SCHEDULED);

        final Preference disconnectNotificationStart = findPreference(PREF_DISCONNECT_NOTIFICATION_START);
        if (disconnectNotificationStart != null) {
            disconnectNotificationStart.setEnabled(disconnectNotificationScheduled);
            disconnectNotificationStart.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_DISCONNECT_NOTIFICATION_START);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference disconnectNotificationEnd = findPreference(PREF_DISCONNECT_NOTIFICATION_END);
        if (disconnectNotificationEnd != null) {
            disconnectNotificationEnd.setEnabled(disconnectNotificationScheduled);
            disconnectNotificationEnd.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_DISCONNECT_NOTIFICATION_END);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference disconnectNotification = findPreference(PREF_DISCONNECT_NOTIFICATION);
        if (disconnectNotification != null) {
            disconnectNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    final boolean scheduled = PREF_DO_NOT_DISTURB_SCHEDULED.equals(newVal.toString());

                    Objects.requireNonNull(disconnectNotificationStart).setEnabled(scheduled);
                    Objects.requireNonNull(disconnectNotificationEnd).setEnabled(scheduled);
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_DISCONNECT_NOTIFICATION);
                        }
                    });
                    return true;
                }
            });

        }

        String nightModeState = prefs.getString(MiBandConst.PREF_NIGHT_MODE, PREF_NIGHT_MODE_OFF);
        boolean nightModeScheduled = nightModeState.equals(PREF_NIGHT_MODE_SCHEDULED);

        final Preference nightModeStart = findPreference(PREF_NIGHT_MODE_START);
        if (nightModeStart != null) {
            nightModeStart.setEnabled(nightModeScheduled);
            nightModeStart.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_NIGHT_MODE_START);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference nightModeEnd = findPreference(PREF_NIGHT_MODE_END);
        if (nightModeEnd != null) {
            nightModeEnd.setEnabled(nightModeScheduled);
            nightModeEnd.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_NIGHT_MODE_END);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference nightMode = findPreference(PREF_NIGHT_MODE);
        if (nightMode != null) {

            nightMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    final boolean scheduled = PREF_NIGHT_MODE_SCHEDULED.equals(newVal.toString());

                    Objects.requireNonNull(nightModeStart).setEnabled(scheduled);
                    Objects.requireNonNull(nightModeEnd).setEnabled(scheduled);

                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_NIGHT_MODE);
                        }
                    });
                    return true;
                }
            });
        }


        String doNotDisturbState = prefs.getString(MiBandConst.PREF_DO_NOT_DISTURB, PREF_DO_NOT_DISTURB_OFF);
        boolean doNotDisturbScheduled = doNotDisturbState.equals(PREF_DO_NOT_DISTURB_SCHEDULED);

        final Preference doNotDisturbStart = findPreference(PREF_DO_NOT_DISTURB_START);
        if (doNotDisturbStart != null) {
            doNotDisturbStart.setEnabled(doNotDisturbScheduled);
            doNotDisturbStart.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_DO_NOT_DISTURB_START);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference doNotDisturbEnd = findPreference(PREF_DO_NOT_DISTURB_END);
        if (doNotDisturbEnd != null) {
            doNotDisturbEnd.setEnabled(doNotDisturbScheduled);
            doNotDisturbEnd.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_DO_NOT_DISTURB_END);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference doNotDisturb = findPreference(PREF_DO_NOT_DISTURB);
        if (doNotDisturb != null) {
            doNotDisturb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    final boolean scheduled = PREF_DO_NOT_DISTURB_SCHEDULED.equals(newVal.toString());

                    Objects.requireNonNull(doNotDisturbStart).setEnabled(scheduled);
                    Objects.requireNonNull(doNotDisturbEnd).setEnabled(scheduled);

                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_DO_NOT_DISTURB);
                        }
                    });
                    return true;
                }
            });
        }

        addPreferenceHandlerFor(PREF_SWIPE_UNLOCK);
        addPreferenceHandlerFor(PREF_MI2_DATEFORMAT);
        addPreferenceHandlerFor(PREF_DATEFORMAT);
        addPreferenceHandlerFor(PREF_DISPLAY_ITEMS);
        addPreferenceHandlerFor(PREF_SHORTCUTS);
        addPreferenceHandlerFor(PREF_LANGUAGE);
        addPreferenceHandlerFor(PREF_EXPOSE_HR_THIRDPARTY);
        addPreferenceHandlerFor(PREF_WEARLOCATION);
        addPreferenceHandlerFor(PREF_SCREEN_ORIENTATION);
        addPreferenceHandlerFor(PREF_TIMEFORMAT);
        addPreferenceHandlerFor(PREF_BUTTON_1_FUNCTION_SHORT);
        addPreferenceHandlerFor(PREF_BUTTON_2_FUNCTION_SHORT);
        addPreferenceHandlerFor(PREF_BUTTON_3_FUNCTION_SHORT);
        addPreferenceHandlerFor(PREF_BUTTON_1_FUNCTION_LONG);
        addPreferenceHandlerFor(PREF_BUTTON_2_FUNCTION_LONG);
        addPreferenceHandlerFor(PREF_BUTTON_3_FUNCTION_LONG);
        addPreferenceHandlerFor(PREF_BUTTON_1_FUNCTION_DOUBLE);
        addPreferenceHandlerFor(PREF_BUTTON_2_FUNCTION_DOUBLE);
        addPreferenceHandlerFor(PREF_BUTTON_3_FUNCTION_DOUBLE);
        addPreferenceHandlerFor(PREF_VIBRATION_STRENGH_PERCENTAGE);
        addPreferenceHandlerFor(PREF_POWER_MODE);
        addPreferenceHandlerFor(PREF_LIFTWRIST_NOSHED);
        addPreferenceHandlerFor(PREF_DISCONNECTNOTIF_NOSHED);
        addPreferenceHandlerFor(PREF_BUTTON_BP_CALIBRATE);
        addPreferenceHandlerFor(PREF_ALTITUDE_CALIBRATE);
        addPreferenceHandlerFor(PREF_LONGSIT_PERIOD);
        addPreferenceHandlerFor(PREF_LONGSIT_SWITCH);
        addPreferenceHandlerFor(PREF_DO_NOT_DISTURB_NOAUTO);
        addPreferenceHandlerFor(PREF_FIND_PHONE_ENABLED);
        addPreferenceHandlerFor(PREF_ANTILOST_ENABLED);
        addPreferenceHandlerFor(PREF_HYDRATION_SWITCH);
        addPreferenceHandlerFor(PREF_HYDRATION_PERIOD);
        addPreferenceHandlerFor(PREF_AMPM_ENABLED);
        addPreferenceHandlerFor(PREF_LEFUN_INTERFACE_LANGUAGE);

        addPreferenceHandlerFor(PREF_HYBRID_HR_DRAW_WIDGET_CIRCLES);
        addPreferenceHandlerFor(PREF_HYBRID_HR_FORCE_WHITE_COLOR);
        addPreferenceHandlerFor(PREF_HYBRID_HR_SAVE_RAW_ACTIVITY_FILES);

        addPreferenceHandlerFor(PREF_SONYSWR12_STAMINA);
        addPreferenceHandlerFor(PREF_SONYSWR12_LOW_VIBRATION);
        addPreferenceHandlerFor(PREF_SONYSWR12_SMART_INTERVAL);

        String displayOnLiftState = prefs.getString(PREF_ACTIVATE_DISPLAY_ON_LIFT, PREF_DO_NOT_DISTURB_OFF);
        boolean displayOnLiftScheduled = displayOnLiftState.equals(PREF_DO_NOT_DISTURB_SCHEDULED);

        final Preference rotateWristCycleInfo = findPreference(PREF_MI2_ROTATE_WRIST_TO_SWITCH_INFO);
        if (rotateWristCycleInfo != null) {
            rotateWristCycleInfo.setEnabled(!PREF_DO_NOT_DISTURB_OFF.equals(displayOnLiftState));
            rotateWristCycleInfo.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_MI2_ROTATE_WRIST_TO_SWITCH_INFO);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference displayOnLiftStart = findPreference(PREF_DISPLAY_ON_LIFT_START);
        if (displayOnLiftStart != null) {
            displayOnLiftStart.setEnabled(displayOnLiftScheduled);
            displayOnLiftStart.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_DISPLAY_ON_LIFT_START);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference displayOnLiftEnd = findPreference(PREF_DISPLAY_ON_LIFT_END);
        if (displayOnLiftEnd != null) {
            displayOnLiftEnd.setEnabled(displayOnLiftScheduled);
            displayOnLiftEnd.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_DISPLAY_ON_LIFT_END);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference displayOnLift = findPreference(PREF_ACTIVATE_DISPLAY_ON_LIFT);
        if (displayOnLift != null) {
            displayOnLift.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    final boolean scheduled = PREF_DO_NOT_DISTURB_SCHEDULED.equals(newVal.toString());
                    Objects.requireNonNull(displayOnLiftStart).setEnabled(scheduled);
                    Objects.requireNonNull(displayOnLiftEnd).setEnabled(scheduled);
                    if (rotateWristCycleInfo != null) {
                        rotateWristCycleInfo.setEnabled(!PREF_DO_NOT_DISTURB_OFF.equals(newVal.toString()));
                    }
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_ACTIVATE_DISPLAY_ON_LIFT);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference cannedMessagesDismissCall = findPreference("canned_messages_dismisscall_send");
        if (cannedMessagesDismissCall != null) {
            cannedMessagesDismissCall.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    ArrayList<String> messages = new ArrayList<>();
                    for (int i = 1; i <= 16; i++) {
                        String message = prefs.getString("canned_message_dismisscall_" + i, null);
                        if (message != null && !message.equals("")) {
                            messages.add(message);
                        }
                    }
                    CannedMessagesSpec cannedMessagesSpec = new CannedMessagesSpec();
                    cannedMessagesSpec.type = CannedMessagesSpec.TYPE_REJECTEDCALLS;
                    cannedMessagesSpec.cannedMessages = messages.toArray(new String[0]);
                    GBApplication.deviceService().onSetCannedMessages(cannedMessagesSpec);
                    return true;
                }
            });
        }

        setInputTypeFor(HuamiConst.PREF_BUTTON_ACTION_BROADCAST_DELAY, InputType.TYPE_CLASS_NUMBER);
        setInputTypeFor(HuamiConst.PREF_BUTTON_ACTION_PRESS_MAX_INTERVAL, InputType.TYPE_CLASS_NUMBER);
        setInputTypeFor(HuamiConst.PREF_BUTTON_ACTION_PRESS_COUNT, InputType.TYPE_CLASS_NUMBER);
        setInputTypeFor(MiBandConst.PREF_MIBAND_DEVICE_TIME_OFFSET_HOURS, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        setInputTypeFor(MakibesHR3Constants.PREF_FIND_PHONE_DURATION, InputType.TYPE_CLASS_NUMBER);
        setInputTypeFor(DeviceSettingsPreferenceConst.PREF_RESERVER_ALARMS_CALENDAR, InputType.TYPE_CLASS_NUMBER);





        //ZeTime properties:
        addPreferenceHandlerFor(ZeTimeConstants.PREF_SCREENTIME);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_ANALOG_MODE);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_ACTIVITY_TRACKING);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_HANDMOVE_DISPLAY);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_DO_NOT_DISTURB);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_DO_NOT_DISTURB_START);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_DO_NOT_DISTURB_END);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_CALORIES_TYPE);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_DATE_FORMAT);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_ENABLE);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_START);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_END);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_THRESHOLD);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_MO);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_TU);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_WE);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_TH);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_FR);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_SA);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_SU);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_SMS_SIGNALING);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_ANTI_LOSS_SIGNALING);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_CALENDAR_SIGNALING);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_CALL_SIGNALING);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_MISSED_CALL_SIGNALING);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_EMAIL_SIGNALING);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_INACTIVITY_SIGNALING);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_LOW_POWER_SIGNALING);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_SOCIAL_SIGNALING);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_ZETIME_HEARTRATE_ALARM);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_ZETIME_MAX_HEARTRATE);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_ZETIME_MIN_HEARTRATE);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_USER_SLEEP_GOAL);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_USER_CALORIES_GOAL);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_USER_DISTANCE_GOAL);
        addPreferenceHandlerFor(ZeTimeConstants.PREF_USER_ACTIVETIME_GOAL);

        //QHybrid
        final Preference qHybridPreferences = findPreference("pref_key_qhybrid");
        if (qHybridPreferences != null)
        {
            qHybridPreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), ConfigActivity.class));
                    return true;
                }
            });
        }

        //Pebble
        final Preference calendarBlacklist = findPreference("pref_key_blacklist_calendars");
        calendarBlacklist.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent enableIntent = new Intent(getActivity(), CalBlacklistActivity.class);
                startActivity(enableIntent);
                return true;
            }
        });
        final Preference pebbleEmuAddress = findPreference("pebble_emu_addr");
        pebbleEmuAddress.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newVal) {
                Intent refreshIntent = new Intent(DeviceManager.ACTION_REFRESH_DEVICELIST);
                LocalBroadcastManager.getInstance(GBApplication.getContext()).sendBroadcast(refreshIntent);
                preference.setSummary(newVal.toString());
                return true;
            }

        });
        final Preference pebbleEmuPort = findPreference("pebble_emu_port");
        pebbleEmuPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newVal) {
                Intent refreshIntent = new Intent(DeviceManager.ACTION_REFRESH_DEVICELIST);
                LocalBroadcastManager.getInstance(GBApplication.getContext()).sendBroadcast(refreshIntent);
                preference.setSummary(newVal.toString());
                return true;
            }
        });
        final Preference pebbleLocationAcquire = findPreference("location_aquire");
        pebbleLocationAcquire.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (ActivityCompat.checkSelfPermission(GBApplication.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                }

                LocationManager locationManager = (LocationManager) GBApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, false);
                if (provider != null) {
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        setLocationPreferences(location);
                    } else {
                        locationManager.requestSingleUpdate(provider, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                setLocationPreferences(location);
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                                LOG.info("provider status changed to " + status + " (" + provider + ")");
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                                LOG.info("provider enabled (" + provider + ")");
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                                LOG.info("provider disabled (" + provider + ")");
                                GB.toast(getActivity(), getString(R.string.toast_enable_networklocationprovider), 3000, 0);
                            }
                        }, null);
                    }
                } else {
                    LOG.warn("No location provider found, did you deny location permission?");
                }
                return true;
            }
        });

        //MiBand/Amazfit/Huami/...
        final Preference enableHeartrateSleepSupport = findPreference(PREF_MIBAND_USE_HR_FOR_SLEEP_DETECTION);
        if (enableHeartrateSleepSupport != null) {
            enableHeartrateSleepSupport.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    GBApplication.deviceService().onEnableHeartRateSleepSupport(Boolean.TRUE.equals(newVal));
                    return true;
                }
            });
        }

        final Preference heartrateMeasurementInterval = findPreference("heartrate_measurement_interval");
        if (heartrateMeasurementInterval != null) {
            heartrateMeasurementInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    GBApplication.deviceService().onSetHeartRateMeasurementInterval(Integer.parseInt((String) newVal));
                    return true;
                }
            });
        }

        final Preference goalNotification = findPreference(PREF_MI2_GOAL_NOTIFICATION);
        if (goalNotification != null) {
            goalNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_MI2_GOAL_NOTIFICATION);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference inactivityWarnings = findPreference(PREF_MI2_INACTIVITY_WARNINGS);
        if (inactivityWarnings != null) {
            inactivityWarnings.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_MI2_INACTIVITY_WARNINGS);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference inactivityWarningsThreshold = findPreference(PREF_MI2_INACTIVITY_WARNINGS_THRESHOLD);
        if (inactivityWarningsThreshold != null) {
            inactivityWarningsThreshold.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_MI2_INACTIVITY_WARNINGS_THRESHOLD);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference inactivityWarningsStart = findPreference(PREF_MI2_INACTIVITY_WARNINGS_START);
        if (inactivityWarningsStart != null) {
            inactivityWarningsStart.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_MI2_INACTIVITY_WARNINGS_START);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference inactivityWarningsEnd = findPreference(PREF_MI2_INACTIVITY_WARNINGS_END);
        if (inactivityWarningsEnd != null) {
            inactivityWarningsEnd.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_MI2_INACTIVITY_WARNINGS_END);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference inactivityWarningsDnd = findPreference(PREF_MI2_INACTIVITY_WARNINGS_DND);
        if (inactivityWarningsDnd != null) {
            inactivityWarningsDnd.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_MI2_INACTIVITY_WARNINGS_DND);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference inactivityWarningsDndStart = findPreference(PREF_MI2_INACTIVITY_WARNINGS_DND_START);
        if (inactivityWarningsDndStart != null) {
            inactivityWarningsDndStart.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_MI2_INACTIVITY_WARNINGS_DND_START);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference inactivityWarningsDndEnd = findPreference(PREF_MI2_INACTIVITY_WARNINGS_DND_END);
        if (inactivityWarningsDndEnd != null) {
            inactivityWarningsDndEnd.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(PREF_MI2_INACTIVITY_WARNINGS_DND_END);
                        }
                    });
                    return true;
                }
            });
        }

        final Preference tryVibrationGenericSms = findPreference("mi_try_generic_sms");
        if (tryVibrationGenericSms != null) {
            tryVibrationGenericSms.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    NotificationSpec notificationSpec = new NotificationSpec();
                    notificationSpec.phoneNumber = "TEST";
                    notificationSpec.type = NotificationType.GENERIC_SMS;
                    GBApplication.deviceService().onNotification(notificationSpec);
                    return true;
                }
            });
        }
    }

    static DeviceSpecificSettingsFragment newInstance(String settingsFileSuffix, @NonNull int[] supportedSettings) {
        DeviceSpecificSettingsFragment fragment = new DeviceSpecificSettingsFragment();
        fragment.setSettingsFileSuffix(settingsFileSuffix, supportedSettings);

        return fragment;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment;
        if (preference instanceof XTimePreference) {
            dialogFragment = new XTimePreferenceFragment();
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
            dialogFragment.setTargetFragment(this, 0);
            if (getFragmentManager() != null) {
                dialogFragment.show(getFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
            }
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    private void addPreferenceHandlerFor(final String preferenceKey) {
        Preference pref = findPreference(preferenceKey);
        if (pref != null) {
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            GBApplication.deviceService().onSendConfiguration(preferenceKey);
                        }
                    });
                    return true;
                }
            });
        }
    }

    private void setInputTypeFor(final String preferenceKey, final int editTypeFlags) {
        EditTextPreference textPreference = findPreference(preferenceKey);
        if (textPreference != null) {
            textPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(editTypeFlags);
                }
            });
        }
    }

    private void setLocationPreferences(Location location) {
        String latitude = String.format(Locale.US, "%.6g", location.getLatitude());
        String longitude = String.format(Locale.US, "%.6g", location.getLongitude());
        LOG.info("got location. Lat: " + latitude + " Lng: " + longitude);
        GB.toast(getActivity(), getString(R.string.toast_aqurired_networklocation), 2000, 0);
        androidx.preference.EditTextPreference pref_latitude = (androidx.preference.EditTextPreference) findPreference("location_latitude");
        androidx.preference.EditTextPreference pref_longitude = (androidx.preference.EditTextPreference) findPreference("location_longitude");
        pref_latitude.setText(latitude);
        pref_longitude.setText(longitude);
        pref_latitude.setSummary(latitude);
        pref_longitude.setSummary(longitude);
    }
}
