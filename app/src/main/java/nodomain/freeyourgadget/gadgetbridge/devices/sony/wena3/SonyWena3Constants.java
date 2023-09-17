package nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3;

import android.graphics.Color;

import java.util.UUID;

public class SonyWena3Constants {
    public static final String BT_DEVICE_NAME = "WNW-21A";
    private static final String uuidTemplate = "4EFD%s-A6C1-16F0-062F-F196CF496695";

    public static final UUID COMMON_SERVICE_UUID = UUID.fromString(String.format(uuidTemplate, "1501"));
    public static final UUID COMMON_SERVICE_CHARACTERISTIC_MODE_UUID = UUID.fromString(String.format(uuidTemplate, "1503"));
    public static final UUID COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID = UUID.fromString(String.format(uuidTemplate, "1514"));
    public static final UUID COMMON_SERVICE_CHARACTERISTIC_INFO_UUID = UUID.fromString(String.format(uuidTemplate, "1520"));
    public static final UUID COMMON_SERVICE_CHARACTERISTIC_STATE_UUID = UUID.fromString(String.format(uuidTemplate, "1521"));

    public static final UUID NOTIFICATION_SERVICE_UUID = UUID.fromString(String.format(uuidTemplate, "4001"));
    public static final UUID NOTIFICATION_SERVICE_CHARACTERISTIC_UUID = UUID.fromString(String.format(uuidTemplate, "4002"));

    public static int[] LED_PRESETS = {
            Color.rgb(255, 0, 0),
            Color.rgb(255, 255, 0),
            Color.rgb(0, 255, 0),
            Color.rgb(0, 255, 255),
            Color.rgb(0, 0, 255),
            Color.rgb(255, 0, 255),
            Color.rgb(255, 255, 255)
    };

    public static final long EPOCH_START = 1577836800000L;

    public static final int ALARM_SLOTS = 9;
    public static final int ALARM_DEFAULT_SMART_WAKEUP_MARGIN_MINUTES = 10;
}

