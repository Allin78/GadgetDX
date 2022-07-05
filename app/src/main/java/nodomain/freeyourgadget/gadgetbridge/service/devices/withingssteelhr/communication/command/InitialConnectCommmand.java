package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command;

public class InitialConnectCommmand extends AbstractCommand {
    @Override
    public short getType() {
        return WithingsCommandTypes.INITIAL_CONNECT;
    }
}
