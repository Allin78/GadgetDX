package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.command;

/**
 * Contains all identified commandtypes in the used TLV format of the messages exchanged
 * between device and app.
 */
public final class WithingsCommandTypes {

    public static final short SET_TIME = 1281;

    public static final short GET_BATTERY_STATUS = 1284;

    public static final short SET_WORKOUT_LIST = 1292;

    public static final short INITIAL_CONNECT = 273;

    public static final short START_HANDS_CALIBRATION = 286;

    public static final short STOP_HANDS_CALIBRATION = 287;

    private WithingsCommandTypes() {}
}
