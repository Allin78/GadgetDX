/*
 *   Copyright (C) 2023 akasaka / Genjitsu Labs
 *
 *     This file is part of Gadgetbridge.
 *
 *     Gadgetbridge is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Gadgetbridge is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3.per_app_notifications;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.AbstractGBActivity;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.entities.Wena3PerAppNotificationSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.LedColor;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.VibrationKind;

public class SonyWena3PerAppNotificationSettingDetailActivity extends AbstractGBActivity {
    private static final Logger LOG = LoggerFactory.getLogger(SonyWena3PerAppNotificationSettingDetailActivity.class);

    private SonyWena3PerAppNotificationSettingsRepository repository = null;
    private String bundleId = null;

    private Spinner mSpinnerLedPattern;
    private Spinner mSpinnerVibrationPattern;
    private Spinner mSpinnerVibrationCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wena3_per_app_setting_detail);
        Button mButtonSave = findViewById(R.id.buttonSaveSettings);
        Button mButtonDelete = findViewById(R.id.buttonDeleteSettings);
        mSpinnerLedPattern = findViewById(R.id.spinnerLedType);
        mSpinnerVibrationPattern = findViewById(R.id.spinnerVibraType);
        mSpinnerVibrationCount = findViewById(R.id.spinnerVibraCount);

        String title = getIntent().getStringExtra(SonyWena3PerAppNotificationSettingsAdapter.STRING_EXTRA_PACKAGE_TITLE);
        setTitle(title);
        bundleId = getIntent().getStringExtra(SonyWena3PerAppNotificationSettingsAdapter.STRING_EXTRA_PACKAGE_NAME);

        try (DBHandler db = GBApplication.acquireDB()) {
            repository = new SonyWena3PerAppNotificationSettingsRepository(db.getDaoSession());
        } catch(Exception e) {
            LOG.error("Failed to acquire DB", e);
        }

        mButtonDelete.setOnClickListener(view -> {
            repository.setSettingsForAppId(bundleId, null);
            finish();
        });

        mButtonSave.setOnClickListener(view -> {
            saveSettings();
            finish();
        });

        Wena3PerAppNotificationSetting setting = repository.getSettingsForAppId(bundleId);
        if(setting != null) {
            if(setting.getLedPatternIdName() != null) {
                LedColor color = LedColor.valueOf(setting.getLedPatternIdName().toUpperCase());
                mSpinnerLedPattern.setSelection(color.ordinal() + 1);
            } else {
                mSpinnerLedPattern.setSelection(0);
            }

            if(setting.getVibrationPatternIdName() != null) {
                VibrationKind kind = VibrationKind.valueOf(setting.getVibrationPatternIdName().toUpperCase());
                mSpinnerVibrationPattern.setSelection(kind.ordinal() + 1);
            } else {
                mSpinnerVibrationPattern.setSelection(0);
            }

            if(setting.getVibrationCount() != null) {
                mSpinnerVibrationCount.setSelection(setting.getVibrationCount() + 1);
            } else {
                mSpinnerVibrationCount.setSelection(0);
            }
        }
    }

    private void saveSettings() {
        LedColor led = null;
        VibrationKind vibra = null;
        Integer vibraTimes = null;

        if(mSpinnerLedPattern.getSelectedItemPosition() > 0) {
            led = LedColor.valueOf(getResources().getStringArray(R.array.prefs_wena3_led_pattern_values)[mSpinnerLedPattern.getSelectedItemPosition() - 1].toUpperCase());
        }

        if(mSpinnerVibrationPattern.getSelectedItemPosition() > 0) {
            vibra = VibrationKind.valueOf(getResources().getStringArray(R.array.prefs_wena3_vibration_pattern_values)[mSpinnerVibrationPattern.getSelectedItemPosition() - 1].toUpperCase());
        }

        if(mSpinnerVibrationCount.getSelectedItemPosition() > 0) {
            vibraTimes = mSpinnerVibrationCount.getSelectedItemPosition() - 1;
        }

        Wena3PerAppNotificationSetting setting = new Wena3PerAppNotificationSetting(
                bundleId,
                led == null ? null : led.name().toLowerCase(),
                vibra == null ? null : vibra.name().toLowerCase(),
                vibraTimes
        );
        repository.setSettingsForAppId(bundleId, setting);
    }
}
