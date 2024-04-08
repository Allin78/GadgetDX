package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Watchface;

import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;

public class SendWatchfaceConfirm extends Request {
    private String fileName;
    public SendWatchfaceConfirm(HuaweiSupportProvider support, String filename) {
        super(support);
        this.serviceId = Watchface.id;
        this.commandId = Watchface.WatchfaceConfirm.id;
        this.fileName = filename;
        this.addToResponse = false;
    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new Watchface.WatchfaceConfirm.Request(this.paramsProvider, this.fileName ).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }

}
