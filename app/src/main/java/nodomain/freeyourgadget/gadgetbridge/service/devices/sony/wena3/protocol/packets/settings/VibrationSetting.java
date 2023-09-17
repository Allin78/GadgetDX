package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class VibrationSetting implements Wena3Packetable {
    public boolean smartVibration;
    public VibrationStrength strength;

    public VibrationSetting(boolean smartVibration, VibrationStrength strength) {
        this.smartVibration = smartVibration;
        this.strength = strength;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[] {
                0x08,
                (byte) strength.ordinal(),
                (byte) (smartVibration ? 0x01 : 0x00)
        };
    }
}

