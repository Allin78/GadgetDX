package nodomain.freeyourgadget.gadgetbridge.devices.banglejs.sleep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.entities.AbstractActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;

public class BangleJsSleepFeatureExtractor implements SleepClassificationFeatureExtractor {
    private final int vectorLength = 3;
    @Override
    public float[] getVector(List<AbstractActivitySample> featureSamples) {
        if(featureSamples.isEmpty()){
            return new float[this.vectorLength];
        }
        ArrayList<Double> heartrates = new ArrayList<>();
        double hrSum = 0;
        double hr_min = Double.MAX_VALUE;
        double hr_max = Double.MIN_VALUE;
        int step_count = 0;
        int movement = 0;
        for (int i = 0; i < featureSamples.size(); i++) {
            ActivitySample entry = featureSamples.get(i);
            double currentHr = entry.getHeartRate();
            hrSum += currentHr;
            heartrates.add(currentHr);
            step_count += entry.getSteps();
            movement += entry.getRawIntensity();


            if (currentHr < hr_min) {
                hr_min = currentHr;
            }
            if (currentHr > hr_max) {
                hr_max = currentHr;
            }

        }

        Collections.sort(heartrates);
        double hr_median = 0;
        int hr_mid = heartrates.size()/2;
        if(heartrates.size()%2 ==0){
            hr_median = (heartrates.get(hr_mid)+heartrates.get(hr_mid-1))/2;
        }else{
            hr_median = heartrates.get(hr_mid);
        }


        double hr_mean = hrSum/heartrates.size();
        double hr_std = 0;
        for (double currentHeartrate : heartrates) {
            hr_std += Math.pow(currentHeartrate - hr_mean, 2);
        }
        hr_std = Math.sqrt(hr_std / heartrates.size());


        float[] features = new float[this.vectorLength];

        features[0] = (float) step_count / 1500;
        features[1] = ((float) movement) / 2048;
        features[2] = (float) (hr_median - 30) / 185;
        //features[3] = (float) (hr_min - 30) / 185;
        //features[4] = (float) (hr_max - 30) / 185;
        //features[5] = (float) (hr_std / 60);

        return features;
    }

    @Override
    public int getVectorSize() {
        return this.vectorLength;
    }
}
