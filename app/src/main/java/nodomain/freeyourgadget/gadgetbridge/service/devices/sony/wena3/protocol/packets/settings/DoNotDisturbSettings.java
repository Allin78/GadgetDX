package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class DoNotDisturbSettings implements Wena3Packetable {
    public boolean enable;
    public int startHour;
    public int startMinute;
    public int endHour;
    public int endMinute;

    public DoNotDisturbSettings(boolean enable, int startHour, int startMinute, int endHour, int endMinute) {
        this.enable = enable;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer.allocate(6)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x14)
                .put((byte) (enable ? 0x1 : 0x0))
                .put((byte) startHour)
                .put((byte) startMinute)
                .put((byte) endHour)
                .put((byte) endMinute)
                .array();
    }
}
