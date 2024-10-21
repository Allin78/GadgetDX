package nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro;

import static nodomain.freeyourgadget.gadgetbridge.util.GB.hexdump;

import android.content.SharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import nodomain.freeyourgadget.gadgetbridge.devices.xiaomi.redmibuds5pro.prefs.Gestures;
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
        switch (config) {
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_AMBIENT_SOUND_CONTROL:
                return encodeSetAmbientSoundControl();
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_NOISE_CANCELLING_STRENGTH:
                return encodeSetNoiseCancellingStrength();
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_TRANSPARENCY_STRENGTH:
                return encodeSetTransparencyStrength();
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_ADAPTIVE_NOISE_CANCELLING:
                return encodeSetAdaptiveNoiseCancelling();
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_PERSONALIZED_NOISE_CANCELLING:
                return encodeSetCustomizedNoiseCancelling();
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_SINGLE_TAP_LEFT:
                return encodeSetGesture(config, Gestures.InteractionType.SINGLE, Gestures.Position.LEFT);
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_SINGLE_TAP_RIGHT:
                return encodeSetGesture(config, Gestures.InteractionType.SINGLE, Gestures.Position.RIGHT);
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_DOUBLE_TAP_LEFT:
                return encodeSetGesture(config, Gestures.InteractionType.DOUBLE, Gestures.Position.LEFT);
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_DOUBLE_TAP_RIGHT:
                return encodeSetGesture(config, Gestures.InteractionType.DOUBLE, Gestures.Position.RIGHT);
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_TRIPLE_TAP_LEFT:
                return encodeSetGesture(config, Gestures.InteractionType.TRIPLE, Gestures.Position.LEFT);
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_TRIPLE_TAP_RIGHT:
                return encodeSetGesture(config, Gestures.InteractionType.TRIPLE, Gestures.Position.RIGHT);
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_LONG_TAP_MODE_LEFT:
                return encodeSetGesture(config, Gestures.InteractionType.LONG, Gestures.Position.LEFT);
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_LONG_TAP_MODE_RIGHT:
                return encodeSetGesture(config, Gestures.InteractionType.LONG, Gestures.Position.RIGHT);
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_LONG_TAP_SETTINGS_LEFT:
                return encodeSetLongGestureMode(config, Gestures.Position.LEFT);
            case DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_LONG_TAP_SETTINGS_RIGHT:
                return encodeSetLongGestureMode(config, Gestures.Position.RIGHT);
            default:
                LOG.debug("Unsupported config: " + config);
        }

        return super.encodeSendConfiguration(config);
    }

    public byte[] encodeSetLongGestureMode(String config, Gestures.Position position) {
        Prefs prefs = getDevicePrefs();
        byte value = (byte) Integer.parseInt(prefs.getString(config, "7"));
        byte[] payload = new byte[] {0x04, 0x00, 0x0a, (byte) 0xFF, (byte) 0xFF};
        if (position == Gestures.Position.LEFT) {
            payload[3] = value;
        } else {
            payload[4] = value;
        }
        return new Message(MessageType.PHONE_REQUEST, Opcode.SET_CONFIG, sequenceNumber++, payload).encode();
    }

    public byte[] encodeSetGesture(String config, Gestures.InteractionType interactionType, Gestures.Position position) {
        Prefs prefs = getDevicePrefs();
        byte value = (byte) Integer.parseInt(prefs.getString(config, "1"));
        byte[] payload = new byte[] {0x05, 0x00, 0x02, interactionType.value, (byte) 0xFF, (byte) 0xFF};
        if (position == Gestures.Position.LEFT) {
            payload[4] = value;
        } else {
            payload[5] = value;
        }
        return new Message(MessageType.PHONE_REQUEST, Opcode.SET_CONFIG, sequenceNumber++, payload).encode();
    }

    public byte[] encodeSetAdaptiveNoiseCancelling() {
        Prefs prefs = getDevicePrefs();
        byte value = (byte) (prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_ADAPTIVE_NOISE_CANCELLING, false) ? 0x01 : 0x00);
        return new Message(MessageType.PHONE_REQUEST, Opcode.SET_CONFIG, sequenceNumber++, new byte[]{0x03, 0x00, 0x25, value}).encode();
    }

    public byte[] encodeSetCustomizedNoiseCancelling() {
        Prefs prefs = getDevicePrefs();
        byte value = (byte) (prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_PERSONALIZED_NOISE_CANCELLING, false) ? 0x01 : 0x00);
        return new Message(MessageType.PHONE_REQUEST, Opcode.SET_CONFIG, sequenceNumber++, new byte[]{0x03, 0x00, 0x3b, value}).encode();
    }

    public byte[] encodeSetNoiseCancellingStrength() {
        Prefs prefs = getDevicePrefs();
        byte mode = (byte) Integer.parseInt(prefs.getString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_NOISE_CANCELLING_STRENGTH, "0"));
        return new Message(MessageType.PHONE_REQUEST, Opcode.SET_CONFIG, sequenceNumber++, new byte[]{0x04, 0x00, 0x0b, 0x01, mode}).encode();
    }

    public byte[] encodeSetTransparencyStrength() {
        Prefs prefs = getDevicePrefs();
        byte mode = (byte) Integer.parseInt(prefs.getString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_TRANSPARENCY_STRENGTH, "0"));
        return new Message(MessageType.PHONE_REQUEST, Opcode.SET_CONFIG, sequenceNumber++, new byte[]{0x04, 0x00, 0x0b, 0x02, mode}).encode();
    }

    public byte[] encodeGetConfig() {
        Message strength = new Message(MessageType.PHONE_REQUEST, Opcode.GET_CONFIG, sequenceNumber++, new byte[]{0x00, 0x0b});
        Message adaptiveAnc = new Message(MessageType.PHONE_REQUEST, Opcode.GET_CONFIG, sequenceNumber++, new byte[]{0x00, 0x25});
        Message customAnc = new Message(MessageType.PHONE_REQUEST, Opcode.GET_CONFIG, sequenceNumber++, new byte[]{0x00, 0x3b});
        Message gestures = new Message(MessageType.PHONE_REQUEST, Opcode.GET_CONFIG, sequenceNumber++, new byte[]{0x00, 0x02});
        Message longGestures = new Message(MessageType.PHONE_REQUEST, Opcode.GET_CONFIG, sequenceNumber++, new byte[]{0x00, 0x0a});

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(strength.encode());
            outputStream.write(adaptiveAnc.encode());
            outputStream.write(customAnc.encode());
            outputStream.write(gestures.encode());
            outputStream.write(longGestures.encode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }

    public void decodeGetConfig(byte[] configPayload) {

        SharedPreferences preferences = getDevicePrefs().getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        switch (configPayload[2]) {
            case 0x02:
                LOG.debug("[Gesture] Click config: {}", hexdump(configPayload));
                editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_SINGLE_TAP_LEFT, Integer.toString(configPayload[4]));
                editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_SINGLE_TAP_RIGHT, Integer.toString(configPayload[5]));

                editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_DOUBLE_TAP_LEFT, Integer.toString(configPayload[7]));
                editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_DOUBLE_TAP_RIGHT, Integer.toString(configPayload[8]));

                editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_TRIPLE_TAP_LEFT, Integer.toString(configPayload[10]));
                editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_TRIPLE_TAP_RIGHT, Integer.toString(configPayload[11]));

                editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_LONG_TAP_MODE_LEFT, Integer.toString(configPayload[13]));
                editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_LONG_TAP_MODE_RIGHT, Integer.toString(configPayload[14]));
                break;
            case 0x0A:
                LOG.debug("[Gesture] Long press: {}", hexdump(configPayload));
                editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_LONG_TAP_SETTINGS_LEFT, Integer.toString(configPayload[3]));
                editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_CONTROL_LONG_TAP_SETTINGS_RIGHT, Integer.toString(configPayload[4]));
                break;
            case 0x0B:
                byte mode = configPayload[4];
                if (configPayload[3] == 0x01) {
                    editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_NOISE_CANCELLING_STRENGTH, Integer.toString(mode));
                } else if (configPayload[3] == 0x02) {
                    editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_TRANSPARENCY_STRENGTH, Integer.toString(mode));
                }
            case 0x25:
                editor.putBoolean(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_ADAPTIVE_NOISE_CANCELLING, configPayload[3] == 0x01);
                break;
            case 0x3B:
                editor.putBoolean(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_PERSONALIZED_NOISE_CANCELLING, configPayload[3] == 0x01);
                break;
        }
        editor.apply();
    }

    public byte[] encodeSetAmbientSoundControl() {
        Prefs prefs = getDevicePrefs();
        byte mode = (byte) Integer.parseInt(prefs.getString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_AMBIENT_SOUND_CONTROL, "0"));
        return new Message(MessageType.PHONE_REQUEST, Opcode.ANC, sequenceNumber++, new byte[]{0x02, 0x04, mode}).encode();
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

        String fwVersion1 = ((fw[0] >> 4) & 0xF) + "." + (fw[0] & 0xF) + "." + ((fw[1] >> 4) & 0xF) + "." + (fw[1] & 0xF);
        String fwVersion2 = ((fw[2] >> 4) & 0xF) + "." + (fw[2] & 0xF) + "." + ((fw[3] >> 4) & 0xF) + "." + (fw[3] & 0xF);
        String hwVersion = String.format("VID: 0x%02X%02X, PID: 0x%02X%02X", vidPid[0], vidPid[1], vidPid[2], vidPid[3]);

        info.fwVersion = fwVersion1;
        info.fwVersion2 = fwVersion2;
        info.hwVersion = hwVersion;

        events.add(parseBatteryInfo(batteryData[0], 1));
        events.add(parseBatteryInfo(batteryData[1], 2));
        events.add(parseBatteryInfo(batteryData[2], 0));
        events.add(info);

        return events.toArray(new GBDeviceEvent[0]);
    }

    private void decodeDeviceRunInfo(byte[] deviceRunInfoPayload) {
        int i = 0;
        while (i < deviceRunInfoPayload.length) {
            byte len = deviceRunInfoPayload[i];
            byte index = deviceRunInfoPayload[i + 1];
            switch (index) {
                case 0x09:
                    SharedPreferences preferences = getDevicePrefs().getPreferences();
                    SharedPreferences.Editor editor = preferences.edit();
                    byte mode = deviceRunInfoPayload[i + 2];
                    editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_AMBIENT_SOUND_CONTROL, Integer.toString(mode));
                    editor.apply();
                    break;
                // TODO: Autoplay?
                case 0x0A:
                    LOG.debug("AUTOPLAY: {}", deviceRunInfoPayload[i + 2]);
            }
            i += len + 1;
        }
    }

    private GBDeviceEvent[] decodeDeviceUpdate(Message updateMessage) {
        byte[] updatePayload = updateMessage.getPayload();
        List<GBDeviceEvent> events = new ArrayList<>();

        int i = 0;
        while (i < updatePayload.length) {
            byte len = updatePayload[i];
            byte index = updatePayload[i + 1];
            switch (index) {
                case 0x00:
                    events.add(parseBatteryInfo(updatePayload[i + 2], 1));
                    events.add(parseBatteryInfo(updatePayload[i + 3], 2));
                    events.add(parseBatteryInfo(updatePayload[i + 4], 0));
                    break;
                case 0x04:
                    SharedPreferences preferences = getDevicePrefs().getPreferences();
                    SharedPreferences.Editor editor = preferences.edit();

                    byte mode = updatePayload[i + 2];
                    editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_AMBIENT_SOUND_CONTROL, Integer.toString(mode));
                    editor.apply();
                    break;
                default:
                    LOG.debug("Unimplemented device update: {}", hexdump(updatePayload));
            }
            i += len + 1;
        }
        events.add(new GBDeviceEventSendBytes(new Message(MessageType.RESPONSE, Opcode.REPORT_STATUS, updateMessage.getSequenceNumber(), new byte[]{}).encode()));
        return events.toArray(new GBDeviceEvent[0]);
    }

    private GBDeviceEvent[] decodeNotifyConfig(Message notifyMessage) {

        byte[] notifyPayload = notifyMessage.getPayload();
        List<GBDeviceEvent> events = new ArrayList<>();

        int i = 0;
        while (i < notifyPayload.length) {
            byte len = notifyPayload[i];
            byte index = notifyPayload[i + 2];
            switch (index) {
                case 0x0C:
                    LOG.debug("Received earbuds position info");
                    /*
                    e.g. 0C 03
                            0011
                            wearing left, wearing right, left in case, right in case
                     */
                    break;
                case 0x0B:
                    SharedPreferences preferences = getDevicePrefs().getPreferences();
                    SharedPreferences.Editor editor = preferences.edit();

                    byte soundCtrlMode = notifyPayload[i + 3];
                    editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_AMBIENT_SOUND_CONTROL, Integer.toString(soundCtrlMode));

                    byte mode = notifyPayload[i + 4];
                    if (notifyPayload[i + 3] == 0x01) {
                        editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_NOISE_CANCELLING_STRENGTH, Integer.toString(mode));
                    } else {
                        editor.putString(DeviceSettingsPreferenceConst.PREF_REDMI_BUDS_5_PRO_TRANSPARENCY_STRENGTH, Integer.toString(mode));
                    }

                    editor.apply();
                    break;
            }

            i += len + 1;
        }
        events.add(new GBDeviceEventSendBytes(new Message(MessageType.RESPONSE, Opcode.NOTIFY_CONFIG, notifyMessage.getSequenceNumber(), new byte[]{}).encode()));
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

                LOG.debug("[INIT] Sending device run info request");
                Message runInfo = new Message(MessageType.PHONE_REQUEST, Opcode.GET_DEVICE_RUN_INFO, sequenceNumber++, new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});
                events.add(new GBDeviceEventSendBytes(runInfo.encode()));

                LOG.debug("[INIT] Sending configuration request");
                events.add(new GBDeviceEventSendBytes(encodeGetConfig()));
            } else if (response.getType() == MessageType.RESPONSE && response.getOpcode() == Opcode.GET_DEVICE_INFO) {
                LOG.debug("[INIT] Received device info");
                if (getDevice().getState() != GBDevice.State.INITIALIZED) {
                    events.addAll(Arrays.asList(decodeDeviceInfo(response.getPayload())));
                    LOG.debug("[INIT] Device Initialized");
                    events.add(new GBDeviceEventUpdateDeviceState(GBDevice.State.INITIALIZED));
                }
            } else if (response.getType() == MessageType.RESPONSE && response.getOpcode() == Opcode.GET_DEVICE_RUN_INFO) {
                LOG.debug("[INIT] Received device run info");
                decodeDeviceRunInfo(response.getPayload());
            } else if (response.getOpcode() == Opcode.REPORT_STATUS) {
                events.addAll(Arrays.asList(decodeDeviceUpdate(response)));
            } else if (response.getOpcode() == Opcode.GET_CONFIG) {

                decodeGetConfig(response.getPayload());

            } else if (response.getOpcode() == Opcode.NOTIFY_CONFIG) {
                events.addAll(Arrays.asList(decodeNotifyConfig(response)));
            } else {
                LOG.debug("[ERROR] Unhandled message: {}", response);
            }
        }
        return events.toArray(new GBDeviceEvent[0]);
    }


}
