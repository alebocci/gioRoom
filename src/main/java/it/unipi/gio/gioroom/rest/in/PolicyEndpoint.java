package it.unipi.gio.gioroom.rest.in;


import it.unipi.gio.gioroom.Logic;
import it.unipi.gio.gioroom.model.SchedulePolicy;
import it.unipi.gio.gioroom.model.Slot;
import it.unipi.gio.gioroom.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/policy")
public class PolicyEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyEndpoint.class);

    @Value("${auth_server.io}")
    private String authIp;

    @Value("${auth_server.port}")
    private Integer authPort;

    private RestTemplate restTemplate;

    private Logic logic;

    @Autowired
    public PolicyEndpoint(Logic logic, RestTemplate restTemplate){
        this.logic=logic;
        this.restTemplate=restTemplate;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getPolicy() {
        String policy = logic.getPolicy();
        HashMap<String, String> res = new HashMap<>();
        res.put("policy",policy);
        return ResponseEntity.ok().body(res);
    }

    @RequestMapping(value="/avg",method = RequestMethod.PUT)
    public ResponseEntity setAvg(@RequestHeader("Authorization") String token) {
        LOG.info("Set policy avg request, token auth: {}",token);
        if(!checkToken(token)){return ResponseEntity.status(HttpStatus.FORBIDDEN).build();}
        logic.setPolicy(Logic.Policy.AVG, null);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/priority",method = RequestMethod.PUT)
    public ResponseEntity setPriority(@RequestHeader("Authorization") String token,@RequestBody List<User> users) {
        LOG.info("Set policy priority request, token auth: {}",token);
        if(!checkToken(token)){return ResponseEntity.status(HttpStatus.FORBIDDEN).build();}
        if(users==null || users.isEmpty()){return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();}
        SchedulePolicy userList = new SchedulePolicy();
        userList.addSlot(new Slot(),users);
        logic.setPolicy(Logic.Policy.PRIORITY, userList);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/schedule",method = RequestMethod.PUT)
    public ResponseEntity setSchedule(@RequestHeader("Authorization") String token, @RequestBody SchedulePolicy schedule) {
        LOG.info("Set policy schedule request, token auth: {}",token);
        if(!checkToken(token)){return ResponseEntity.status(HttpStatus.FORBIDDEN).build();}
        if(schedule==null){return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();}
        if(!schedule.checkScheduleValidity()){return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();}
        logic.setPolicy(Logic.Policy.PRIORITY, schedule);
        return ResponseEntity.ok().build();
    }


    private boolean checkToken(String token){
       /*
        String baseAddress = "http://"+authIp+":"+authPort+"/api/authorization";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            restTemplate.exchange(baseAddress, HttpMethod.GET, entity,Void.class);
        }catch (HttpStatusCodeException | ResourceAccessException e){
            return false;
        }*/
        return true;
    }

}
