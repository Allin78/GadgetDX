package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command;

public class SimpleCommand extends AbstractCommand {
    private short type;

    public SimpleCommand(short type) {
        this.type = type;
    }

    @Override
    public short getType() {
        return type;
    }
}
