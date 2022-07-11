package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AlarmName extends WithingsStructure {

    private String name;

    public AlarmName(String name) {
        this.name = name;
    }

    @Override
    public short getLength() {
        return (short) ((name != null ? name.getBytes().length : 0) + 1 + HEADER_SIZE);
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer rawDataBuffer) {
        if (name == null) {
            rawDataBuffer.put((byte)0);
        } else {
            byte[] localeAsBytes = name.getBytes(StandardCharsets.UTF_8);
            rawDataBuffer.put((byte)localeAsBytes.length);
            rawDataBuffer.put(localeAsBytes);
        }
    }

    @Override
    short getType() {
        return 0;
    }
}
