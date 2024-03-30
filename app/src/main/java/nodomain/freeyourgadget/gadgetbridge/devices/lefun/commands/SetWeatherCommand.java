package nodomain.freeyourgadget.gadgetbridge.devices.lefun.commands;

import java.nio.ByteBuffer;

import nodomain.freeyourgadget.gadgetbridge.devices.lefun.LefunConstants;

public class SetWeatherCommand extends BaseCommand {
    private boolean success;
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

    public boolean isSuccess() {
        return success;
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
    protected void deserializeParams(byte id, ByteBuffer params) {
       validateIdAndLength(id, params, LefunConstants.CMD_WEATHER, 1);
        success = params.get() == 1;
    }

    @Override
    protected byte serializeParams(ByteBuffer params) {
        params.put(weather);
        params.put(temp);
        params.put(maxTemp);
        params.put(minTemp);
        params.put(airPressure);
        params.put(altitude);
        params.put(speed);
        params.put(unknown);
        params.put(humidity);

        return LefunConstants.CMD_WEATHER;
    }
}
