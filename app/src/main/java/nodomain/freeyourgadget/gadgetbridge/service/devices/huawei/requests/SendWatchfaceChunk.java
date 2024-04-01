package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.WatchfaceUpload;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiWatchfaceManager;

public class SendWatchfaceChunk extends Request {
    HuaweiWatchfaceManager huaweiWatchfaceManager;
    public SendWatchfaceChunk(HuaweiSupportProvider support,
                              HuaweiWatchfaceManager watchfaceManager) {
        super(support);
        this.huaweiWatchfaceManager = watchfaceManager;
        this.serviceId = WatchfaceUpload.id;
        this.commandId = WatchfaceUpload.WatchfaceSendNextChunk.id;
    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
            //FIXME need new package type with raw data chunks ?
        return new WatchfaceUpload.WatchfaceSendNextChunk(this.paramsProvider).serializeFileChunk(
                huaweiWatchfaceManager.getCurrentChunk(),
                huaweiWatchfaceManager.getCurrentUploadPosition()
        );

    }

}
