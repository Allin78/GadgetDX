package nodomain.freeyourgadget.gadgetbridge.externalevents.notifications;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.service.notification.StatusBarNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.model.PhoneAlarmSpec;

public class AlarmNotificationHandler {
    public static final String PACKAGE_NAME_GOOGLE_DESKCLOCK = "com.google.android.deskclock";
    public static final String GOOGLE_DESKCLOCK_NOTIFICATION_CHANNEL_SNOOZED = "Snoozed Alarms";
    public static final String GOOGLE_DESKCLOCK_NOTIFICATION_CHANNEL_PENDING = "Upcoming Alarms";
    public static final String GOOGLE_DESKCLOCK_NOTIFICATION_CHANNEL_ALERT = "Firing";
    private static final Logger LOG = LoggerFactory.getLogger(AlarmNotificationHandler.class);
    private static final String TOKEN_ALARM_CANCELLED = "cancelled";
    private static final int ALARM_CANCELLED_WAIT_FOR_SNOOZE_DELAY = 500;
    private Handler handler = new Handler();
    private PhoneAlarmSpec alarmToDismiss = null;
    private Runnable dismissAlarmRunnable = () -> {
        // broadcast alarm to device services
        if (alarmToDismiss != null) {
            GBApplication.deviceService().onPhoneAlarm(alarmToDismiss);
        }

        // clear the pending alarm dismissal
        alarmToDismiss = null;
    };

    public boolean handle(Context context, StatusBarNotification sbn) {
        if (sbn.getPackageName().equals(PACKAGE_NAME_GOOGLE_DESKCLOCK)) {
            return handleGoogleDeskclockNotification(context, sbn);
        }

        return false;
    }

    public boolean handleGoogleDeskclockNotification(Context context, StatusBarNotification sbn) {
        // TODO notification handling when SDK < 26

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false;
        }

        LOG.debug("Handling Google Clock notification: {}", sbn);
        String channelId = sbn.getNotification().getChannelId();

        switch (channelId) {
            case GOOGLE_DESKCLOCK_NOTIFICATION_CHANNEL_ALERT: {
                // an alarm is currently being presented
                PhoneAlarmSpec alarm = new PhoneAlarmSpec();
                alarm.id = sbn.getId();
                alarm.action = PhoneAlarmSpec.Action.ALERT;
                alarm.timestamp = (int) (sbn.getNotification().when / 1000);
                alarm.name = "Alarm";
                GBApplication.deviceService().onPhoneAlarm(alarm);
                return true;
            }
            case GOOGLE_DESKCLOCK_NOTIFICATION_CHANNEL_SNOOZED: {
                // an alarm has been snoozed

                // send the alarm to the device services
                PhoneAlarmSpec alarm = new PhoneAlarmSpec();
                alarm.id = sbn.getId();
                alarm.action = PhoneAlarmSpec.Action.SNOOZE;
                alarm.timestamp = (int) (sbn.getNotification().when / 1000);
                alarm.name = "Alarm"; // TODO can we get the name from the notification?
                GBApplication.deviceService().onPhoneAlarm(alarm);
                return true;
            }
            default:
                LOG.warn("Unhandled channel ID {}", channelId);
        }

        return false;
    }

    public void handleGoogleDeskclockNotificationRemoved(Context context, StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        LOG.debug("Handling Google Clock notification: {}", sbn);

        if (!sbn.getNotification().getChannelId().equals(GOOGLE_DESKCLOCK_NOTIFICATION_CHANNEL_ALERT)) {
            LOG.warn("Unhandled channel ID {}", sbn.getNotification().getChannelId());
            return;
        }

        if (alarmToDismiss != null) {
            GBApplication.deviceService().onPhoneAlarm(alarmToDismiss);
        }

        alarmToDismiss = new PhoneAlarmSpec();
        alarmToDismiss.id = sbn.getId();
        alarmToDismiss.action = PhoneAlarmSpec.Action.DISMISS;
        alarmToDismiss.timestamp = (int) (sbn.getNotification().when / 1000);
        alarmToDismiss.name = "Alarm"; // TODO can we get the name from the notification?

        handler.postDelayed(dismissAlarmRunnable, ALARM_CANCELLED_WAIT_FOR_SNOOZE_DELAY);
    }

    public void handleRemove(Context context, StatusBarNotification sbn) {
        if (sbn.getPackageName().equals(PACKAGE_NAME_GOOGLE_DESKCLOCK)) {
            handleGoogleDeskclockNotificationRemoved(context, sbn);
        }
    }
}
