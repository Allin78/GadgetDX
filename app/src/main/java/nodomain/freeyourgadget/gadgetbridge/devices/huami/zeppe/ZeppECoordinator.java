/*  Copyright (C) 2017-2021 Andreas Shimokawa, Daniele Gobbetti, João
    Paulo Barraca, José Rebelo, pangwalla, tiparega, randnv20

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
package nodomain.freeyourgadget.gadgetbridge.devices.huami.zeppe;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.InstallHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiCoordinator;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.zeppe.ZeppESupport;

public class ZeppECoordinator extends HuamiCoordinator {
    private static final Logger LOG = LoggerFactory.getLogger(ZeppECoordinator.class);

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("Zepp E", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public InstallHandler findInstallHandler(Uri uri, Context context) {
        ZeppEFWInstallHandler handler = new ZeppEFWInstallHandler(uri, context);
        return handler.isValid() ? handler : null;
    }

    @Override
    public int getBondingStyle() {
        return BONDING_STYLE_REQUIRE_KEY;
    }

    @Override
    public boolean supportsHeartRateMeasurement(GBDevice device) {
        return true;
    }

    @Override
    public boolean supportsActivityTracks() {
        return true;
    }

    @Override
    public boolean supportsWeather() {
        return true;
    }

    @Override
    public boolean supportsMusicInfo() {
        return true;
    }
    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return new int[]{
                R.xml.devicesettings_amazfitgtsgtr,
                R.xml.devicesettings_vibrationpatterns,
                R.xml.devicesettings_wearlocation,
                R.xml.devicesettings_heartrate_sleep,
                R.xml.devicesettings_goal_notification,
                R.xml.devicesettings_timeformat,
                R.xml.devicesettings_liftwrist_display,
                R.xml.devicesettings_inactivity_dnd,
                R.xml.devicesettings_disconnectnotification,
                R.xml.devicesettings_sync_calendar,
                R.xml.devicesettings_expose_hr_thirdparty,
                R.xml.devicesettings_bt_connected_advertisement,
                R.xml.devicesettings_device_actions,
                R.xml.devicesettings_high_mtu,
                R.xml.devicesettings_overwrite_settings_on_connection,
                R.xml.devicesettings_huami2021_fetch_operation_time_unit,
                R.xml.devicesettings_transliteration
        };
    }

    @NonNull
    @Override
    public Class<? extends DeviceSupport> getDeviceSupportClass() {
        return ZeppESupport.class;
    }


    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_zepp_e;
    }
}
