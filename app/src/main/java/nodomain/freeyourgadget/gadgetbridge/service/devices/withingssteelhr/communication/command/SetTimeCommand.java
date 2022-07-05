package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command;

public class SetTimeCommand extends AbstractCommand {

    @Override
    public short getType() {
        return WithingsCommandTypes.SET_TIME;
    }
}
