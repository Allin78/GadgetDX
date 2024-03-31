package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class WatchfaceUpload {
    public static final byte id = 0x28;
    public static class WatchfaceStartSend {
        public static final byte id = 0x02;
        public static class Request extends HuaweiPacket {

            public Request(ParamsProvider paramsProvider,
                            int fileSize,
                            String watchfaceName,
                            String watchfaceVersion) {
                super(paramsProvider);
                this.serviceId = WatchfaceUpload.id;
                this.commandId = id;
                String filename = watchfaceName + "_" + watchfaceVersion;

                this.tlv = new HuaweiTLV()
                        .put(0x01, filename)
                        .put(0x02, fileSize)
                        .put(0x03, (byte) 0x1) // ???
                        .put(0x05, watchfaceName)
                        .put(0x06, watchfaceVersion);

                this.complete = true;

            }

            public static class Response extends HuaweiPacket {
                public Response (ParamsProvider paramsProvider) {
                    super(paramsProvider);
                }
            }
        }


    }

    public static class WatchfaceSendHash {
        public static final byte id = 0x03;

        public static class Request extends HuaweiPacket {

            public Request(ParamsProvider paramsProvider,
                            byte[] hash) {
                super(paramsProvider);
                this.serviceId = WatchfaceUpload.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x01, (byte) 1) //???
                        .put(0x03, hash);

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
