package it.unipi.gio.gioroom.drivers;

import com.fasterxml.jackson.databind.ser.std.StdArraySerializers;
import it.unipi.gio.gioroom.model.Slot;
import org.apache.tomcat.jni.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class LightHourDriver {

    static class JSONtranslate{
        private Sys sys;
        private Integer timezone;

        public Sys getSys() {
            return sys;
        }


        public void setSys(Sys sys) {
            this.sys = sys;
        }

        public Integer getTimezone() {
            return timezone;
        }

        public void setTimezone(Integer timezone) {
            this.timezone = timezone;
        }

        Long getSunrise() {
            return sys.getSunrise();
        }
        Long getSunset() {
            return sys.getSunset();
        }

        public static class Sys{
            private Long sunrise;
            private Long sunset;

            Long getSunrise() {
                return sunrise;
            }

            public void setSunrise(Long sunrise) {
                this.sunrise = sunrise;
            }

            Long getSunset() {
                return sunset;
            }

            public void setSunset(Long sunset) {
                this.sunset = sunset;
            }
        }

    }
    private Slot daylight;
    private RestTemplate restTemplate = new RestTemplate();
    private LocalDate today = LocalDate.now();
    private final String appId="2dc9810e31efdc0237e5cc5480b0d431";
    private final String city="Pisa";

    public Slot getDaylight(){
        if(today.isAfter(LocalDate.now()) || daylight==null){
            today=LocalDate.now();
            JSONtranslate response;

            try {
                response = restTemplate.getForObject("http://api.openweathermap.org/data/2.5/weather?q="+city+"&APPID="+appId,JSONtranslate.class);
            }catch (HttpStatusCodeException | ResourceAccessException e){
                return daylight;
            }
            if(response==null){
                return daylight;
            }

            LocalTime sunset = Instant.ofEpochSecond(response.getSunset()).atZone(TimeZone.getDefault().toZoneId()).toLocalTime();
            LocalTime sunrise = Instant.ofEpochSecond(response.getSunrise()).atZone(TimeZone.getDefault().toZoneId()).toLocalTime();
            daylight = new Slot(sunrise,sunset);
        }
        return daylight;
    }

}
