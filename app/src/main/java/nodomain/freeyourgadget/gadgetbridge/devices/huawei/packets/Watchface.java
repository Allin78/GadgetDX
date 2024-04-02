package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class Watchface {
    public static final byte id = 0x27;

    public static class WatchfaceParams {
        public static final byte id = 0x01;
        public static class Request extends HuaweiPacket {

            public Request(ParamsProvider paramsProvider) {
                super(paramsProvider);
                this.serviceId = Watchface.id;
                this.commandId = id;


                this.tlv = new HuaweiTLV()
                        .put(0x01)
                        .put(0x02)
                        .put(0x03)
                        .put(0x04)
                        .put(0x05)
                        .put(0x0e)
                        .put(0x0f);

                this.complete = true;

            }

            public static class Response extends HuaweiPacket {
                public Response (ParamsProvider paramsProvider) {
                    super(paramsProvider);
                }
            }
        }


    }

}
