package nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.QueryBuilder;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.AbstractSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySampleDao;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.ActivitySampleHandler;

public class WithingsSteelHRSampleProvider extends AbstractSampleProvider<WithingsSteelHRActivitySample> {
    private static final Logger logger = LoggerFactory.getLogger(WithingsSteelHRSampleProvider.class);

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
        switch (rawType) {
            case 0:
                return ActivityKind.TYPE_UNKNOWN;
            case 1:
                return ActivityKind.TYPE_LIGHT_SLEEP;
            case 2:
            case 3:
                return ActivityKind.TYPE_DEEP_SLEEP;
            case 4:
                return ActivityKind.TYPE_UNKNOWN;
            default:
                return ActivityKind.TYPE_ACTIVITY;
        }
    }

    @Override
    public int toRawActivityKind(int activityKind) {
        switch (activityKind) {
            case ActivityKind.TYPE_UNKNOWN:
                return 0;
            case ActivityKind.TYPE_LIGHT_SLEEP:
                return 1;
            case ActivityKind.TYPE_DEEP_SLEEP:
                return 2;
            default:
                return activityKind;
        }
    }

    @Override
    public float normalizeIntensity(int rawIntensity) {
        logger.debug("########### normalizeIntensity " + rawIntensity);
        return 0;
    }

    @Override
    public WithingsSteelHRActivitySample createActivitySample() {
        return new WithingsSteelHRActivitySample();
    }
}
