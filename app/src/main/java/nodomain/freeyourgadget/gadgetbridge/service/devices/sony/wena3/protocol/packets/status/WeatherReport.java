package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.Wena3Packetable;

public class WeatherReport implements Wena3Packetable {
    public List<WeatherDay> fiveDays;

    public WeatherReport(List<WeatherDay> fiveDays) {
        this.fiveDays = fiveDays;
        assert this.fiveDays.size() == 5;
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer buf = ByteBuffer
                .allocate(21)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 0x03);

        for(int i = 0; i < 5; i++) {
            WeatherDay current = this.fiveDays.get(i);
            buf.put(current.day.packed());
            buf.put(current.night.packed());
            buf.put((byte) (current.temperatureMinimum + 100));
            buf.put((byte) (current.temperatureMaximum + 100));
        }

        return buf.array();
    }
}

