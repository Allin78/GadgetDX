package nodomain.freeyourgadget.gadgetbridge.service.devices.jabra;


import nodomain.freeyourgadget.gadgetbridge.service.serial.AbstractSerialDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;

public class JabraDeviceSupport extends AbstractSerialDeviceSupport {

    @Override
    public void onSendConfiguration(String config) {
        super.onSendConfiguration(config);
    }

    @Override
    public void onTestNewFunction() {
        super.onTestNewFunction();
    }
    @Override
    public boolean connect() {
        getDeviceIOThread().start();
        return true;
    }

    @Override
    public synchronized JabraIOThread getDeviceIOThread() {
        return (JabraIOThread) super.getDeviceIOThread();
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    protected GBDeviceProtocol createDeviceProtocol() {
        return new JabraProtocol(getDevice());
    }

    @Override
    protected GBDeviceIoThread createDeviceIOThread() {
        return new JabraIOThread(getDevice(), getContext(), (JabraProtocol) getDeviceProtocol(),
                JabraDeviceSupport.this, getBluetoothAdapter());
    }


}
