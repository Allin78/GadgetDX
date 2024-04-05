package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FileUpload.FileUploadParams;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.GBZipFile;
import nodomain.freeyourgadget.gadgetbridge.util.UriHelper;
import nodomain.freeyourgadget.gadgetbridge.util.ZipFileException;

public class HuaweiUploadManager {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiUploadManager.class);
    private final HuaweiSupportProvider support;
    byte[] fileBin;
    byte[] fileSHA256;
    byte fileType = 1; // 1 - watchface, 2 - music
    int fileSize = 0;

    int currentUploadPosition = 0;
    int uploadChunkSize =0;

    String fileName = ""; //FIXME generate random name

    //ack values set from 28 4 response
    FileUploadParams fileUploadParams;


    public HuaweiUploadManager(HuaweiSupportProvider support) {
        this.support=support;
    }

    public void setWatchfaceUri(Uri uri) {

        UriHelper uriHelper;

        try {
            uriHelper = UriHelper.get(uri, support.getContext());

            GBZipFile watchfacePackage = new GBZipFile(uriHelper.openInputStream());
            fileBin = watchfacePackage.getFileFromZip("com.huawei.watchface");
            fileSize = fileBin.length;


        } catch (ZipFileException e) {
            LOG.error("Unable to read watchface file.", e);
            return;
        } catch (FileNotFoundException e) {
            LOG.error("The watchface file was not found.", e);
            return;
        } catch (IOException e) {
            LOG.error("General IO error occurred.", e);
            return;
        } catch (Exception e) {
            LOG.error("Unknown error occurred.", e);
            return;
        }


        try {
            MessageDigest m = MessageDigest.getInstance("SHA256");
            m.update(fileBin, 0, fileBin.length);
            fileSHA256 =  m.digest();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Digest alghoritm not found.", e);
            return;
        }

        currentUploadPosition = 0;
        uploadChunkSize = 0;
        //TODO: generate random watchfaceName and watchfaceVersion
        LOG.info("watchface loaded, SHA256: "+ GB.hexdump(fileSHA256) + " fileName: " + fileName);

    }

    public int getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte getFileType() {
        return this.fileType;
    }

    public byte[] getFileSHA256() {
        return fileSHA256;
    }

    public void setUploadChunkSize(int chunkSize) {
        uploadChunkSize = chunkSize;
    }

    public void setCurrentUploadPosition (int pos) {
        currentUploadPosition = pos;
    }

    public int getCurrentUploadPosition() {
        return currentUploadPosition;
    }

    public byte[] getCurrentChunk() {
        byte[] ret = new byte[uploadChunkSize];
        System.arraycopy(fileBin, currentUploadPosition, ret, 0, uploadChunkSize);
        return ret;
    }

    public void setFileUploadParams(FileUploadParams params) {
        this.fileUploadParams = params;
    }


    public short getUnitSize() {
        return fileUploadParams.unit_size;
    }

    public void setDeviceBusy() {
        final GBDevice device = support.getDevice();
        device.setBusyTask(support.getContext().getString(R.string.uploading_watchface));
        device.sendDeviceUpdateIntent(support.getContext());
    }

    public void unsetDeviceBusy() {
        final GBDevice device = support.getDevice();
        if (device != null && device.isConnected()) {
            if (device.isBusy()) {
                device.unsetBusyTask();
                device.sendDeviceUpdateIntent(support.getContext());
            }
            device.sendDeviceUpdateIntent(support.getContext());
        }
    }


}
