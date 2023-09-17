package nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3;

import android.app.Activity;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

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
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.SonyWena3DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.unknown.UnknownDeviceSupport;

public class SonyWena3Coordinator extends AbstractBLEDeviceCoordinator {
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SONY_WENA_3;
    }

    @Nullable
    @Override
    public Class<? extends Activity> getPairingActivity() {
        return null;
    }

    @Override
    public String getManufacturer() {
        return "Sony";
    }

    @Override
    public boolean supportsAppsManagement(GBDevice device) {
        return false;
    }

    @Override
    public Class<? extends Activity> getAppsManagementActivity() {
        return null;
    }

    @NonNull
    @Override
    public Class<? extends DeviceSupport> getDeviceSupportClass() {
        return SonyWena3DeviceSupport.class;
    }

    @NonNull
    @Override
    public Collection<? extends ScanFilter> createBLEScanFilters() {
        ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setDeviceName(SonyWena3Constants.BT_DEVICE_NAME);
        ArrayList<ScanFilter> result = new ArrayList<>();
        result.add(builder.build());
        return result;
    }

    @Override
    protected void deleteDevice(@NonNull GBDevice gbDevice, @NonNull Device device, @NonNull DaoSession session) throws GBException {

    }

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        if(SonyWena3Constants.BT_DEVICE_NAME.equals(candidate.getName())) {
            return DeviceType.SONY_WENA_3;
        }
        return DeviceType.UNKNOWN;
    }

    @Override
    public int getBondingStyle() {
        return BONDING_STYLE_BOND;
    }

    //////////////////
    // Capability Definitions
    /////////////////

    @Override
    public boolean supportsCalendarEvents() {
        return false;
    }

    @Override
    public boolean supportsRealtimeData() {
        return false;
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
    public boolean supportsAppReordering() {
        return false;
    }

    @Override
    public boolean supportsStressMeasurement() {
        return false;
    }

    @Override
    public boolean supportsSpo2() {
        return false;
    }

    @Override
    public boolean supportsHeartRateStats() {
        return false;
    }

    @Override
    public SampleProvider<? extends ActivitySample> getSampleProvider(GBDevice device, DaoSession session) {
        return null;
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
    public int getAlarmSlotCount(GBDevice device) {
        return SonyWena3Constants.ALARM_SLOTS;
    }

    @Override
    public boolean supportsSmartWakeup(GBDevice device) {
        return true;
    }

    @Override
    public boolean supportsMusicInfo() {
        return true;
    }

    @Override
    public boolean supportsFindDevice() {
        return false;
    }

    @Override
    public boolean supportsWeather() {
        return true;
    }

    @Override
    public boolean isExperimental() {
        return true;
    }

    @Override
    public String[] getSupportedLanguageSettings(GBDevice device) {
        return new String[]{
                "auto",
                "en_US",
                "ja_JP"
        };
    }

    @Override
    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return new int[]{
                R.xml.devicesettings_wena3
        };
    }
}
