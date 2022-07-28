package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Random;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.WithingsUUID;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Challenge;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ChallengeResponse;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.Probe;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ProbeOsVersion;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.ProbeReply;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.WithingsStructure;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.MessageHandler;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessage;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.WithingsMessageTypes;

public class AuthenticationHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationHandler.class);
    private WithingsSteelHRDeviceSupport support;

    public AuthenticationHandler(WithingsSteelHRDeviceSupport support) {
        this.support = support;
    }

    public void startAuthentication() {
        support.setAuthenticationInProgress(true);
        Message message = new WithingsMessage(WithingsMessageTypes.PROBE);
        message.addDataStructure(new Probe((short) 1, (short) 1, 5100401));
        message.addDataStructure(new ProbeOsVersion((short) Build.VERSION.SDK_INT));
        sendMessage(message);
    }

    public void handleAuthenticationResponse(Message response) {
        short messageType = response.getType();
        if (messageType == WithingsMessageTypes.PROBE) {
            handleProbeReply(response);
        } else if (messageType == WithingsMessageTypes.CHALLENGE) {
            handleChallenge(response);
        } else {
            logger.warn("Received unkown message: " + messageType + ", will ignore this.");
        }
    }

    private void finishAuthentication() {
        support.setAuthenticationInProgress(false);
    }

    private void sendMessage(Message message) {
        TransactionBuilder builder = support.createTransactionBuilder("authentication");
        BluetoothGattCharacteristic characteristic = support.getCharacteristic(WithingsUUID.WITHINGS_WRITE_CHARACTERISTIC_UUID);
        builder.write(characteristic, message.getRawData());
        builder.queue(support.getQueue());
    }

    private void handleChallenge(Message challengeMessage) {
        try {
            Challenge challenge = getTypeFromReply(Challenge.class, challengeMessage);
            // TODO: Save this somewhere:
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
            sendMessage(message);
        } catch (Exception e) {
            logger.error("Failed to create response to challenge: " + e.getMessage());
        }
    }

    private void handleProbeReply(Message message) {
        // TODO: verify the incoming challenge response.
        ProbeReply probeReply = getTypeFromReply(ProbeReply.class, message);
        if (probeReply == null) {
            throw new IllegalArgumentException("Message does not contain the required datastructure ProbeReply");
        }

        support.getDevice().setFirmwareVersion(String.valueOf(probeReply.getFirmwareVersion()));
        finishAuthentication();
    }

    private <T extends WithingsStructure> T getTypeFromReply(Class<T> type, Message message) {
        for (WithingsStructure structure : message.getDataStructures()) {
            if (type.isInstance(structure)) {
                return (T)structure;
            }
        }

        return null;
    }
}
