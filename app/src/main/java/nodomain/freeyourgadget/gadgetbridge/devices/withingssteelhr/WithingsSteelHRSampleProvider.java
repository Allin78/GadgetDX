package nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class WithingsSteelHRSampleProvider extends AbstractSampleProvider<WithingsSteelHRActivitySample> {


    public WithingsSteelHRSampleProvider(GBDevice device, DaoSession session) {
        super(device, session);
    }

    @Override
    public AbstractDao<WithingsSteelHRActivitySample, ?> getSampleDao() {
        return getSession().getWithingsSteelHRActivitySampleDao();
    }

    @Nullable
    @Override
    protected Property getRawKindSampleProperty() {
        return WithingsSteelHRActivitySampleDao.Properties.RawKind;
    }

    @NonNull
    @Override
    protected Property getTimestampSampleProperty() {
        return WithingsSteelHRActivitySampleDao.Properties.Timestamp;
    }

    @NonNull
    @Override
    protected Property getDeviceIdentifierSampleProperty() {
        return WithingsSteelHRActivitySampleDao.Properties.DeviceId;
    }

    @Override
    public int normalizeType(int rawType) {
        return 0;
    }

    @Override
    public int toRawActivityKind(int activityKind) {
        return 0;
    }

    @Override
    public float normalizeIntensity(int rawIntensity) {
        return 0;
    }

    @Override
    public WithingsSteelHRActivitySample createActivitySample() {
        return new WithingsSteelHRActivitySample();
    }
}
