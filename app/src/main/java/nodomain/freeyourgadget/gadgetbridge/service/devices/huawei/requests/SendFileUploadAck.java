package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FileUpload;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;

public class SendFileUploadAck extends Request {
    byte noEncryption = 0;
    public SendFileUploadAck(HuaweiSupportProvider support, byte noEncryption) {
        super(support);
        this.serviceId = FileUpload.id;
        this.commandId = FileUpload.FileUploadConsultAck.id;
        this.noEncryption = noEncryption;
        this.addToResponse = false;
    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new FileUpload.FileUploadConsultAck.Request(this.paramsProvider, this.noEncryption ).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }
}
