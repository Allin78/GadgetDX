package nodomain.freeyourgadget.gadgetbridge.service.devices.galaxy_buds;

import static nodomain.freeyourgadget.gadgetbridge.util.CheckSums.getCRC16ansi;
import static nodomain.freeyourgadget.gadgetbridge.util.GB.hexdump;

import android.content.SharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

public class GalaxyBudsProtocol extends GBDeviceProtocol {
    private static final Logger LOG = LoggerFactory.getLogger(GalaxyBudsProtocol.class);

    final UUID UUID_DEVICE_CTRL = UUID.fromString("00001102-0000-1000-8000-00805f9b34fd");
    private static final byte PREAMBLE = (byte) 0xFE;
    private boolean isFirstExchange = true;

    private static final byte MASK_BATTERY = 0x7f;
    private static final byte MASK_BATTERY_CHARGING = (byte) 0x80;

    //incoming
    private static final byte battery_status = (byte) 0x60;
    private static final byte battery_status2 = (byte) 0x61;
    private static final short audio_mode_status = (short) 0xc01e;

    //outgoing
    private static final short find_device = (short) 0xf002;
    private static final short in_ear_detection = (short) 0xf004;
    private static final short audio_mode = (short) 0xf00f;

    @Override
    public GBDeviceEvent[] decodeResponse(byte[] responseData) {
        List<GBDeviceEvent> devEvts = new ArrayList<>();
        LOG.debug("received data: " + hexdump(responseData));

        if (isFirstExchange) {
            isFirstExchange = false;
            devEvts.add(new GBDeviceEventVersionInfo()); //TODO: this is a weird hack to make the DBHelper happy. Replace with proper + detection
        }

        ByteBuffer incoming = ByteBuffer.wrap(responseData);
        incoming.order(ByteOrder.LITTLE_ENDIAN);

        byte sof = incoming.get();
        if (sof != PREAMBLE) {
            LOG.error("Error in message, wrong start of frame: " + hexdump(responseData));
            return null;
        }
        byte type = incoming.get();
        byte length = incoming.get();
        byte message_id = incoming.get();

        byte[] payload = Arrays.copyOfRange(responseData, incoming.position(), incoming.position() + length);

        switch (message_id) {
            case battery_status:
                devEvts.addAll(Arrays.asList(handleBatteryInfo(Arrays.copyOfRange(payload, 1, 3))));
                break;
            case battery_status2:
                devEvts.addAll(Arrays.asList(handleBatteryInfo(Arrays.copyOfRange(payload, 2, 4))));
                break;
            default:
                LOG.debug("Unhandled: " + hexdump(responseData));

        }
        return devEvts.toArray(new GBDeviceEvent[devEvts.size()]);
    }



    byte[] encodeMessage(short control, short command, byte[] payload) {

        ByteBuffer msgBuf = ByteBuffer.allocate(8 + payload.length);
        msgBuf.order(ByteOrder.LITTLE_ENDIAN);
        msgBuf.put((byte) 0x55); //sof
        msgBuf.putShort(control);
        msgBuf.putShort(command);
        msgBuf.putShort((short) payload.length);
        msgBuf.put((byte) 0x00); //fsn TODO: is this always 0?
        msgBuf.put(payload);


        return msgBuf.array();
    }





    @Override
    public byte[] encodeFindDevice(boolean start) {
        byte payload = (byte) (start ? 0x01 : 0x00);
        return encodeMessage((short) 0x120, find_device, new byte[]{payload});
    }

    @Override
    public byte[] encodeSendConfiguration(String config) {
        return super.encodeSendConfiguration(config);
    }

    @Override
    public byte[] encodeSetTime() {
        // This are earphones, there is no time to set here. However this method gets called soon
        // after connecting, hence we use it to perform some initializations.
        // TODO: Find a way to send more requests during the first connection
        return new byte[0];
    }

    private GBDeviceEvent[] handleBatteryInfo(byte[] payload) {
        LOG.debug("Battery payload: " + hexdump(payload));
        LOG.debug("pl: " + payload.length);
        LOG.debug("p0: " + payload[0]);
        LOG.debug("p1: " + payload[1]);
        

        GBDeviceEventBatteryInfo evBattery1 = new GBDeviceEventBatteryInfo();
        evBattery1.batteryIndex = 0;
        evBattery1.level = 0;
        evBattery1.level = payload[0];
        evBattery1.state = BatteryState.UNKNOWN;

        GBDeviceEventBatteryInfo evBattery2 = new GBDeviceEventBatteryInfo();
        evBattery2.batteryIndex = 1;
        evBattery2.level = 0;
        evBattery2.level = payload[1];
        evBattery2.state = BatteryState.UNKNOWN;

        return new GBDeviceEvent[]{evBattery1, evBattery2};
    }

    protected GalaxyBudsProtocol(GBDevice device) {
        super(device);

    }
}
