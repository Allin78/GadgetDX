package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Watchface;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;

public class GetWatchfaceParams extends Request{

    private static final Logger LOG = LoggerFactory.getLogger(GetWatchfaceParams.class);

    public GetWatchfaceParams(HuaweiSupportProvider support) {
        super(support);
        this.serviceId = Watchface.id;
        this.commandId = Watchface.WatchfaceParams.id;

    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new Watchface.WatchfaceParams.Request(paramsProvider).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }

    @Override
    protected void processResponse() throws ResponseParseException {
        if (!(receivedPacket instanceof Watchface.WatchfaceParams.Response))
            throw new ResponseTypeMismatchException(receivedPacket, Watchface.WatchfaceParams.Response.class);

        Watchface.WatchfaceParams.Response resp = (Watchface.WatchfaceParams.Response)(receivedPacket);
        supportProvider.getHuaweiCoordinator().setWatchfaceDeviceParams(resp.params);
    }
}
