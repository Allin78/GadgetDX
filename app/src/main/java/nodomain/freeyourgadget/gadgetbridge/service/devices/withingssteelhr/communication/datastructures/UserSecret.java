package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class UserSecret extends WithingsStructure {

    private String secret = "2EM5zNP37QzM00hmP6BFTD92nG15XwNd";

    @Override
    public short getLength() {
        return 37;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        addStringAsBytesWithLengthByte(buffer, secret);
    }

    @Override
    public short getType() {
        return WithingsStructureType.USER_SECRET;
    }
}
