package nodomain.freeyourgadget.gadgetbridge.service.devices.femometer;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.SharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.femometer.FemometerVinca2SampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.FemometerVinca2TemperatureSample;
import nodomain.freeyourgadget.gadgetbridge.externalevents.DripMeasurementBroadcaster;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryState;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattCharacteristic;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.IntentListener;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.battery.BatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.battery.BatteryInfoProfile;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfo;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfoProfile;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.healthThermometer.HealthThermometerProfile;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.healthThermometer.TemperatureInfo;

public class FemometerVinca2DeviceSupport extends AbstractBTLEDeviceSupport {

    private final DeviceInfoProfile<FemometerVinca2DeviceSupport> deviceInfoProfile;
    private final BatteryInfoProfile<FemometerVinca2DeviceSupport> batteryInfoProfile;
    private final HealthThermometerProfile<FemometerVinca2DeviceSupport> healthThermometerProfile;
    private static final Logger LOG = LoggerFactory.getLogger(FemometerVinca2DeviceSupport.class);

    public static final UUID UNKNOWN_SERVICE_UUID = UUID.fromString((String.format(AbstractBTLEDeviceSupport.BASE_UUID, "fef5")));
    // Characteristic 8082caa8-41a6-4021-91c6-56f9b954cc34 READ WRITE
    // Characteristic 9d84b9a3-000c-49d8-9183-855b673fda31 READ WRITE
    // Characteristic 457871e8-d516-4ca1-9116-57d0b17b9cb2 READ WRITE NO RESPONSE WRITE
    // Characteristic 5f78df94-798c-46f5-990a-b3eb6a065c88 READ NOTIFY

    public static final UUID CONFIGURATION_SERVICE_UUID = UUID.fromString("0f0e0d0c-0b0a-0908-0706-050403020100");
    public static final UUID CONFIGURATION_SERVICE_ALARM_CHARACTERISTIC = UUID.fromString("1f1e1d1c-1b1a-1918-1716-151413121110"); // READ WRITE
    public static final UUID CONFIGURATION_SERVICE_SETTING_CHARACTERISTIC = UUID.fromString("2f2e2d2c-2b2a-2928-2726-252423222120"); // WRITE
    public static final UUID CONFIGURATION_SERVICE_INDICATION_CHARACTERISTIC = UUID.fromString("3f3e3d3c-3b3a-3938-3736-353433323130"); // INDICATE

    public FemometerVinca2DeviceSupport() {
        super(LOG);

        /// Initialize Services
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addSupportedService(GattService.UUID_SERVICE_BATTERY_SERVICE);
        addSupportedService(GattService.UUID_SERVICE_DEVICE_INFORMATION);
        addSupportedService(GattService.UUID_SERVICE_HEALTH_THERMOMETER);
        addSupportedService(GattService.UUID_SERVICE_CURRENT_TIME);
        addSupportedService(GattService.UUID_SERVICE_REFERENCE_TIME_UPDATE);
        addSupportedService(UNKNOWN_SERVICE_UUID);
        addSupportedService(CONFIGURATION_SERVICE_UUID);

        /// Device Info
        IntentListener deviceInfoListener = intent -> {
            String action = intent.getAction();
            if (DeviceInfoProfile.ACTION_DEVICE_INFO.equals(action)) {
                DeviceInfo info = intent.getParcelableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO);
                if (info == null) return;
                GBDeviceEventVersionInfo versionCmd = new GBDeviceEventVersionInfo();
                versionCmd.hwVersion = info.getHardwareRevision();
                versionCmd.fwVersion = info.getSoftwareRevision(); // firmware always reported null
                handleGBDeviceEvent(versionCmd);
            }
        };

        deviceInfoProfile = new DeviceInfoProfile<>(this);
        deviceInfoProfile.addListener(deviceInfoListener);
        addSupportedProfile(deviceInfoProfile);

