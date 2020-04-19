package nodomain.freeyourgadget.gadgetbridge.entities;

public abstract class AbstractHybridHRActivitySample extends AbstractActivitySample {
    abstract public int getCalories();

    @Override
    public int getRawIntensity() {
        return getCalories();
    }
}
