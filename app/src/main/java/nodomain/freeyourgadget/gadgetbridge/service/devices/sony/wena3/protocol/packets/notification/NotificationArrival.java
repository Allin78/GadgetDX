package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification;

import android.util.Xml;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.LedColor;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.NotificationFlags;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.NotificationKind;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.notification.defines.VibrationOptions;
import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.util.TimeUtil;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class NotificationArrival implements Wena3Packetable {
    public NotificationKind kind;
    public int id;
    public String title;
    public String message;
    public String actionLabel;
    public Date dateTime;
    public VibrationOptions vibration;
    public LedColor ledColor;
    public NotificationFlags flags;

    public NotificationArrival(
            NotificationKind kind,
            int id,
            String title,
            String message,
            String actionLabel,
            Date dateTime,
            VibrationOptions vibration,
            LedColor ledColor,
            NotificationFlags flags
    ) {
        this.kind = kind;
        this.id = id;
        this.title = title;
        this.message = message;
        this.actionLabel = actionLabel;
        this.dateTime = dateTime;
        this.vibration = vibration;
        this.ledColor = ledColor;
        this.flags = flags;
    }

    @Override
    public byte[] toByteArray() {
        assert vibration.count <= 255;

        byte vibraFlags = 0;
        byte vibraCount = (byte)vibration.count;

        if(vibration.continuous) {
            vibraFlags = 0x3;
            vibraCount = 0;
        } else {
            if(vibraCount > 4) vibraCount = 4;
            else if (vibraCount < 0) vibraCount = 1;
        }

        byte[] encodedTitle = StringUtils.truncate(title.trim(), 31).getBytes(StandardCharsets.UTF_8);
        byte[] encodedMessage = StringUtils.truncate(message.trim(), 239).getBytes(StandardCharsets.UTF_8);
        byte[] encodedAction = StringUtils.truncate(actionLabel.trim(), 15).getBytes(StandardCharsets.UTF_8);

        ByteBuffer buf = ByteBuffer
                .allocate(18 + encodedTitle.length + encodedMessage.length + encodedAction.length)
                .order(ByteOrder.LITTLE_ENDIAN);

        buf.put((byte) 0x00) // marker
                .put((byte)ledColor.ordinal())
                .put(vibraFlags)
                .put((byte)vibration.kind.ordinal())
                .put(vibraCount)
                .put((byte)encodedTitle.length)
                .put((byte)encodedMessage.length)
                .put((byte)flags.value)
                .put((byte)kind.ordinal())
                .put((byte)encodedAction.length)
                .putInt(id)
                .putInt(TimeUtil.dateToWenaTime(dateTime))
                .put(encodedTitle)
                .put(encodedMessage)
                .put(encodedAction);

        return buf.array();
    }
}
