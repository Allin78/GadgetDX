package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.activity;

import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.GregorianCalendar;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr.WithingsSteelHRSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.BaseActivitySummary;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.User;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.externalevents.opentracks.OpenTracksController;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.LiveHeartRate;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.LiveWorkoutEnd;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.LiveWorkoutPauseState;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.LiveWorkoutStart;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructureType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WorkoutGpsState;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WorkoutType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessage;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageType;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

public class LiveWorkoutDataHandler {
    private static final Logger logger = LoggerFactory.getLogger(LiveWorkoutDataHandler.class);
    private final WithingsSteelHRDeviceSupport support;
    private BaseActivitySummary baseActivitySummary;

    public LiveWorkoutDataHandler(WithingsSteelHRDeviceSupport support) {
        this.support = support;
    }


    public void handleMessage(Message message) {
        List<WithingsStructure> data = message.getDataStructures();
        if (data != null) {
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
                case WithingsStructureType.LIVE_HR:
                    handleHeartrate((LiveHeartRate) data);
                    break;
                default:
                    logger.info("Received yet unhandled live workout data of type '" + data.getType() + "' with data '" + GB.hexdump(data.getRawData()) + "'.");
            }
        }
    }

    private void handleStart(LiveWorkoutStart workoutStart) {
        sendGpsState();
        if (baseActivitySummary == null) {
            baseActivitySummary = new BaseActivitySummary();
        }

        baseActivitySummary.setStartTime(workoutStart.getStarttime());
    }

    private void handlePause(LiveWorkoutPauseState workoutPause) {
        // Not sure what to do with these events at the moment so we just log them.
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
        OpenTracksController.stopRecording(support.getContext());
        baseActivitySummary.setEndTime(workoutEnd.getEndtime());
        saveBaseActivitySummary();
        baseActivitySummary = null;
    }

    private void handleType(WorkoutType workoutType) {
        WithingsActivityType withingsWorkoutType = WithingsActivityType.fromCode(workoutType.getActivityType());
        OpenTracksController.startRecording(support.getContext(), withingsWorkoutType.toActivityKind());
        if (baseActivitySummary == null) {
            baseActivitySummary = new BaseActivitySummary();
        }

        baseActivitySummary.setActivityKind(withingsWorkoutType.toActivityKind());
    }

    private void handleHeartrate(WithingsStructure structure) {
        int heartRate = 0;
        if (structure instanceof LiveHeartRate) {
            heartRate = ((LiveHeartRate)structure).getHeartrate();
        }

        if (heartRate > 0) {
            saveHeartRateData(heartRate);
        }
    }

    private void sendGpsState() {
        Message message = new WithingsMessage((short)(WithingsMessageType.START_LIVE_WORKOUT | 16384), new WorkoutGpsState(isGpsEnabled()));
        support.sendToDevice(message);
    }

    private boolean isGpsEnabled() {
        final LocationManager manager = (LocationManager) support.getContext().getSystemService(support.getContext().LOCATION_SERVICE );
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER );
    }

    private void saveHeartRateData(int heartRate) {
        WithingsSteelHRActivitySample sample = new WithingsSteelHRActivitySample();
        sample.setTimestamp((int) (GregorianCalendar.getInstance().getTimeInMillis() / 1000L));
        sample.setHeartRate(heartRate);
        sample.setRawIntensity(ActivitySample.NOT_MEASURED);
        sample.setRawKind(MiBandSampleProvider.TYPE_ACTIVITY);
        try (DBHandler dbHandler = GBApplication.acquireDB()) {
            Long userId = DBHelper.getUser(dbHandler.getDaoSession()).getId();
            Long deviceId = DBHelper.getDevice(support.getDevice(), dbHandler.getDaoSession()).getId();
            WithingsSteelHRSampleProvider provider = new WithingsSteelHRSampleProvider(support.getDevice(), dbHandler.getDaoSession());
            sample.setDeviceId(deviceId);
            sample.setUserId(userId);
            provider.addGBActivitySample(sample);
        } catch (Exception ex) {
            logger.warn("Error saving current heart rate: " + ex.getLocalizedMessage());
        }
        Intent intent = new Intent(DeviceService.ACTION_REALTIME_SAMPLES)
                .putExtra(DeviceService.EXTRA_REALTIME_SAMPLE, sample);
        LocalBroadcastManager.getInstance(support.getContext()).sendBroadcast(intent);
    }

    private void saveBaseActivitySummary() {
        try (DBHandler dbHandler = GBApplication.acquireDB()) {
            DaoSession session = dbHandler.getDaoSession();
            Device device = DBHelper.getDevice(support.getDevice(), session);
            User user = DBHelper.getUser(session);
            baseActivitySummary.setDevice(device);
            baseActivitySummary.setUser(user);
            session.getBaseActivitySummaryDao().insertOrReplace(baseActivitySummary);
        } catch (Exception ex) {
            GB.toast(support.getContext(), "Error saving activity summary", Toast.LENGTH_LONG, GB.ERROR, ex);
        }
    }
}
