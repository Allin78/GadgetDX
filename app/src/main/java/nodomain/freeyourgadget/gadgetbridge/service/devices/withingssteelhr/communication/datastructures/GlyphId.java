package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GlyphId extends WithingsStructure {

    private long unicode;

    public long getUnicode() {
        return unicode;
    }

    @Override
    public short getLength() {
        return 8;
    }

    @Override
    protected void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        int value = rawDataBuffer.getInt();
        unicode = ByteBuffer.allocate(4).putInt(value).order(ByteOrder.LITTLE_ENDIAN).getInt(0);
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public short getType() {
        return WithingsStructureType.GLYPH_ID;
    }
}
