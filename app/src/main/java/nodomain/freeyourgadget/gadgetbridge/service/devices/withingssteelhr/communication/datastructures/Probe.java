package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class Probe extends WithingsStructure {

    private short os;

    private short app;

    private long version;

    public Probe(short os, short app, long version) {
        this.os = os;
        this.app = app;
        this.version = version;
    }

    @Override
    public short getLength() {
        return 10;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.put((byte) (os & 255));
        buffer.put((byte) (app & 255));
        buffer.putInt((int) (version & 4294967295L));
    }

    @Override
    public short getType() {
        return WithingsStructureType.PROBE;
    }
}
