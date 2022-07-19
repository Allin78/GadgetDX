package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

/**
 * Contains all identified commandtypes in the used TLV format of the messages exchanged
 * between device and app.
 */
public final class WithingsMessageTypes {

    public static final short SET_TIME = 1281;
    public static final short GET_BATTERY_STATUS = 1284;
    public static final short SET_WORKOUT_LIST = 1292;
    public static final short INITIAL_CONNECT = 273;
    public static final short START_HANDS_CALIBRATION = 286;
    public static final short STOP_HANDS_CALIBRATION = 287;
    public static final short SET_ACTIVITY_TARGET = 1290;
    public static final short SET_USER = 1282;
    public static final short SET_USER_UNIT = 274;
    public static final short SET_LOCALE = 282;
    public static final short SETUP_FINISHED = 275;
    public static final short GET_LIVE_HR = 2376;
    public static final short LIVE_HR = (short) -32448;
    public static final short GET_HR = 2343;
    public static final short SYNC = 16705;
    public static final short SYNC_OK = 277;
    public static final short GET_ALARM_SETTINGS = 298;
    public static final short SET_ALARM = 325;
    public static final short START_ALARM_SETTING = 293;
    public static final short ENABLE_ALARM = 2330;
    public static final short GET_ANCS_STATUS = 2353;
    public static final short SET_ANCS_STATUS = 2345;
    public static final short GET_SCREEN_SETTINGS = 1293;
    public static final short GET_ACTIVITY_SAMPLES = 2424;

    private WithingsMessageTypes() {}
}
