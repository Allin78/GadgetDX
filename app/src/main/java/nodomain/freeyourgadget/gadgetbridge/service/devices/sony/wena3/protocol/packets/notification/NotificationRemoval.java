package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.NotificationKind;

public class NotificationRemoval implements Wena3Packetable {
    public NotificationKind kind;
    public int id;

    public NotificationRemoval(NotificationKind kind, int id) {
        this.kind = kind;
        this.id = id;
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer
                .allocate(7)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x01)
                .put((byte)kind.ordinal())
                .putInt(id)
                .array();
    }
}
