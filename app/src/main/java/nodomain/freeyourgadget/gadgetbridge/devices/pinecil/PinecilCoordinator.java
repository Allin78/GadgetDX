/*  Copyright (C) 2023 Marc Nause

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
package nodomain.freeyourgadget.gadgetbridge.devices.pinecil;

import android.app.Activity;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractBLEDeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.InstallHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.pinecil.PinecilDeviceSupport;

public class PinecilCoordinator extends AbstractBLEDeviceCoordinator {

    @Override
    public String getManufacturer() {
        return "Pine64";
    }

    @NonNull
    @Override
    public Collection<? extends ScanFilter> createBLEScanFilters() {
        // Is this method ever called?
        ParcelUuid bulkDataService = new ParcelUuid(PinecilConstants.UUID_SERVICE_BULK_DATA);
        return Collections.singletonList(new ScanFilter.Builder().setServiceUuid(bulkDataService).build());
    }

//    @NonNull
//    @Override
//    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
//        /* Device advertises service, but Gadgetbridge does not seem to recognize this. */
//        if (candidate.supportsService(PinecilConstants.UUID_SERVICE_BULK_DATA)
//                || (candidate.getName() != null && candidate.getName().startsWith("Pinecil-"))) {
//            return DeviceType.PINECIL;
//        }
//
//        return DeviceType.UNKNOWN;
//    }


    @Override
    public boolean supports(GBDeviceCandidate candidate) {
        return candidate.supportsService(PinecilConstants.UUID_SERVICE_BULK_DATA);
    }

    @Override
    public int getBondingStyle() {
        return BONDING_STYLE_LAZY;
    }

    @Nullable
    @Override
    public Class<? extends Activity> getPairingActivity() {
        return null;
    }

    @Override
    public SampleProvider<? extends ActivitySample> getSampleProvider(GBDevice device, DaoSession session) {
        return null;
    }

    @Override
    public boolean supportsActivityDataFetching() {
        return false;
    }

    @Override
    public boolean supportsActivityTracking() {
        return false;
    }

    @Override
    public InstallHandler findInstallHandler(Uri uri, Context context) {
        return null;
    }

    @Override
    public boolean supportsScreenshots() {
        return false;
    }

    @Override
    public boolean supportsSmartWakeup(GBDevice device) {
        return false;
    }


    @Override
    public boolean supportsAppsManagement(GBDevice device) {
        return false;
    }

    @Override
    public Class<? extends Activity> getAppsManagementActivity() {
        return null;
    }

    @Override
    protected void deleteDevice(@NonNull GBDevice gbDevice, @NonNull Device device, @NonNull DaoSession session) throws GBException {

    }

    @Override
    public boolean supportsCalendarEvents() {
        return false;
    }

    @Override
    public boolean supportsRealtimeData() {
        return false;
    }

    @Override
    public boolean supportsFindDevice() {
        return false;
    }

    @Override
    public int getBatteryCount() {
        return 0;
    }

    @NonNull
    @Override
    public Class<? extends DeviceSupport> getDeviceSupportClass() {
        return PinecilDeviceSupport.class;
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_pinecil;
    }

    @Override
    public int getDefaultIconResource() {
        return R.drawable.ic_device_pinecilv2;
    }

    @Override
    public int getDisabledIconResource() {
        return R.drawable.ic_device_pinecilv2_disabled;
    }
}
