package nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro;

import static nodomain.freeyourgadget.gadgetbridge.util.GB.hexdump;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventSendBytes;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventUpdateDeviceState;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro.protocol.Authentication;
import nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro.protocol.Message;
import nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro.protocol.MessageType;
import nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro.protocol.Opcode;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class RedmiBuds5ProProtocol extends GBDeviceProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(RedmiBuds5ProProtocol.class);
    final UUID UUID_DEVICE_CTRL = UUID.fromString("0000fd2d-0000-1000-8000-00805f9b34fb");

    private byte sequenceNumber = 0;

    protected RedmiBuds5ProProtocol(GBDevice device) {
        super(device);
    }

    public byte[] encodeStartAuthentication() {
        byte[] authRnd = Authentication.random();
        LOG.debug("[AUTH] Sending challenge: " + hexdump(authRnd));

//        byte[] expectedResponse = Authentication.encrypt(authRnd.clone());
//        LOG.debug("Expected result: " + hexdump(expectedResponse));

        byte[] payload = new byte[17];
        payload[0] = 0x01;
        System.arraycopy(authRnd, 0, payload, 1, 16);
        return new Message(MessageType.PHONE_REQUEST, Opcode.AUTH_CHALLENGE, sequenceNumber++, payload).encode();
    }

    private GBDeviceEventVersionInfo decodeDeviceInfo(byte[] deviceInfoPayload) {
        
        GBDeviceEventVersionInfo info = new GBDeviceEventVersionInfo();
        byte[] fw = new byte[4];
        byte[] vidPid = new byte[4];
        int i = 0;
        while (i < deviceInfoPayload.length) {
            byte len = deviceInfoPayload[i];
            byte index = deviceInfoPayload[i + 1];
            switch (index) {
                case 0x01:
                    System.arraycopy(deviceInfoPayload, i + 2, fw, 0, 4);
                    break;
                case 0x03:
                    System.arraycopy(deviceInfoPayload, i + 2, vidPid, 0, 4);
                    break;
            }
            i += len + 1;
        }

        String fwVersion1 = (fw[0] >> 4) + "." + (fw[0] & 0xF) + "." +
                (fw[1] >> 4) + "." + (fw[1] & 0xF);
        String fwVersion2 = (fw[2] >> 4) + "." + (fw[2] & 0xF) + "." +
                (fw[3] >> 4) + "." + (fw[3] & 0xF);
        String hwVersion = String.format("VID: 0x%02X%02X, PID: 0x%02X%02X",
                vidPid[0], vidPid[1], vidPid[2], vidPid[3]);

        info.fwVersion = fwVersion1;
        info.fwVersion2 = fwVersion2;
        info.hwVersion = hwVersion;
        return info;
    }

    @Override
    public GBDeviceEvent[] decodeResponse(byte[] responseData) {

        LOG.debug("Incoming message: " + hexdump(responseData));

        Message response = Message.fromBytes(responseData);
        LOG.debug("Parsed message: " + response);

        List<GBDeviceEvent> events = new ArrayList<>();
        if (response.getType() == MessageType.RESPONSE && response.getOpcode() == Opcode.AUTH_CHALLENGE) {
            LOG.debug("[AUTH] Received Challenge Response");
            /*
                TODO Should check if equal, but does not really matter
             */
            LOG.debug("[AUTH] Sending authentication confirmation");
            events.add(new GBDeviceEventSendBytes(new Message(MessageType.PHONE_REQUEST, Opcode.AUTH_CONFIRM, sequenceNumber++, new byte[]{0x01, 0x00}).encode()));
        } else if (response.getType() == MessageType.RESPONSE && response.getOpcode() == Opcode.AUTH_CONFIRM) {
            LOG.debug("[AUTH] Confirmed first authentication step");

        } else if (response.getType() == MessageType.EARBUDS_REQUEST && response.getOpcode() == Opcode.AUTH_CHALLENGE) {
            byte[] responsePayload = response.getPayload();
            byte[] challenge = new byte[16];
            System.arraycopy(responsePayload, 1, challenge, 0, 16);

            LOG.info("[AUTH] Received Challenge: {}", hexdump(challenge));
            byte[] challengeResponse = Authentication.encrypt(challenge);
            LOG.info("[AUTH] Sending Challenge Response: {}", hexdump(challengeResponse));

            byte[] payload = new byte[17];
            payload[0] = 0x01;
            System.arraycopy(challengeResponse, 0, payload, 1, 16);
            Message res = new Message(MessageType.RESPONSE, Opcode.AUTH_CHALLENGE, response.getSequenceNumber(), payload);
            events.add(new GBDeviceEventSendBytes(res.encode()));

        } else if (response.getType() == MessageType.EARBUDS_REQUEST && response.getOpcode() == Opcode.AUTH_CONFIRM) {
            LOG.debug("[AUTH] Received authentication confirmation");
            Message res = new Message(MessageType.RESPONSE, Opcode.AUTH_CONFIRM, response.getSequenceNumber(), new byte[]{0x01});
            LOG.debug("[AUTH] Sending final authentication confirmation");
            events.add(new GBDeviceEventSendBytes(res.encode()));

            LOG.debug("[INIT] Sending device info request");
            Message info = new Message(MessageType.PHONE_REQUEST, Opcode.GET_DEVICE_INFO, sequenceNumber++, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});
            events.add(new GBDeviceEventSendBytes(info.encode()));
        } else if (response.getType() == MessageType.RESPONSE && response.getOpcode() == Opcode.GET_DEVICE_INFO) {
            LOG.debug("[INIT] Received device info");
            if (getDevice().getState() != GBDevice.State.INITIALIZED) {
                events.add(decodeDeviceInfo(response.getPayload()));
                LOG.debug("[INIT] Device Initialized");
                events.add(new GBDeviceEventUpdateDeviceState(GBDevice.State.INITIALIZED));
            }
        }

        return events.toArray(new GBDeviceEvent[0]);
    }


}
