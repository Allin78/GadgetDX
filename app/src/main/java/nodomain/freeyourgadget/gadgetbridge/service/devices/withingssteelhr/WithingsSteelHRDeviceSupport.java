package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.net.Uri;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
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
import nodomain.freeyourgadget.gadgetbridge.service.btle.ServerTransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.ConversationQueue;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.MessageHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.WithingsServerAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.WithingsUUID;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivityTarget;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AlarmName;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AlarmSettings;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AncsStatus;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.DataStructureFactory;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.GetActivitySamples;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Locale;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.UserUnit;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.UserUnitConstants;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.MessageFactory;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessage;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageTypes;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Time;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class WithingsSteelHRDeviceSupport extends AbstractBTLEDeviceSupport {

    private static final Logger logger = LoggerFactory.getLogger(WithingsSteelHRDeviceSupport.class);
    private MessageHandler messageHandler;
    private ConversationQueue conversationQueue;
    private boolean firstTimeConnect;
    private BluetoothGattCharacteristic initNotificationCharacteristic;
    private BluetoothGattCharacteristic handleNotificationsCharacteristic;
    private BluetoothDevice device;

    public WithingsSteelHRDeviceSupport() {
        super(logger);
        messageHandler = new MessageHandler(this, new MessageFactory(new DataStructureFactory()));
        addSupportedService(WithingsUUID.WITHINGS_SERVICE_UUID);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addANCSService();
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        conversationQueue = new ConversationQueue(this);
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));
        getDevice().setFirmwareVersion("N/A");
        getDevice().setFirmwareVersion2("N/A");
        BluetoothGattCharacteristic characteristic = getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID);
        builder.notify(characteristic, true);
        builder.requestMtu(119);
        return builder;
    }

    @Override
    public boolean connectFirstTime() {
        firstTimeConnect = true;
        return connect();
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        if (firstTimeConnect) {
            conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.INITIAL_CONNECT));
            conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SET_LOCALE, new Locale("de")));
            conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.START_HANDS_CALIBRATION));
            conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.STOP_HANDS_CALIBRATION));
            conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SET_TIME, new Time()));
            conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SET_ACTIVITY_TARGET, new ActivityTarget(5000)));
            conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SETUP_FINISHED));
        }

        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_BATTERY_STATUS));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_HR));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SET_ANCS_STATUS, new AncsStatus(false)));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_ANCS_STATUS));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SET_ANCS_STATUS, new AncsStatus(true)));
        conversationQueue.send();
        if (!firstTimeConnect) {
            finishInitialization();
        }
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

        if (data.length >= 5) {
            short messageType = (short) BLETypeConversions.toInt16(data[2], data[1]);
            conversationQueue.onResponseReceived(messageType);
        }
        messageHandler.handleMessage(data);
        return true;
    }

    public void finishInitialization() {
        TransactionBuilder builder = createTransactionBuilder("setupFinished");
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
        builder.queue(getQueue());
    }

    public void handleSyncMessage(Message syncMessage) {
        conversationQueue.clear();
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SYNC, false));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_ANCS_STATUS));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_BATTERY_STATUS));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SET_TIME, new Time()));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SET_ACTIVITY_TARGET, new ActivityTarget(5000)));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SET_USER_UNIT, new UserUnit(UserUnitConstants.DISTANCE, UserUnitConstants.UNIT_KM)));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SET_USER_UNIT, new UserUnit(UserUnitConstants.CLOCK_MODE, UserUnitConstants.UNIT_24H)));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_ALARM_SETTINGS));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_SCREEN_SETTINGS));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_ALARM_SETTINGS));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.START_ALARM_SETTING));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.ENABLE_ALARM));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_ACTIVITY_SAMPLES, new GetActivitySamples(new Date().getTime()/1000, (short)5)));
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.SYNC_OK, false));
        conversationQueue.send();
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

        if (alarms.size() == 0) {
            return;
        }

        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.START_ALARM_SETTING));
        for (Alarm alarm : alarms) {
            if (!alarm.getUnused()) {
                AlarmSettings alarmSettings = new AlarmSettings();
                alarmSettings.setHour((short) alarm.getHour());
                alarmSettings.setMinute((short) alarm.getMinute());
                // TODO find out how to map the values of GB to the withings settings.
                alarmSettings.setDayOfWeek((short) 255);
                Message alarmMessage = new WithingsMessage(WithingsMessageTypes.SET_ALARM, alarmSettings);
                alarmMessage.addDataStructure(new AlarmName(alarm.getTitle()));
                conversationQueue.addMessage(alarmMessage);
                conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.ENABLE_ALARM));
            }
        }
        conversationQueue.send();
    }

    @Override
    public boolean onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        this.device = device;
        return super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
    }

    @Override
    public boolean onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        return super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
    }

    @Override
    public boolean onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        return super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
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
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_HR));
        conversationQueue.send();
    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {
        logger.debug("onEnableRealtimeHeartRateMeasurement");
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
        conversationQueue.clear();
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DAY_OF_MONTH, -2);
        conversationQueue.addMessage(new WithingsMessage(WithingsMessageTypes.GET_ACTIVITY_SAMPLES, new GetActivitySamples(c.getTimeInMillis()/1000, (short)5)));
        conversationQueue.send();
//        if (device != null) {
//            ByteBuffer allocate = ByteBuffer.allocate(8);
//            allocate.put((byte)0x00);
//            allocate.put((byte)0x02);
//            allocate.put((byte)0x01);
//            allocate.put((byte)0x01);
//            int nextInt = new Random().nextInt();
//            allocate.putInt(Integer.valueOf(nextInt));
//            initNotificationCharacteristic.setValue(allocate.array());
//            ServerTransactionBuilder serverTransactionBuilder = createServerTransactionBuilder("notify");
//            serverTransactionBuilder.add(new WithingsServerAction(device, initNotificationCharacteristic));
//            serverTransactionBuilder.queue(getQueue());
//        } else {
//            GB.toast("Could not send notification as there is no bluetooth device connected!", Toast.LENGTH_LONG, GB.ERROR);
//        }
    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    private void addANCSService() {
        BluetoothGattService withingsGATTService = new BluetoothGattService(WithingsUUID.WITHINGS_APP_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        initNotificationCharacteristic = new BluetoothGattCharacteristic(WithingsUUID.INIT_NOTIFICATIONS_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        initNotificationCharacteristic.addDescriptor(new BluetoothGattDescriptor(WithingsUUID.CCC_DESCRIPTOR_UUID, BluetoothGattCharacteristic.PERMISSION_WRITE));
        withingsGATTService.addCharacteristic(initNotificationCharacteristic);
        withingsGATTService.addCharacteristic(new BluetoothGattCharacteristic(WithingsUUID.INIT_NOTIFICATIONS_RESPONSE_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE));
        handleNotificationsCharacteristic = new BluetoothGattCharacteristic(WithingsUUID.HANDLE_NOTIFICATIONS_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        handleNotificationsCharacteristic.addDescriptor(new BluetoothGattDescriptor(WithingsUUID.CCC_DESCRIPTOR_UUID, BluetoothGattCharacteristic.PERMISSION_WRITE));
        withingsGATTService.addCharacteristic(handleNotificationsCharacteristic);
        addSupportedServerService(withingsGATTService);
    }

//    private byte[] hexToBytes(String str) {
//        byte[] val = new byte[str.length() / 2];
//        for (int i = 0; i < val.length; i++) {
//            int index = i * 2;
//            int j = Integer.parseInt(str.substring(index, index + 2), 16);
//            val[i] = (byte) j;
//        }
//
//        return val;
//    }
}
