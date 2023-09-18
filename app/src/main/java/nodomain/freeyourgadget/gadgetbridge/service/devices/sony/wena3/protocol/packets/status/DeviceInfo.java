package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DeviceInfo {
    public String firmwareName;
    public String serialNo;

    public DeviceInfo(byte[] packet) {
        ByteBuffer buf = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN);
        byte[] fwNameStr = new byte[8];
        buf.get(fwNameStr);
        serialNo = Integer.toString(buf.getInt());
        firmwareName = new String(fwNameStr, StandardCharsets.UTF_8);
    }
}
