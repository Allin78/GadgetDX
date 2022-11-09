package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.devices.huami.HuamiConst;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivityKind;
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
import nodomain.freeyourgadget.gadgetbridge.service.btle.ServerTransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.incoming.IncomingMessageHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.incoming.IncomingMessageHandlerFactory;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.incoming.LiveWorkoutHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.activity.WithingsActivityType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.WithingsServerAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.WithingsUUID;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.ActivitySampleHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.BatteryStateHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.Conversation;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.ConversationQueue;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.HeartbeatHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.ResponseHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.SetupFinishedHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.SimpleConversation;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.SyncFinishedHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation.WorkoutScreenListHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ActivityTarget;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AlarmName;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AlarmSettings;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AlarmStatus;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.AncsStatus;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.DataStructureFactory;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.EndOfTransmission;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.GetActivitySamples;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ImageData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ImageMetaData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Locale;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Probe;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ProbeOsVersion;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ScreenSettings;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Time;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.TypeVersion;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.User;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.UserUnit;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.UserUnitConstants;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WorkoutScreen;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.ExpectedResponse;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.MessageFactory;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.MessageBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessage;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification.GetNotificationAttributes;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification.GetNotificationAttributesResponse;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification.NotificationProvider;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification.NotificationSource;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.GBPrefs;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class WithingsSteelHRDeviceSupport extends AbstractBTLEDeviceSupport {

    private static final Logger logger = LoggerFactory.getLogger(WithingsSteelHRDeviceSupport.class);
    public static final String LAST_ACTIVITY_SYNC = "lastActivitySync";
    public static final String HANDS_CALIBRATION_CMD = "withings_hands_calibration";
    private MessageBuilder messageBuilder;
    private LiveWorkoutHandler liveWorkoutHandler;
    private ConversationQueue conversationQueue;
    private boolean firstTimeConnect;
    private BluetoothGattCharacteristic notificationSourceCharacteristic;
    private BluetoothGattCharacteristic dataSourceCharacteristic;
    private BluetoothDevice device;
    private boolean syncInProgress;
    private ActivityUser activityUser;
    private NotificationProvider notificationProvider;
    private IncomingMessageHandlerFactory incomingMessageHandlerFactory;
    private int mtuSize;

    public WithingsSteelHRDeviceSupport() {
        super(logger);
        notificationProvider = NotificationProvider.getInstance(this);
        messageBuilder = new MessageBuilder(this, new MessageFactory(new DataStructureFactory()));
        liveWorkoutHandler = new LiveWorkoutHandler(this);
        incomingMessageHandlerFactory = IncomingMessageHandlerFactory.getInstance(this);
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
        mtuSize = mtu;
        if (firstTimeConnect) {
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.INITIAL_CONNECT));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_LOCALE, new Locale("de")));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.START_HANDS_CALIBRATION));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.STOP_HANDS_CALIBRATION));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_TIME, new Time()));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_USER_UNIT, new UserUnit(UserUnitConstants.DISTANCE, UserUnitConstants.UNIT_KM)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_USER_UNIT, new UserUnit(UserUnitConstants.CLOCK_MODE, UserUnitConstants.UNIT_24H)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_ACTIVITY_TARGET, new ActivityTarget(activityUser.getStepsGoal())));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_ANCS_STATUS));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_ANCS_STATUS, new AncsStatus(true)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_BATTERY_STATUS), new BatteryStateHandler(this));
            addScreenListCommands();
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SETUP_FINISHED), new SetupFinishedHandler(this));
        } else {
            Message message = new WithingsMessage(WithingsMessageType.PROBE);
            message.addDataStructure(new Probe((short) 1, (short) 1, 5100401));
            message.addDataStructure(new ProbeOsVersion((short) Build.VERSION.SDK_INT));
            conversationQueue.clear();
            addSimpleConversationToQueue(message, new AuthenticationHandler(this));
        }

        if (!firstTimeConnect) {
            finishInitialization();
        }
        conversationQueue.send();
    }

    public void doSync() {
        if (syncInProgress) {
            return;
        }

        conversationQueue.clear();
        syncInProgress = true;
        try {
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.INITIAL_CONNECT));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_ANCS_STATUS));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_BATTERY_STATUS), new BatteryStateHandler(this));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_TIME, new Time()));
            WithingsMessage message = new WithingsMessage(WithingsMessageType.SET_USER);
            message.addDataStructure(getUser());
            // The UserSecret appears in the original communication with the HealthMate app. Until now GB works without the secret.
            // This makes the "authentication" far easier. However if it turns out that this is needed, we would need to find a way to savely store a unique generated secret.
            //  message.addDataStructure(new UserSecret());
            addSimpleConversationToQueue(message);
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_ACTIVITY_TARGET, new ActivityTarget(activityUser.getStepsGoal())));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_USER_UNIT, new UserUnit(UserUnitConstants.DISTANCE, UserUnitConstants.UNIT_KM)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_USER_UNIT, new UserUnit(UserUnitConstants.CLOCK_MODE, UserUnitConstants.UNIT_24H)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_ALARM_SETTINGS));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_SCREEN_SETTINGS));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_ALARM));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_ALARM_ENABLED));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_WORKOUT_SCREEN_LIST), new WorkoutScreenListHandler(this));
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(getLastSyncTimestamp());
            message = new WithingsMessage(WithingsMessageType.GET_ACTIVITY_SAMPLES, ExpectedResponse.EOT);
            message.addDataStructure(new GetActivitySamples(c.getTimeInMillis() / 1000, (short) 0));
            addSimpleConversationToQueue(message, new ActivitySampleHandler(this));
            message = new WithingsMessage(WithingsMessageType.GET_MOVEMENT_SAMPLES, ExpectedResponse.EOT);
            message.addDataStructure(new GetActivitySamples(c.getTimeInMillis() / 1000, (short) 0));
            message.addDataStructure(new TypeVersion());
            addSimpleConversationToQueue(message, new ActivitySampleHandler(this));
            message = new WithingsMessage(WithingsMessageType.GET_HEARTRATE_SAMPLES, ExpectedResponse.EOT);
            message.addDataStructure(new GetActivitySamples(c.getTimeInMillis() / 1000, (short) 0));
            message.addDataStructure(new TypeVersion());
            addSimpleConversationToQueue(message, new ActivitySampleHandler(this));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SYNC_OK));
        } catch (Exception e) {
            logger.error("Could not synchronize! ", e);
            conversationQueue.clear();
        } finally {
            // This must be done in all cases or the watch won't respond anymore!
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SYNC_OK), new SyncFinishedHandler(this));
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

        boolean complete = messageBuilder.buildMessage(data);
        if (complete) {
            Message message = messageBuilder.getMessage();
            if (message.isIncomingMessage()) {
                logger.debug("received incoming message: " + message.getType());
                IncomingMessageHandler handler = incomingMessageHandlerFactory.getHandler(message);
                handler.handleMessage(message);
            } else {
                conversationQueue.processResponse(message);
            }
        }

        return true;
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        notificationProvider.notifyClient(notificationSpec);
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

        boolean noAlarmsEnabled = true;
        conversationQueue.clear();
        addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_ALARM));
        for (Alarm alarm : alarms) {
            if (alarm.getEnabled() && !alarm.getUnused()) {
                noAlarmsEnabled = false;
                addAlarm(alarm);
            }
        }

        if (noAlarmsEnabled) {
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_ALARM_ENABLED, new AlarmStatus(false)));
        }

        conversationQueue.send();
    }

    @Override
    public boolean onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        this.device = device;
        return true;
    }

    @Override
    public boolean onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        return super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
    }

    @Override
    public boolean onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        if (characteristic.getUuid().equals(WithingsUUID.CONTROL_POINT_CHARACTERISTIC_UUID)) {
            GetNotificationAttributes request = new GetNotificationAttributes();
            request.deserialize(value);
            notificationProvider.handleNotificationAttributeRequest(request);
        }

        return true;
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
        doSync();
    }

    @Override
    public void onReset(int flags) {
    }

    @Override
    public void onHeartRateTest() {
        conversationQueue.clear();
        addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_HR), new HeartbeatHandler(this));
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
        try {
            switch (config) {
                case HuamiConst.PREF_WORKOUT_ACTIVITY_TYPES_SORTABLE:
                    setWorkoutActivityTypes();
                    break;
                default:
                    logger.debug("unknown configuration setting received: " + config);
            }
        } catch (Exception e) {
            GB.toast("Error setting configuration", Toast.LENGTH_LONG, GB.ERROR, e);
        }
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

    public void sendToDevice(Message message) {
        if (message == null) {
            return;
        }

        TransactionBuilder builder = createTransactionBuilder("conversation");
        builder.setGattCallback(this);
        BluetoothGattCharacteristic characteristic = getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID);
        byte[] rawData = message.getRawData();
        builder.writeChunkedData(characteristic, rawData, mtuSize - 4);
        builder.queue(getQueue());
    }

    public void sendAncsNotificationSourceNotification(NotificationSource notificationSource) {
        try {
            ServerTransactionBuilder builder = performServer("notificationSourceNotification");
            notificationSourceCharacteristic.setValue(notificationSource.serialize());
            builder.add(new WithingsServerAction(device, notificationSourceCharacteristic));
            builder.queue(getQueue());
        } catch (IOException e) {
            logger.error("Could not send notification.", e);
            GB.toast("Could not send notification.", Toast.LENGTH_LONG, GB.ERROR, e);
        }
    }

    public void sendAncsDataSourceNotification(GetNotificationAttributesResponse response) {
        try {
            ServerTransactionBuilder builder = performServer("dataSourceNotification");
            byte[] data = response.serialize();
            dataSourceCharacteristic.setValue(response.serialize());
            builder.add(new WithingsServerAction(device, dataSourceCharacteristic));
            builder.queue(getQueue());
        } catch (IOException e) {
            logger.error("Could not send notification.", e);
            GB.toast("Could not send notification.", Toast.LENGTH_LONG, GB.ERROR, e);
        }
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

    void onAuthenticationFinished() {
        if (!firstTimeConnect && shoudSync()) {
            doSync();
        } else {
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_ANCS_STATUS, new AncsStatus(true)));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_ANCS_STATUS));
            addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.GET_BATTERY_STATUS), new BatteryStateHandler(this));
            conversationQueue.send();
        }
    }

    private void addAlarm(Alarm alarm) {
        AlarmSettings alarmSettings = new AlarmSettings();
        alarmSettings.setHour((short) alarm.getHour());
        alarmSettings.setMinute((short) alarm.getMinute());
        alarmSettings.setDayOfWeek(mapRepetitionToWithingsValue(alarm));
        if (alarm.getSmartWakeup()) {
            // Healthmate has the possibility to change the minutecount, in GB we use a fixed value of 15
            alarmSettings.setSmartWakeupMinutes((short) 15);
        }

        Message alarmMessage = new WithingsMessage(WithingsMessageType.SET_ALARM, alarmSettings);
        if (!StringUtils.isEmpty(alarm.getTitle())) {
            AlarmName alarmName = new AlarmName(alarm.getTitle());
            alarmMessage.addDataStructure(alarmName);
        }

        addSimpleConversationToQueue(alarmMessage);
        addSimpleConversationToQueue(new WithingsMessage(WithingsMessageType.SET_ALARM_ENABLED, new AlarmStatus(true)));
    }

    private short mapRepetitionToWithingsValue(Alarm alarm) {
        int repetition = 0;
        if (alarm.getRepetition(Alarm.ALARM_MON)) {
            repetition += 0x02;
        }
        if (alarm.getRepetition(Alarm.ALARM_TUE)) {
            repetition += 0x04;
        }
        if (alarm.getRepetition(Alarm.ALARM_WED)) {
            repetition += 0x08;
        }
        if (alarm.getRepetition(Alarm.ALARM_THU)) {
            repetition += 0x10;
        }
        if (alarm.getRepetition(Alarm.ALARM_FRI)) {
            repetition += 0x20;
        }
        if (alarm.getRepetition(Alarm.ALARM_SAT)) {
            repetition += 0x40;
        }
        if (alarm.getRepetition(Alarm.ALARM_SUN)) {
            repetition += 0x01;
        }

        return (short)(repetition + 0x80);
    }

    private void addANCSService() {
        BluetoothGattService withingsGATTService = new BluetoothGattService(WithingsUUID.WITHINGS_ANCS_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        notificationSourceCharacteristic = new BluetoothGattCharacteristic(WithingsUUID.NOTIFICATION_SOURCE_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        notificationSourceCharacteristic.addDescriptor(new BluetoothGattDescriptor(WithingsUUID.CCC_DESCRIPTOR_UUID, BluetoothGattCharacteristic.PERMISSION_WRITE));
        withingsGATTService.addCharacteristic(notificationSourceCharacteristic);
        withingsGATTService.addCharacteristic(new BluetoothGattCharacteristic(WithingsUUID.CONTROL_POINT_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE));
        dataSourceCharacteristic = new BluetoothGattCharacteristic(WithingsUUID.DATA_SOURCE_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        dataSourceCharacteristic.addDescriptor(new BluetoothGattDescriptor(WithingsUUID.CCC_DESCRIPTOR_UUID, BluetoothGattCharacteristic.PERMISSION_WRITE));
        withingsGATTService.addCharacteristic(dataSourceCharacteristic);
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
    private void saveLastSyncTimestamp(@NonNull long timestamp) {
        SharedPreferences.Editor editor = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()).edit();
        editor.putLong(LAST_ACTIVITY_SYNC, timestamp);
        editor.apply();
    }

    private long getLastSyncTimestamp() {
        SharedPreferences settings = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());
        long lastSyncTime =  settings.getLong(LAST_ACTIVITY_SYNC, 0);
        if (lastSyncTime > 0) {
            return lastSyncTime;
        } else {
            Date currentDate = new Date();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(currentDate.getTime());
            c.add(Calendar.DAY_OF_MONTH, -1);
            return c.getTimeInMillis();
        }
    }

    private boolean shoudSync() {
        long lastSynced = getLastSyncTimestamp();
        int quarterOfAnHourMillis = 15 * 60 * 1000;
        return new Date().getTime() - lastSynced > quarterOfAnHourMillis;
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

    private void addScreenListCommands() {
        // TODO: this needs to be more reworked, at the moment for example the notification screen is always on and this is full of magic numbers that need to be identified properly:
        Message message = new WithingsMessage(WithingsMessageType.SET_SCREEN_LIST);
        ScreenSettings settings = new ScreenSettings();
        settings.setId(0xff);
        settings.setIdOnDevice((byte)6);
        message.addDataStructure(settings);
        settings = new ScreenSettings();
        settings.setId(0x3d);
        settings.setIdOnDevice((byte)1);
        message.addDataStructure(settings);
        settings = new ScreenSettings();
        settings.setId(0x33);
        settings.setIdOnDevice((byte)4);
        message.addDataStructure(settings);
        settings = new ScreenSettings();
        settings.setId(0x2d);
        settings.setIdOnDevice((byte)2);
        message.addDataStructure(settings);
        settings = new ScreenSettings();
        settings.setId(0x2a);
        settings.setIdOnDevice((byte)3);
        message.addDataStructure(settings);
        settings = new ScreenSettings();
        settings.setId(0x26);
        settings.setIdOnDevice((byte)7);
        message.addDataStructure(settings);
        settings = new ScreenSettings();
        settings.setId(0x39);
        settings.setIdOnDevice((byte)9);
        message.addDataStructure(settings);
        message.addDataStructure(new EndOfTransmission());
        addSimpleConversationToQueue(message);
    }

    private void sendDistanceUnit() {
        Message message = new WithingsMessage(WithingsMessageType.SET_USER_UNIT, new UserUnit(UserUnitConstants.DISTANCE, UserUnitConstants.UNIT_KM));
        sendToDevice(message);
    }

    private void sendTimeFormat() {
        GBPrefs gbPrefs = new GBPrefs(new Prefs(GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress())));
        String timeFormat = gbPrefs.getTimeFormat();
        short format = UserUnitConstants.UNIT_12H;
        if (timeFormat.equals(DeviceSettingsPreferenceConst.PREF_TIMEFORMAT_24H)) {
            format = UserUnitConstants.UNIT_24H;
        }

        Message message = new WithingsMessage(WithingsMessageType.SET_USER_UNIT, new UserUnit(UserUnitConstants.CLOCK_MODE, format));
        sendToDevice(message);
    }

    private void sendUserInfo() {
        Message message = new WithingsMessage(WithingsMessageType.SET_USER);
        message.addDataStructure(getUser());
        sendToDevice(message);
    }

    private void sendActivityTarget() {
        Message message = new WithingsMessage(WithingsMessageType.SET_ACTIVITY_TARGET, new ActivityTarget(activityUser.getStepsGoal()));
        sendToDevice(message);
    }

    private void setWorkoutActivityTypes() {
        final SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(gbDevice.getAddress());

        final List<String> allActivityTypes = Arrays.asList(getContext().getResources().getStringArray(R.array.pref_withings_steel_activity_types_values));
        final List<String> defaultActivityTypes = Arrays.asList(getContext().getResources().getStringArray(R.array.pref_withings_steel_activity_types_default));
        final String activityTypesPref = prefs.getString("workout_activity_types_sortable", null);

        final List<String> enabledActivityTypes;
        if (activityTypesPref == null || activityTypesPref.equals("")) {
            enabledActivityTypes = defaultActivityTypes;
        } else {
            enabledActivityTypes = Arrays.asList(activityTypesPref.split(","));
        }

        conversationQueue.clear();
        for (int i = 0; i < enabledActivityTypes.size(); i++) {
            String workoutType = enabledActivityTypes.get(i);
            try {
                Message message = createWorkoutScreenMessage(workoutType);
                if (i == enabledActivityTypes.size() - 1) {
                    message.addDataStructure(new EndOfTransmission());
                }
                addSimpleConversationToQueue(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        conversationQueue.send();
    }

    @NonNull
    private Message createWorkoutScreenMessage(String workoutType) {
        WithingsActivityType withingsActivityType = WithingsActivityType.fromPrefValue(workoutType);
        int code = withingsActivityType.getCode();
        Message message = new WithingsMessage(WithingsMessageType.SET_WORKOUT_SCREEN, ExpectedResponse.NONE);
        WorkoutScreen workoutScreen = new WorkoutScreen();
        workoutScreen.setId(code);
        final int stringId = getContext().getResources().getIdentifier("activity_type_" + workoutType, "string", getContext().getPackageName());
        workoutScreen.setName(getContext().getString(stringId));
        message.addDataStructure(workoutScreen);

        ImageMetaData imageMetaData = new ImageMetaData();
        imageMetaData.setHeight((byte)24);
        imageMetaData.setWidth((byte)22);
        message.addDataStructure(imageMetaData);

        ImageData imageData = new ImageData();
        final int drawableId = ActivityKind.getIconId(withingsActivityType.toActivityKind());
        Drawable drawable = getContext().getDrawable(drawableId);
        imageData.setImageData(IconHelper.getIconBytesFromDrawable(drawable));
        message.addDataStructure(imageData);

        return message;
    }

}
