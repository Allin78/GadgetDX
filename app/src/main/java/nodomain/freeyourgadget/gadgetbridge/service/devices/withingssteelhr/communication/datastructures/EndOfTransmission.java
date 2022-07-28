package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class EndOfTransmission extends WithingsStructure {
    @Override
    public short getLength() {
        return 6;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    public short getType() {
        return WithingsStructureType.END_OF_TRANSMISSION;
    }
}
