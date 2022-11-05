package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

import android.bluetooth.BluetoothGattCharacteristic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.WithingsUUID;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Challenge;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ChallengeResponse;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ProbeReply;
import nodomain.freeyourgadget.gadgetbridge.util.StringUtils;

public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private WithingsSteelHRDeviceSupport support;
    private MessageFactory messageFactory;
    private ByteArrayOutputStream pendingMessage;
    private Message message;

    public MessageHandler(WithingsSteelHRDeviceSupport support, MessageFactory messageFactory) {
        this.support = support;
        this.messageFactory = messageFactory;
    }

    public synchronized boolean handleMessage(byte[] rawData) {
        if (pendingMessage == null && rawData[0] == 0x01) {
            pendingMessage = new ByteArrayOutputStream();
        }

        try {
            pendingMessage.write(rawData);
        } catch(IOException e) {
            logger.error("Could not write data to stream: " + StringUtils.bytesToHex(rawData));
            return false;
        }

        if (!isMessageComplete(pendingMessage.toByteArray())) {
            return false;
        } else {
            Message message = messageFactory.createMessageFromRawData(pendingMessage.toByteArray());
            pendingMessage = null;
            if (message == null) {
                logger.info("Cannot handle null message");
                return false;
            }

            this.message = message;
            return true;
        }
    }

    public Message getMessage() {
        return message;
    }

    private boolean isMessageComplete(byte[] messageData) {
        if (messageData.length < 5) {
            return false;
        }

        short totalDataLength = (short) BLETypeConversions.toInt16(messageData[4], messageData[3]);
        byte[] rawStructureData = Arrays.copyOfRange(messageData, 5, messageData.length);
        if (rawStructureData.length == totalDataLength) {
            return true;
        }

        return false;
    }
}
