package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.GregorianCalendar;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr.WithingsSteelHRSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryState;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.BatteryValues;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.HeartRate;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.MessageFactory;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageTypes;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(WithingsSteelHRDeviceSupport.class);
    private WithingsSteelHRDeviceSupport support;
    private MessageFactory messageFactory;
    private ByteArrayOutputStream pendingMessage;

    public MessageHandler(WithingsSteelHRDeviceSupport support, MessageFactory messageFactory) {
        this.support = support;
        this.messageFactory = messageFactory;

    }

    public boolean handleMessage(byte[] rawData) {
        if (pendingMessage == null) {
            pendingMessage = new ByteArrayOutputStream();
        }

        try {
            pendingMessage.write(rawData);
        } catch(IOException e) {
            logger.error("Could not write data to stream: " + StringUtils.bytesToHex(rawData));
            return false;
        }

        if (!isMessageComplete(pendingMessage.toByteArray())) {
            return false;
        } else {
            Message message = messageFactory.createMessageFromRawData(pendingMessage.toByteArray());
            pendingMessage = null;
            if (message == null) {
                logger.info("Cannot handle null message");
                return false;
            }

            switch (message.getType()) {
                case WithingsMessageTypes.GET_BATTERY_STATUS:
                    if (message.getDataStructures().size() > 0) {
                        handleBatteryState((BatteryValues) message.getDataStructures().get(0));
                    }
                    break;
                case WithingsMessageTypes.GET_HR:
                    if (message.getDataStructures().size() > 0) {
                        handleHeartRateData((HeartRate) message.getDataStructures().get(0));
                    }
                    break;
                case WithingsMessageTypes.SYNC:
                    support.handleSyncMessage(message);
                    break;
                case WithingsMessageTypes.SETUP_FINISHED:
                    support.finishInitialization();
                    break;
                default:
                    logger.warn("Unknown message type received: " + message.getType());
                    return false;
            }
        }
        return true;
    }

    private void handleBatteryState(BatteryValues batteryValues) {
        GBDeviceEventBatteryInfo batteryInfo = new GBDeviceEventBatteryInfo();
        batteryInfo.level = batteryValues.getPercent();
        switch (batteryValues.getStatus()) {
            case 0:
                batteryInfo.state = BatteryState.BATTERY_CHARGING;
                break;
            case 1:
                batteryInfo.state = BatteryState.BATTERY_LOW;
                break;
            default:
                batteryInfo.state = BatteryState.BATTERY_NORMAL;
        }
        batteryInfo.voltage = batteryValues.getVolt();
        support.evaluateGBDeviceEvent(batteryInfo);
    }

    private void handleHeartRateData(HeartRate heartRate) {
        WithingsSteelHRActivitySample sample = new WithingsSteelHRActivitySample();
        sample.setTimestamp((int) (GregorianCalendar.getInstance().getTimeInMillis() / 1000L));
        sample.setHeartRate(heartRate.getHeartrate());
        logger.info("Current heart rate is: " + sample.getHeartRate() + " BPM");
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
    }

    private boolean isMessageComplete(byte[] messageData) {
        if (messageData.length < 5) {
            return false;
        }

        short totalDataLength = (short) BLETypeConversions.toInt16(messageData[4], messageData[3]);
        byte[] rawStructureData = Arrays.copyOfRange(messageData, 5, messageData.length);
        if (rawStructureData.length == totalDataLength) {
            return true;
        }

        return false;
    }
}
