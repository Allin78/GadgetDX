package nodomain.freeyourgadget.gadgetbridge.service.devices.freebuds;


import static nodomain.freeyourgadget.gadgetbridge.util.GB.hexdump;
import static nodomain.freeyourgadget.gadgetbridge.util.CheckSums.getCRC16xmodem;

import android.content.SharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryState;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;
import nodomain.freeyourgadget.gadgetbridge.devices.freebuds.AbstractFreebudsCoordinator;


public class FreebudsProtocol extends GBDeviceProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(FreebudsProtocol.class);
    final UUID UUID_DEVICE_CTRL = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private boolean isFirstExchange = true;


    //
    private static final byte SOM_BUDS = (byte) 0x5A;
    //incoming
    //Battery changed notification
    private static final short battery_status_chg = (short) 0x0127;
    //Response for fetch request
    private static final short battery_status_req_resp = (short) 0x0108;
    private static final short device_info_req_resp = (short) 0x0107;

    private static final short audio_mode_status_req_resp = (short) 0x2B2A;
    private static final short audio_mode_status_chg = (short) 0x2B04;

    private static final short in_ear_status_req_resp = (short) 0x2B11;
    private static final short in_ear_status_chg = (short) 0x2B10;


    //
    private static final byte battery_earphone_left = 0x05;
    private static final byte battery_earphone_right = 0x06;
    private static final byte battery_case = 0x07;
    //
    private static final byte audio_mode_anc = 0x01;
    private static final byte audio_mode_tra = 0x02;
    private static final byte audio_mode_off = 0x00;

    private static final byte in_ear_detection_off = 0x00;

    private static final byte in_ear_detection_on = 0x01;

    // The Basics of the protocol are described in https://mmk.pw/en/posts/freebuds-4i-proto/ and https://mmk.pw/en/posts/freelace-pro-proto/
    @Override
    public GBDeviceEvent[] decodeResponse(byte[] responseData) {
        List<GBDeviceEvent> devEvts = new ArrayList<>();
        LOG.debug("received data: " + hexdump(responseData));
        LOG.debug("received data length: " + responseData.length);

        if (isFirstExchange) {
            isFirstExchange = false;
            devEvts.add(new GBDeviceEventVersionInfo()); //TODO: this is a weird hack to make the DBHelper happy. Replace with proper + detection
        }

        ByteBuffer incoming = ByteBuffer.wrap(responseData);
        incoming.order(ByteOrder.BIG_ENDIAN);


        //msg Header
        byte sof = incoming.get();
        //first byte
        if (sof != SOM_BUDS) {
            LOG.error("Error in message, wrong start of frame: " + hexdump(responseData));
            return null;
        }
        // next two bytes
        // msg length value: parameters + (checksum (2b) + 1b extra)
        int length = incoming.getShort();

        byte zeroByte = incoming.get();
        //first byte
        if (zeroByte != 0x00) {
            LOG.error("Error in message, byte two not null: " + hexdump(responseData));
            return null;
        }
        //msg payload
        short message_id = incoming.getShort();
        byte[] payload;
        try {
            //copy without extra byte
            payload = Arrays.copyOfRange(responseData, incoming.position(), incoming.position() + length - 1);
        } catch (Exception e) {
            LOG.error("Error getting payload data: " + length + " , " + e);
            return null;
        }

        switch (message_id) {
            //battery state can have two different IDs but same payload format
            case battery_status_req_resp:
                LOG.debug("bat req-resp 0x0108: " + hexdump(payload));
            case battery_status_chg:
                LOG.debug("bat chg 0x0127: " + hexdump(payload));
                //copy without checksum
                //length is still three to long
                devEvts.addAll(handleBatteryInfo(Arrays.copyOfRange(payload, 0, length - 3)));
                break;

            case audio_mode_status_req_resp:
                devEvts.add(handleAudioModeStatus(Arrays.copyOfRange(payload, 0, length - 3)));
                LOG.debug("audio mode 0x2b2a: " + hexdump(payload));
                break;

            case in_ear_status_req_resp:
                devEvts.add(handleInEarState(Arrays.copyOfRange(payload, 0, length - 3)));
                LOG.debug("in ear detection 0x2b11: " + hexdump(payload));
                break;

            case device_info_req_resp:
                devEvts.add(handleDeviceInfo(Arrays.copyOfRange(payload, 0, length - 3)));
                LOG.debug("device info 0x0107: " + hexdump(payload));
                break;
            default:
                LOG.debug("Unhandled: " + hexdump(payload));

        }
        return devEvts.toArray(new GBDeviceEvent[devEvts.size()]);
    }

    byte[] encodeMessage(short command, byte[] payload) {
        //msg header length value: parameters + (checksum (2b) + 1b extra)
        short payload_size = (short) (3 + payload.length);

        ByteBuffer msgBuf = ByteBuffer.allocate(3 + payload_size);

        msgBuf.order(ByteOrder.BIG_ENDIAN);
        //msg structure
        //header
        msgBuf.put((byte) SOM_BUDS);  //magic byte
        msgBuf.putShort((short) payload_size);
        msgBuf.put((byte) 0x0); //const 0x0
        // msg payload command  + parameters as tlv
        msgBuf.putShort(command); //command id
        msgBuf.put(payload);
        //LOG.debug("outgoing - msg data: " + hexdump(msgBuf.array()));

        // checksum
        msgBuf.position(0);
        ByteBuffer crcBuf = ByteBuffer.allocate(msgBuf.capacity() + 2);
        crcBuf.order(ByteOrder.BIG_ENDIAN);
        crcBuf.put(msgBuf);
        crcBuf.putShort((short) getCRC16xmodem(msgBuf.array()));
        LOG.debug("Request will be sent: " + hexdump(crcBuf.array()));
        return crcBuf.array();
    }

    byte[] encodeBatteryStatusReq() {
        //Type-Length-Value(s): send one parameter with zero as length value
        return encodeMessage((short) battery_status_req_resp, new byte[]{0x01, (byte) 0x00});
    }

    byte[] encodeInEarStateReq() {
        //Type-Length-Value(s): send one parameter with zero as length value
        return encodeMessage((short) in_ear_status_req_resp, new byte[]{0x01, (byte) 0x00});
    }

    byte[] encodeDeviceInfoReq() {
        //Type-Length-Value(s): send one parameter with zero as length value
        return encodeMessage((short) device_info_req_resp, new byte[]{0x01, (byte) 0x00});
    }

    byte[] encodeAudioModeStatusReq() {
        //Type-Length-Value(s): send one parameter with zero as length value
        return encodeMessage((short) audio_mode_status_req_resp, new byte[]{0x01, (byte) 0x00});
    }

    // - Switching modes with GB is reported back correctly via audio feedback.
    // - The change by touch is adopted by GB in the settings page as the new active value.
    byte[] encodeAudioMode(String desired) {
        byte[] payload = new byte[]{0x01, 0x02, (byte) audio_mode_off, (byte) 0x00};

        switch (desired) {
            case "anc":
                payload[2] = (byte) audio_mode_anc;
                payload[3] = (byte) 0xff;
                break;
            case "transparency": //audio feedback calls this "awareness"
                payload[2] = (byte) audio_mode_tra;
                payload[3] = (byte) 0xff;
                break;
            case "off":
            default:
        }
        LOG.debug("Set Audio Mode: " + desired + " payload: " + hexdump(payload));

        return encodeMessage(audio_mode_status_chg, payload);
    }


