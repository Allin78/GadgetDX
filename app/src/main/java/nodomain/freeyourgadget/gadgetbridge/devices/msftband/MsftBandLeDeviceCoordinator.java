package nodomain.freeyourgadget.gadgetbridge.devices.msftband;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractDeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.InstallHandler;
import nodomain.freeyourgadget.gadgetbridge.devices.SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;

/**
 * Coordinator for MSFT Band using Bluetooth LowEnergy
 */
public class MsftBandLeDeviceCoordinator extends AbstractDeviceCoordinator {
    /**
     * Returns the kind of device type this coordinator supports.
     *
     * @return
     */
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.MSFT_BAND_LE;
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

    /**
     * Returns true if this device/coordinator supports taking screenshots.
     *
     * @return
     */
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
    public int getAlarmSlotCount() {
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
     * Returns true if the given device supports heart rate measurements.
     *
     * @param device
     * @return
     */
    @Override
    public boolean supportsHeartRateMeasurement(GBDevice device) {
        return false;
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
        String name = candidate.getDevice().getName();
        if (!TextUtils.isEmpty(name) && name.contains("MSFT Band") && name.endsWith(" LE")) {
            return DeviceType.MSFT_BAND_LE;
        }
        return DeviceType.UNKNOWN;
    }

    /**
     * Returns the readable name of the manufacturer.
     */
    @Override
    public String getManufacturer() {
        return "Microsoft";
    }

    /**
     * Returns true if this device/coordinator supports managing device apps.
     *
     * @return
     */
    @Override
    public boolean supportsAppsManagement() {
        return false;
    }

    /**
     * Returns the Activity class that will be used to manage device apps.
     *
     * @return
     */
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
     * Indicates whether the device supports current weather and/or weather
     * forecast display.
     */
    @Override
    public boolean supportsWeather() {
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
}
