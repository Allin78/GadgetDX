package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

import android.content.Intent;

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
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.LiveHeartRate;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;

public class LiveHeartrateHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(LiveHeartrateHandler.class);
    private final WithingsSteelHRDeviceSupport support;

    public LiveHeartrateHandler(WithingsSteelHRDeviceSupport support) {
        this.support = support;
    }


    @Override
    public void handleMessage(Message message) {
        List<WithingsStructure> data = message.getDataStructures();
        if (data == null || data.isEmpty()) {
            return;
        }


        WithingsStructure structure = data.get(0);
        int heartRate = 0;
        if (structure instanceof LiveHeartRate) {
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
}
