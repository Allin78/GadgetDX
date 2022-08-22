package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification;

public final class AncsConstants {

    public static final byte EVENT_ID_NOTIFICATION_ADDED = 0;
    public static final byte EVENT_ID_NOTIFICATION_MODIFIED = 1;
    public static final byte EVENT_ID_NOTIFICATION_REMOVED = 2;

    public static final byte EVENT_FLAGS_SILENT = (1 << 0);
    public static final byte EVENT_FLAGS_IMPORTANT = (1 << 1);
    public static final byte EVENT_FLAGS_PREEXISTING = (1 << 2);
    public static final byte EVENT_FLAGS_POSITIVE_ACTION = (1 << 3);
    public static final byte EVENT_FLAGS_NEGATIVE_ACTION = (1 << 4);

    public static final byte CATEGORY_ID_OTHER = 0;
    public static final byte CATEGORY_ID_INCOMING_CALL = 1;
    public static final byte CATEGORY_ID_MISSED_CALL = 2;
    public static final byte CATEGORY_ID_VOICEMAIL = 3;
    public static final byte CATEGORY_ID_SOCIAL = 4;
    public static final byte CATEGORY_ID_SCHEDULE = 5;
    public static final byte CATEGORY_ID_EMAIL = 6;
    public static final byte CATEGORY_ID_NEWS = 7;
    public static final byte CATEGORY_ID_HEALTHANDFITNESS = 8;
    public static final byte CATEGORY_ID_BUSINESSANDFINANCE = 9;
    public static final byte CATEGORY_ID_LOCATION = 10;
    public static final byte CATEGORY_ID_ENTERTAINMENT = 11;

    private AncsConstants(){}
}
