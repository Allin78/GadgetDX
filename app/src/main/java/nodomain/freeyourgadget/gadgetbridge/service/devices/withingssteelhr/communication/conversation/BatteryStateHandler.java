package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.conversation;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.BatteryState;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.WithingsSteelHRDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures.BatteryValues;
import nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.message.Message;

public class BatteryStateHandler extends AbstractResponseHandler {

    public BatteryStateHandler(WithingsSteelHRDeviceSupport support) {
        super(support);
    }

    @Override
    public void handleResponse(Message response) {
        handleBatteryState(response.getStructureByType(BatteryValues.class));
    }

    private void handleBatteryState(BatteryValues batteryValues) {
        GBDeviceEventBatteryInfo batteryInfo = new GBDeviceEventBatteryInfo();
        batteryInfo.level = batteryValues.getPercent();
        switch (batteryValues.getStatus()) {
            case 0:
                batteryInfo.state = BatteryState.BATTERY_CHARGING;
                break;
            case 1:
                batteryInfo.state = BatteryState.BATTERY_LOW;
                break;
            default:
                batteryInfo.state = BatteryState.BATTERY_NORMAL;
        }
        batteryInfo.voltage = batteryValues.getVolt();
        support.evaluateGBDeviceEvent(batteryInfo);
    }
}
