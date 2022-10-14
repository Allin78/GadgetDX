package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.LiveWorkoutEnd;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.LiveWorkoutPauseState;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.LiveWorkoutStart;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructureType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WorkoutType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class LiveWorkoutDataHandler {
    private static final Logger logger = LoggerFactory.getLogger(LiveWorkoutDataHandler.class);

    public void handleMessage(Message message) {
        List<WithingsStructure> data = message.getDataStructures();
        if (data !=  null) {
            handleLiveData(data);
        }
    }

    private void handleLiveData(List<WithingsStructure> dataList) {
        for (WithingsStructure data : dataList) {
            switch (data.getType()) {
                case WithingsStructureType.LIVE_WORKOUT_START:
                    handleStart((LiveWorkoutStart) data);
                    break;
                case WithingsStructureType.LIVE_WORKOUT_END:
                    handleEnd((LiveWorkoutEnd) data);
                    break;
                case WithingsStructureType.LIVE_WORKOUT_PAUSE_STATE:
                    handlePause((LiveWorkoutPauseState) data);
                    break;
                case WithingsStructureType.WORKOUT_TYPE:
                    handleType((WorkoutType) data);
                    break;
                default:
                    logger.info("Received yet unhandled live workout data of type '" + data.getType() + "' with data '" + GB.hexdump(data.getRawData()) + "'.");
            }
        }
    }

    private void handleStart(LiveWorkoutStart workoutStart) {
        logger.info("Got workout start: " + workoutStart.getStarttime());
    }

    private void handlePause(LiveWorkoutPauseState workoutPause) {
        if (workoutPause.getStarttime() == null) {
            if (workoutPause.getLengthInSeconds() > 0) {
                logger.info("Got workout pause end with duration: " + workoutPause.getLengthInSeconds());
            } else {
                logger.info("Currently no pause happened");
            }
        } else {
            logger.info("Got workout pause started at: " + workoutPause.getStarttime());
        }

    }

    private void handleEnd(LiveWorkoutEnd workoutEnd) {
        logger.info("Got workout end: " + workoutEnd.getEndtime());

    }

    private void handleType(WorkoutType workoutType) {
        WithingsActivityType withingsWorkoutType = WithingsActivityType.fromCode(workoutType.getActivityType());
        logger.info("Got workout type: " + withingsWorkoutType);
    }
}
