package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FileUpload;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiUploadManager;

public class SendFileUploadHash extends Request{
    HuaweiUploadManager huaweiUploadManager;
    public SendFileUploadHash(HuaweiSupportProvider support,
                              HuaweiUploadManager huaweiUploadManager) {
        super(support);
        this.huaweiUploadManager = huaweiUploadManager;
        this.serviceId = FileUpload.id;
        this.commandId = FileUpload.FileHashSend.id;
        this.addToResponse = false;
    }


    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new FileUpload.FileHashSend.Request(this.paramsProvider,
                    huaweiUploadManager.getFileSHA256()
            ).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }
}
