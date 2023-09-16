package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.devices.flipper.zero.support.FlipperZeroBaseSupport;

public class SonyWena3DeviceSupport extends AbstractBTLEDeviceSupport {
    public SonyWena3DeviceSupport() {
        super(LoggerFactory.getLogger(SonyWena3DeviceSupport.class));
    }
    @Override
    public boolean useAutoConnect() {
        return true;
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));

        // TODO: init and all

        getDevice().setFirmwareVersion("???");
        getDevice().setFirmwareVersion2("??2");

        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
        return builder;
    }
}
