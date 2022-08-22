package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

/**
 * Contains all identified commandtypes in the used TLV format of the messages exchanged
 * between device and app.
 */
public final class WithingsMessageTypes {

    public static final short PROBE = 257;
    public static final short CHALLENGE = 296;
    public static final short SET_TIME = 1281;
    public static final short GET_BATTERY_STATUS = 1284;
    public static final short SET_SCREEN_LIST = 1292;
    public static final short INITIAL_CONNECT = 273;
    public static final short START_HANDS_CALIBRATION = 286;
    public static final short STOP_HANDS_CALIBRATION = 287;
    public static final short SET_ACTIVITY_TARGET = 1290;
    public static final short SET_USER = 1282;
    public static final short GET_USER = 1283;
    public static final short SET_USER_UNIT = 274;
    public static final short SET_LOCALE = 282;
    public static final short SETUP_FINISHED = 275;
    public static final short GET_LIVE_HR = 2376;
    public static final short LIVE_HR = (short) -32448;
    public static final short GET_HR = 2343;
    public static final short GET_WORKOUT_SCREEN_LIST = 315;
    public static final short SYNC = 321;
    public static final short SYNC_RESPONSE = 16705;
    public static final short SYNC_OK = 277;
    public static final short GET_ALARM_SETTINGS = 298;
    public static final short SET_ALARM = 325;
    public static final short GET_ALARM = 293;
    public static final short GET_ALARM_ENABLED = 2330;
    public static final short GET_ANCS_STATUS = 2353;
    public static final short SET_ANCS_STATUS = 2345;
    public static final short GET_SCREEN_SETTINGS = 1293;
    // The next to do nearly the same, when I look at the responses, though only the first seems to deliver sleep samples
    public static final short GET_ACTIVITY_SAMPLES = 2424;
    public static final short GET_MOVEMENT_SAMPLES = 1286;

    public static final short GET_SPORT_MODE = 2371;
    public static final short GET_HEARTRATE_SAMPLES = 2344;

    private WithingsMessageTypes() {}
}
