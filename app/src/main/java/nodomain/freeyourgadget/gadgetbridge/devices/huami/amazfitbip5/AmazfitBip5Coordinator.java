/*  Copyright (C) 2023 José Rebelo

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
package nodomain.freeyourgadget.gadgetbridge.devices.huami.amazfitbip5;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.Huami2021Coordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.AbstractHuami2021FWInstallHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.amazfitbip5.AmazfitBip5Support;

public class AmazfitBip5Coordinator extends Huami2021Coordinator {
    private static final Logger LOG = LoggerFactory.getLogger(AmazfitBip5Coordinator.class);

    @Override
    public boolean isExperimental() {
        return true;
    }

    @NonNull
    @Override
    public Class<? extends DeviceSupport> getDeviceSupportClass() {
        return AmazfitBip5Support.class;
    }

    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile(HuamiConst.AMAZFIT_BIP5_NAME + ".*");
    }

    @Override
    public AbstractHuami2021FWInstallHandler createFwInstallHandler(final Uri uri, final Context context) {
        return new AmazfitBip5FWInstallHandler(uri, context);
    }

    @Override
    public boolean supportsContinuousFindDevice() {
        return true;
    }

    @Override
    public boolean supportsGpxUploads() {
        return true;
    }

    @Override
    public boolean supportsControlCenter() {
        return true;
    }

    @Override
    public boolean supportsToDoList() {
        return true;
    }

    @Override
    public boolean supportsWifiHotspot(final GBDevice device) {
        return false;
    }

    @Override
    public boolean supportsFtpServer(final GBDevice device) {
        return false;
    }

    @Override
    public boolean supportsBluetoothPhoneCalls(final GBDevice device) {
        return true;
    }


    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_amazfit_bip5;
    }


    @Override
    public int getDefaultIconResource() {
        return R.drawable.ic_device_amazfit_bip;
    }

    @Override
    public int getDisabledIconResource() {
        return R.drawable.ic_device_amazfit_bip_disabled;
    }
}
