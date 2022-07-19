package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;

public class WithingsMessage extends AbstractMessage {
    private short type;
    private boolean needsResponse = true;

    public WithingsMessage(short type) {
        this.type = type;
    }

    public WithingsMessage(short type, boolean needsResponse) {
        this.type = type;
        this.needsResponse = needsResponse;
    }

    public WithingsMessage(short type, WithingsStructure dataStructure) {
        this.type = type;
        this.addDataStructure(dataStructure);
    }

    @Override
    public boolean needsResponse() {
        return needsResponse;
    }

    @Override
    public short getType() {
        return type;
    }
}
