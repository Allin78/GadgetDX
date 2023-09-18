package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines;

public enum LedColor {
    NONE,
    RED,
    YELLOW,
    GREEN,
    CYAN,
    BLUE,
    PURPLE,
    WHITE;

    public static final LedColor[] LUT = new LedColor[] {
            NONE,
            RED,
            YELLOW,
            GREEN,
            CYAN,
            BLUE,
            PURPLE,
            WHITE
    };
}

