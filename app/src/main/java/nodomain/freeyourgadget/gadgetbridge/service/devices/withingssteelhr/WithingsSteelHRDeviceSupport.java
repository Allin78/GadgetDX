package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
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
import nodomain.freeyourgadget.gadgetbridge.service.btle.BtLEAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattCharacteristic;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.WriteAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.IntentListener;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.battery.BatteryInfoProfile;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfoProfile;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.InitOperation;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command.InitialConnectCommmand;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command.SetTimeCommand;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command.SimpleCommand;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command.WithingsCommandTypes;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Time;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class WithingsSteelHRDeviceSupport extends AbstractBTLEDeviceSupport {

    private static final Logger logger = LoggerFactory.getLogger(WithingsSteelHRDeviceSupport.class);

    private boolean hasBeenCalibrated = false;

    public WithingsSteelHRDeviceSupport() {
        super(logger);
        addSupportedService(UUID.fromString("00000020-5749-5448-0037-000000000000"));
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
//        addSupportedServerService(new BluetoothGattService(UUID.fromString("00000050-5749-5448-0037-000000000000"), BluetoothGattService.SERVICE_TYPE_PRIMARY));
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        // mark the device as initializing
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        // initialize...
        BluetoothGattCharacteristic mainCharacteristic = getCharacteristic(UUID.fromString("00000024-5749-5448-0037-000000000000"));
        builder.notify(mainCharacteristic, true);
//        builder.notify(getCharacteristic(UUID.fromString("00000021-5749-5448-0037-000000000000")), true);
//        builder.notify(getCharacteristic(UUID.fromString("00000023-5749-5448-0037-000000000000")), true);
        builder.requestMtu(80).queue(getQueue());
        builder.add(new BtLEAction(mainCharacteristic) {
            @Override
            public boolean expectsResult() {
                return true;
            }

            @Override
            public boolean run(BluetoothGatt gatt) {
                return new WriteAction(getCharacteristic(), new InitialConnectCommmand().getRawData()).run(gatt);
            }
        });


        // mark the device as initialized
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
        return builder;
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        if (super.onCharacteristicChanged(gatt, characteristic)) {
            return true;
        }

        UUID characteristicUUID = characteristic.getUuid();
        byte[] data = characteristic.getValue();
        logger.debug("Characteristic changed:" + characteristicUUID + " with data " + Arrays.toString(data));
        if (data.length == 0) {
            return true;
        }

        switch (data[0]) {
            case (byte)0x01:
                logger.info("characteristic change: " + characteristicUUID + " data: " + StringUtils.bytesToHex(data));
//                BluetoothGattCharacteristic mainCharacteristic = getCharacteristic(UUID.fromString("00000024-5749-5448-0037-000000000000"));
                if (BLETypeConversions.toInt16(data[2], data[1]) == WithingsCommandTypes.INITIAL_CONNECT) {
                    TransactionBuilder builder = createTransactionBuilder("Init main");
                    byte[] command = getTimeCommand();
                    builder.write(characteristic, command);
                    builder.queue(getQueue());
                } else if (BLETypeConversions.toInt16(data[2], data[1]) == WithingsCommandTypes.SET_TIME && !hasBeenCalibrated) {
                    TransactionBuilder builder = createTransactionBuilder("Start calibration");
                    builder.write(characteristic, new SimpleCommand(WithingsCommandTypes.START_HANDS_CALIBRATION).getRawData());
                    builder.queue(getQueue());
                } else if (BLETypeConversions.toInt16(data[2], data[1]) == WithingsCommandTypes.START_HANDS_CALIBRATION) {
                    TransactionBuilder builder = createTransactionBuilder("Stop calibration");
                    builder.write(characteristic, new SimpleCommand(WithingsCommandTypes.STOP_HANDS_CALIBRATION).getRawData());
                    builder.queue(getQueue());
                    hasBeenCalibrated = true;
                } else if (BLETypeConversions.toInt16(data[2], data[1]) == WithingsCommandTypes.STOP_HANDS_CALIBRATION) {
                    TransactionBuilder builder = createTransactionBuilder("SetTime");
                    builder.write(characteristic, getTimeCommand());
                    builder.queue(getQueue());
                }

                return true;
            default:
                logger.info("Unhandled characteristic change: " + characteristicUUID + " code: " + Arrays.toString(data));
                return true;
        }
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        logger.debug("onNotification");
    }

    @Override
    public void onDeleteNotification(int id) {
        logger.debug("onDeleteNotification");

    }

    @Override
    public void onSetTime() {
        logger.debug("onSetTime");

    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {
        logger.debug("onSetAlarms");

    }

    @Override
    public void onSetCallState(CallSpec callSpec) {
        logger.debug("onSetCallState");

    }

    @Override
    public void onSetCannedMessages(CannedMessagesSpec cannedMessagesSpec) {
        logger.debug("onSetCannedMessages");

    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {
        logger.debug("onSetMusicState");

    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {
        logger.debug("onSetMusicInfo");

    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {
        logger.debug("onEnableRealtimeSteps");

    }

    @Override
    public void onInstallApp(Uri uri) {
        logger.debug("onInstallApp");

    }

    @Override
    public void onAppInfoReq() {
        logger.debug("onAppInfoReq");

    }

    @Override
    public void onAppStart(UUID uuid, boolean start) {
        logger.debug("onAppStart");

    }

    @Override
    public void onAppDelete(UUID uuid) {
        logger.debug("onAppDelete");

    }

    @Override
    public void onAppConfiguration(UUID appUuid, String config, Integer id) {
        logger.debug("onAppConfiguration");

    }

    @Override
    public void onAppReorder(UUID[] uuids) {
        logger.debug("onAppReorder");

    }

    @Override
    public void onFetchRecordedData(int dataTypes) {
        logger.debug("onFetchRecordedData");

    }

    @Override
    public void onReset(int flags) {
        logger.debug("onReset");

    }

    @Override
    public void onHeartRateTest() {
        logger.debug("onHeartRateTest");

    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {
        logger.debug("onEnableRealtimeHeartRateMeasurement");

    }

    @Override
    public void onFindDevice(boolean start) {
        logger.debug("onFindDevice");

    }

    @Override
    public void onSetConstantVibration(int integer) {
        logger.debug("onSetConstantVibration");

    }

    @Override
    public void onScreenshotReq() {
        logger.debug("onScreenshotReq");

    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {
        logger.debug("");

    }

    @Override
    public void onSetHeartRateMeasurementInterval(int seconds) {
        logger.debug("");

    }

    @Override
    public void onAddCalendarEvent(CalendarEventSpec calendarEventSpec) {
        logger.debug("");

    }

    @Override
    public void onDeleteCalendarEvent(byte type, long id) {
        logger.debug("");

    }

    @Override
    public void onSendConfiguration(String config) {
        logger.debug("");

    }

    @Override
    public void onReadConfiguration(String config) {
        logger.debug("");

    }

    @Override
    public void onTestNewFunction() {
        logger.debug("");

    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {
        logger.debug("");

    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    private byte[] getTimeCommand() {
        Time time = new Time();
        LocalDateTime now = LocalDateTime.now();
        time.setNow(now);
        ZoneId defaultZoneId = ZoneId.systemDefault();
        ZoneOffset offset = defaultZoneId.getRules().getOffset(now);
        time.setTimeOffsetInSeconds(offset.getTotalSeconds());
        ZoneOffsetTransition transition = defaultZoneId.getRules().nextTransition(ZonedDateTime.now(defaultZoneId).toInstant());
        time.setNextDaylightSavingTransition(transition.getDateTimeBefore());
        time.setNextDaylightSavingTransitionOffsetInSeconds(transition.getOffsetBefore().getTotalSeconds());
        SetTimeCommand command = new SetTimeCommand();
        command.addDataStructure(time);
        return command.getRawData();
    }
}
