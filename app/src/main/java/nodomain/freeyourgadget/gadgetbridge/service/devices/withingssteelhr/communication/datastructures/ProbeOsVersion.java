package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;

public class ProbeOsVersion extends WithingsStructure {

    private short osVersion;

    public ProbeOsVersion(short osVersion) {
        this.osVersion = osVersion;
    }

    @Override
    public short getLength() {
        return 6;
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer buffer) {
        buffer.putShort(osVersion);
    }

    @Override
    public short getType() {
        return WithingsStructureType.PROBE_OS_VERSION;
    }
}
