package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class EndOfTransmission extends WithingsStructure {
    @Override
    public short getLength() {
        return 4;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public byte[] getRawData() {
        ByteBuffer rawDataBuffer = ByteBuffer.allocate(4);
        rawDataBuffer.putShort(getType());
        rawDataBuffer.putShort((short)0);
        return rawDataBuffer.array();
    }

    @Override
    public short getType() {
        return WithingsStructureType.END_OF_TRANSMISSION;
    }
}
