package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class DayStartHourSetting implements Wena3Packetable {
    public int dayStartHour;

    public DayStartHourSetting(int dayStartHour) {
        this.dayStartHour = dayStartHour;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[] {
            0x16, (byte) dayStartHour
        };
    }
}
