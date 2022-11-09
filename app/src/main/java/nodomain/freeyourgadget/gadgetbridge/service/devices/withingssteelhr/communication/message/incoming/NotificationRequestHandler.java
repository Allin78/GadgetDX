package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.incoming;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.IconHelper;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ImageData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ImageMetaData;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.SourceAppId;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessage;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.notification.NotificationProvider;

public class NotificationRequestHandler implements IncomingMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationRequestHandler.class);

    private final WithingsSteelHRDeviceSupport support;
    private Map<String, byte[]> appIconCache = new HashMap<>();

    public NotificationRequestHandler(WithingsSteelHRDeviceSupport support) {
        this.support = support;
    }

    @Override
    public void handleMessage(Message message) {
        SourceAppId appId = message.getStructureByType(SourceAppId.class);
        ImageMetaData imageMetaData = message.getStructureByType(ImageMetaData.class);
        Message reply = new WithingsMessage(WithingsMessageType.GET_NOTIFICATION);
        reply.addDataStructure(appId);
        reply.addDataStructure(imageMetaData);
        ImageData imageData = new ImageData();
        imageData.setImageData(getImageData(appId.getAppId()));
        reply.addDataStructure(imageData);
        support.sendToDevice(reply);
    }

    private byte[] getImageData(String sourceAppId) {
        byte[] imageData = appIconCache.get(sourceAppId);
        if (imageData == null) {
            NotificationSpec notificationSpec = NotificationProvider.getInstance(support).getNotificationSpecForSourceAppId(sourceAppId);
            if (notificationSpec != null) {
                int iconId = notificationSpec.iconId;
                try {
                    Drawable icon = null;
                    if (notificationSpec.iconId != 0) {
                        Context sourcePackageContext = support.getContext().createPackageContext(sourceAppId, 0);
                        icon = sourcePackageContext.getResources().getDrawable(notificationSpec.iconId);
                    }
                    if (icon == null) {
                        PackageManager pm = support.getContext().getPackageManager();
                        icon = pm.getApplicationIcon(sourceAppId);
                    }

                    imageData = IconHelper.getIconBytesFromDrawable(icon);
                    appIconCache.put(sourceAppId, imageData);
                } catch (PackageManager.NameNotFoundException e) {
                    logger.error("Error while updating notification icons", e);
                }
            } else {
                imageData = new byte[0];
            }
        }

        return imageData;
    }
}
