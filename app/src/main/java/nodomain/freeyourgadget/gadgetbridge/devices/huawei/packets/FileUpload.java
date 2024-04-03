package nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiPacket;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.HuaweiTLV;

public class FileUpload {
    public static final byte id = 0x28;

    public static class FileUploadParams {
        public byte file_id = 0;
        public String protocolVersion = "";
        public short app_wait_time = 0;

        public byte bitmap_enable = 0;
        public short unit_size = 0;
        public int max_apply_data_size = 0;
        public short interval =0;
        public int received_file_size =0;

        public byte no_encrypt = 0;
    }

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

    public static class FileUploadConsultAck {
        public static final byte id = 0x04;
        public static class Request extends HuaweiPacket {
            public Request(ParamsProvider paramsProvider, byte noEncryption) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;
                this.tlv = new HuaweiTLV()
                        .put(0x7f, 0x000186A0) //ok
                        .put(0x01, (byte) 0x01); // filetype 1 - watchface, 2 -music
                if (noEncryption == 1)
                    this.tlv.put(0x09, (byte)0x01); // need on devices which generally encrypted, but files
                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {

            public FileUploadParams fileUploadParams;
            public Response (ParamsProvider paramsProvider) {
                super(paramsProvider);
            }

            @Override
            public void parseTlv() throws HuaweiPacket.ParseException {
                if (this.tlv.contains(0x01) && this.tlv.getBytes(0x01).length == 1)
                    this.fileUploadParams.file_id = this.tlv.getByte(0x01);
                if (this.tlv.contains(0x02))
                    this.fileUploadParams.protocolVersion = this.tlv.getString(0x02);
                if (this.tlv.contains(0x03) && this.tlv.getBytes(0x03).length == 2)
                    this.fileUploadParams.app_wait_time = this.tlv.getShort(0x03);
                if (this.tlv.contains(0x04))
                    this.fileUploadParams.bitmap_enable = this.tlv.getByte(0x04);
                if (this.tlv.contains(0x05) && this.tlv.getBytes(0x05).length == 2)
                    this.fileUploadParams.unit_size = this.tlv.getShort(0x05);
                if (this.tlv.contains(0x06) && this.tlv.getBytes(0x06).length == 4)
                    this.fileUploadParams.max_apply_data_size = this.tlv.getInteger(0x06);
                if (this.tlv.contains(0x07) && this.tlv.getBytes(0x07).length == 2)
                    this.fileUploadParams.interval = this.tlv.getShort(0x07);
                if (this.tlv.contains(0x08) && this.tlv.getBytes(0x08).length == 4)
                    this.fileUploadParams.received_file_size = this.tlv.getInteger(0x08);
                if (this.tlv.contains(0x09))
                    this.fileUploadParams.no_encrypt = this.tlv.getByte(0x09);

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

    public static class FileUploadResult {
        public static final byte id = 0x07;

        public static class Request extends HuaweiPacket {
            public Request(ParamsProvider paramsProvider) {
                super(paramsProvider);
                this.serviceId = FileUpload.id;
                this.commandId = id;
                this.tlv = new HuaweiTLV()
                        .put(0x7f, 0x000186A0) //ok
                        .put(0x01, (byte) 0x01); // filetype 1 - watchface, 2 -music

                this.complete = true;
            }
        }

        public static class Response extends HuaweiPacket {

            byte status = 0;
            public Response (ParamsProvider paramsProvider) {
                super(paramsProvider);
            }

            @Override
            public void parseTlv() throws HuaweiPacket.ParseException {
                if (this.tlv.contains(0x02) && this.tlv.getBytes(0x02).length == 1)
                    this.status = this.tlv.getByte(0x02);
            }

        }

    }


}
