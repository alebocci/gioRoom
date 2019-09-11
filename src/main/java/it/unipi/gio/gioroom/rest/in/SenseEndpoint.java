package it.unipi.gio.gioroom.rest.in;


import it.unipi.gio.gioroom.rest.out.ShutterDriver;
import it.unipi.gio.gioroom.rest.out.VaseDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/sense")
public class SenseEndpoint {

    private ShutterDriver shutter;
    private VaseDriver vase;

    @Autowired
    public SenseEndpoint(ShutterDriver shutter, VaseDriver vase){
        this.shutter = shutter;
        this.vase = vase;
    }

    /*************************************************************
     *
     * SHUTTER SENSE
     **************************************************************/
    @RequestMapping(value="/shutter/height",method = RequestMethod.GET)
    public ResponseEntity getHeight() {
        return shutter.getHeight();
    }

    @RequestMapping(value="/shutter/tilt",method = RequestMethod.GET)
    public ResponseEntity getTilt() {
        return shutter.getTilt();
    }

    /*************************************************************
     *
     * VASE SENSE
     **************************************************************/

    @RequestMapping(value="/temperature",method = RequestMethod.GET)
    public ResponseEntity getTemperature() {
        return vase.getTemperature();
    }

    @RequestMapping(value="/vase/moisture",method = RequestMethod.GET)
    public ResponseEntity getMoisture() {
        return vase.getMoisture();
    }

    @RequestMapping(value="/brightness",method = RequestMethod.GET)
    public ResponseEntity getBrightness() {
        return vase.getBrightness();
    }

}
