package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Watchface;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;

public class GetWatchfacesList extends Request{

    private static final Logger LOG = LoggerFactory.getLogger(GetWatchfacesList.class);

    public GetWatchfacesList(HuaweiSupportProvider support) {
        super(support);
        this.serviceId = Watchface.id;
        this.commandId = Watchface.DeviceWatchInfo.id;

    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new Watchface.DeviceWatchInfo.Request(paramsProvider).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }

    @Override
    protected void processResponse() throws ResponseParseException {
        if (!(receivedPacket instanceof Watchface.DeviceWatchInfo.Response))
            throw new ResponseTypeMismatchException(receivedPacket, Watchface.DeviceWatchInfo.Response.class);

        Watchface.DeviceWatchInfo.Response resp = (Watchface.DeviceWatchInfo.Response)(receivedPacket);
        supportProvider.getHuaweiWatchfaceManager().setInstalledWatchfaceInfoList(resp.watchfaceInfoList);
    }
}
