package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.MenuIconId;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.StatusPageId;

public class StatusPageOrderSetting implements Wena3Packetable {
    public List<StatusPageId> pages = new ArrayList<>();

    public StatusPageOrderSetting() {}

    @Override
    public byte[] toByteArray() {
        ByteBuffer buf = ByteBuffer.allocate(1 + pages.size()).order(ByteOrder.LITTLE_ENDIAN);
        buf.put((byte) 0x18);
        for(StatusPageId id : pages) {
            buf.put(id.value);
        }
        return buf.array();
    }
}