        /// Battery
        IntentListener batteryListener = intent -> {
            BatteryInfo info = intent.getParcelableExtra(BatteryInfoProfile.EXTRA_BATTERY_INFO);
            if (info == null) return;
            GBDeviceEventBatteryInfo batteryEvent = new GBDeviceEventBatteryInfo();
            batteryEvent.state = BatteryState.BATTERY_NORMAL;
            batteryEvent.level = info.getPercentCharged();
            evaluateGBDeviceEvent(batteryEvent);
            handleGBDeviceEvent(batteryEvent);
        };
        batteryInfoProfile = new BatteryInfoProfile<>(this);
        batteryInfoProfile.addListener(batteryListener);
        addSupportedProfile(batteryInfoProfile);


        /// Temperature
        IntentListener temperatureListener = intent -> {
            TemperatureInfo info = intent.getParcelableExtra(HealthThermometerProfile.EXTRA_TEMPERATURE_INFO);
            if (info == null) return;
            handleMeasurement(info);
        };
        healthThermometerProfile = new HealthThermometerProfile<>(this);
        healthThermometerProfile.addListener(temperatureListener);
        addSupportedProfile(healthThermometerProfile);
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    /**
     * @param hex formatted like '2ea3' (has to be even length)
     */
    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        // Init Battery
        batteryInfoProfile.requestBatteryInfo(builder);
        batteryInfoProfile.enableNotify(builder, true);

        // Init Device Info
        getDevice().setFirmwareVersion("N/A");
        getDevice().setFirmwareVersion2("N/A");
        deviceInfoProfile.requestDeviceInfo(builder);

        // Mystery stuff that happens in original app, not sure if its required
        BluetoothGattCharacteristic c2 = getCharacteristic(CONFIGURATION_SERVICE_SETTING_CHARACTERISTIC);
        builder.write(c2, hexToBytes("21"));
        builder.write(c2, hexToBytes("02"));
        builder.write(c2, hexToBytes("03"));
        builder.write(c2, hexToBytes("05"));

        // Sync Time
        onSetTime();

        // Init Thermometer
        builder.notify(getCharacteristic(CONFIGURATION_SERVICE_INDICATION_CHARACTERISTIC), true);
        healthThermometerProfile.enableNotify(builder, true);
        healthThermometerProfile.setMeasurementInterval(builder, hexToBytes("0100"));

