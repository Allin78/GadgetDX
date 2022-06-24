package nodomain.freeyourgadget.gadgetbridge.devices.msftband;

import java.util.UUID;

public class MsftBandConstants {

    //region Bluetooth classic
    public static final UUID UUID_PNP_INFORMATION = UUID.fromString("00001200-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SERVICE_00 = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final UUID UUID_1P_COMMAND_SERVICE_01_BT = UUID.fromString("a502ca97-2ba5-413c-a4e0-13804e47b38f");
    public static final UUID UUID_1P_COMMAND_SERVICE_02_BT = UUID.fromString("a502ca99-2ba5-413c-a4e0-13804e47b38f");
    public static final UUID UUID_3P_COMMAND_SERVICE_01_BT = UUID.fromString("a502ca9a-2ba5-413c-a4e0-13804e47b38f");
    public static final UUID UUID_3P_COMMAND_SERVICE_02_BT = UUID.fromString("a502ca9b-2ba5-413c-a4e0-13804e47b38f");
    public static final UUID UUID_1P_PUSH_SERVICE_BT = UUID.fromString("c742e1a2-6320-5abc-9643-d206c677e580");
    //endregion Bluetooth classic

    //region Bluetooth LowEnergy
    // 00001800-0000-1000-8000-00805f9b34fb Generic Access
    // 00001801-0000-1000-8000-00805f9b34fb Generic Attribute
    // 0000180a-0000-1000-8000-00805f9b34fb Device Information
    // 0000180f-0000-1000-8000-00805f9b34fb Battery Service

    public static final UUID UUID_1P_COMMAND_SERVICE_01_BLE = UUID.fromString("a502ca98-2ba5-413c-a4e0-13804e47b38f");
    public static final UUID UUID_1P_PUSH_SERVICE_BLE = UUID.fromString("0bad7fcc-2ee4-f1ac-439f-d7b2ba250294");
    public static final UUID UUID_IN_ONLY_SERVICE_BLE = UUID.fromString("0000feb2-0000-1000-8000-00805f9b34fb");

    //endregion Bluetooth LowEnergy

}
