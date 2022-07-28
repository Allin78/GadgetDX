package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message;

import android.bluetooth.BluetoothGattCharacteristic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Random;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.devices.withingssteelhr.WithingsSteelHRSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.entities.WithingsSteelHRActivitySample;
import nodomain.freeyourgadget.gadgetbridge.service.btle.BLETypeConversions;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.WithingsUUID;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Challenge;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ChallengeResponse;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.HeartRate;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.LiveHeartRate;
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
//            switch (message.getType()) {
//                case WithingsMessageTypes.PROBE:
//                    if (message.getDataStructures().size() > 0) {
//                        handleProbeReply((ProbeReply) message.getDataStructures().get(0));
//                    }
//                    break;
//                case WithingsMessageTypes.CHALLENGE:
//                    if (message.getDataStructures().size() > 0) {
//                        handleChallenge((Challenge) message.getDataStructures().get(0));
//                    }
//                    break;
//                default:
//                    logger.warn("Unknown message type received: " + message.getType());
//                    return true;
//            }
            return true;
        }
    }

    public Message getMessage() {
        return message;
    }

    private void handleChallenge(Challenge challenge) {
        try {
            String secret = "2EM5zNP37QzM00hmP6BFTD92nG15XwNd";
            ByteBuffer allocate = ByteBuffer.allocate(challenge.getChallenge().length + challenge.getMacAddress().getBytes().length + secret.getBytes().length);
            allocate.put(challenge.getChallenge());
            allocate.put(challenge.getMacAddress().getBytes());
            allocate.put(secret.getBytes());
            byte[] hash = MessageDigest.getInstance("SHA1").digest(allocate.array());
            ChallengeResponse challengeResponse = new ChallengeResponse();
            challengeResponse.setResponse(hash);
            Message message = new WithingsMessage(WithingsMessageTypes.CHALLENGE);
            message.addDataStructure(challengeResponse);
            Challenge challengeToSend = new Challenge();
            challengeToSend.setMacAddress(challengeToSend.getMacAddress());
            byte[] bArr = new byte[16];
            new Random().nextBytes(bArr);
            challengeToSend.setChallenge(bArr);
            message.addDataStructure(challengeToSend);
            TransactionBuilder builder = support.createTransactionBuilder("setupFinished");
            BluetoothGattCharacteristic characteristic = support.getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID);
            builder.write(characteristic, message.getRawData());
            builder.queue(support.getQueue());
        } catch (Exception e) {
            logger.error("Failed to create response to challenge: " + e.getMessage());
        }
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

    private void handleProbeReply(ProbeReply probeReply) {
        support.getDevice().setFirmwareVersion(String.valueOf(probeReply.getFirmwareVersion()));
    }
}
