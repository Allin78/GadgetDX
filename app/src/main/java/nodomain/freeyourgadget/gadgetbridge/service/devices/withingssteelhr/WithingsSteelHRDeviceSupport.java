package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.model.CalendarEventSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CannedMessagesSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.MessageHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.WithingsUUID;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivityTarget;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AlarmSettings;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AlarmStatus;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Locale;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessage;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageTypes;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Time;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class WithingsSteelHRDeviceSupport extends AbstractBTLEDeviceSupport {

    private static final Logger logger = LoggerFactory.getLogger(WithingsSteelHRDeviceSupport.class);
    private MessageHandler messageHandler;

    public WithingsSteelHRDeviceSupport() {
        super(logger);
        addSupportedService(WithingsUUID.WITHINGS_SERVICE_UUID);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        messageHandler = new MessageHandler(getDevice());
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        // mark the device as initializing
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        // initialize...
        BluetoothGattCharacteristic characteristic = getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID);
        builder.notify(characteristic, true);
        builder.requestMtu(119);
        // The rest of the initalization has to be done in "onMtuChanged()" as the
        // write commands get truncated before the MTU-exchange is complete.
        return builder;
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        TransactionBuilder builder = createTransactionBuilder("init");
        builder.setGattCallback(this);
        BluetoothGattCharacteristic characteristic = getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID);
        builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.INITIAL_CONNECT).getRawData());
        builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.START_HANDS_CALIBRATION).getRawData());
        builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.STOP_HANDS_CALIBRATION).getRawData());
        builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.SET_TIME, new Time()).getRawData());
        builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.SET_LOCALE, new Locale("de")).getRawData());
        builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.SET_ACTIVITY_TARGET, new ActivityTarget()).getRawData());
        builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.GET_BATTERY_STATUS).getRawData());
        builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.GET_HR).getRawData());
        builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.SETUP_FINISHED).getRawData());

        // finish initialization:
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
        builder.queue(getQueue());
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        if (super.onCharacteristicChanged(gatt, characteristic)) {
            return true;
        }

        UUID characteristicUUID = characteristic.getUuid();
        byte[] data = characteristic.getValue();
        logger.debug("Characteristic changed:" + characteristicUUID + " with data " + StringUtils.bytesToHex(data));
//        if (data.length == 0 ) {
//            return true;
//        }

        if (data.length < 7) {
            return true;
        }

        switch (data[0]) {

            case (byte)0x01:
                short messageType = (short) BLETypeConversions.toInt16(data[2], data[1]);
                logger.debug("Got message of type: " + messageType);
                if (messageType == WithingsMessageTypes.SYNC) {
                    TransactionBuilder builder = createTransactionBuilder("sync");
                    builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.SYNC).getRawData());
                    builder.queue(getQueue());
                } else {
                    messageHandler.handleMessage(data);
                }
                return true;
            default:
                logger.info("Unhandled characteristic change: " + characteristicUUID + " code: " + Arrays.toString(data));
                return false;
        }
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
    }

    @Override
    public void onDeleteNotification(int id) {
    }

    @Override
    public void onSetTime() {
    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {
        if (alarms.size() > 3) {
            throw new IllegalArgumentException("Steel HR does only have three alarmslots!");
        }

        TransactionBuilder builder = createTransactionBuilder("alarm");
        for (Alarm alarm : alarms) {
            if (!alarm.getUnused()) {
                AlarmSettings alarmSettings = new AlarmSettings();
                alarmSettings.setHour((short) alarm.getHour());
                alarmSettings.setMinute((short) alarm.getMinute());
                alarmSettings.setDayOfWeek((short) alarm.getRepetition());
                builder.write(getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID), new WithingsMessage(WithingsMessageTypes.ALARM, alarmSettings).getRawData());
                builder.write(getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID), new WithingsMessage(WithingsMessageTypes.ALARM_STATUS, new AlarmStatus(true)).getRawData());
            }
        }

        builder.queue(getQueue());
    }

    @Override
    public void onSetCallState(CallSpec callSpec) {
    }

    @Override
    public void onSetCannedMessages(CannedMessagesSpec cannedMessagesSpec) {
    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {
    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {
    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
    }

    @Override
    public void onInstallApp(Uri uri) {
    }

    @Override
    public void onAppInfoReq() {
    }

    @Override
    public void onAppStart(UUID uuid, boolean start) {
    }

    @Override
    public void onAppDelete(UUID uuid) {
    }

    @Override
    public void onAppConfiguration(UUID appUuid, String config, Integer id) {
    }

    @Override
    public void onAppReorder(UUID[] uuids) {
    }

    @Override
    public void onFetchRecordedData(int dataTypes) {
    }

    @Override
    public void onReset(int flags) {
    }

    @Override
    public void onHeartRateTest() {
    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {
    }

    @Override
    public void onFindDevice(boolean start) {
    }

    @Override
    public void onSetConstantVibration(int integer) {
    }

    @Override
    public void onScreenshotReq() {
    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {
    }

    @Override
    public void onSetHeartRateMeasurementInterval(int seconds) {
    }

    @Override
    public void onAddCalendarEvent(CalendarEventSpec calendarEventSpec) {
    }

    @Override
    public void onDeleteCalendarEvent(byte type, long id) {
    }

    @Override
    public void onSendConfiguration(String config) {
    }

    @Override
    public void onReadConfiguration(String config) {
    }

    @Override
    public void onTestNewFunction() {
    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }
}
