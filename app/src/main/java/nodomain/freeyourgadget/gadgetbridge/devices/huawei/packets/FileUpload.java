package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class FileUpload {
    public static final byte id = 0x28;
    public static class FileInfoSend {
        public static final byte id = 0x02;
        public static class Request extends HuaweiPacket {

            public Request(ParamsProvider paramsProvider,
                            int fileSize,
                            String watchfaceName,
                            String watchfaceVersion) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;
                String filename = watchfaceName + "_" + watchfaceVersion;

                this.tlv = new HuaweiTLV()
                        .put(0x01, filename)
                        .put(0x02, fileSize)
                        .put(0x03, (byte) 0x1) // file type 1 - watchface, 2 - music
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

    public static class FileHashSend {
        public static final byte id = 0x03;

        public static class Request extends HuaweiPacket {

            public Request(ParamsProvider paramsProvider,
                            byte[] hash) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;

                this.tlv = new HuaweiTLV()
                        .put(0x01, (byte) 1) // filetype 1 - watchface, 2 - music
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

    public static class FileuploadConsultAck {
        public static final byte id = 0x04;
        public static class Request extends HuaweiPacket {
            public Request(ParamsProvider paramsProvider) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;
                this.tlv = new HuaweiTLV()
                        .put(0x7f, 0x000186A0) //ok
                        .put(0x01, (byte) 0x01) // filetype 1 - watchface, 2 -music
                        .put(0x09, (byte) 0x01); //??? present in watchface only
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {
            public Response (ParamsProvider paramsProvider) {
                super(paramsProvider);
            }
        }
    }

    public static class FileNextChunkParams extends HuaweiPacket {
        public static final byte id = 0x05;

        public int bytesUploaded = 0;
        public int nextchunkSize = 0;
        public FileNextChunkParams(ParamsProvider paramsProvider) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;
                this.complete = true;
        }
        @Override
        public void parseTlv() throws HuaweiPacket.ParseException {
            if (this.tlv.contains(0x02) && this.tlv.getBytes(0x02).length == 4)
                this.bytesUploaded = this.tlv.getInteger(0x02);
            if (this.tlv.contains(0x03) && this.tlv.getBytes(0x03).length == 4)
                this.nextchunkSize = this.tlv.getInteger(0x03);
        }

    }

    public static class FileNextChunkSend extends HuaweiPacket {
        public static final byte id = 0x06;

        public FileNextChunkSend(ParamsProvider paramsProvider) {
            super(paramsProvider);
            this.serviceId = FileUpload.id;
            this.commandId = id;
            this.complete = true;
        }
    }

}
