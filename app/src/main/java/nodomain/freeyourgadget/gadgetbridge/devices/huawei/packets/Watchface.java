package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class Watchface {
    public static final byte id = 0x27;

    public static class WatchfaceDeviceParams {
        public String maxVersion = "";
        public short width = 0;
        public short height = 0;
        public byte supportFileType = 1;
        public byte sort = 1;
        public String otherWatchfaceVersions = "";
    }

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

        }


        public static class Response extends HuaweiPacket {
            public WatchfaceDeviceParams params = new WatchfaceDeviceParams();
            public Response (ParamsProvider paramsProvider) {
                super(paramsProvider);
            }

            @Override
            public void parseTlv() throws ParseException {
                try {
                    if(this.tlv.contains(0x01))
                        this.params.maxVersion = this.tlv.getString(0x01);
                    if(this.tlv.contains(0x02))
                        this.params.width = this.tlv.getShort(0x02);
                    if(this.tlv.contains(0x03))
                        this.params.height = this.tlv.getShort(0x03);
                    if(this.tlv.contains(0x04))
                        this.params.supportFileType = this.tlv.getByte(0x04);
                    if(this.tlv.contains(0x05))
                        this.params.sort = this.tlv.getByte(0x05);
                    if(this.tlv.contains(0x06))
                        this.params.otherWatchfaceVersions = this.tlv.getString(0x06);
                } catch (MissingTagException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public static class WatchfaceOperation {
        public static final byte id = 0x03;

        public static class Request extends HuaweiPacket {
            public Request(ParamsProvider paramsProvider,
                           String fileName) {
                super(paramsProvider);
                this.serviceId = Watchface.id;
                this.tlv = new HuaweiTLV()
                        .put(0x01, fileName.split("_")[0])
                        .put(0x02, fileName.split("_")[1])
                        .put(0x03, (byte) 0x01);

                this.commandId = id;
            }


        }

        public static class Response extends HuaweiPacket {
            public Response (ParamsProvider paramsProvider) {
                super(paramsProvider);
            }
        }

    }

    public static class WatchfaceConfirm {
        public static final byte id = 0x05;

        public static class Request extends HuaweiPacket {
            public Request(ParamsProvider paramsProvider,
                           String fileName) {
                super(paramsProvider);
                this.serviceId = Watchface.id;
                this.tlv = new HuaweiTLV()
                        .put(0x01, fileName.split("_")[0])
                        .put(0x02, fileName.split("_")[1])
                        .put(0x7f, 0x000186A0);

                this.commandId = id;
            }

        }

        public static class Response extends HuaweiPacket {
            public Response (ParamsProvider paramsProvider) {
                super(paramsProvider);
            }
        }

    }


}
