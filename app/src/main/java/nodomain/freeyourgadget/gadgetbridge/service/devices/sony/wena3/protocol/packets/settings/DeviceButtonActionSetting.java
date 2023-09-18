package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.DeviceButtonActionId;

public class DeviceButtonActionSetting implements Wena3Packetable {
    public DeviceButtonActionId longPress;
    public DeviceButtonActionId doubleClick;

    public DeviceButtonActionSetting(DeviceButtonActionId longPress, DeviceButtonActionId doubleClick) {
        this.longPress = longPress;
        this.doubleClick = doubleClick;
    }

    @Override
    public byte[] toByteArray() {
        return ByteBuffer.allocate(5)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte)0x1C)
                .putShort(longPress.value)
                .putShort(doubleClick.value)
                .array();
    }
}
