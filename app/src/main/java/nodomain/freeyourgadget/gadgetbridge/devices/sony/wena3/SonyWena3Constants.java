package nodomain.freeyourgadget.gadgetbridge.devices.sony.wena3;

import java.util.UUID;

public class SonyWena3Constants {
    public static String BT_DEVICE_NAME = "WNW-21A";
    private static String uuidTemplate = "4EFD%s-A6C1-16F0-062F-F196CF496695";

    public static UUID COMMON_SERVICE_UUID = UUID.fromString(String.format(uuidTemplate, "1501"));
    public static UUID COMMON_SERVICE_CHARACTERISTIC_MODE_UUID = UUID.fromString(String.format(uuidTemplate, "1503"));
    public static UUID COMMON_SERVICE_CHARACTERISTIC_CONTROL_UUID = UUID.fromString(String.format(uuidTemplate, "1514"));
    public static UUID COMMON_SERVICE_CHARACTERISTIC_INFO_UUID = UUID.fromString(String.format(uuidTemplate, "1520"));
    public static UUID COMMON_SERVICE_CHARACTERISTIC_STATE_UUID = UUID.fromString(String.format(uuidTemplate, "1521"));

    public static UUID NOTIFICATION_SERVICE_UUID = UUID.fromString(String.format(uuidTemplate, "4001"));
    public static UUID NOTIFICATION_SERVICE_CHARACTERISTIC_TX_UUID = UUID.fromString(String.format(uuidTemplate, "4002"));

}
