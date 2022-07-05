package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command;

public class SetWorkoutListCommand extends AbstractCommand {

    @Override
    public short getType() {
        return WithingsCommandTypes.SET_WORKOUT_LIST;
    }
}
