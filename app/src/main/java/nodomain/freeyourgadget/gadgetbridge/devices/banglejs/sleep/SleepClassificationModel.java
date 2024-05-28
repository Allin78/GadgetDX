package nodomain.freeyourgadget.gadgetbridge.devices.banglejs.sleep;

import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.entities.AbstractActivitySample;

public class SleepClassificationModel {
    // This class uses a Tensorflow Lite Model to classify sleep phases.
    // The Training code/ data can be found at TODO <URL>

    private final int windowSize;
    private final int timeSliceLength;
    private Interpreter model = null;
    private SleepClassificationFeatureExtractor featureExtractor = null;


    public SleepClassificationModel(String assetName, SleepClassificationFeatureExtractor featureExtractor, int windowSize, int timeSliceLength) throws IOException {
        this.featureExtractor = featureExtractor;
        this.windowSize = windowSize;
        this.timeSliceLength = timeSliceLength;
        this.model = new Interpreter(this.loadModelFile(assetName));
    }

    public int getWindowSize() {
        return this.windowSize;
    }

    public int getTimeSliceLength() {
        return this.timeSliceLength;
    }

    public SleepLabelEnum predict(List<? extends AbstractActivitySample> allSamples, int timestamp) {
        float[][] output = new float[1][3];
        float[][][] input = this.createModelInput(allSamples, timestamp);
        this.model.run(input, output);
        return this.getSleepLabel(output[0]);
    }

    private SleepLabelEnum getSleepLabel(float[] labelScores) {
        int label_idx = -1;
        float max_val = 0;
        for (int i = 0; i < labelScores.length; i++) {
            if (labelScores[i] > max_val) {
                max_val = labelScores[i];
                label_idx = i;
            }
        }
        if(label_idx==-1 || max_val<0.4){
            return SleepLabelEnum.UNKNOWN;
        }

        //optionally set a minimum score and return UNKNOWN here, if max_val too low
        return SleepLabelEnum.values()[label_idx];
    }

    private List<List<AbstractActivitySample>> createTimeSlices(List<? extends AbstractActivitySample> allSamples, int timestampTo) {
        List<List<AbstractActivitySample>> result = new ArrayList<>();
        int timestampFrom = timestampTo - this.windowSize * this.timeSliceLength;
        for (int i = 0; i < this.windowSize; i++) {
            ArrayList<AbstractActivitySample> currentSamples = new ArrayList<>();
            int curTimestamp = timestampFrom + i * this.timeSliceLength;
            for (AbstractActivitySample sample : allSamples) {
                if (sample.getTimestamp() > curTimestamp && sample.getTimestamp() <= curTimestamp + this.timeSliceLength) {
                    currentSamples.add(sample);
                }
            }
            result.add(currentSamples);
        }
        return result;
    }

    private float[][][] createModelInput(List<? extends AbstractActivitySample> allSamples, int timestampTo) {
        float[][][] result = new float[1][this.windowSize][this.featureExtractor.getVectorSize()];

        List<List<AbstractActivitySample>> timeSlices = this.createTimeSlices(allSamples, timestampTo);
        for (int i = 0; i < this.windowSize; i++) {
            result[0][i] = this.featureExtractor.getVector(timeSlices.get(i));
        }
        return result;
    }

    private ByteBuffer loadModelFile(String assetName) throws IOException {
        try (AssetFileDescriptor fileDescriptor = GBApplication.getContext().getAssets().openFd(assetName)) {
            try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
                FileChannel fileChannel = inputStream.getChannel();
                long startOffset = fileDescriptor.getStartOffset();
                long declareLength = fileDescriptor.getDeclaredLength();
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength);
            }
        }
    }

    public int getInputTimeLength() {
        return this.getTimeSliceLength() * this.getWindowSize();
    }
}
