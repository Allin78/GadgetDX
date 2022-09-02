package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ImageData extends WithingsStructure {

    byte [] imageData;

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    @Override
    public short getLength() {
        return imageData != null ? (short)(imageData.length + 1 + HEADER_SIZE) : 1 + HEADER_SIZE;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        if (imageData != null) {
            addByteArrayWithLengthByte(buffer, imageData);
        } else {
            addByteArrayWithLengthByte(buffer, new byte[0]);
        }
    }

    @Override
    public short getType() {
        return WithingsStructureType.IMAGE_DATA;
    }
}
