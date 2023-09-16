package nodomain.freeyourgadget.gadgetbridge.service.devices.sony.wena3.protocol.packets.status;

import lineageos.weather.util.WeatherUtils;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;

public class WeatherDay {
    public final Weather day;
    public final Weather night;
    public final int temperatureMaximum;
    public final int temperatureMinimum;

    public WeatherDay(Weather day, Weather night, int temperatureMaximum, int temperatureMinimum) {
        this.day = day;
        this.night = night;
        // For some reason, Wena uses Farenheit on the wire, but Celsius on display...
        // Assume a middle ground input in Kelvin.
        this.temperatureMaximum = Math.toIntExact(Math.round(WeatherUtils.celsiusToFahrenheit(temperatureMaximum - 273.15)));
        this.temperatureMinimum = Math.toIntExact(Math.round(WeatherUtils.celsiusToFahrenheit(temperatureMinimum - 273.15)));;
    }

    public static WeatherDay fromSpec(WeatherSpec.Daily daily) {
        return new WeatherDay(
                Weather.fromOpenWeatherMap(daily.conditionCode),
                Weather.fromOpenWeatherMap(daily.conditionCode),
                daily.maxTemp,
                daily.minTemp
        );
    }
}

