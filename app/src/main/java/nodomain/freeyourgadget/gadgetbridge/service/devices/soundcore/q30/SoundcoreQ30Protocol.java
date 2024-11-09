package nodomain.freeyourgadget.gadgetbridge.service.devices.soundcore.q30;

import static nodomain.freeyourgadget.gadgetbridge.util.GB.hexdump;

import android.content.SharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.activities.devicesettings.DeviceSettingsPreferenceConst;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEvent;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.devices.soundcore.AbstractSoundcoreProtocol;
import nodomain.freeyourgadget.gadgetbridge.service.devices.soundcore.SoundcorePacket;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;

public class SoundcoreQ30Protocol extends AbstractSoundcoreProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(SoundcoreQ30Protocol.class);

    protected SoundcoreQ30Protocol(GBDevice device) {
        super(device);
    }

    @Override
    public GBDeviceEvent[] decodeResponse(byte[] responseData) {
        ByteBuffer buf = ByteBuffer.wrap(responseData);
        SoundcorePacket packet = SoundcorePacket.decode(buf);

        if (packet == null)
            return null;

        List<GBDeviceEvent> devEvts = new ArrayList<>();
        short cmd = packet.getCommand();
        byte[] payload = packet.getPayload();

        if (cmd == (short) 0x0101) {
            // a lot of other data is in here, anything interesting?
            String firmware1 = readString(payload, 39, 5);
            String firmware2 = "";
            String serialNumber = readString(payload, 44, 16);
            devEvts.add(buildVersionInfo(firmware1, firmware2, serialNumber));
        } else if (cmd == (short) 0x0106) { // ANC Mode Updated by Button
            decodeAudioMode(payload);
        } else if (cmd == (short) 0x0301) { // Battery Update
            int battery = payload[0] * 20; // untested
            devEvts.add(buildBatteryInfo(0, battery));
        } else if (cmd == (short) 0x8106) {
            // Acknowledgement for changed Ambient Mode
            // empty payload
        } else {
            LOG.debug("Unknown incoming message - command: " + cmd + ", dump: " + hexdump(responseData));
        }
        return devEvts.toArray(new GBDeviceEvent[devEvts.size()]);
    }


    @Override
    public byte[] encodeSendConfiguration(String config) {
        switch (config) {
            case DeviceSettingsPreferenceConst.PREF_SOUNDCORE_AMBIENT_SOUND_CONTROL:
            case DeviceSettingsPreferenceConst.PREF_SOUNDCORE_ANC_MODE:
                return encodeAudioMode();

            default:
                LOG.debug("Unsupported CONFIG: " + config);
        }

        return super.encodeSendConfiguration(config);
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
    private byte[] encodeAudioMode() {
        Prefs prefs = getDevicePrefs();

        byte ambient_sound_mode;
        switch (prefs.getString(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_AMBIENT_SOUND_CONTROL, "off")) {
            case "noise_cancelling":
                ambient_sound_mode = 0x00;
                break;
            case "ambient_sound":
                ambient_sound_mode = 0x01;
                break;
            case "off":
                ambient_sound_mode = 0x02;
                break;
            default:
                LOG.error("Invalid Ambient Mode selected");
                return null;
        }

        byte anc_mode;
        switch (prefs.getString(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_ANC_MODE, "transport")) {
            case "transport":
                anc_mode = 0x00;
                break;
            case "outdoor":
                anc_mode = 0x01;
                break;
            case "indoor":
                anc_mode = 0x02;
                break;
            default:
                LOG.error("Invalid ANC Mode selected");
                return null;
        }

        byte[] payload = new byte[]{ambient_sound_mode, anc_mode, 0x01};
        return new SoundcorePacket((short) 0x8106, payload).encode();
    }

    /**
     * Gets triggered when the button on the device is pressed or transparency toggled with the right palm.
     */
    private void decodeAudioMode(byte[] payload) {
        SharedPreferences prefs = getDevicePrefs().getPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        String ambient_sound_mode = "off";
        String anc_mode = "transport";

        if (payload[0] == 0x00) {
            ambient_sound_mode = "noise_cancelling";
        } else if (payload[0] == 0x01) {
            ambient_sound_mode = "ambient_sound";
        } else if (payload[0] == 0x02) {
            ambient_sound_mode = "off";
        }

        if (payload[1] == 0x00) {
            anc_mode = "transport";
        } else if (payload[1] == 0x01) {
            anc_mode = "outdoor";
        } else if (payload[1] == 0x02) {
            anc_mode = "indoor";
        }

        // payload has two more bytes
        // payload[2] always 1 ?
        // payload[3] checksum ?

        editor.putString(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_AMBIENT_SOUND_CONTROL, ambient_sound_mode);
        editor.putString(DeviceSettingsPreferenceConst.PREF_SOUNDCORE_ANC_MODE, anc_mode);
        editor.apply();
    }
}
