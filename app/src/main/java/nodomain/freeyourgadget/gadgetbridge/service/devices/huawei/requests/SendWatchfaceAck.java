package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.WatchfaceUpload;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiWatchfaceManager;

public class SendWatchfaceAck extends Request {
    public SendWatchfaceAck(HuaweiSupportProvider support) {
        super(support);
        this.serviceId = WatchfaceUpload.id;
        this.commandId = WatchfaceUpload.WatchfaceSendConsultAck.id;
    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new WatchfaceUpload.WatchfaceSendConsultAck.Request(this.paramsProvider ).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }
}
