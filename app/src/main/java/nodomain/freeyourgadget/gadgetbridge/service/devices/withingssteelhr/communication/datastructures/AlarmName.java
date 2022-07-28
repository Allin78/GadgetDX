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
        addStringAsBytesWithLengthByte(rawDataBuffer, name);
    }

    @Override
    public short getType() {
        return 0;
    }
}
