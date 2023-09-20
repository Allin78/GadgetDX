package nodomain.freeyourgadget.gadgetbridge.devices.infinitime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.InfiniTimeActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.InfiniTimeActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
import nodomain.freeyourgadget.gadgetbridge.util.Optional;

import java.util.List;

public class InfiniTimeActivitySampleProvider extends AbstractSampleProvider<InfiniTimeActivitySample> {
    private GBDevice mDevice;
    private DaoSession mSession;

    public InfiniTimeActivitySampleProvider(GBDevice device, DaoSession session) {
        super(device, session);

        mSession = session;
        mDevice = device;
    }

    @Override
    public AbstractDao<InfiniTimeActivitySample, ?> getSampleDao() {
        return getSession().getInfiniTimeActivitySampleDao();
    }

    @Nullable
    @Override
    protected Property getRawKindSampleProperty() {
        return InfiniTimeActivitySampleDao.Properties.RawKind;
    }

    @NonNull
    @Override
    protected Property getTimestampSampleProperty() {
        return InfiniTimeActivitySampleDao.Properties.Timestamp;
    }

    @NonNull
    @Override
    protected Property getDeviceIdentifierSampleProperty() {
        return InfiniTimeActivitySampleDao.Properties.DeviceId;
    }

    @Override
    public int normalizeType(int rawType) {
        return rawType;
    }

    @Override
    public int toRawActivityKind(int activityKind) {
        return activityKind;
    }

    @Override
    public float normalizeIntensity(int rawIntensity) {
        return rawIntensity;
    }

    /**
     * Factory method to creates an empty sample of the correct type for this sample provider
     *
     * @return the newly created "empty" sample
     */
    @Override
    public InfiniTimeActivitySample createActivitySample() {
        return new InfiniTimeActivitySample();
    }

    public Optional<InfiniTimeActivitySample> getSampleForTimestamp(int timestamp) {
        List<InfiniTimeActivitySample> foundSamples = this.getGBActivitySamples(timestamp, timestamp, ActivityKind.TYPE_ALL);
        if (foundSamples.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(foundSamples.get(0));
    }
}
