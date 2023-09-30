package nodomain.freeyourgadget.gadgetbridge.devices.femometer;

import androidx.annotation.NonNull;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractTimeSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.FemometerVinca2TemperatureSample;
import nodomain.freeyourgadget.gadgetbridge.entities.FemometerVinca2TemperatureSampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class FemometerVinca2SampleProvider extends AbstractTimeSampleProvider<FemometerVinca2TemperatureSample> {

    public FemometerVinca2SampleProvider(GBDevice device, DaoSession session) {
        super(device, session);
    }

    @Override
    @NonNull
    public AbstractDao<FemometerVinca2TemperatureSample, ?> getSampleDao() {
        return getSession().getFemometerVinca2TemperatureSampleDao();
    }

    @NonNull
    protected Property getTimestampSampleProperty() {
        return FemometerVinca2TemperatureSampleDao.Properties.Timestamp;
    }

    @NonNull
    protected Property getDeviceIdentifierSampleProperty() {
        return FemometerVinca2TemperatureSampleDao.Properties.DeviceId;
    }

    @Override
    public FemometerVinca2TemperatureSample createSample() {
        return new FemometerVinca2TemperatureSample();
    }
}
