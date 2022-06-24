package nodomain.freeyourgadget.gadgetbridge.service.devices.msftband;

import android.net.Uri;

import java.util.ArrayList;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.service.serial.AbstractSerialDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceIoThread;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;

/**
 * MSFT Band support with classic bluetooth
 * WIP. works:
 * - NO fetaure support implemented for classic bluetooth!
 */
public class MsftBandDeviceSupport extends AbstractSerialDeviceSupport {


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

    /**
     * Gets the given option from the device, sets the values in the preferences.
     * The config name is device specific.
     *
     * @param config the device specific option to get from the device
     */
    @Override
    public void onReadConfiguration(String config) {

    }

    /**
     * Attempts to establish a connection to the device. Implementations may perform
     * the connection in a synchronous or asynchronous way.
     * Returns true if a connection attempt was made. If the implementation is synchronous
     * it may also return true if the connection was successfully established, however
     * callers shall not rely on that.
     * <p/>
     * The actual connection state change (successful or not) will be reported via the
     * #getDevice device as a device change Intent.
     *
     * @see #connectFirstTime()
     * @see GBDevice#ACTION_DEVICE_CHANGED
     */
    @Override
    public boolean connect() {
        getDeviceProtocol();
        getDeviceIOThread().start();
        return true;
    }

    /**
     * Returns true if a connection attempt shall be made automatically whenever
     * needed (e.g. when a notification shall be sent to the device while not connected.
     */
    @Override
    public boolean useAutoConnect() {
        return false;
    }

    /**
     * Factory method to create the device specific GBDeviceProtocol instance to be used.
     */
    @Override
    protected GBDeviceProtocol createDeviceProtocol() {
        return new MsftBandProtocol(getDevice());
    }

    /**
     * Factory method to create the device specific GBDeviceIoThread instance to be used.
     */
    @Override
    protected GBDeviceIoThread createDeviceIOThread() {
        return new MsftBandIOThread(getDevice(), getContext(), (MsftBandProtocol) createDeviceProtocol(), this, getBluetoothAdapter());
    }
}
