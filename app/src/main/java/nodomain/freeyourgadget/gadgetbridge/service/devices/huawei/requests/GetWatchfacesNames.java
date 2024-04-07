package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.Watchface;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huawei.HuaweiSupportProvider;

public class GetWatchfacesNames extends Request{

    private static final Logger LOG = LoggerFactory.getLogger(GetWatchfacesNames.class);

    List<Watchface.InstalledWatchfaceInfo> watchfaceInfoList;
    public GetWatchfacesNames(HuaweiSupportProvider support, List<Watchface.InstalledWatchfaceInfo> list) {
        super(support);
        this.serviceId = Watchface.id;
        this.commandId = Watchface.WatchfaceNameInfo.id;
        this.watchfaceInfoList = list;

    }

    @Override
    protected List<byte[]> createRequest() throws RequestCreationException {
        try {
            return new Watchface.WatchfaceNameInfo.Request(paramsProvider, this.watchfaceInfoList).serialize();
        } catch (HuaweiPacket.CryptoException e) {
            throw new RequestCreationException(e);
        }
    }

    @Override
    protected void processResponse() throws ResponseParseException {
        if (!(receivedPacket instanceof Watchface.WatchfaceNameInfo.Response))
            throw new ResponseTypeMismatchException(receivedPacket, Watchface.WatchfaceNameInfo.Response.class);

        Watchface.WatchfaceNameInfo.Response resp = (Watchface.WatchfaceNameInfo.Response)(receivedPacket);
        supportProvider.getHuaweiWatchfaceManager().setWatchfacesNames(resp.watchFaceNames);
    }
}
