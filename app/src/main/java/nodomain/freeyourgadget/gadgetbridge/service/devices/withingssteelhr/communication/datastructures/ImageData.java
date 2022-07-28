package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ImageData extends WithingsStructure {

    byte [] imageData;

    @Override
    public short getLength() {

        return imageData != null ? (short)imageData.length : 0;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put(imageData);
    }

    @Override
    public short getType() {
        return WithingsStructureType.IMAGE_DATA;
    }
}
