package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ActivitySampleMovement extends WithingsStructure {

    private short steps;
    private int distance;
    private int asc;
    private int desc;

    public short getSteps() {
        return steps;
    }

    public void setSteps(short steps) {
        this.steps = steps;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public short getLength() {
        return 18;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.putShort(steps);
        buffer.putInt(distance);
        buffer.putInt(asc);
        buffer.putInt(desc);
    }

    @Override
    public void fillFromRawDataAsBuffer(ByteBuffer rawDataBuffer) {
        steps = rawDataBuffer.getShort();
        distance = rawDataBuffer.getInt();
        asc = rawDataBuffer.getInt();
        desc = rawDataBuffer.getInt();
    }

    @Override
    public short getType() {
        return WithingsStructureType.ACTIVITY_SAMPLE_MOVEMENT;
    }
}
