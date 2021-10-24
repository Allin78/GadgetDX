package nodomain.freeyourgadget.gadgetbridge.service.devices.galaxy_buds;

import android.net.Uri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.service.devices.nothing.Ear1Support;
import nodomain.freeyourgadget.gadgetbridge.service.devices.nothing.NothingIOThread;
import nodomain.freeyourgadget.gadgetbridge.service.devices.nothing.NothingProtocol;
import nodomain.freeyourgadget.gadgetbridge.service.serial.AbstractSerialDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;

public class GalaxyBudsDeviceSupport extends AbstractSerialDeviceSupport {
    private static final Logger LOG = LoggerFactory.getLogger(GalaxyBudsDeviceSupport.class);

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {

    }

    @Override
    public void onInstallApp(Uri uri) {

    }

    @Override
    public void onAppConfiguration(UUID appUuid, String config, Integer id) {

    }

    @Override
    public void onHeartRateTest() {

    }

    @Override
    public void onSetConstantVibration(int integer) {

    }

    @Override
    public void onSetHeartRateMeasurementInterval(int seconds) {

    }

    @Override
    public void onReadConfiguration(String config) {

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

    @Override
    public synchronized GalaxyBudsIOThread getDeviceIOThread() {
        return (GalaxyBudsIOThread) super.getDeviceIOThread();
    }


    @Override
    protected GBDeviceProtocol createDeviceProtocol() {
        return new GalaxyBudsProtocol(getDevice());
    }

    @Override
    protected GBDeviceIoThread createDeviceIOThread() {
        return new GalaxyBudsIOThread(getDevice(), getContext(), (GalaxyBudsProtocol) getDeviceProtocol(), GalaxyBudsDeviceSupport.this, getBluetoothAdapter());
    }
}