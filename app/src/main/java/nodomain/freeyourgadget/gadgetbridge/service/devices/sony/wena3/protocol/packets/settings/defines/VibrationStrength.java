package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines;

public enum VibrationStrength {
    NORMAL,
    WEAK,
    STRONG;

    public static VibrationStrength fromInt(int value) {
        if(value == WEAK.ordinal()) return WEAK;
        else if (value == STRONG.ordinal()) return STRONG;

        return NORMAL;
    }
}
