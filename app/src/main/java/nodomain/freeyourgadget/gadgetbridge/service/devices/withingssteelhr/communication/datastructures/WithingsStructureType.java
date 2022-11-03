package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

public class WithingsStructureType {

    public static final short END_OF_TRANSMISSION = 256;
    public static final short PROBE_REPLY = 257;
    public static final short PROBE = 298;
    public static final short CHALLENGE = 290;
    public static final short CHALLENGE_RESPONSE = 291;
    public static final short PROBE_OS_VERSION = 2344;
    public static final short TIME = 1281;
    public static final short SCREEN_SETTINGS = 1302;
    public static final short WORKOUT_SCREEN_SETTINGS = 317;
    public static final short BATTERY_STATUS = 1284;
    public static final short USER = 1283;
    public static final short USER_SECRET = 1299;
    public static final short USER_UNIT = 281;
    public static final short ACTIVITY_TARGET = 1297;
    public static final short LOCALE = 289;
    public static final short LIVE_HR = 2369;
    public static final short HR = 2343;
    public static final short ACTIVITY_HR = 2345;
    public static final short ALARM = 1298;
    public static final short ALARM_STATUS = 2329;
    public static final short ALARM_NAME = 1300;
    public static final short STEPS = 2390;
    public static final short IMAGE_META_DATA = 2397;
    public static final short IMAGE_DATA = 2398;
    public static final short ANCS_STATUS = 2346;

    public static final short GET_ACTIVITY_SCREEN_LIST = 316;
    public static final short ACTIVITY_SCREEN_DATA = 317;
    public static final short GET_ACTIVITY_SAMPLES = 1286;
    public static final short ACTIVITY_SAMPLE_TIME = 1537;
    public static final short ACTIVITY_SAMPLE_DURATION = 1538;
    public static final short ACTIVITY_SAMPLE_MOVEMENT = 1539;
    public static final short ACTIVITY_SAMPLE_WALK = 1540;
    public static final short ACTIVITY_SAMPLE_RUN = 1541;
    public static final short ACTIVITY_SAMPLE_SWIM = 1549;
    public static final short ACTIVITY_SAMPLE_SLEEP = 1543;
    // There are two structure types containing information about calories:
    public static final short ACTIVITY_SAMPLE_CALORIES = 1544;
    public static final short ACTIVITY_SAMPLE_CALORIES_2 = 1546;

    public static final short ACTIVITY_SAMPLE_RECOVERY = 1547;
    public static final short WORKOUT_TYPE = 2409;
    public static final short LIVE_WORKOUT_START = 2418;
    public static final short LIVE_WORKOUT_END = 2419;
    public static final short LIVE_WORKOUT_PAUSE_STATE = 2439;
    public static final short WORKOUT_GPS_STATE = 321;

    private WithingsStructureType() {}
}
