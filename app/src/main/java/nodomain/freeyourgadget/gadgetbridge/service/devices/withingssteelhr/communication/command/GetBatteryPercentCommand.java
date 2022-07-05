package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command;

public class GetBatteryPercentCommand extends AbstractCommand {

    @Override
    public short getType() {
        return WithingsCommandTypes.GET_BATTERY_STATUS;
    }
}
