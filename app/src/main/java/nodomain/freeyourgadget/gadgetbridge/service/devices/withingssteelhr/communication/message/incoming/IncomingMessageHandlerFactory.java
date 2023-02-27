package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.incoming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.GlyphRequestHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageType;

public class IncomingMessageHandlerFactory {

    private static final Logger logger = LoggerFactory.getLogger(IncomingMessageHandlerFactory.class);
    private static IncomingMessageHandlerFactory instance;
    private final WithingsSteelHRDeviceSupport support;
    private Map<Short, IncomingMessageHandler> handlers = new HashMap<>();

    private IncomingMessageHandlerFactory(WithingsSteelHRDeviceSupport support) {
        this.support = support;
    }

    public static IncomingMessageHandlerFactory getInstance(WithingsSteelHRDeviceSupport support) {
        if (instance == null) {
            instance = new IncomingMessageHandlerFactory(support);
        }

        return instance;
    }

    public IncomingMessageHandler getHandler(Message message) {
        IncomingMessageHandler handler = handlers.get(message.getType());
        switch (message.getType()) {
            case WithingsMessageType.START_LIVE_WORKOUT:
            case WithingsMessageType.STOP_LIVE_WORKOUT:
            case WithingsMessageType.GET_WORKOUT_GPS_STATUS:
                if (handler == null) {
                    handlers.put(message.getType(), new LiveWorkoutHandler(support));
                }
                break;
            case WithingsMessageType.LIVE_WORKOUT_DATA:
                if (handler == null) {
                    handlers.put(message.getType(), new LiveHeartrateHandler(support));
                }
                break;
            case WithingsMessageType.GET_NOTIFICATION:
                if (handler == null) {
                    handlers.put(message.getType(), new NotificationRequestHandler(support));
                }
                break;
            case WithingsMessageType.GET_UNICODE_GLYPH:
                if (handler == null) {
                    handlers.put(message.getType(), new GlyphRequestHandler(support));
                }
                break;
            case WithingsMessageType.SYNC:
                if (handler == null) {
                    handlers.put(message.getType(), new SyncRequestHandler(support));
                }
                break;
            default:
                logger.warn("Unhandled incoming message type: " + message.getType());
        }

        return handlers.get(message.getType());
    }

}
