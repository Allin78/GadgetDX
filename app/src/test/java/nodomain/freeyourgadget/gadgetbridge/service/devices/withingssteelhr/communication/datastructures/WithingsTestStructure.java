package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class WithingsTestStructure extends WithingsStructure {
    @Override
    public short getLength() {
        return 6;
    }

    @Override
    public byte[] getRawData() {
        ByteBuffer rawDataBuffer = ByteBuffer.allocate(getLength());
        rawDataBuffer.putShort(getType());
        rawDataBuffer.put("Test".getBytes(StandardCharsets.UTF_8));
        return rawDataBuffer.array();
    }

    @Override
    short getType() {
        return 99;
    }

    @Override
    void addSubStructure(WithingsStructure subStructure) {

    }
}
