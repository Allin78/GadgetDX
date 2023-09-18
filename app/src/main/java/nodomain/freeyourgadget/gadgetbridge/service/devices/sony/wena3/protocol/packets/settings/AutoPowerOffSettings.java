package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AutoPowerOffSettings extends DoNotDisturbSettings {

    public AutoPowerOffSettings(boolean enable, int startHour, int startMinute, int endHour, int endMinute) {
        super(enable, startHour, startMinute, endHour, endMinute);
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer.allocate(6)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x15)
                .put((byte) (enable ? 0x1 : 0x0))
                .put((byte) startHour)
                .put((byte) startMinute)
                .put((byte) endHour)
                .put((byte) endMinute)
                .array();
    }
}
