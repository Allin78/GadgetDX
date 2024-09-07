package nodomain.freeyourgadget.gadgetbridge.service.devices.redmibuds5pro;

import nodomain.freeyourgadget.gadgetbridge.service.serial.AbstractSerialDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;

public class RedmiBuds5ProDeviceSupport extends AbstractSerialDeviceSupport {
    @Override
    protected GBDeviceProtocol createDeviceProtocol() {
        return new RedmiBuds5ProProtocol(getDevice());
    }

    @Override
    protected GBDeviceIoThread createDeviceIOThread() {
        return new RedmiBuds5ProIOThread(getDevice(), getContext(),
                (RedmiBuds5ProProtocol) getDeviceProtocol(),
                RedmiBuds5ProDeviceSupport.this, getBluetoothAdapter());
    }

    @Override
    public boolean connect() {
        getDeviceIOThread().start();
        return true;
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }
}
