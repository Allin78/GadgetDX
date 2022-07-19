package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import nodomain.freeyourgadget.gadgetbridge.service.btle.BtLEServerAction;

public class WithingsServerAction extends BtLEServerAction
{
    private BluetoothGattCharacteristic characteristic;

    public WithingsServerAction(BluetoothDevice device, BluetoothGattCharacteristic characteristic) {
        super(device);
        this.characteristic = characteristic;
    }

    @Override
    public boolean expectsResult() {
        return false;
    }

    @Override
    public boolean run(BluetoothGattServer server) {
        return server.notifyCharacteristicChanged(getDevice(), characteristic, false);
    }
}
