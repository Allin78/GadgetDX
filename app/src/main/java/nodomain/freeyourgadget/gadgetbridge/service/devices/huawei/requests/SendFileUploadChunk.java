package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FileUpload;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiUploadManager;

public class SendFileUploadChunk extends Request {
    HuaweiUploadManager huaweiUploadManager;
    public SendFileUploadChunk(HuaweiSupportProvider support,
                               HuaweiUploadManager watchfaceManager) {
        super(support);
        this.huaweiUploadManager = watchfaceManager;
        this.serviceId = FileUpload.id;
        this.commandId = FileUpload.FileNextChunkSend.id;
        this.addToResponse = false;
    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
            //FIXME need new package type with raw data chunks ?
        return new FileUpload.FileNextChunkSend(this.paramsProvider).serializeFileChunk(
                huaweiUploadManager.getCurrentChunk(),
                huaweiUploadManager.getCurrentUploadPosition(),
                huaweiUploadManager.getUnitSize()
        );

    }

}
