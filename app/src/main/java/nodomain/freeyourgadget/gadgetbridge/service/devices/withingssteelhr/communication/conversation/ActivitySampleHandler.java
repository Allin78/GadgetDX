package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr.WithingsSteelHRSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.activity.ActivityEntry;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivityCategory;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleCalories;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleCalories2;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleDuration;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleMovement;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleSleep;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleTime;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.SleepActivitySample;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.VasistasHeartrate;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.VasistasWalk;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructureType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class ActivitySampleHandler extends AbstractResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ActivitySampleHandler.class);
    private ActivityEntry activityEntry;

    public ActivitySampleHandler(WithingsSteelHRDeviceSupport support) {
        super(support);
    }

    @Override
    public void handleResponse(Message response) {
        List<WithingsStructure> data = response.getDataStructures();
        if (data !=  null) {
            handleActivityData(data);
        }
    }

    private void handleActivityData(List<WithingsStructure> dataList) {
        for (WithingsStructure data : dataList) {
            switch (data.getType()) {
                case WithingsStructureType.ACTIVITY_SAMPLE_TIME:
                    handleTimestamp(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_DURATION:
                    handleDuration(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_MOVEMENT:
                    handleMovement(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_CALORIES:
                    handleMetCal(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_CALORIES_2:
                    handleMetCalEarned(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_SLEEP:
                    handleVasistasSleep(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_WALK:
                    handleWalk(data);
                    break;
                case WithingsStructureType.VASISTAS_HR:
                    handleHeartrate(data);
                    break;
                case WithingsStructureType.SLEEP_ACTIVITY_SAMPLE:
                    handleSleep(data);
                    break;
                case WithingsStructureType.ACTIVITY_CATEGORY:
                    handleCategory(data);
                    break;
                default:
                    logger.info("Received yet unhandled activity data of type '" + data.getType() + "' with data '" + data.getRawData() + "'.");
            }
        }

        if (activityEntry != null) {
            saveData(activityEntry);
        }

    }

    private void handleTimestamp(WithingsStructure data) {
        if (activityEntry != null) {
            saveData(activityEntry);
        }

        activityEntry = new ActivityEntry();
        activityEntry.setTimestamp((int)(((ActivitySampleTime)data).getDate().getTime()/1000));
        activityEntry.setRawKind(ActivityKind.TYPE_UNKNOWN);
    }

    private void handleCategory(WithingsStructure data) {
        activityEntry.setRawKind(((ActivityCategory)data).getCategory());
    }

    private void handleSleep(WithingsStructure data) {
        SleepActivitySample sleepSample = (SleepActivitySample) data;
        logger.debug("Received sleep data " + StringUtils.bytesToHex(data.getRawData()));
//        if (activityEntry != null) {
//            saveData(activityEntry);
//        }
//
//        activityEntry = new ActivityEntry();
//        activityEntry.setTimestamp(sleepSample.getStartdate());
    }

    private void handleDuration(WithingsStructure data) {
        activityEntry.setDuration(((ActivitySampleDuration)data).getDuration());
    }

    private void handleHeartrate(WithingsStructure data) {
        activityEntry.setHeartrate(((VasistasHeartrate)data).getHeartrate());
    }

    private void handleMovement(WithingsStructure data) {
        activityEntry.setSteps(((ActivitySampleMovement)data).getSteps());
        activityEntry.setDistance(((ActivitySampleMovement)data).getDistance());
    }

    private void handleWalk(WithingsStructure data) {
        activityEntry.setRawIntensity(((VasistasWalk)data).getLevel());
    }

    private void handleVasistasSleep(WithingsStructure data) {
        activityEntry.setRawKind(((ActivitySampleSleep)data).getSleepType());
    }

    private void handleMetCal(WithingsStructure data) {
        activityEntry.setCalories(((ActivitySampleCalories)data).getCalories());
    }

    private void handleMetCalEarned(WithingsStructure data) {
        activityEntry.setCalories(((ActivitySampleCalories2)data).getCalories());

    }

    private void handleActiRecovV1V2(WithingsStructure data) {

    }

    private void saveData(ActivityEntry activityEntry) {
        WithingsSteelHRActivitySample sample = new WithingsSteelHRActivitySample();
        sample.setTimestamp(activityEntry.getTimestamp());
        sample.setHeartRate(activityEntry.getHeartrate());
        sample.setSteps(activityEntry.getSteps());
        sample.setRawKind(activityEntry.getRawKind());
        sample.setCalories(activityEntry.getCalories());
        sample.setDistance(activityEntry.getDistance());
        sample.setRawIntensity(activityEntry.getRawIntensity());
        try (DBHandler dbHandler = GBApplication.acquireDB()) {
            Long userId = DBHelper.getUser(dbHandler.getDaoSession()).getId();
            Long deviceId = DBHelper.getDevice(device, dbHandler.getDaoSession()).getId();
            WithingsSteelHRSampleProvider provider = new WithingsSteelHRSampleProvider(device, dbHandler.getDaoSession());
            sample.setDeviceId(deviceId);
            sample.setUserId(userId);
            provider.addGBActivitySample(sample);
        } catch (Exception ex) {
            logger.warn("Error saving current activity data: " + ex.getLocalizedMessage());
        }
    }
}
