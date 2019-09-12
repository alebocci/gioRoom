package it.unipi.gio.gioroom.rest.out;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class VaseDriver {
     public static class VaseResponse{
        String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(VaseDriver.class);


    private InetAddress ip;
    private int port;
    private String baseAddress;
    private RestTemplate restTemplate;

    private String serverPort;

    public VaseDriver(InetAddress ip, int port, RestTemplate restTemplate, String serverPort){
        if(ip==null) return;
        this.ip = ip;
        this.port = port;
        baseAddress = "http://"+this.ip.getHostName()+":"+port+"/api";
        this.restTemplate = restTemplate;
        this.serverPort=serverPort;
        connectVase();
    }

    private void connectVase(){
        ResponseEntity<Void> response;
        try {
            HashMap<String,String> request = new HashMap<>();
            request.put("port",""+serverPort);
            HttpEntity<Map<String,String>> entity = new HttpEntity<>(request);
            restTemplate.exchange(baseAddress+"/goal/disable", HttpMethod.PUT, entity,Void.class);
        }catch (HttpStatusCodeException | ResourceAccessException e){
            //do nothing
        }
    }

    public HttpStatus watering(){
        HttpStatus status;
        try {
            ResponseEntity<Void> response = restTemplate.exchange(baseAddress+"/act/watering", HttpMethod.PUT, HttpEntity.EMPTY,Void.class);
            status = response.getStatusCode();
        }catch (HttpStatusCodeException e){
            status= e.getStatusCode();
        }catch (ResourceAccessException e) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return status;

    }

    public Float getTemperatureValue(){
        Float temperature=null;
        try {
            ResponseEntity<VaseResponse> response = restTemplate.getForEntity(baseAddress+"/sense/temperature",VaseResponse.class);
            VaseResponse body = response.getBody();
            if(body!=null){temperature = Float.parseFloat(body.value);}
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return null;
        }
        return temperature;
    }

    public Float getBrightnessValue(){
        Float brightness=null;
        try {
            ResponseEntity<VaseResponse> response = restTemplate.getForEntity(baseAddress+"/sense/brightness",VaseResponse.class);
            VaseResponse body = response.getBody();
            if(body!=null){brightness = Float.parseFloat(body.value);}
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return null;
        }
        return brightness;
    }

    public Float getMoistureValue(){
        Float moisture=null;
        try {
            ResponseEntity<VaseResponse> response = restTemplate.getForEntity(baseAddress+"/sense/moisture",VaseResponse.class);
            VaseResponse body = response.getBody();
            if(body!=null){moisture = Float.parseFloat(body.value);}
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return null;
        }
        return moisture;
    }

    public ResponseEntity<VaseResponse> getTemperature(){
        ResponseEntity<VaseResponse> response;
        try {
            response = restTemplate.getForEntity(baseAddress+"/sense/temperature", VaseResponse.class);
        }catch (HttpStatusCodeException e){
            return ResponseEntity.status(e.getStatusCode()).build();
        }catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if(response.getBody()==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response.getBody());

    }

    public ResponseEntity<VaseResponse> getMoisture(){
        ResponseEntity<VaseResponse> response;
        try {
            response = restTemplate.getForEntity(baseAddress+"/sense/moisture",VaseResponse.class);
        }catch (HttpStatusCodeException e){
            return ResponseEntity.status(e.getStatusCode()).build();
        }catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if(response.getBody()==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response.getBody());

    }

    public ResponseEntity<VaseResponse> getBrightness(){
        ResponseEntity<VaseResponse> response;
        try {
            response = restTemplate.getForEntity(baseAddress+"/sense/brightness", VaseResponse.class);
        }catch (HttpStatusCodeException e){
            return ResponseEntity.status(e.getStatusCode()).build();
        }catch (ResourceAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if(response.getBody()==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response.getBody());
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
}

