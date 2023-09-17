package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class SingleAlarmSetting implements Wena3Packetable {
    public boolean enable;
    // Bitmask: See model.Alarm.ALARM_MON, TUE, ...
    public byte repetition;
    public int smartAlarmMargin;
    public int hour;
    public int minute;

    public SingleAlarmSetting(boolean enable, byte repetition, int smartAlarmMargin, int hour, int minute) {
        this.enable = enable;
        this.repetition = repetition;
        this.smartAlarmMargin = smartAlarmMargin;
        this.hour = hour;
        this.minute = minute;
    }

    // NB: normally this never occurs on the wire
    //     outside of an AlarmListSettings packet!
    @Override
    public byte[] toByteArray() {
        // For some reason their bitmask starts on Sunday!
        // So this brings it in line with what Gadgetbridge expects...
        byte newRepetition = (byte) ((((repetition & ~Alarm.ALARM_SUN) << 1) | ((repetition & Alarm.ALARM_SUN) >> 6)) & 0xFF);
        return ByteBuffer.allocate(5)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) (enable ? 0x2 : 0x1)) // 0x0 means no object...
                .put(newRepetition)
                .put((byte) smartAlarmMargin)
                .put((byte) hour)
                .put((byte) minute)
                .array();
    }

    public static byte[] emptyPacket() {
        return ByteBuffer.allocate(5)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(new byte[] {0, 0, 0, 0, 0})
                .array();
    }
}
