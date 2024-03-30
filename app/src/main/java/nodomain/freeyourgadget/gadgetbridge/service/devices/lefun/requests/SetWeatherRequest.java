package nodomain.freeyourgadget.gadgetbridge.service.devices.lefun.requests;


import nodomain.freeyourgadget.gadgetbridge.devices.lefun.LefunConstants;
import nodomain.freeyourgadget.gadgetbridge.devices.lefun.commands.SetWeatherCommand;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.devices.lefun.LefunDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.miband.operations.OperationStatus;

public class SetWeatherRequest extends Request{


    private byte weather;
    private byte temp;
    private byte maxTemp;
    private byte minTemp;
    private byte airPressure;
    private byte altitude;
    private byte speed;

    // No idea what is this more exactly since my device does not display it
    // If anyone has any idea please update it with the current naming.
    private byte unknown;
    private byte humidity;



    public SetWeatherRequest(LefunDeviceSupport support, TransactionBuilder builder) {
        super(support, builder);
    }


    public void setWeather(byte weather) { this.weather = weather; }

    public void setMinTemp(byte minTemp) { this.minTemp = minTemp; }

    public void setMaxTemp(byte maxTemp) { this.maxTemp = maxTemp; }

    public void setTemp(byte temp) { this.temp = temp; }

    public void setAirPressure(byte airPressure) { this.airPressure = airPressure; }

    public void setAltitude(byte altitude) { this.altitude = altitude; }

    public void setSpeed(byte speed) { this.speed = speed; }

    // No idea what is this more exactly since my device does not display it
    // If anyone has any idea please update it with the current naming.
    public void setUnknown(byte unknown) { this.unknown = unknown; }

    public void setHumidity(byte humidity) { this.humidity = humidity; }

    @Override
    public byte[] createRequest() {
        SetWeatherCommand cmd = new SetWeatherCommand();
        cmd.setWeather(weather);
        cmd.setTemp(temp);
        cmd.setMaxTemp(maxTemp);
        cmd.setMinTemp(minTemp);
        cmd.setAirPressure(airPressure);
        cmd.setAltitude(altitude);
        cmd.setSpeed(speed);
        cmd.setUnknown(unknown);
        cmd.setHumidity(humidity);
        return cmd.serialize();
    }

    @Override
    public void handleResponse(byte[] data) {
        SetWeatherCommand cmd = new SetWeatherCommand();
        cmd.deserialize(data);
        if (!cmd.isSuccess())
            reportFailure("Could not set weather");

        operationStatus = OperationStatus.FINISHED;
    }

    @Override
    public int getCommandId() {
        return LefunConstants.CMD_WEATHER;
    }
}
