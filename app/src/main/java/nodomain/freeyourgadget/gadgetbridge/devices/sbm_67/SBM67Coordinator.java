/*  Copyright (C) 2023 Daniele Gobbetti

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
package nodomain.freeyourgadget.gadgetbridge.devices.sbm_67;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractBLEDeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.InstallHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;

public class SBM67Coordinator extends AbstractBLEDeviceCoordinator {
    @Override
    public int getBondingStyle() {
        return BONDING_STYLE_NONE;
    }

    /**
     * Hook for subclasses to perform device-specific deletion logic, e.g. db cleanup.
     *
     * @param gbDevice the GBDevice
     * @param device   the corresponding database Device
     * @param session  the session to use
     * @throws GBException
     */
    @Override
    protected void deleteDevice(@NonNull GBDevice gbDevice, @NonNull Device device, @NonNull DaoSession session) throws GBException {

    }


    /**
     * Checks whether this coordinator handles the given candidate.
     * Returns the supported device type for the given candidate or
     * DeviceType.UNKNOWN
     *
     * @param candidate
     * @return the supported device type for the given candidate.
     */
    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        if (("SBM67".equalsIgnoreCase(candidate.getName())) ||
                ("BPM Smart".equalsIgnoreCase(candidate.getName()))) {
            return DeviceType.SBM_67;
        }
        return DeviceType.UNKNOWN;
    }

    /**
     * Returns the kind of device type this coordinator supports.
     *
     * @return
     */
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SBM_67;
    }

    /**
     * Returns the Activity class to be started in order to perform a pairing of a
     * given device after its discovery.
     *
     * @return the activity class for pairing/initial authentication, or null if none
     */
    @Nullable
    @Override
    public Class<? extends Activity> getPairingActivity() {
        return null;
    }

    /**
     * Returns true if activity data fetching is supported by the device
     * (with this coordinator).
     * This enables the sync button in control center and the device can thus be asked to send the data
     * (as opposed the device pushing the data to us by itself)
     *
     * @return
     */
    @Override
    public boolean supportsActivityDataFetching() {
        return false;
    }

    /**
     * Returns true if activity tracking is supported by the device
     * (with this coordinator).
     * This enables the ChartsActivity.
     *
     * @return
     */
    @Override
    public boolean supportsActivityTracking() {
        return false;
    }

    /**
     * Returns the sample provider for the device being supported.
     *
     * @param device
     * @param session
     * @return
     */
    @Override
    public SampleProvider<? extends ActivitySample> getSampleProvider(GBDevice device, DaoSession session) {
        return null;
    }

    /**
     * Finds an install handler for the given uri that can install the given
     * uri on the device being managed.
     *
     * @param uri
     * @param context
     * @return the install handler or null if that uri cannot be installed on the device
     */
    @Override
    public InstallHandler findInstallHandler(Uri uri, Context context) {
        return null;
    }

    @Override
    public boolean supportsScreenshots() {
        return false;
    }

    /**
     * Returns the number of alarms this device/coordinator supports
     * Shall return 0 also if it is not possible to set alarms via
     * protocol, but only on the smart device itself.
     *
     * @return
     */
    @Override
    public int getAlarmSlotCount(GBDevice device) {
        return 0;
    }

    /**
     * Returns true if this device/coordinator supports alarms with smart wakeup
     *
     * @param device
     * @return
     */
    @Override
    public boolean supportsSmartWakeup(GBDevice device) {
        return false;
    }

    /**
     * Returns the readable name of the manufacturer.
     */
    @Override
    public String getManufacturer() {
        return "Sanitas";
    }

    @Override
    public boolean supportsAppsManagement(GBDevice device) {
        return false;
    }

    @Override
    public Class<? extends Activity> getAppsManagementActivity() {
        return null;
    }

    /**
     * Indicates whether the device has some kind of calender we can sync to.
     * Also used for generated sunrise/sunset events
     */
    @Override
    public boolean supportsCalendarEvents() {
        return false;
    }

    /**
     * Indicates whether the device supports getting a stream of live data.
     * This can be live HR, steps etc.
     */
    @Override
    public boolean supportsRealtimeData() {
        return false;
    }

    /**
     * Indicates whether the device supports being found by vibrating,
     * making some sound or lighting up
     */
    @Override
    public boolean supportsFindDevice() {
        return false;
    }

    @Override
    public SBM67BloodPressureSampleProvider getBloodPressureSampleProvider(GBDevice device, DaoSession session) {
        return new SBM67BloodPressureSampleProvider(device, session);
    }

    @Override
    public boolean supportsBloodPressureMeasurement() {
        return true;
    }
}
