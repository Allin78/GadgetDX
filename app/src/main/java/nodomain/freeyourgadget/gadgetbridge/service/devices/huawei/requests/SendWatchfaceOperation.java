package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Watchface;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;

public class SendWatchfaceOperation extends Request {
    private String fileName;
    private byte operation;
    public SendWatchfaceOperation(HuaweiSupportProvider support, String filename, byte operation) {
        super(support);
        this.serviceId = Watchface.id;
        this.commandId = Watchface.WatchfaceOperation.id;
        this.fileName = filename;
        this.operation = operation;
    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new Watchface.WatchfaceOperation.Request(this.paramsProvider, this.fileName, this.operation ).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }

}
