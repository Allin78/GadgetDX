package nodomain.freeyourgadget.gadgetbridge.service.devices.freebuds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.service.AbstractHeadphoneDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;

public class FreebudsDeviceSupport extends AbstractHeadphoneDeviceSupport {

    private static final Logger LOG = LoggerFactory.getLogger(FreebudsDeviceSupport.class);

    @Override
    public void onSendConfiguration(String config) {
        super.onSendConfiguration(config);
    }

    @Override
    public void onTestNewFunction() {
        super.onTestNewFunction();
    }

    @Override
    public synchronized FreebudsIOThread getDeviceIOThread() {
        return (FreebudsIOThread) super.getDeviceIOThread();
    }

    @Override
    public boolean useAutoConnect() {
        return false;
    }

    protected GBDeviceProtocol createDeviceProtocol() {
        return new FreebudsProtocol(getDevice());
    }

    @Override
    protected GBDeviceIoThread createDeviceIOThread() {
        return new FreebudsIOThread(getDevice(), getContext(), (FreebudsProtocol) getDeviceProtocol(),
                FreebudsDeviceSupport.this, getBluetoothAdapter());
    }

}
