package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class DisplaySetting implements Wena3Packetable {
    public boolean displayOnRaiseWrist;
    public Language language;
    public int displayDuration;
    public DisplayOrientation orientation;
    public DisplayDesign design;
    public FontSize fontSize;
    public boolean weatherInStatusBar;

    public DisplaySetting(boolean displayOnRaiseWrist, Language language, int displayDuration, DisplayOrientation orientation, DisplayDesign design, FontSize fontSize, boolean weatherInStatusBar) {
        this.displayOnRaiseWrist = displayOnRaiseWrist;
        this.language = language;
        this.displayDuration = displayDuration;
        this.orientation = orientation;
        this.design = design;
        this.fontSize = fontSize;
        this.weatherInStatusBar = weatherInStatusBar;
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte)0x1A)
                .put((byte) fontSize.ordinal())
                .put((byte) orientation.ordinal())
                .put((byte) language.ordinal())
                .put((byte) (displayOnRaiseWrist ? 0x1 : 0x0))
                .put((byte) design.ordinal())
                .put((byte) (weatherInStatusBar ? 0x1 : 0x0))
                .put((byte) displayDuration)
                .array();
    }
}