// Should be doable, but I haven't found the ID and parameters yet.
    /*
    @Override
    public byte[] encodeFindDevice(boolean start) {
     //TODO find ID + parameter
      return encodeMessage(find_device_req_resp,payload);
     } */

    byte[] encodeInEarState(byte state) {
        byte[] payload = new byte[]{0x01, 0x01, (byte) state};
        LOG.debug("New In Ear config State: " + state + " payload: " + hexdump(payload));

        return encodeMessage(in_ear_status_chg, payload);
    }

    // The structure of this function is based on the NothingProtocol class.
    @Override
    public byte[] encodeSendConfiguration(String config) {

        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());

        switch (config) {
            case DeviceSettingsPreferenceConst.PREF_FREEBUDS5I_INEAR:
                return encodeInEarState((prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_FREEBUDS5I_INEAR, true) ? (byte) 0x01 : (byte) 0x00));


            case DeviceSettingsPreferenceConst.PREF_FREEBUDS5I_AUDIOMODE:
                return encodeAudioMode(prefs.getString(DeviceSettingsPreferenceConst.PREF_FREEBUDS5I_AUDIOMODE, "off"));


            default:
                LOG.debug("CONFIG: " + config);
        }

        return super.encodeSendConfiguration(config);
    }

    // The structure of this function is based on the GalaxyBudsProtocol class.
    private List<GBDeviceEvent> handleBatteryInfo(byte[] payload) {
        List<GBDeviceEvent> deviceEvents = new ArrayList<>();
        LOG.debug("Battery payload: " + hexdump(payload));
        LOG.debug("pl: " + payload.length);
        LOG.debug("p0 - Left: " + payload[battery_earphone_left]);
        LOG.debug("p1 - Right: " + payload[battery_earphone_right]);
        LOG.debug("p2 - Case: " + payload[battery_case]);

        int batteryLevel1 = payload[battery_earphone_left];
        int batteryLevel2 = payload[battery_earphone_right];
        int batteryLevel3 = payload[battery_case];

        int numBatteries = payload[4];


        GBDeviceEventBatteryInfo evBattery1 = new GBDeviceEventBatteryInfo();
        evBattery1.batteryIndex = 0;
        evBattery1.level = GBDevice.BATTERY_UNKNOWN;
        evBattery1.level = (batteryLevel1 > 0) ? batteryLevel1 : GBDevice.BATTERY_UNKNOWN;
        evBattery1.state = (batteryLevel1 > 0) ? BatteryState.BATTERY_NORMAL : BatteryState.UNKNOWN;
        deviceEvents.add(evBattery1);

        if (numBatteries > 1) {
            GBDeviceEventBatteryInfo evBattery2 = new GBDeviceEventBatteryInfo();
            evBattery2.batteryIndex = 1;
            evBattery2.level = GBDevice.BATTERY_UNKNOWN;
            evBattery2.level = (batteryLevel2 > 0) ? batteryLevel2 : GBDevice.BATTERY_UNKNOWN;
            evBattery2.state = (batteryLevel2 > 0) ? BatteryState.BATTERY_NORMAL : BatteryState.UNKNOWN;
            deviceEvents.add(evBattery2);

            GBDeviceEventBatteryInfo evBattery3 = new GBDeviceEventBatteryInfo();
            evBattery3.batteryIndex = 2; //case

            evBattery3.level = GBDevice.BATTERY_UNKNOWN;
            evBattery3.level = (batteryLevel3 > 0) ? batteryLevel3 : GBDevice.BATTERY_UNKNOWN;
            evBattery3.state = (batteryLevel3 > 0) ? BatteryState.BATTERY_NORMAL : BatteryState.UNKNOWN;
            deviceEvents.add(evBattery3);
        } else {
            LOG.debug("Fewer batteries found than expected: " + numBatteries);

        }

        return deviceEvents;
    }

    private GBDeviceEvent handleAudioModeStatus(byte[] payload) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());
        SharedPreferences.Editor editor = prefs.edit();
        byte audioMode = payload[3];


        switch (audioMode) {
            case audio_mode_off:
                editor.putString(DeviceSettingsPreferenceConst.PREF_FREEBUDS5I_AUDIOMODE, "off").apply();
                break;
            case audio_mode_anc:
                editor.putString(DeviceSettingsPreferenceConst.PREF_FREEBUDS5I_AUDIOMODE, "anc").apply();
                break;
            case audio_mode_tra:
                editor.putString(DeviceSettingsPreferenceConst.PREF_FREEBUDS5I_AUDIOMODE, "transparency").apply();
                break;
            default:
                LOG.warn("Unknown audio mode. Payload: " + hexdump(payload));

        }

        return null;
    }

    // Tested with Firefox playback on Android. No audio feedback available.
    private GBDeviceEvent handleInEarState(byte[] payload) {
        SharedPreferences prefs = GBApplication.getDeviceSpecificSharedPrefs(getDevice().getAddress());
        SharedPreferences.Editor editor = prefs.edit();
        byte state = payload[2];

        switch (state) {
            case in_ear_detection_off:
                editor.putBoolean(DeviceSettingsPreferenceConst.PREF_FREEBUDS5I_INEAR, false).apply();

                break;
            case in_ear_detection_on:
                editor.putBoolean(DeviceSettingsPreferenceConst.PREF_FREEBUDS5I_INEAR, true).apply();
                break;
            default:
                LOG.warn("Unknown in ear detection. Payload: " + hexdump(payload));

        }

        return null;
    }


    private GBDeviceEventVersionInfo handleDeviceInfo(byte[] payload) {
        LOG.debug("info : " + hexdump(payload));

        GBDeviceEventVersionInfo versionInfo = new GBDeviceEventVersionInfo();

        ByteBuffer incoming = ByteBuffer.wrap(payload);
        incoming.order(ByteOrder.BIG_ENDIAN);

        // The numbering of the parameters is a bit odd.
        // The length of the values in this payload is not static, e.g. the length of the firmware string can change after an update

        while (incoming.position() < incoming.limit() - 3) {
            byte len = payload[incoming.position() + 1];
            if (incoming.position() + len > incoming.limit()) {
                LOG.error(" length value is larger than remaining bytes " + hexdump(payload));

                break;
            }
            // The first parameter has the ID 2 and is no printable string - skip.
            // The next two IDs are 3 (HW) and 7 (SW). The rest is not used here.
            switch (payload[incoming.position()]) {

                case 0x03:
                    LOG.debug("hwVersion: " + incoming.position() + hexdump(payload, incoming.position() + 2, len));
                    versionInfo.hwVersion = new String(Arrays.copyOfRange(payload, incoming.position() + 2, incoming.position() + len + 2), StandardCharsets.UTF_8);
                    break;

                case 0x07:
                    LOG.debug("fwVersion: " + incoming.position() + hexdump(payload, incoming.position() + 2, len));
                    versionInfo.fwVersion = new String(Arrays.copyOfRange(payload, incoming.position() + 2, incoming.position() + len + 2), StandardCharsets.UTF_8);
                    break;

                default:
                    break;
            }
            incoming.position(incoming.position() + len + 2);
        }
        return versionInfo;
    }


    private AbstractFreebudsCoordinator getCoordinator() {
        return (AbstractFreebudsCoordinator) getDevice().getDeviceCoordinator();
    }

    protected FreebudsProtocol(GBDevice device) {
        super(device);
    }

}
