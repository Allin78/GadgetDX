package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;

public class WithingsMessage extends AbstractMessage {
    private short type;
    private ExpectedResponse expectedResponse = ExpectedResponse.SIMPLE;
    private boolean isIncoming;

    public WithingsMessage(short type) {
        this.type = type;
    }

    public WithingsMessage(short type, boolean incoming) {
        this.type = type;
        this.isIncoming = incoming;
    }

    public WithingsMessage(short type, ExpectedResponse expectedResponse) {
        this.type = type;
        this.expectedResponse = expectedResponse;
    }

    public WithingsMessage(short type, WithingsStructure dataStructure) {
        this.type = type;
        this.addDataStructure(dataStructure);
    }

    @Override
    public boolean needsResponse() {
        return expectedResponse == ExpectedResponse.SIMPLE;
    }

    @Override
    public boolean needsEOT() {
        return expectedResponse == ExpectedResponse.EOT;
    }

    @Override
    public short getType() {
        return type;
    }

    @Override
    public boolean isIncomingMessage() {
        return isIncoming;
    }
}
