package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class HomeIconOrderSetting implements Wena3Packetable {
    public HomeIconId left;
    public HomeIconId center;
    public HomeIconId right;

    public HomeIconOrderSetting(HomeIconId left, HomeIconId center, HomeIconId right) {
        this.left = left;
        this.center = center;
        this.right = right;
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer.allocate(7)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x1B)
                .putShort(left.value)
                .putShort(center.value)
                .putShort(right.value)
                .array();
    }
}

