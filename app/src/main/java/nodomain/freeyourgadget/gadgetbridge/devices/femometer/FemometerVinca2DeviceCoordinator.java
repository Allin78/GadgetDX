package nodomain.freeyourgadget.gadgetbridge.devices.femometer;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.GBException;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractDeviceCoordinator;
import nodomain.freeyourgadget.gadgetbridge.devices.TimeSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.FemometerVinca2TemperatureSample;
import nodomain.freeyourgadget.gadgetbridge.entities.FemometerVinca2TemperatureSampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDeviceCandidate;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceType;
import nodomain.freeyourgadget.gadgetbridge.service.DeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.femometer.FemometerVinca2DeviceSupport;

public class FemometerVinca2DeviceCoordinator extends AbstractDeviceCoordinator {
    @Override
    public String getManufacturer() {
        return "Joytech Healthcare";
    }

    @Override
    public String getAppCacheSortFilename() {
        return "null";
    }

    @Override
    public String getAppFileExtension() {
        return "null";
    }

    @NonNull
    @Override
    public Class<? extends DeviceSupport> getDeviceSupportClass() {
        return FemometerVinca2DeviceSupport.class;
    }

    @Override
    public TimeSampleProvider<FemometerVinca2TemperatureSample> getTemperatureSampleProvider(GBDevice device, DaoSession session) {
        return new FemometerVinca2SampleProvider(device, session);
    }

    @NonNull
    @Override
    public DeviceType getSupportedType(GBDeviceCandidate candidate) {
        String name = candidate.getName();
        if (name != null && name.equals("BM-Vinca2")) {
            return DeviceType.FEMOMETER_VINCA2;
        }
        return DeviceType.UNKNOWN;
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_femometer_vinca2;
    }

    @Override
    public int getDefaultIconResource() {
        return R.drawable.ic_device_default;
    }

    @Override
    public int getDisabledIconResource() {
        return R.drawable.ic_device_default_disabled;
    }


    @Override
    protected void deleteDevice(@NonNull GBDevice gbDevice, @NonNull Device device, @NonNull DaoSession session) throws GBException {
        Long deviceId = device.getId();
        QueryBuilder<?> qb = session.getFemometerVinca2TemperatureSampleDao().queryBuilder();
        qb.where(FemometerVinca2TemperatureSampleDao.Properties.DeviceId.eq(deviceId)).buildDelete().executeDeleteWithoutDetachingEntities();
    }


    @Override
    public int getBondingStyle(){
        return BONDING_STYLE_NONE;
    }

    @Nullable
    @Override
    public Class<? extends Activity> getPairingActivity() {
        return null;
    }

    @Override
    public boolean supportsActivityDataFetching() {
        return true;
    }

    @Override
    public int getAlarmSlotCount(final GBDevice device) {
        return 1;
    }

    @Override
    public int[] getSupportedDeviceSpecificSettings(GBDevice device) {
        return new int[]{
                R.xml.devicesettings_volume,
                R.xml.devicesettings_femometer,
                R.xml.devicesettings_temperature_scale_cf,
        };
    }

}
