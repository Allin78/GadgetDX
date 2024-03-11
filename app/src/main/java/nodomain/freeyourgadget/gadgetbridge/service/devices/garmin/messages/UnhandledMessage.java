package nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.messages;

public class UnhandledMessage extends GFDIMessage {

    private final int messageType;

    public UnhandledMessage(int messageType) {
        this.messageType = messageType;

        this.statusMessage = new GenericStatusMessage(messageType, Status.UNSUPPORTED);

    }

    @Override
    protected boolean generateOutgoing() {
        return false;
    }
}
