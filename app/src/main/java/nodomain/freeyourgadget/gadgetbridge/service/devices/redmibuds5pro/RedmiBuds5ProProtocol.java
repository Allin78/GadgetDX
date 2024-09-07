package nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro;

import static nodomain.freeyourgadget.gadgetbridge.util.GB.hexdump;

import android.content.SharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventSendBytes;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventUpdateDeviceState;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryState;
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

        byte[] payload = new byte[17];
        payload[0] = 0x01;
        System.arraycopy(authRnd, 0, payload, 1, 16);
        return new Message(MessageType.PHONE_REQUEST, Opcode.AUTH_CHALLENGE, sequenceNumber++, payload).encode();
    }

    @Override
    public byte[] encodeSendConfiguration(String config) {
        return encodeSetAmbientSoundControl();
    }

    public byte[] encodeSetAmbientSoundControl() {
        Prefs prefs = getDevicePrefs();
        byte anc_mode;
        switch (prefs.getString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_AMBIENT_SOUND_CONTROL, "off")) {
            case "off":
                anc_mode = 0x00;
                break;
            case "noise_cancelling":
                anc_mode = 0x01;
                break;
            case "ambient_sound":
                anc_mode = 0x02;
                break;
            default:
                LOG.error("Invalid audio mode");
                return null;
        }
        return new Message(MessageType.PHONE_REQUEST, Opcode.ANC, sequenceNumber++, new byte[]{0x02, 0x04, anc_mode}).encode();
    }

    private GBDeviceEventBatteryInfo parseBatteryInfo(byte batteryInfo, int index) {

        if (batteryInfo == (byte) 0xff) {
            return null;
        }
        GBDeviceEventBatteryInfo batteryEvent = new GBDeviceEventBatteryInfo();
        batteryEvent.state = (batteryInfo & 128) != 0 ? BatteryState.BATTERY_CHARGING : BatteryState.BATTERY_NORMAL;
        batteryEvent.batteryIndex = index;
        batteryEvent.level = (batteryInfo & 127);
        LOG.debug("Battery {}: {}", index, batteryEvent.level);
        return batteryEvent;
    }

    private GBDeviceEvent[] decodeDeviceInfo(byte[] deviceInfoPayload) {

        List<GBDeviceEvent> events = new ArrayList<>();

        GBDeviceEventVersionInfo info = new GBDeviceEventVersionInfo();
        byte[] fw = new byte[4];
        byte[] vidPid = new byte[4];
        byte[] batteryData = new byte[3];
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
                case 0x07:
                    System.arraycopy(deviceInfoPayload, i + 2, batteryData, 0, 3);
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

        events.add(parseBatteryInfo(batteryData[0], 1));
        events.add(parseBatteryInfo(batteryData[1], 2));
        events.add(parseBatteryInfo(batteryData[2], 0));
        events.add(info);

        return events.toArray(new GBDeviceEvent[0]);
    }

    private GBDeviceEvent[] decodeDeviceUpdate(byte[] updatePayload) {
        List<GBDeviceEvent> events = new ArrayList<>();

        int i = 0;
        while (i < updatePayload.length) {
            byte len = updatePayload[i];
            byte index = updatePayload[i + 1];
            switch (index) {
                case 0x00:
                    events.add(parseBatteryInfo(updatePayload[i+2], 1));
                    events.add(parseBatteryInfo(updatePayload[i+3], 2));
                    events.add(parseBatteryInfo(updatePayload[i+4], 0));
                    break;
                case 0x04:
                    SharedPreferences preferences = getDevicePrefs().getPreferences();
                    SharedPreferences.Editor editor = preferences.edit();
                    switch (updatePayload[i+2]) {
                        case 0x00:
                            editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_AMBIENT_SOUND_CONTROL, "off");
                            break;
                        case 0x01:
                            editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_AMBIENT_SOUND_CONTROL, "noise_cancelling");
                            break;
                        case 0x02:
                            editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_AMBIENT_SOUND_CONTROL, "ambient_sound");
                            break;

                    }
                    editor.apply();
            }
            i += len + 1;
        }

        return events.toArray(new GBDeviceEvent[0]);
    }


    @Override
    public GBDeviceEvent[] decodeResponse(byte[] responseData) {

        LOG.debug("Incoming message: " + hexdump(responseData));

        List<GBDeviceEvent> events = new ArrayList<>();

        List<Message> messages = Message.splitPiggybackedMessages(responseData);

        for (Message response : messages) {

            LOG.debug("Parsed message: " + response);


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
                    events.addAll(Arrays.asList(decodeDeviceInfo(response.getPayload())));
                    LOG.debug("[INIT] Device Initialized");
                    events.add(new GBDeviceEventUpdateDeviceState(GBDevice.State.INITIALIZED));
                }
            } else if (response.getOpcode() == Opcode.REPORT_STATUS) {
                events.addAll(Arrays.asList(decodeDeviceUpdate(response.getPayload())));
            }
        }
        return events.toArray(new GBDeviceEvent[0]);
    }


}
