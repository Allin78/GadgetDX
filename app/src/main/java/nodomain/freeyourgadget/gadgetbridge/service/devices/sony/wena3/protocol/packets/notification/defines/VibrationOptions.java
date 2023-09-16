package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines;

public class VibrationOptions {
    public VibrationKind kind;
    public int count;
    public boolean continuous;

    public VibrationOptions(VibrationKind kind, int count, boolean continuous) {
        this.kind = kind;
        this.count = count;
        this.continuous = continuous;
    }
}

