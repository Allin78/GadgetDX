package nodomain.freeyourgadget.gadgetbridge.devices.qhybrid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.HybridHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.HybridHRActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class HybridHRActivitySampleProvider extends AbstractSampleProvider<HybridHRActivitySample> {
    public HybridHRActivitySampleProvider(GBDevice device, DaoSession session) {
        super(device, session);
    }

    @Override
    public AbstractDao<HybridHRActivitySample, ?> getSampleDao() {
        return getSession().getHybridHRActivitySampleDao();
    }

    @Nullable
    @Override
    protected Property getRawKindSampleProperty() {
        return null;
    }

    @NonNull
    @Override
    protected Property getTimestampSampleProperty() {
        return HybridHRActivitySampleDao.Properties.Timestamp;
    }

    @NonNull
    @Override
    protected Property getDeviceIdentifierSampleProperty() {
        return HybridHRActivitySampleDao.Properties.DeviceId;
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
    public HybridHRActivitySample createActivitySample() {
        return new HybridHRActivitySample();
    }

    @Override
    public List<HybridHRActivitySample> getActivitySamples(int timestamp_from, int timestamp_to) {
        return super.getActivitySamples(timestamp_from, timestamp_to);
    }

    @Override
    public List<HybridHRActivitySample> getAllActivitySamples(int timestamp_from, int timestamp_to) {
        return super.getAllActivitySamples(timestamp_from, timestamp_to);
    }
}