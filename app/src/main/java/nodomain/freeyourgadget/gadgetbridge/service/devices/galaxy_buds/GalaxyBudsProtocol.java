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
        LOG.debug("message id: "+ message_id);
        LOG.debug("payload: "+ hexdump(payload));

        switch (message_id) {
            case battery_status:
                devEvts.add(handleBatteryInfo(Arrays.copyOfRange(payload,2,4)));
            case battery_status2:
                devEvts.add(handleBatteryInfo(Arrays.copyOfRange(payload,2,4)));
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

    private GBDeviceEvent handleBatteryInfo(byte[] payload) {
        LOG.debug("Battery payload: " + hexdump(payload));
        LOG.debug("pl: " + payload.length);
        LOG.debug("p0: " + payload[0]);
        LOG.debug("p1: " + payload[1]);
        /* payload:
        1st byte is number of batteries, then $number pairs follow:
        {idx, value}

        idx is 0x02 for left ear, 0x03 for right ear, 0x04 for case
        value goes from 0-64 (equivalent of 0-100 in hexadecimal)


        Since Gadgetbridge supports only one battery, we use an average of the levels for the
        battery level.
        If one of the batteries is recharging, we consider the battery as recharging.
         */

        GBDeviceEventBatteryInfo evBattery = new GBDeviceEventBatteryInfo();
        evBattery.level = 0;
        boolean batteryCharging = false;

        int numBatteries = payload.length;
        evBattery.level = (short) payload[0];
        //for (int i = 0; i < numBatteries; i++) {
          //  evBattery.level = (short) payload[0];
            //if (!batteryCharging)
            //    batteryCharging = ((payload[2 + 2 * i] & MASK_BATTERY_CHARGING) == MASK_BATTERY_CHARGING);
            //LOG.debug("single battery level: " + hexdump(payload, 2+2*i,1) +"-"+ ((payload[2+2*i] & 0xff))+":" + evBattery.level);
        //}

        evBattery.state = BatteryState.UNKNOWN;
        //evBattery.state = batteryCharging ? BatteryState.BATTERY_CHARGING : evBattery.state;

        return evBattery;
    }

    protected GalaxyBudsProtocol(GBDevice device) {
        super(device);

    }
}
