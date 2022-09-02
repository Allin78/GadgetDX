package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nodomain.freeyourgadget.gadgetbridge.devices.pebble.PebbleColor;
import nodomain.freeyourgadget.gadgetbridge.devices.pebble.PebbleIconID;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;

public class NotificationProvider {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProvider.class);
    private final WithingsSteelHRDeviceSupport support;
    private final Map<Integer, NotificationSpec> pendingNotifications = new HashMap<>();

    public NotificationProvider(WithingsSteelHRDeviceSupport support) {
        this.support = support;
    }

    public void notifyClient(NotificationSpec spec) {
        NotificationSource notificationSource = new NotificationSource(spec.getId(),
                                                                        AncsConstants.EVENT_ID_NOTIFICATION_ADDED,
                                                                        AncsConstants.EVENT_FLAGS_IMPORTANT,
                                                                        mapNotificationType(spec.type),
                                                                        (byte)1);
        pendingNotifications.put(notificationSource.getNotificationUID(), spec);
        support.sendAncsNotificationSourceNotification(notificationSource);
    }

    public void handleNotificationAttributeRequest(GetNotificationAttributes request) {
        NotificationSpec spec = pendingNotifications.get(request.getNotificationUID());
        if (spec == null) {
            logger.info("No pending notification with notificationUID " + request.getNotificationUID());
            NotificationSource notificationSource = new NotificationSource(request.getNotificationUID(),
                                                                            AncsConstants.EVENT_ID_NOTIFICATION_REMOVED,
                                                                            AncsConstants.EVENT_FLAGS_IMPORTANT,
                                                                            (byte)0,
                                                                            (byte)1);
            support.sendAncsNotificationSourceNotification(notificationSource);
            return;
        }

        GetNotificationAttributesResponse response = new GetNotificationAttributesResponse(request.getNotificationUID());
        List<RequestedNotificationAttribute> requestedAttributes = request.getAttributes();

        boolean complete = false;

        for (RequestedNotificationAttribute requestedAttribute : requestedAttributes) {
            NotificationAttribute attribute = new NotificationAttribute();
            attribute.setAttributeID(requestedAttribute.getAttributeID());
            attribute.setAttributeMaxLength(requestedAttribute.getAttributeMaxLength());
            String value = "";
            if (requestedAttribute.getAttributeID() == 0) {
                value = spec.sourceAppId;
            }
            if (requestedAttribute.getAttributeID() == 1) {
                complete = true;
                value = spec.sender != null? spec.sender : (spec.phoneNumber != null? spec.phoneNumber : spec.sourceName);
            }
            if (requestedAttribute.getAttributeID() == 2) {
                complete = true;
                value = spec.title != null? spec.title : spec.subject;
            }
            if (requestedAttribute.getAttributeID() == 3) {
                complete = true;
                value = spec.body;
            }

            if (value != null) {
                if (requestedAttribute.getAttributeMaxLength() == 0 || requestedAttribute.getAttributeMaxLength() >= value.length()) {
                    attribute.setValue(value);
                } else {
                    attribute.setValue(value.substring(0, requestedAttribute.getAttributeMaxLength()));
                }
            }

            response.addAttribute(attribute);
        }

        support.sendAncsDataSourceNotification(response);

        if (complete) {
            pendingNotifications.remove(request.getNotificationUID());
        }
    }

    private byte mapNotificationType(NotificationType type) {
        switch (type) {
            case GENERIC_ALARM_CLOCK:
            case BUSINESS_CALENDAR:
            case GENERIC_CALENDAR:
                return AncsConstants.CATEGORY_ID_SCHEDULE;
            case GENERIC_EMAIL:
            case YAHOO_MAIL:
            case GOOGLE_INBOX:
            case GMAIL:
            case OUTLOOK:
                return AncsConstants.CATEGORY_ID_EMAIL;
            case GENERIC_NAVIGATION:
                return AncsConstants.CATEGORY_ID_LOCATION;
            case GENERIC_PHONE:
                return AncsConstants.CATEGORY_ID_INCOMING_CALL;
            case MAILBOX:
                return AncsConstants.CATEGORY_ID_MISSED_CALL;
            case LINE:
            case RIOT:
            case SIGNAL:
            case WIRE:
            case SKYPE:
            case SLACK:
            case SNAPCHAT:
            case TELEGRAM:
            case THREEMA:
            case KONTALK:
            case ANTOX:
            case DISCORD:
            case TRANSIT:
            case TWITTER:
            case VIBER:
            case WECHAT:
            case WHATSAPP:
            case FACEBOOK:
            case FACEBOOK_MESSENGER:
            case LINKEDIN:
            case HIPCHAT:
            case INSTAGRAM:
            case KAKAO_TALK:
            case GENERIC_SMS:
            case GOOGLE_MESSENGER:
            case GOOGLE_HANGOUTS:
                return AncsConstants.CATEGORY_ID_SOCIAL;
            default:
                return AncsConstants.CATEGORY_ID_OTHER;
        }
    }
    
}
