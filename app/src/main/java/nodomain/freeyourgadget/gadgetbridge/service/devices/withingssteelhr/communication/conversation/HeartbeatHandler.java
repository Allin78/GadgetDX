package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.GregorianCalendar;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr.WithingsSteelHRSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.HeartRate;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.LiveHeartRate;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;

public class HeartbeatHandler extends AbstractResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

    public HeartbeatHandler(WithingsSteelHRDeviceSupport support) {
        super(support);
    }

    @Override
    public void handleResponse(Message response) {
        if (response.getDataStructures().size() > 0) {
            handleHeartRateData(response.getDataStructures().get(0));
        }
    }

    private void handleHeartRateData(WithingsStructure structure) {
        int heartRate = 0;
        if (structure instanceof HeartRate) {
            heartRate = ((HeartRate)structure).getHeartrate();
        } else if (structure instanceof LiveHeartRate) {
            heartRate = ((LiveHeartRate)structure).getHeartrate();
        }

        if (heartRate > 0) {
            saveHeartRateData(heartRate);
        }
    }

    private void saveHeartRateData(int heartRate) {
        WithingsSteelHRActivitySample sample = new WithingsSteelHRActivitySample();
        sample.setTimestamp((int) (GregorianCalendar.getInstance().getTimeInMillis() / 1000L));
        sample.setHeartRate(heartRate);
        logger.info("Current heart rate is: " + sample.getHeartRate() + " BPM");
        try (DBHandler dbHandler = GBApplication.acquireDB()) {
            Long userId = DBHelper.getUser(dbHandler.getDaoSession()).getId();
            Long deviceId = DBHelper.getDevice(device, dbHandler.getDaoSession()).getId();
            WithingsSteelHRSampleProvider provider = new WithingsSteelHRSampleProvider(device, dbHandler.getDaoSession());
            sample.setDeviceId(deviceId);
            sample.setUserId(userId);
            provider.addGBActivitySample(sample);
        } catch (Exception ex) {
            logger.warn("Error saving current heart rate: " + ex.getLocalizedMessage());
        }
    }
}
