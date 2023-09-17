package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventFindPhone;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3.SonyWena3Constants;
import nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3.SonyWena3SettingKeys;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.NotificationArrival;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.NotificationRemoval;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.LedColor;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.NotificationFlags;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.NotificationKind;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.VibrationKind;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.VibrationOptions;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.AlarmListSettings;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.CameraAppTypeSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.MenuIconSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.StatusPageOrderSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.DisplayDesign;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.DisplayOrientation;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.DisplaySetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.DoNotDisturbSettings;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.FontSize;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.HomeIconId;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.HomeIconOrderSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.Language;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.SingleAlarmSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.TimeSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.TimeZoneSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.VibrationSetting;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.MenuIconId;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.StatusPageId;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.VibrationStrength;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.DeviceStateInfo;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.MusicInfo;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.NotificationServiceStatusRequest;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.NotificationServiceStatusRequestType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.Weather;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.WeatherDay;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status.WeatherReport;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class SonyWena3DeviceSupport extends AbstractBTLEDeviceSupport {
    private static final Logger LOG = LoggerFactory.getLogger(SonyWena3DeviceSupport.class);
    private String lastMusicInfo = null;
    public SonyWena3DeviceSupport() {
        super(LoggerFactory.getLogger(SonyWena3DeviceSupport.class));
        addSupportedService(SonyWena3Constants.COMMON_SERVICE_UUID);
        addSupportedService(SonyWena3Constants.NOTIFICATION_SERVICE_UUID);
    }
    @Override
    public boolean useAutoConnect() {
        return true;
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        // Sync current time to device
        sendCurrentTime(builder);

        // Sync camera mode to device
        builder.write(
                getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                CameraAppTypeSetting.findOut(getContext().getPackageManager()).toByteArray()
        );

        // Get battery state
        builder.read(getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_STATE_UUID));

        // Subscribe to updates
        builder.notify(getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_STATE_UUID), true);
        builder.notify(getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID), true);
        builder.notify(getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_INFO_UUID), true);
        builder.notify(getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_MODE_UUID), true);
        builder.notify(getCharacteristic(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID), true);


        // TODO: init and all

        getDevice().setFirmwareVersion("???");
        getDevice().setFirmwareVersion2("??2");


        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
        return builder;
    }

    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if(characteristic.getUuid().equals(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_STATE_UUID)) {
            DeviceStateInfo stateInfo = new DeviceStateInfo(characteristic.getValue());
            getDevice().setBatteryLevel(stateInfo.batteryPercentage);
            return true;
        }
        else if (characteristic.getUuid().equals(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID)) {
            NotificationServiceStatusRequest request = new NotificationServiceStatusRequest(characteristic.getValue());
            if(request.requestType == NotificationServiceStatusRequestType.MUSIC_INFO_FETCH.value) {
                LOG.debug("Request for music info received");
                sendMusicInfo(lastMusicInfo);
                return true;
            }
            else if(request.requestType == NotificationServiceStatusRequestType.LOCATE_PHONE.value) {
                LOG.debug("Request for find phone received");
                GBDeviceEventFindPhone findPhoneEvent = new GBDeviceEventFindPhone();
                findPhoneEvent.event = GBDeviceEventFindPhone.Event.START;
                evaluateGBDeviceEvent(findPhoneEvent);
                return true;
            }
            else {
                LOG.warn("Unknown NotificationServiceStatusRequest %d", request.requestType);
            }
        }
        return false;
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if(characteristic.getUuid().equals(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_STATE_UUID)) {
            DeviceStateInfo stateInfo = new DeviceStateInfo(characteristic.getValue());
            getDevice().setBatteryLevel(stateInfo.batteryPercentage);
            return true;
        }
        return false;
    }

    private void sendCurrentTime(@Nullable TransactionBuilder b) {
        try {
            TransactionBuilder builder = b == null ? performInitialized("updateDateTime") : b;

            TimeZone tz = TimeZone.getDefault();
            Date currentTime = new Date();

            builder.write(
                    getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                    new TimeSetting(currentTime).toByteArray()
            );

            builder.write(
                    getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                    new TimeZoneSetting(tz, currentTime).toByteArray()
            );

            if(b == null) performImmediately(builder);
        } catch (IOException e) {
            LOG.warn("Unable to send current time", e);
        }
    }

    private void sendMusicInfo(@Nullable String musicInfo) {
        try {
            TransactionBuilder builder = performInitialized("updateMusic");

            builder.write(
                    getCharacteristic(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID),
                    new MusicInfo(musicInfo != null ? musicInfo: "").toByteArray()
            );

            performImmediately(builder);
        } catch (IOException e) {
            LOG.warn("Unable to send music info", e);
        }
    }

    private void sendWeatherInfo(WeatherReport weather, @Nullable TransactionBuilder b) {
        try {
            TransactionBuilder builder = b == null ? performInitialized("updateWeather") : b;

            builder.write(
                    getCharacteristic(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID),
                    weather.toByteArray()
            );

            if(b == null) performImmediately(builder);
        } catch (IOException e) {
            LOG.warn("Unable to send current weather", e);
        }
    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {
        StringBuilder sb = new StringBuilder();
        boolean hasTrackName = musicSpec.track != null && musicSpec.track.trim().length() > 0;
        boolean hasArtistName = musicSpec.artist != null && musicSpec.artist.trim().length() > 0;

        if(hasTrackName) {
            sb.append(musicSpec.track.trim());
        }
        if(hasArtistName && hasArtistName) {
            sb.append(" / ");
        }
        if(hasArtistName) {
            sb.append(musicSpec.artist.trim());
        }

        lastMusicInfo = sb.toString();
        sendMusicInfo(lastMusicInfo);
    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {
        if(stateSpec.state == MusicStateSpec.STATE_PLAYING && lastMusicInfo != null) {
            sendMusicInfo(lastMusicInfo);
        } else if (stateSpec.state == MusicStateSpec.STATE_STOPPED || stateSpec.state == MusicStateSpec.STATE_PAUSED) {
            lastMusicInfo = "";
            sendMusicInfo("");
        }
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
        try {
            TransactionBuilder builder = performInitialized("sendNotify");

            StringBuilder bodyBuilder = new StringBuilder();

            if(notificationSpec.sender != null && notificationSpec.sender.length() > 0) {
                bodyBuilder.append(notificationSpec.sender);
                bodyBuilder.append(":");
            }

            if(notificationSpec.title != null && notificationSpec.title.length() > 0) {
                if(bodyBuilder.length() > 0) {
                    bodyBuilder.append("\n");
                }
                bodyBuilder.append("[ ");
                bodyBuilder.append(notificationSpec.title);
                bodyBuilder.append(" ]");
            }

            if(notificationSpec.subject != null && notificationSpec.subject.length() > 0) {
                if(bodyBuilder.length() > 0) {
                    bodyBuilder.append("\n");
                }
                bodyBuilder.append("- ");
                bodyBuilder.append(notificationSpec.subject);
            }

            if(notificationSpec.body != null) {
                if(bodyBuilder.length() > 0) {
                    bodyBuilder.append("\n");
                }
                bodyBuilder.append(notificationSpec.body);
            }

            String actionLabel = notificationSpec.attachedActions.isEmpty() ? "" :
                    notificationSpec.attachedActions.get(0).title;

            boolean hasAction = !notificationSpec.attachedActions.isEmpty();

            NotificationFlags flags = NotificationFlags.NONE;
            // TODO: Figure out how actions work

            builder.write(
                    getCharacteristic(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID),
                    new NotificationArrival(
                            NotificationKind.APP,
                            notificationSpec.getId(),
                            notificationSpec.sourceName,
                            bodyBuilder.toString(),
                            actionLabel,
                            new Date(notificationSpec.when),
                            new VibrationOptions(VibrationKind.STEP_UP, 1, false),
                            LedColor.GREEN,
                            flags
                    ).toByteArray()
            );

            performImmediately(builder);
        } catch (IOException e) {
            LOG.warn("Unable to send notification", e);
        }
    }

    @Override
    public void onDeleteNotification(int id) {
        try {
            TransactionBuilder builder = performInitialized("delNotify");

            builder.write(
                    getCharacteristic(SonyWena3Constants.NOTIFICATION_SERVICE_CHARACTERISTIC_UUID),
                    new NotificationRemoval(NotificationKind.APP, id).toByteArray()
            );

            performImmediately(builder);
        } catch (IOException e) {
            LOG.warn("Unable to send notification", e);
        }
    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {
        if(weatherSpec.forecasts.size() < 4) return;

        ArrayList<WeatherDay> days = new ArrayList<>();
        // Add today
        days.add(
                new WeatherDay(
                        Weather.fromOpenWeatherMap(weatherSpec.currentConditionCode),
                        Weather.fromOpenWeatherMap(weatherSpec.currentConditionCode),
                        weatherSpec.todayMaxTemp,
                        weatherSpec.todayMinTemp
                )
        );

        // Add other days
        for(int i = 0; i < 4; i++) {
            days.add(WeatherDay.fromSpec(weatherSpec.forecasts.get(i)));
        }

        WeatherReport report = new WeatherReport(days);
        sendWeatherInfo(report, null);
    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {
        try {
            Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()));
            TransactionBuilder builder = performInitialized("alarmSetting");

            assert alarms.size() <= SonyWena3Constants.ALARM_SLOTS;

            int wakeupMargin = prefs.getInt(SonyWena3SettingKeys.SMART_WAKEUP_MARGIN_MINUTES,
                    SonyWena3Constants.ALARM_DEFAULT_SMART_WAKEUP_MARGIN_MINUTES);

            for(
                int i = 0;
                i < SonyWena3Constants.ALARM_SLOTS;
                i += AlarmListSettings.MAX_ALARMS_IN_PACKET
            ) {
                AlarmListSettings pkt = new AlarmListSettings(new ArrayList<>(), i);

                for(int j = 0; j < AlarmListSettings.MAX_ALARMS_IN_PACKET; j++) {
                    if(i + j < alarms.size()) {
                        Alarm alarm = alarms.get(i + j);
                        SingleAlarmSetting sas = new SingleAlarmSetting(
                                alarm.getEnabled(),
                                (byte) alarm.getRepetition(),
                                alarm.getSmartWakeup() ? wakeupMargin : 0,
                                alarm.getHour(),
                                alarm.getMinute()
                        );
                        pkt.alarms.add(sas);
                    }
                }

                builder.write(
                        getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                        pkt.toByteArray()
                );
            }

            performImmediately(builder);
        } catch (IOException e) {
            LOG.warn("Unable to send alarms", e);
            GB.toast("Failed to save alarms", Toast.LENGTH_SHORT, GB.ERROR);
        }
    }

    private void sendDisplaySettings(TransactionBuilder b) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()));

        String localeString = prefs.getString(DeviceSettingsPreferenceConst.PREF_LANGUAGE, DeviceSettingsPreferenceConst.PREF_LANGUAGE_AUTO);
        if (localeString == null || localeString.equals(DeviceSettingsPreferenceConst.PREF_LANGUAGE_AUTO)) {
            String language = Locale.getDefault().getLanguage();
            String country = Locale.getDefault().getCountry();

            if (country == null) {
                country = language;
            }
            localeString = language + "_" + country.toUpperCase();
        }
        LOG.info("Setting device to locale: " + localeString);

        Language languageCode = Language.ENGLISH;

        switch (localeString.substring(0, 2)) {
            case "en":
                languageCode = Language.ENGLISH;
                break;
            case "ja":
                languageCode = Language.JAPANESE;
                break;
        }
        LOG.info("Resolved locale: %d", languageCode.ordinal());

        DisplaySetting pkt = new DisplaySetting(
                prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_SCREEN_LIFT_WRIST, false),
                languageCode,
                prefs.getInt(DeviceSettingsPreferenceConst.PREF_SCREEN_TIMEOUT, 5),
                (prefs.getString(DeviceSettingsPreferenceConst.PREF_WEARLOCATION, "left")
                        .equals("left") ? DisplayOrientation.LEFT_HAND : DisplayOrientation.RIGHT_HAND),
                (prefs.getBoolean(SonyWena3SettingKeys.RICH_DESIGN_MODE, false) ? DisplayDesign.RICH : DisplayDesign.NORMAL),
                (prefs.getBoolean(SonyWena3SettingKeys.LARGE_FONT_SIZE, false) ? FontSize.LARGE : FontSize.NORMAL),
                prefs.getBoolean(SonyWena3SettingKeys.WEATHER_IN_STATUSBAR, true)
        );

        b.write(
                getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                pkt.toByteArray()
        );
    }

    private void sendDnDSettings(TransactionBuilder b) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()));
        String dndMode = prefs.getString(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_NOAUTO, DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_OFF);
        boolean isDndOn = (dndMode != null && dndMode.equals(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_SCHEDULED));
        String start = prefs.getString(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_NOAUTO_START, "22:00");
        String end = prefs.getString(DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_NOAUTO_END, "06:00");

        Calendar startCalendar = GregorianCalendar.getInstance();
        Calendar endCalendar = GregorianCalendar.getInstance();
        DateFormat df = new SimpleDateFormat("HH:mm");

        try {
            startCalendar.setTime(df.parse(start));
            endCalendar.setTime(df.parse(end));
        } catch (ParseException e) {
            LOG.error("settings error: " + e);
        }

        byte startH = (byte)startCalendar.get(Calendar.HOUR_OF_DAY);
        byte startM = (byte)startCalendar.get(Calendar.MINUTE);
        byte endH = (byte)endCalendar.get(Calendar.HOUR_OF_DAY);
        byte endM = (byte)endCalendar.get(Calendar.MINUTE);

        DoNotDisturbSettings dndPkt = new DoNotDisturbSettings(isDndOn, startH, startM, endH, endM);
        b.write(
                getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                dndPkt.toByteArray()
        );
    }

    private void sendVibrationSettings(TransactionBuilder b) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()));
        boolean smartVibration = prefs.getBoolean(SonyWena3SettingKeys.SMART_VIBRATION, true);
        VibrationStrength strength = VibrationStrength.fromInt(prefs.getInt(SonyWena3SettingKeys.VIBRATION_STRENGTH, 0));
        VibrationSetting pkt = new VibrationSetting(smartVibration, strength);

        b.write(
                getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                pkt.toByteArray()
        );
    }

    private void sendHomeScreenSettings(TransactionBuilder b) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()));
        int leftId = prefs.getInt(SonyWena3SettingKeys.LEFT_HOME_ICON, 4864);
        int centerId = prefs.getInt(SonyWena3SettingKeys.CENTER_HOME_ICON, 2560);
        int rightId = prefs.getInt(SonyWena3SettingKeys.RIGHT_HOME_ICON, 4352);

        b.write(
                getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                new HomeIconOrderSetting(
                        new HomeIconId(leftId),
                        new HomeIconId(centerId),
                        new HomeIconId(rightId)
                ).toByteArray()
        );
    }

    private void sendMenuSettings(TransactionBuilder b) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()));
        MenuIconSetting menu = new MenuIconSetting();
        for(int i = 0; i < SonyWena3SettingKeys.MAX_MENU_ICONS; i++) {
            int id = prefs.getInt(SonyWena3SettingKeys.menuIconKeyFor(i), 0);
            if(id != 0) {
                menu.iconList.add(new MenuIconId(id));
            }
        }

        b.write(
                getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                menu.toByteArray()
        );
    }

    private void sendStatusPageSettings(TransactionBuilder b) {
        Prefs prefs = new Prefs(GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress()));
        StatusPageOrderSetting pageOrderSetting = new StatusPageOrderSetting();
        for(int i = 0; i < SonyWena3SettingKeys.MAX_STATUS_PAGES; i++) {
            int id = prefs.getInt(SonyWena3SettingKeys.statusPageKeyFor(i), 0);
            if(id != 0) {
                pageOrderSetting.pages.add(new StatusPageId(id));
            }
        }

        b.write(
                getCharacteristic(SonyWena3Constants.COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID),
                pageOrderSetting.toByteArray()
        );
    }


    @Override
    public void onSendConfiguration(String config) {
        try {
            TransactionBuilder builder = performInitialized("sendConfig");
            if(config.startsWith(SonyWena3SettingKeys.MENU_ICON_KEY_PREFIX)) {
                sendMenuSettings(builder);
            } else if(config.startsWith(SonyWena3SettingKeys.STATUS_PAGE_KEY_PREFIX)) {
                sendStatusPageSettings(builder);
            } else switch (config) {
                case DeviceSettingsPreferenceConst.PREF_SCREEN_LIFT_WRIST:
                case DeviceSettingsPreferenceConst.PREF_LANGUAGE:
                case DeviceSettingsPreferenceConst.PREF_SCREEN_TIMEOUT:
                case DeviceSettingsPreferenceConst.PREF_WEARLOCATION:
                case SonyWena3SettingKeys.RICH_DESIGN_MODE:
                case SonyWena3SettingKeys.LARGE_FONT_SIZE:
                case SonyWena3SettingKeys.WEATHER_IN_STATUSBAR:
                    sendDisplaySettings(builder);
                    break;

                case SonyWena3SettingKeys.SMART_WAKEUP_MARGIN_MINUTES:
                    // Resend alarms
                    GBApplication.deviceService(gbDevice).onSetAlarms(new ArrayList<>(DBHelper.getAlarms(gbDevice)));
                    break;

                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_NOAUTO:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_NOAUTO_END:
                case DeviceSettingsPreferenceConst.PREF_DO_NOT_DISTURB_NOAUTO_START:
                    sendDnDSettings(builder);
                    break;

                case SonyWena3SettingKeys.VIBRATION_STRENGTH:
                case SonyWena3SettingKeys.SMART_VIBRATION:
                    sendVibrationSettings(builder);
                    break;

                case SonyWena3SettingKeys.LEFT_HOME_ICON:
                case SonyWena3SettingKeys.CENTER_HOME_ICON:
                case SonyWena3SettingKeys.RIGHT_HOME_ICON:
                    sendHomeScreenSettings(builder);
                    break;

                default:
                    LOG.warn("Unsupported setting %s", config);
                    return;
            }

            performImmediately(builder);
        } catch(Exception e) {
            LOG.warn("Failed to update settings");
        }
    }
}