        // mark the device as initialized
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
        return builder;
    }

    @Override
    public void onSetTime() {
        // Same Code as in PineTime (without the local time)
        GregorianCalendar now = BLETypeConversions.createCalendar();
        byte[] bytesCurrentTime = BLETypeConversions.calendarToCurrentTime(now, 0);

        TransactionBuilder builder = new TransactionBuilder("set time");
        builder.write(getCharacteristic(GattCharacteristic.UUID_CHARACTERISTIC_CURRENT_TIME), bytesCurrentTime);
        builder.queue(getQueue());
    }


    @Override
    public void onFetchRecordedData(int dataTypes) {
        // todo: this is only for debugging, remove this
        try (DBHandler db = GBApplication.acquireDB()) {
            FemometerVinca2SampleProvider sampleProvider = new FemometerVinca2SampleProvider(getDevice(), db.getDaoSession());
            for (FemometerVinca2TemperatureSample sample : sampleProvider.getSampleDao().loadAll()){
                TemperatureInfo info = new TemperatureInfo();
                info.setTemperature(sample.getTemperature());
                info.setTimestamp(new Date(sample.getTimestamp()));
                sendIntentToOtherApps(info);
            }
        } catch (Exception e) {
            LOG.error("Error acquiring database", e);
        }
    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {
        try {
            TransactionBuilder builder = performInitialized("applyThermometerSetting");

            Alarm alarm  = alarms.get(0);
            String alarm_hex = alarm.getEnabled()? "01" : "00"; // first byte 00/01: turn alarm off/on
            alarm_hex += String.format("%02X", alarm.getHour()); // second byte: hour
            alarm_hex += String.format("%02X", alarm.getMinute()); // third byte: minute

            builder.write(getCharacteristic(CONFIGURATION_SERVICE_ALARM_CHARACTERISTIC), hexToBytes(alarm_hex));
            builder.write(getCharacteristic(CONFIGURATION_SERVICE_SETTING_CHARACTERISTIC), hexToBytes("01"));
            // read-request on char1 results in given alarm
            builder.queue(getQueue());
        } catch (IOException e) {
            LOG.warn(" Unable to apply setting ", e);
        }
    }

    @Override
    public void onSendConfiguration(String config) {
        TransactionBuilder builder;
        SharedPreferences sharedPreferences = GBApplication.getDeviceSpecificSharedPrefs(this.getDevice().getAddress());
        LOG.info(" onSendConfiguration: " + config);
        try {
            builder = performInitialized("sendConfig: " + config);
            switch (config) {
                case DeviceSettingsPreferenceConst.PREF_FEMOMETER_MEASUREMENT_MODE:
                    setMeasurementMode(sharedPreferences);
                    break;
                case DeviceSettingsPreferenceConst.PREF_VOLUME:
                    setVolume(sharedPreferences);
                    break;
                case DeviceSettingsPreferenceConst.PREF_TEMPERATURE_SCALE_CF:
                    String scale = sharedPreferences.getString(DeviceSettingsPreferenceConst.PREF_TEMPERATURE_SCALE_CF,  "c");
                    String value = "c".equals(scale) ? "0a" : "0b";
                    applySetting(hexToBytes(value), null);
            }
            builder.queue(getQueue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Set Measurement Mode
     * modes (0- quick, 1- normal, 2- long)
     */
    private void setMeasurementMode(SharedPreferences sharedPreferences) {
        String measurementMode = sharedPreferences.getString(DeviceSettingsPreferenceConst.PREF_FEMOMETER_MEASUREMENT_MODE, "0");
        byte[] confirmation = hexToBytes("1e");
        switch (measurementMode) {
            case "0":
                applySetting(hexToBytes("1a"), confirmation);
                break;
            case "1":
                applySetting(hexToBytes("1c"), confirmation);
                break;
            case "2":
                applySetting(hexToBytes("1d"), confirmation);
                break;
        }
    }

    /** Set Volume
     * volumes 0-30 (0-10: quiet, 11-20: normal, 21-30: loud)
     */
    private void setVolume(SharedPreferences sharedPreferences) {
        int volume = sharedPreferences.getInt(DeviceSettingsPreferenceConst.PREF_VOLUME, 50);
        byte[] confirmation = hexToBytes("fd");
        if (volume < 11) {
            applySetting(hexToBytes("09"), confirmation);
        } else if (volume < 21) {
            applySetting(hexToBytes("14"), confirmation);
        } else {
            applySetting(hexToBytes("16"), confirmation);
        }
    }

    private void applySetting(byte[] value, byte[] confirmation) {
        try {
            TransactionBuilder builder = performInitialized("applyThermometerSetting");
            builder.write(getCharacteristic(CONFIGURATION_SERVICE_SETTING_CHARACTERISTIC), value);
            if (confirmation != null) {
                builder.write(getCharacteristic(CONFIGURATION_SERVICE_SETTING_CHARACTERISTIC), confirmation);
            }
            builder.queue(getQueue());
        } catch (IOException e) {
            LOG.warn(" Unable to apply setting ", e);
        }
    }

    private void handleMeasurement(TemperatureInfo info) {
        Date timestamp = info.getTimestamp();
        float temperature = info.getTemperature();
        int temperatureType = info.getTemperatureType();
        try (DBHandler db = GBApplication.acquireDB()) {
            Long userId = DBHelper.getUser(db.getDaoSession()).getId();
            Long deviceId = DBHelper.getDevice(getDevice(), db.getDaoSession()).getId();
            long time = timestamp.getTime();

            FemometerVinca2SampleProvider sampleProvider = new FemometerVinca2SampleProvider(getDevice(), db.getDaoSession());
            FemometerVinca2TemperatureSample temperatureSample = new FemometerVinca2TemperatureSample(time, deviceId, userId, temperature, temperatureType);
            sampleProvider.addSample(temperatureSample);
        } catch (Exception e) {
            LOG.error("Error acquiring database", e);
        }
        DripMeasurementBroadcaster.sendMeasurement(info);
    }
}
