package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ImageMetaData extends WithingsStructure {

    private byte unknown = 0x00;
    private byte width;
    private byte height;

    public byte getWidth() {
        return width;
    }

    public void setWidth(byte width) {
        this.width = width;
    }

    public byte getHeight() {
        return height;
    }

    public void setHeight(byte height) {
        this.height = height;
    }

    @Override
    public short getLength() {
        return 7;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put(unknown);
        buffer.put(width);
        buffer.put(height);
    }

    @Override
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        unknown = rawDataBuffer.get();
        width = rawDataBuffer.get();
        height = rawDataBuffer.get();
    }

    @Override
    public short getType() {
        return WithingsStructureType.IMAGE_META_DATA;
    }
}
