package nodomain.freeyourgadget.gadgetbridge.service.devices.soundcore;

import static nodomain.freeyourgadget.gadgetbridge.util.GB.hexdump;

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

import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.soundcore.SoundcoreLiberty3ProCoordinator;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class SoundcoreLibertyProtocol extends GBDeviceProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(SoundcoreLibertyProtocol.class);

    private static final int battery_case = 0;
    private static final int battery_earphone_left = 1;
    private static final int battery_earphone_right = 2;


//    final UUID UUID_DEVICE_CTRL = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//    final UUID UUID_DEVICE_CTRL = UUID.fromString("0000110b-0000-1000-8000-00805f9b34fb"); //crash
//    final UUID UUID_DEVICE_CTRL = UUID.fromString("0000110e-0000-1000-8000-00805f9b34fb"); //crash
//    final UUID UUID_DEVICE_CTRL = UUID.fromString("0000111e-0000-1000-8000-00805f9b34fb");  // HFP data incoming
    final UUID UUID_DEVICE_CTRL = UUID.fromString("0cf12d31-fac3-4553-bd80-d6832e7b3952"); // no incoming data
//    final UUID UUID_DEVICE_CTRL = UUID.fromString("66666666-6666-6666-6666-666666666666"); // no incoming data

    private SoundcoreLiberty3ProCoordinator getCoordinator() {
        return (SoundcoreLiberty3ProCoordinator) getDevice().getDeviceCoordinator();
    }

    protected SoundcoreLibertyProtocol(GBDevice device) {
        super(device);

//        device.setFirmwareVersion("N/A");
//        device.setFirmwareVersion2("N/A");
//        device.setBatteryLevel(getCoordinator();
    }

    private String readString(byte[] data, int position, int size) {
        if (position + size > data.length) throw new IllegalStateException();
        return new String(data, position, size, StandardCharsets.UTF_8);
    }

    private GBDeviceEventBatteryInfo buildBatteryInfo(int batteryIndex, int level) {
        GBDeviceEventBatteryInfo info = new GBDeviceEventBatteryInfo();
        info.batteryIndex = batteryIndex;
        info.level = level;
        return info;
    }

    private GBDeviceEventVersionInfo buildVersionInfo(String firmware1, String firmware2, String serialNumber) {
        GBDeviceEventVersionInfo info = new GBDeviceEventVersionInfo();
        info.hwVersion = serialNumber;
        info.fwVersion = firmware1;
        info.fwVersion2 = firmware2;
        return info;
    }

    @Override
    public GBDeviceEvent[] decodeResponse(byte[] responseData) {
        // Byte 0-4: Header
        // Byte 5-6: Command (Audio-Mode)
        // Byte 7: Size of data
        // Byte 8-(x-1): Data
        // Byte x: Checksum
        if (responseData.length == 0) return null;

        List<GBDeviceEvent> devEvts = new ArrayList<>();

        byte[] command = Arrays.copyOfRange(responseData, 5, 7);
        byte[] data = Arrays.copyOfRange(responseData, 8, responseData.length-1);

        if (Arrays.equals(command, new byte[]{0x01, 0x01})) {
            // a lot of other data is in here, anything interesting?
            String firmware1 = readString(data, 7, 5);
            String firmware2 = readString(data, 12, 5);
            String serialNumber = readString(data, 17, 16);
            devEvts.add(buildVersionInfo(firmware1, firmware2, serialNumber));
        } else if (Arrays.equals(command, new byte[]{0x01, (byte) 0x8d})) {
            LOG.debug("Unknown incoming message - command: " + hexdump(command) + ", dump: " + hexdump(responseData));
        } else if (Arrays.equals(command, new byte[]{0x05, (byte) 0x82})) {
            LOG.debug("Unknown incoming message - command: " + hexdump(command) + ", dump: " + hexdump(responseData));
        } else if (Arrays.equals(command, new byte[]{0x05, 0x01})) {
            LOG.debug("Unknown incoming message - command: " + hexdump(command) + ", dump: " + hexdump(responseData));
        } else if (Arrays.equals(command, new byte[]{0x06, 0x01})) { //Sound Mode Update
            decodeAudioMode(data);
        } else if (Arrays.equals(command, new byte[]{0x01, 0x03})) { // Battery Update
            // unsure which battery is left and which is right
            int batteryLeft = data[1] * 20;
            int batteryRight = data[2] * 20;
            int batteryCase = data[3] * 20;

            devEvts.add(buildBatteryInfo(battery_case, batteryCase));
            devEvts.add(buildBatteryInfo(battery_earphone_left, batteryLeft));
            devEvts.add(buildBatteryInfo(battery_earphone_right, batteryRight));
        } else {
            // see https://github.com/gmallios/SoundcoreManager/blob/master/soundcore-lib/src/models/packet_kind.rs
            // for a mapping for other soundcore devices (similar protocol?)
            LOG.debug("Unknown incoming message - command: " + hexdump(command) + ", dump: " + hexdump(responseData));
        }
        return devEvts.toArray(new GBDeviceEvent[devEvts.size()]);
    }

    /**
     * Encodes the following settings to a payload to set the audio-mode on the headphones:
     * PREF_SOUNDCORE_AMBIENT_SOUND_CONTROL If ANC, Transparent or neither should be active
     * PREF_SOUNDCORE_ADAPTIVE_NOISE_CANCELLING If the strenght of the ANC should be set manual or adaptively according to ambient noise
     * PREF_SONY_AMBIENT_SOUND_LEVEL How strong the ANC should be in manual mode
     * PREF_SOUNDCORE_TRANSPARENCY_VOCAL_MODE If the Transparency should focus on vocals or should be fully transparent
     * PREF_SOUNDCORE_WIND_NOISE_REDUCTION If Transparency or ANC should reduce Wind Noise
     * @return The payload
     */
    byte[] encodeAudioMode() {
        Prefs prefs = getDevicePrefs();

        byte anc_mode;
        switch (prefs.getString(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_AMBIENT_SOUND_CONTROL, "off")) {
            case "noise_cancelling":
                anc_mode = 0x00;
                break;
            case "ambient_sound":
                anc_mode = 0x01;
                break;
            case "off":
                anc_mode = 0x02;
                break;
            default:
                LOG.error("Invalid Audio Mode selected");
                return null;
        }

        byte anc_strength;
        switch (prefs.getInt(DeviceSettingsPreferenceConst.PREF_SONY_AMBIENT_SOUND_LEVEL, 0)) {
            case 0:
                anc_strength = 0x10;
                break;
            case 1:
                anc_strength = 0x20;
                break;
            case 2:
                anc_strength = 0x30;
                break;
            default:
                LOG.error("Invalid ANC Strength selected");
                return null;
        }

        byte adaptive_anc = encodeBoolean(prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_ADAPTIVE_NOISE_CANCELLING, true));
        byte vocal_mode = encodeBoolean(prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_TRANSPARENCY_VOCAL_MODE, false));
        byte windnoise_reduction = encodeBoolean(prefs.getBoolean(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_WIND_NOISE_REDUCTION, false));

        byte[] payload = new byte[]{0x00, anc_mode, anc_strength, vocal_mode, adaptive_anc, windnoise_reduction, 0x01};
        return encodeMessage((byte) 0x06, (byte) 0x81, payload);
    }


    byte[] encodeMessage(byte command1, byte command2, byte[] payload) {
        int size = 8 + payload.length + 1;
        ByteBuffer msgBuf = ByteBuffer.allocate(size);
        msgBuf.order(ByteOrder.BIG_ENDIAN);
        msgBuf.put(new byte[] {0x08, (byte) 0xee, 0x00, 0x00, 0x00}); // header
        msgBuf.put(command1);
        msgBuf.put(command2);
        msgBuf.put((byte) size);

        msgBuf.put(payload);

        byte checksum = -10;
        checksum += command1 + command2 + size;
        for (int b : payload) {
            checksum += b;
        }
        msgBuf.put(checksum);

        return msgBuf.array();
    }

    void decodeAudioMode(byte[] payload) {
        SharedPreferences prefs = getDevicePrefs().getPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        String soundmode = "off";
        int anc_strength = 0;

        if (payload[1] == 0x00) {
            soundmode = "noise_cancelling";
        } else if (payload[1] == 0x01) {
            soundmode = "ambient_sound";
        } else if (payload[1] == 0x02) {
            soundmode = "off";
        }

        if (payload[2] == 0x10) {
            anc_strength = 0;
        } else if (payload[2] == 0x20) {
            anc_strength = 1;
        } else if (payload[2] == 0x30) {
            anc_strength = 2;
        }

        boolean vocal_mode = (payload[3] == 0x01);
        boolean adaptive_anc = (payload[4] == 0x01);
        boolean windnoiseReduction = (payload[5] == 0x01);

        editor.putString(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_AMBIENT_SOUND_CONTROL, soundmode);
        editor.putInt(DeviceSettingsPreferenceConst.PREF_SONY_AMBIENT_SOUND_LEVEL, anc_strength);
        editor.putBoolean(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_TRANSPARENCY_VOCAL_MODE, vocal_mode);
        editor.putBoolean(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_ADAPTIVE_NOISE_CANCELLING, adaptive_anc);
        editor.putBoolean(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_WIND_NOISE_REDUCTION, windnoiseReduction);
        editor.apply();
    }

    byte[] encodeDeviceInfoRequest() {
        byte[] payload = new byte[]{0x00};
        return encodeMessage((byte) 0x01, (byte) 0x01, payload);
    }

    byte[] encodeMysteryDataRequest1() {
        byte[] payload = new byte[]{0x00, 0x00};
        return encodeMessage((byte) 0x01, (byte) 0x8d, payload);
    }
    byte[] encodeMysteryDataRequest2() {
        byte[] payload = new byte[]{0x00};
        return encodeMessage((byte) 0x05, (byte) 0x01, payload);
    }

    byte[] encodeMysteryDataRequest3() {
        byte[] payload = new byte[]{0x00, 0x00};
        return encodeMessage((byte) 0x05, (byte) 0x82, payload);
    }

    byte encodeBoolean(boolean bool) {
        if (bool) return 0x01;
        else return 0x00;
    }

    @Override
    public byte[] encodeSendConfiguration(String config) {
        Prefs prefs = getDevicePrefs();

        switch (config) {
            case DeviceSettingsPreferenceConst.PREF_SOUNDCORE_AMBIENT_SOUND_CONTROL:
            case DeviceSettingsPreferenceConst.PREF_SOUNDCORE_WIND_NOISE_REDUCTION:
            case DeviceSettingsPreferenceConst.PREF_SOUNDCORE_TRANSPARENCY_VOCAL_MODE:
            case DeviceSettingsPreferenceConst.PREF_SOUNDCORE_ADAPTIVE_NOISE_CANCELLING:
            case DeviceSettingsPreferenceConst.PREF_SONY_AMBIENT_SOUND_LEVEL:
                return encodeAudioMode();
            default:
                LOG.debug("CONFIG: " + config);
        }

        return super.encodeSendConfiguration(config);
    }
}
