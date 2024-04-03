package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;



import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FileUpload;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiUploadManager;

public class SendFileUploadInfo extends Request{
    HuaweiUploadManager huaweiUploadManager;
    public SendFileUploadInfo(HuaweiSupportProvider support,
                              HuaweiUploadManager huaweiUploadManager) {
        super(support);
        this.huaweiUploadManager = huaweiUploadManager;
        this.serviceId = FileUpload.id;
        this.commandId = FileUpload.FileInfoSend.id;

    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new FileUpload.FileInfoSend.Request(this.paramsProvider,
                    huaweiUploadManager.getFileSize(),
                    huaweiUploadManager.getFileName(),
                    huaweiUploadManager.getFileType()
            ).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }

}
