package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class WorkoutScreenData extends WithingsStructure {

    public long id;
    public short version;
    public String name;
    public short faceMode;
    public int flag;

    @Override
    public short getLength() {
        return (short) ((name != null ? name.getBytes().length : 0) + 13);
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {

    }

    @Override
    protected void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        this.id = rawDataBuffer.getInt() & 4294967295L;
        this.version = (short) (rawDataBuffer.get() & 255);
        this.name = getNextString(rawDataBuffer);
        this.faceMode = (short) (rawDataBuffer.get() & 255);
        this.flag = rawDataBuffer.getShort() & 65535;
    }

    @Override
    public short getType() {
        return WithingsStructureType.WORKOUT_SCREEN_DATA;
    }
}
