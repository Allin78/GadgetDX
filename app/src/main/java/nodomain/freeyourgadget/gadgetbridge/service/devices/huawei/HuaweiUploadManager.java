package nodomain.freeyourgadget.gadgetbridge.service.devices.huawei;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import nodomain.freeyourgadget.gadgetbridge.devices.huawei.packets.FileUpload.FileUploadParams;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.GBZipFile;
import nodomain.freeyourgadget.gadgetbridge.util.UriHelper;
import nodomain.freeyourgadget.gadgetbridge.util.ZipFileException;

public class HuaweiUploadManager {
    private static final Logger LOG = LoggerFactory.getLogger(HuaweiUploadManager.class);
    private final HuaweiSupportProvider support;
    byte[] watchfaceBin;
    byte[] watchfaceSHA256;
    int fileSize = 0;

    int currentUploadPosition = 0;
    int uploadChunkSize =0;

    String watchfaceName = "413493857"; //FIXME generate random name
    String watchfaceVersion = "1.0.0"; //FIXME generate random version

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
            String watchfaceDescription = new String(watchfacePackage.getFileFromZip("description.xml"));
            watchfaceBin = watchfacePackage.getFileFromZip("com.huawei.watchface");
            fileSize = watchfaceBin.length;


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
            m.update(watchfaceBin, 0, watchfaceBin.length);
            watchfaceSHA256 =  m.digest();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Digest alghoritm not found.", e);
            return;
        }

        currentUploadPosition = 0;
        uploadChunkSize = 0;
        //TODO: generate random watchfaceName and watchfaceVersion
        LOG.info("watchface loaded, SHA256: "+ GB.hexdump(watchfaceSHA256) + " watchfaceName: " + watchfaceName + " watchfaceVersion: "+watchfaceVersion);

    }

    public int getFileSize() {
        return fileSize;
    }

    public String getWatchfaceName() {
        return watchfaceName;
    }

    public String getWatchfaceVersion() {
        return watchfaceVersion;
    }

    public byte[] getWatchfaceSHA256() {
        return watchfaceSHA256;
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
        System.arraycopy(watchfaceBin, currentUploadPosition, ret, 0, uploadChunkSize);
        return ret;
    }

    public void setFileUploadParams(FileUploadParams params) {
        this.fileUploadParams = params;
    }


    public short getUnitSize() {
        return fileUploadParams.unit_size;
    }


}
