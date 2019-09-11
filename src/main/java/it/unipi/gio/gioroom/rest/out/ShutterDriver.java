package it.unipi.gio.gioroom.rest.out;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ShutterDriver {

    private static final Logger LOG = LoggerFactory.getLogger(ShutterDriver.class);

    private InetAddress ip;
    private int port;
    private String baseAddress;
    private RestTemplate restTemplate;

    public enum LightLevel {
        DARK, LOW, MEDIUM, BRIGHT, UNDEFINED
    }

    public ShutterDriver(InetAddress ip, int port, RestTemplate restTemplate){
        this.ip = ip;
        this.port = port;
        baseAddress = "http://"+ip.getHostName()+":"+port+"/api/";
        this.restTemplate = restTemplate;
        connectShutter();
    }

    private void connectShutter(){
        ResponseEntity<Void> response;
        try {
            restTemplate.exchange(baseAddress+"goal/disable", HttpMethod.PUT, HttpEntity.EMPTY,Void.class);
        }catch (HttpStatusCodeException | ResourceAccessException e){
            //do nothing
        }
    }

    public HttpStatus openShutter() {
        HttpStatus status;
        try {
            ResponseEntity<Void> response = restTemplate.exchange(baseAddress+"act/open", HttpMethod.PUT, HttpEntity.EMPTY,Void.class);
            status = response.getStatusCode();
        }catch (HttpStatusCodeException e){
            status= e.getStatusCode();
        }catch (ResourceAccessException e) {
                status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return status;
    }

    public HttpStatus closeShutter() {
        HttpStatus status;
        try {
            ResponseEntity<Void> response = restTemplate.exchange(baseAddress+"act/close",HttpMethod.PUT, HttpEntity.EMPTY, Void.class);
            status = response.getStatusCode();
        }catch (HttpStatusCodeException e){
            status= e.getStatusCode();
        }catch (ResourceAccessException e) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return status;
    }

    public HttpStatus tiltShutter(LightLevel l) {
        HttpStatus status;
        try {
            ResponseEntity<Void> response = restTemplate.exchange(baseAddress+"act/tilt?name="+l.name().toLowerCase(),HttpMethod.PUT, HttpEntity.EMPTY, Void.class);
            status = response.getStatusCode();
        }catch (HttpStatusCodeException e){
            status= e.getStatusCode();
        }catch (ResourceAccessException e) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return status;
    }

    public HttpStatus tiltThereShutter(boolean open){
        String op;
        if(open){op = "open";}
        else{op = "close";}
        HttpStatus status;
        try {
            ResponseEntity<Void> response = restTemplate.exchange(baseAddress+"act/tilt?name="+op,HttpMethod.PUT, HttpEntity.EMPTY, Void.class);
            status = response.getStatusCode();
        }catch (HttpStatusCodeException e){
            status= e.getStatusCode();
        }catch (ResourceAccessException e) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return status;
    }

    public HttpStatus goToShutter(int h) {
        HttpStatus status;
        try {
            ResponseEntity<Void> response = restTemplate.exchange(baseAddress+"act/goto?pos="+h,HttpMethod.PUT, HttpEntity.EMPTY, Void.class);
            status = response.getStatusCode();
        }catch (HttpStatusCodeException e){
            status= e.getStatusCode();
        }catch (ResourceAccessException e) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return status;
    }

    public HttpStatus stopShutter(){
        HttpStatus status;
        try {
            ResponseEntity<Void> response = restTemplate.exchange(baseAddress+"act/stop",HttpMethod.PUT, HttpEntity.EMPTY, Void.class);
            status = response.getStatusCode();
        }catch (HttpStatusCodeException e){
            status= e.getStatusCode();
        }catch (ResourceAccessException e) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return status;
    }

    public synchronized String getIp() {
        return ip.getHostName();
    }

    public synchronized void setIp(String ip) {
        if(ip==null){
            return;
        }
        if(!this.ip.getHostName().equals(ip)){
            try {
                this.ip = InetAddress.getByName(ip);
                baseAddress = "http://"+this.ip+":"+port+"/api";
            } catch (UnknownHostException | IllegalArgumentException e) {
                //do nothing
            }
        }

    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized void setPort(Integer port) {
        if(port==null){
            return;
        }
        baseAddress = "http://"+this.ip+":"+port+"/api";
        this.port = port;
    }

    public synchronized void setIpPort(String ip, int port){
        setIp(ip);
        setPort(port);
    }


    public ResponseEntity<String> getHeight() {
        try {
            return restTemplate.getForEntity(baseAddress+"sense/height",String.class);
        }catch (HttpStatusCodeException e){
            return ResponseEntity.status(e.getStatusCode()).build();
        }catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    public ResponseEntity<String> getTilt(){
        try {
            return restTemplate.getForEntity(baseAddress+"sense/tilt",String.class);
        }catch (HttpStatusCodeException e){
            return ResponseEntity.status(e.getStatusCode()).build();
        }catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    public Integer getHeightValue() {
        Integer height=null;
        try {
            ResponseEntity<SimpleResponse> response = restTemplate.getForEntity(baseAddress+"sense/height",SimpleResponse.class);
            SimpleResponse body = response.getBody();
            if(body!=null){height = body.height;}
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return null;
        }
        return height;
    }

    public String getTiltValue() {
        String tilt=null;
        try {
            ResponseEntity<SimpleResponse> response = restTemplate.getForEntity(baseAddress+"sense/tilt",SimpleResponse.class);
            SimpleResponse body = response.getBody();
            if(body!=null){tilt = body.tilt_level;}
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return null;
        }
        return tilt;
    }

    public static class SimpleResponse {
        Integer height;
        String tilt_level;

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public String getTilt_level() {
            return tilt_level;
        }

        public void setTilt_level(String tilt_level) {
            this.tilt_level = tilt_level;
        }
    }

}
