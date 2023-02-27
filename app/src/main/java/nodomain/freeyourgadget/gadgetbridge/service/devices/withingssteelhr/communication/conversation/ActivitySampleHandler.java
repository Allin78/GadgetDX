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
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.activity.SleepActivitySampleHelper;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.activity.WithingsActivityType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WorkoutType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleCalories;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleCalories2;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleDuration;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleMovement;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleSleep;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivitySampleTime;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivityHeartrate;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructureType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

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
                    handleCalories1(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_CALORIES_2:
                    handleCalories2(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_SLEEP:
                    handleSleep(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_WALK:
                    handleWalk(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_RUN:
                    handleWalk(data);
                    break;
                case WithingsStructureType.ACTIVITY_SAMPLE_SWIM:
                    handleSwim(data);
                    break;
                case WithingsStructureType.ACTIVITY_HR:
                    handleHeartrate(data);
                    break;
                case WithingsStructureType.WORKOUT_TYPE:
                    handleWorkoutType(data);
                    break;
                default:
                    logger.info("Received yet unhandled activity data of type '" + data.getType() + "' with data '" + GB.hexdump(data.getRawData()) + "'.");
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
    }

    private void handleWorkoutType(WithingsStructure data) {
        WithingsActivityType activityType = WithingsActivityType.fromCode(((WorkoutType)data).getActivityType());
        activityEntry.setRawKind(activityType.toActivityKind());
    }

    private void handleDuration(WithingsStructure data) {
        activityEntry.setDuration(((ActivitySampleDuration)data).getDuration());
    }

    private void handleHeartrate(WithingsStructure data) {
        activityEntry.setHeartrate(((ActivityHeartrate)data).getHeartrate());
    }

    private void handleMovement(WithingsStructure data) {
        activityEntry.setSteps(((ActivitySampleMovement)data).getSteps());
        activityEntry.setDistance(((ActivitySampleMovement)data).getDistance());
    }

    private void handleWalk(WithingsStructure data) {
        activityEntry.setRawKind(ActivityKind.TYPE_WALKING);
    }

    private void handleRun(WithingsStructure data) {
        activityEntry.setRawKind(ActivityKind.TYPE_RUNNING);
    }

    private void handleSwim(WithingsStructure data) {
        activityEntry.setRawKind(ActivityKind.TYPE_SWIMMING);
    }

    private void handleSleep(WithingsStructure data) {
        int sleepType;
        switch (((ActivitySampleSleep)data).getSleepType()) {
            case 0:
                sleepType = ActivityKind.TYPE_LIGHT_SLEEP;
                activityEntry.setRawIntensity(0);
                break;
            case 2:
                sleepType = ActivityKind.TYPE_DEEP_SLEEP;
                activityEntry.setRawIntensity(70);
                break;
            case 3:
                sleepType = ActivityKind.TYPE_REM_SLEEP;
                activityEntry.setRawIntensity(80);
                break;
            default:
                sleepType = ActivityKind.TYPE_LIGHT_SLEEP;
                activityEntry.setRawIntensity(50);
        }

            activityEntry.setRawKind(sleepType);
    }

    private void handleCalories1(WithingsStructure data) {
        activityEntry.setRawIntensity(((ActivitySampleCalories)data).getMet());
        activityEntry.setCalories(((ActivitySampleCalories)data).getCalories());
    }

    private void handleCalories2(WithingsStructure data) {
        activityEntry.setRawIntensity(((ActivitySampleCalories2)data).getMet());
        activityEntry.setCalories(((ActivitySampleCalories2)data).getCalories());

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
            sample = SleepActivitySampleHelper.mergeIfNecessary(provider, sample);
            provider.addGBActivitySample(sample);
        } catch (Exception ex) {
            logger.warn("Error saving current activity data: " + ex.getLocalizedMessage());
        }
    }
}
