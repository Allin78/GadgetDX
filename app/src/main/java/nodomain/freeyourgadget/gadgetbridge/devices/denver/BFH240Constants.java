package nodomain.freeyourgadget.gadgetbridge.devices.denver;

import java.util.UUID;

public final class BFH240Constants {

    public static final UUID BFH240_DEVICE_UUID = UUID.fromString("0F75D814-6585-DC0F-AC61-5BE055255E27");

    //Known Services
    public static final UUID BFH240_GENERIC_ACCESS_PROFILE = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static final UUID BFH240_GENERIC_ATTRIBUTE_PROFILE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");

    public static final UUID BFH240_SERVICE1 = UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb");
    public static final UUID BFH240_SERVICE2 = UUID.fromString("f000ffc0-0451-4000-b000-000000000000");
    public static final UUID BFH240_SERVICE3 = UUID.fromString("c3e6fea0-e966-1000-8000-be99c223df6a");
    /* FEE7 => FEA1 (READ Notify) => Pedometer

    public static final UUID BFH240_SERVICE0007_WRITE = UUID.fromString("c3e6fea1-e966-1000-8000-be99c223df6a");
    public static final UUID BFH240_SERVICE0007_READ_NOTIFY = UUID.fromString("c3e6fea2-e966-1000-8000-be99c223df6a");

    public static final UUID BFH240_SERVICE000D_WRITE_NOTIFY = UUID.fromString("f000ffc1-0451-4000-b000-000000000000");
    public static final UUID BFH240_SERVICE000D_WRITE_NOTIFY = UUID.fromString("f000ffc2-0451-4000-b000-000000000000");


    /*
    //Verified command bytes
    public static final byte CMD_SET_ALARM_1 = (byte)0x09;
    public static final byte CMD_SET_ALARM_2 = (byte)0x22;
    public static final byte CMD_SET_ALARM_3 = (byte)0x23;

    public static final byte CMD_SET_DATE_AND_TIME = 0x08;


    public static final byte CMD_MEASURE_HEART = (byte)0x0D;    //param1: 0, param2: 0 -> STOP | 1 -> START
    public static final byte CMD_VIBRATE = (byte)0x07;  //param1: 0, param2: 1


    public static final byte CMD_SWITCH_PHOTO_MODE = (byte)0x25;        //param1: 0, param2: 0 -> OFF | 1 -> ON
    public static final byte CMD_SWITCH_12HOUR_MODE = (byte)0x3E;       //byte1: 1 -> 12HourMode | 0 -> 24HourMode
    public static final byte CMD_SWITCH_METRIC_IMPERIAL = (byte)0x3A;   //param1: 0, param2: 0 -> METRIC | 1 -> IMPERIAL //Also requests walked steps


    //Verified receive bytes
    public static final byte RECEIVE_DEVICE_INFO = (byte)0xF6;
    public static final byte RECEIVE_BATTERY_LEVEL = (byte)0xF7;
    public static final byte RECEIVE_STEPS_DATA = (byte)0xF9;
    public static final byte RECEIVE_HEART_DATA = (byte)0xE8;
    public static final byte RECEIVE_PHOTO_TRIGGER = (byte)0xF3;

    //Verified icon bytes
    public static final byte ICON_CALL = (byte)0x00;
    public static final byte ICON_SMS = (byte)0x01;
    public static final byte ICON_WECHAT = (byte)0x02;
    public static final byte ICON_QQ = (byte)0x03;
    public static final byte ICON_FACEBOOK = (byte)0x04;
    public static final byte ICON_SKYPE = (byte)0x05;
    public static final byte ICON_TWITTER = (byte)0x06;
    public static final byte ICON_WHATSAPP = (byte)0x07;
    public static final byte ICON_LINE = (byte)0x08;
    public static final byte ICON_TALK = (byte)0x09;
    public static final byte ICON_RUNNER = (byte)0x0A;



    //Most probably correct command bytes
    public static final byte CMD_SET_STEPLENGTH = (byte)0x3F;  //param1: 0, param2: STEPLENGTH


    //Probably correct command bytes
    public static final byte CMD_SET_INACTIVITY_WARNING_TIME = (byte)0x24;   //param1: 0, param2: time

    public static final byte CMD_SET_HEART_TARGET = (byte)0x01;  //param1: 0, param2: HEART TARGET
    public static final byte CMD_SET_STEP_TARGET = (byte)0x03;  //param1: 0, param2: STEP TARGET

    public static final byte CMD_FIND_DEVICE = (byte)0x36;          //param1: 0, param2: 1
    public static final byte CMD_SET_DISCONNECT_REMIND = (byte)0x37;    //param1: 0, param2: 0 -> ??? | 1 -> ???
    public static final byte CMD_SET_AUTODETECT_HEART = (byte)0x38;     //param1: 0, param2: 0 -> ??? | 1 -> ???

    public static final byte CMD_READ_HISTORY_SLEEP_COUNT = (byte)0x32; //param1: 0, param2: 0

    public static final byte CMD_SET_NOON_TIME = (byte)0x26;    //param1: start time, param2: end time
    public static final byte CMD_SET_SLEEP_TIME = (byte)0x27;   //param1: start time, param2: end time


    //Could be correct command bytes
    //Send PhoneName 0x17 and 0x18
    //Send PhoneNumber 0x19 and 0x20
    //Weather 0x3B
    //Power Management 0x39
    //User Id 0x35
    //



    //______________________________________________________________________________________________
    //It may be that BFH16 uses the same communication protocol as JYOU
    //copied the following JYOU vars:

    public static final byte CMD_SET_HEARTRATE_AUTO = 0x38;
    public static final byte CMD_SET_HEARTRATE_WARNING_VALUE = 0x01;
    public static final byte CMD_SET_TARGET_STEPS = 0x03;
    //public static final byte CMD_GET_STEP_COUNT = 0x1D;
    public static final byte CMD_GET_SLEEP_TIME = 0x32;
    public static final byte CMD_SET_DND_SETTINGS = 0x39;

    public static final byte CMD_ACTION_HEARTRATE_SWITCH = 0x0D;
    public static final byte CMD_ACTION_SHOW_NOTIFICATION = 0x2C;
    public static final byte CMD_ACTION_REBOOT_DEVICE = 0x0E;

     */
}
