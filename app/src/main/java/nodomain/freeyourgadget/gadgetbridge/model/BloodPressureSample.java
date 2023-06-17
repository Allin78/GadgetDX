package nodomain.freeyourgadget.gadgetbridge.model;


public interface BloodPressureSample extends TimeSample {

    public Integer getSystolicPressure();

    public void setSystolicPressure(Integer i);

    public Integer getDiastolicPressure();

    public void setDiastolicPressure(Integer i);

    public Integer getPulse();

    public void setPulse(Integer i);
}
