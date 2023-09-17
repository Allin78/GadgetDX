package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.settings.defines.MenuIconId;

public class MenuIconSetting implements Wena3Packetable {
    public List<MenuIconId> iconList = new ArrayList<>();

    public MenuIconSetting() {
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer buf = ByteBuffer.allocate(1 + iconList.size()).order(ByteOrder.LITTLE_ENDIAN);
        buf.put((byte) 0x19);
        for(MenuIconId id : iconList) {
            buf.put(id.value);
        }
        return buf.array();
    }
}
