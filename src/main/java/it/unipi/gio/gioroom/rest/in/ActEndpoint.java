package it.unipi.gio.gioroom.rest.in;


import it.unipi.gio.gioroom.Logic;
import it.unipi.gio.gioroom.rest.out.ShutterDriver;
import it.unipi.gio.gioroom.rest.out.VaseDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;

@RestController
@RequestMapping("/api/act")
public class ActEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(ActEndpoint.class);
    private ShutterDriver shutter;
    private VaseDriver vase;
    private Logic logic;
    @Autowired
    public ActEndpoint(ShutterDriver shutterDriver, VaseDriver vaseDriver, Logic logic){
        this.shutter=shutterDriver;
        this.vase=vaseDriver;
        this.logic=logic;
    }

    /*************************************************************
     *
     * SHUTTER ACT
     **************************************************************/

    @RequestMapping(value="/shutter/close",method = RequestMethod.PUT)
    public ResponseEntity closeShutter() {
        logic.deactivateGoal();
        HttpStatus response = shutter.closeShutter();
        return ResponseEntity.status(response).build();
    }

    @RequestMapping(value="/shutter/open",method = RequestMethod.PUT)
    public ResponseEntity openShutter() {
        logic.deactivateGoal();
        HttpStatus response = shutter.openShutter();
        return ResponseEntity.status(response).build();
    }

    @RequestMapping(value="/shutter/goto",method = RequestMethod.PUT)
    public ResponseEntity goTo(@RequestParam(name="pos")  int pos) {
        if(pos <0 || pos >100){
            return ResponseEntity.badRequest().body("Invalid position value.");
        }
        logic.deactivateGoal();
        HttpStatus response = shutter.goToShutter(pos);
        return ResponseEntity.status(response).build();
    }

    @RequestMapping(value="shutter/tilt",method = RequestMethod.PUT)
    public ResponseEntity tilt(@RequestParam(name="pos", required = false) Integer pos,
                               @RequestParam(name="name", required = false) String l) {
        if((pos==null && l==null) || (pos!=null && l!=null)){
            return ResponseEntity.badRequest().body("Either \"name\" or \"pos\" parameters are required.");
        }
        logic.deactivateGoal();
        ShutterDriver.LightLevel level;
        HttpStatus response;
        if (pos != null) {
            switch (pos) {
                case 1:
                    level = ShutterDriver.LightLevel.DARK;
                    break;
                case 2:
                    level = ShutterDriver.LightLevel.LOW;
                    break;
                case 3:
                    level = ShutterDriver.LightLevel.MEDIUM;
                    break;
                case 4:
                    level = ShutterDriver.LightLevel.BRIGHT;
                    break;
                case 0:
                    response= shutter.tiltThereShutter(false);
                    return ResponseEntity.status(response).build();
                case 5:
                    response = shutter.tiltThereShutter(true);
                    return ResponseEntity.status(response).build();
                default:
                    return ResponseEntity.badRequest().body("Invalid position name.");
            }
        }else {
            switch (l){
                case "dark":
                    level= ShutterDriver.LightLevel.DARK;
                    break;
                case "low":
                    level= ShutterDriver.LightLevel.LOW;
                    break;
                case "medium":
                    level= ShutterDriver.LightLevel.MEDIUM;
                    break;
                case "bright":
                    level= ShutterDriver.LightLevel.BRIGHT;
                    break;
                case "open":
                    response = shutter.tiltThereShutter(true);
                    return ResponseEntity.status(response).build();
                case "close":
                    response = shutter.tiltThereShutter(false);
                    return ResponseEntity.status(response).build();
                default:
                    return ResponseEntity.badRequest().body("Invalid position name.");
            }
        }
        response=shutter.tiltShutter(level);
        return ResponseEntity.status(response).build();
    }

    @RequestMapping(value="/shutter/stop",method = RequestMethod.PUT)
    public ResponseEntity stopShutter() {
        logic.deactivateGoal();
        HttpStatus response = shutter.stopShutter();
        return ResponseEntity.status(response).build();
    }


    /*************************************************************
     *
     * VASE ACT
     **************************************************************/
    @RequestMapping(value="/vase/watering",method = RequestMethod.PUT)
    public ResponseEntity watering() {
        logic.deactivateGoal();
        HttpStatus response = vase.watering();
        return ResponseEntity.status(response).build();
    }

    /*****************************************************************
     *
     * REACTIVATE GOAL LOGIC
     *****************************************************************/

    @RequestMapping(value="goal",method = RequestMethod.PUT)
    public ResponseEntity activateGoal(@RequestParam(name="time", required = false) String duration) {
        LocalTime activateTime = LocalTime.MIN;
        if(duration!=null){
            activateTime = LocalTime.parse(duration);
        }
        logic.activateGoal(activateTime);
        return ResponseEntity.ok().build();
    }

}
