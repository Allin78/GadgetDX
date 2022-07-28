package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityUser;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.model.CalendarEventSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CannedMessagesSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.WaitAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.ActivitySampleHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.BatteryStateHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.Conversation;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.ConversationQueue;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.SetupFinishedHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.SyncFinishedHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Status;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.MessageHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.WithingsUUID;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.HeartbeatHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.ResponseHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.SimpleConversation;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivityTarget;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AlarmName;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AlarmSettings;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AncsStatus;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.DataStructureFactory;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.GetActivitySamples;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Locale;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.TypeVersion;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.User;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.UserSecret;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.UserUnit;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.UserUnitConstants;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.VasistasType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructureType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.ExpectedResponse;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.MessageFactory;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessage;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageTypes;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Time;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class WithingsSteelHRDeviceSupport extends AbstractBTLEDeviceSupport {

    private static final Logger logger = LoggerFactory.getLogger(WithingsSteelHRDeviceSupport.class.getSimpleName());
    private MessageHandler messageHandler;
    private ConversationQueue conversationQueue;
    private boolean firstTimeConnect;
    private BluetoothGattCharacteristic initNotificationCharacteristic;
    private BluetoothGattCharacteristic handleNotificationsCharacteristic;
    private BluetoothDevice device;
    private boolean authenticationInProgress;
    private boolean syncInProgress;
    private AuthenticationHandler authenticationHandler;
    private ActivityUser activityUser;

    public WithingsSteelHRDeviceSupport() {
        super(logger);
        authenticationHandler = new AuthenticationHandler(this);
        messageHandler = new MessageHandler(this, new MessageFactory(new DataStructureFactory()));
        addSupportedService(WithingsUUID.WITHINGS_SERVICE_UUID);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addANCSService();
        activityUser = new ActivityUser();
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
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.INITIAL_CONNECT));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_LOCALE, new Locale("de")));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.START_HANDS_CALIBRATION));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.STOP_HANDS_CALIBRATION));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_TIME, new Time()));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_USER_UNIT, new UserUnit(UserUnitConstants.DISTANCE, UserUnitConstants.UNIT_KM)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_USER_UNIT, new UserUnit(UserUnitConstants.CLOCK_MODE, UserUnitConstants.UNIT_24H)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_ACTIVITY_TARGET, new ActivityTarget(activityUser.getStepsGoal())));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_ANCS_STATUS, new AncsStatus(false)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_ANCS_STATUS));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_ANCS_STATUS, new AncsStatus(true)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_BATTERY_STATUS), new BatteryStateHandler(this));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SETUP_FINISHED), new SetupFinishedHandler(this));
        } else {
            authenticationHandler.startAuthentication();
        }

        if (!firstTimeConnect) {
            finishInitialization();
        }

        conversationQueue.send();
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

        boolean complete = messageHandler.handleMessage(data);
        if (complete) {
            Message message = messageHandler.getMessage();
            if (message.isIncomingMessage()) {
                logger.debug("received incoming message: " + message.getType());
                if (message.getType() == WithingsMessageTypes.SYNC && !syncInProgress) {
                    conversationQueue.clear();
                    addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.INITIAL_CONNECT, ExpectedResponse.NONE));
                    addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SYNC_RESPONSE, ExpectedResponse.NONE));
                    addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SYNC_OK));
                    conversationQueue.send();
                    doSync();
                }
            } else if (authenticationInProgress) {
                authenticationHandler.handleAuthenticationResponse(message);
            } else {
                conversationQueue.processResponse(message);
            }
        }

        return true;
    }



    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        Class clazz = notificationSpec.getClass();
        logger.info("Got notification from " + notificationSpec.sender + " to send: " + notificationSpec.body);
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

        conversationQueue.clear();
        addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_ALARM));
        for (Alarm alarm : alarms) {
            if (alarm.getEnabled()) {
                AlarmSettings alarmSettings = new AlarmSettings();
                alarmSettings.setHour((short) alarm.getHour());
                alarmSettings.setMinute((short) alarm.getMinute());
                // TODO find out how to map the values of GB to the withings settings.
                alarmSettings.setDayOfWeek((short) alarm.getRepetition());
                if (alarm.getSmartWakeup()) {
                    alarmSettings.setYetUnknown((short) 15);
                }
                Message alarmMessage = new WithingsMessage(WithingsMessageTypes.SET_ALARM, alarmSettings);
                alarmMessage.addDataStructure(new AlarmName(alarm.getTitle()));
                addSimpleConversationToQueue(alarmMessage);
                addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_ALARM_ENABLED));
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
        logger.info("onSetCannedMessages");
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
        doSync();
    }

    @Override
    public void onReset(int flags) {
    }

    @Override
    public void onHeartRateTest() {
        conversationQueue.clear();
        addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_HR), new HeartbeatHandler(this));
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
//        conversationQueue.clear();
//        addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.CMD_NOTIFICATION_APP_ENABLED_GET, new Status()));
//        addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.CMD_NOTIFICATION_APP_ENABLED_SET, new Status()));
//        conversationQueue.send();
        TransactionBuilder builder= createTransactionBuilder("test");
        BluetoothGattCharacteristic characteristic = getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID);
        builder.write(characteristic, new WithingsMessage(WithingsMessageTypes.SET_ANCS_STATUS, new AncsStatus(true)).getRawData());
        builder.add(new WaitAction(100));
        builder.write(characteristic, GB.hexStringToByteArray("0109310000"));
        builder.add(new WaitAction(100));
        builder.write(characteristic, GB.hexStringToByteArray("00020000fd542259"));
        builder.queue(getQueue());
    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    void setAuthenticationInProgress(boolean inProgress) {
        authenticationInProgress = inProgress;
//        if (!authenticationInProgress) {
//            doSync();
//        }
    }

    public void finishInitialization() {
        TransactionBuilder builder = createTransactionBuilder("setupFinished");
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
        builder.queue(getQueue());
    }

    public void finishSync() {
        syncInProgress = false;
        saveLastSyncTimestamp(new Date().getTime());
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

    private void addSimpleConversationToQueue(Message message) {
        addSimpleConversationToQueue(message, null);
    }

    private void addSimpleConversationToQueue(Message message, ResponseHandler handler) {
        Conversation conversation = new SimpleConversation(handler);
        conversation.setRequest(message);
        conversationQueue.addConversation(conversation);
    }
    private void doSync() {
        conversationQueue.clear();
        syncInProgress = true;
        try {
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.INITIAL_CONNECT, ExpectedResponse.NONE));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SYNC_RESPONSE, ExpectedResponse.NONE));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_ANCS_STATUS));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_USER));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_BATTERY_STATUS), new BatteryStateHandler(this));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_TIME, new Time()));
            WithingsMessage message = new WithingsMessage(WithingsMessageTypes.SET_USER);
            message.addDataStructure(getUser());
            message.addDataStructure(new UserSecret());
            addSimpleConversationToQueue(message);
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_ACTIVITY_TARGET, new ActivityTarget(activityUser.getStepsGoal())));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_USER_UNIT, new UserUnit(UserUnitConstants.DISTANCE, UserUnitConstants.UNIT_KM)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SET_USER_UNIT, new UserUnit(UserUnitConstants.CLOCK_MODE, UserUnitConstants.UNIT_24H)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_ALARM_SETTINGS));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_SCREEN_SETTINGS));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_ALARM));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_ALARM_ENABLED), new SyncFinishedHandler(this));

            // TODO: add handler for workoutscreenlist
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_WORKOUT_SCREEN_LIST));
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(getLastSyncTimestamp());
            message = new WithingsMessage(WithingsMessageTypes.GET_ACTIVITY_SAMPLES, ExpectedResponse.EOT);
            message.addDataStructure(new GetActivitySamples(c.getTimeInMillis() / 1000, (short) 0));
            message.addDataStructure(new VasistasType(WithingsStructureType.VASISTAS_TYPE_WORKOUT_PAUSE));
            addSimpleConversationToQueue(message, new ActivitySampleHandler(this));
            message = new WithingsMessage(WithingsMessageTypes.GET_MOVEMENT_SAMPLES, ExpectedResponse.EOT);
            message.addDataStructure(new GetActivitySamples(c.getTimeInMillis() / 1000, (short) 0));
            message.addDataStructure(new TypeVersion());
            addSimpleConversationToQueue(message, new ActivitySampleHandler(this));
            message = new WithingsMessage(WithingsMessageTypes.GET_BODY_VASISTAS, ExpectedResponse.EOT);
            message.addDataStructure(new GetActivitySamples(c.getTimeInMillis() / 1000, (short) 0));
            message.addDataStructure(new TypeVersion());
            addSimpleConversationToQueue(message, new ActivitySampleHandler(this));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.GET_SPORT_MODE, new GetActivitySamples(c.getTimeInMillis() / 1000, (short) 0)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SYNC_OK));
        } catch (Exception e) {
            logger.error("Could not synchronize! ", e);
            conversationQueue.clear();
        } finally {
            // This must be done in all cases or the watch won't respond anymore!
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageTypes.SYNC_OK));
        }
        conversationQueue.send();
    }

    private void saveLastSyncTimestamp(@NonNull long timestamp) {
        SharedPreferences.Editor editor = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()).edit();
        editor.putLong("lastActivitySync", timestamp);
        editor.apply();
    }

    private long getLastSyncTimestamp() {
        SharedPreferences settings = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());
        long lastSyncTime =  settings.getLong("lastActivitySync", 0);
        if (lastSyncTime > 0) {
            return lastSyncTime;
        } else {
            Date currentDate = new Date();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(currentDate.getTime());
            c.add(Calendar.DAY_OF_MONTH, -2);
            return c.getTimeInMillis();
        }
    }

    private User getUser() {
        User user = new User();
        ActivityUser activityUser = new ActivityUser();
        user.setName(activityUser.getName());
        user.setGender((byte) activityUser.getGender());
        user.setHeight(activityUser.getHeightCm());
        user.setWeight(activityUser.getWeightKg());
        user.setBirthdate(activityUser.getUserBirthday());
        return user;
    }
}
