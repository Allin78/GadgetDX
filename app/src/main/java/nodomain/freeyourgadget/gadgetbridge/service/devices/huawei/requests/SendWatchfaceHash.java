package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.WatchfaceUpload;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiWatchfaceManager;

public class SendWatchfaceHash extends Request{
    HuaweiWatchfaceManager huaweiWatchfaceManager;
    public SendWatchfaceHash(HuaweiSupportProvider support,
                             HuaweiWatchfaceManager huaweiWatchfaceManager) {
        super(support);
        this.huaweiWatchfaceManager = huaweiWatchfaceManager;
        this.serviceId = WatchfaceUpload.id;
        this.commandId = WatchfaceUpload.WatchfaceSendHash.id;
    }


    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new WatchfaceUpload.WatchfaceSendHash.Request(this.paramsProvider,
                    huaweiWatchfaceManager.getWatchfaceSHA256()
            ).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }
}
