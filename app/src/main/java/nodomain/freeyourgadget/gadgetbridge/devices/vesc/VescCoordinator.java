package nodomain.freeyourgadget.gadgetbridge.devices.vesc;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelUuid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class VescCoordinator extends AbstractDeviceCoordinator {
    public final static String UUID_SERVICE_SERIAL_HM10 = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public final static String UUID_CHARACTERISTIC_SERIAL_TX_HM10 = "0000ffe1-0000-1000-8000-00805f9b34fb";

    public final static String UUID_SERVICE_SERIAL_NRF = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public final static String UUID_CHARACTERISTIC_SERIAL_TX_NRF = "0000ffe0-0000-1000-8000-00805f9b34fb";


    @Override
    protected void deleteDevice(GBDevice gbDevice, Device device, DaoSession session) throws GBException {

    }

    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        ParcelUuid[] uuids = candidate.getServiceUuids();
        Logger logger = LoggerFactory.getLogger(getClass());
        for(ParcelUuid uuid: uuids){
            logger.debug("service: {}", uuid.toString());
        }
        for(ParcelUuid uuid : uuids){
            if(uuid.getUuid().toString().equals(UUID_SERVICE_SERIAL_NRF)){
                return DeviceType.VESC_NRF;
            }else if(uuid.getUuid().toString().equals(UUID_SERVICE_SERIAL_HM10)){
                return DeviceType.VESC_HM10;
            }
        }
        return DeviceType.UNKNOWN;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.VESC_HM10; // TODO: this limits this coordinator to NRF serial service
    }

    @Override
    public Class<? extends Activity> getPairingActivity() {
        return null;
    }

    @Override
    public boolean supportsActivityDataFetching() {
        return false;
    }

    @Override
    public int getBondingStyle() {
        return BONDING_STYLE_NONE;
    }

    @Override
    public boolean supportsActivityTracking() {
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
    public int getAlarmSlotCount() {
        return 0;
    }

    @Override
    public boolean supportsSmartWakeup(GBDevice device) {
        return false;
    }

    @Override
    public boolean supportsHeartRateMeasurement(GBDevice device) {
        return false;
    }

    @Override
    public String getManufacturer() {
        return "Benjamin Vedder";
    }

    @Override
    public boolean supportsAppsManagement() {
        return true;
    }

    @Override
    public Class<? extends Activity> getAppsManagementActivity() {
        return VescControlActivity.class;
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
    public boolean supportsWeather() {
        return false;
    }

    @Override
    public boolean supportsFindDevice() {
        return false;
    }
}
