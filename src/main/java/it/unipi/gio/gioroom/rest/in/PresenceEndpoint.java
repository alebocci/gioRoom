package it.unipi.gio.gioroom.rest.in;

import it.unipi.gio.gioroom.drivers.PresenceDriver;
import it.unipi.gio.gioroom.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/presence")
public class PresenceEndpoint {

    public static class PresenceBody{
        private User user;
        private LocalTime duration;
        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public LocalTime getDuration() {
            return duration;
        }

        public void setDuration(LocalTime duration) {
            this.duration = duration;
        }

    }
    private PresenceDriver presenceDriver;

    public PresenceEndpoint(PresenceDriver presenceDriver){
        this.presenceDriver=presenceDriver;
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity setPresence(@RequestBody PresenceBody body) {
        if(body.user==null || body.duration==null){
            return ResponseEntity.badRequest().build();
        }
        if(!presenceDriver.addUserPresence(body.user,body.duration)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity deletePresence(@RequestBody PresenceBody body) {
        if(body.user==null){
            return ResponseEntity.badRequest().build();
        }
        if(!presenceDriver.removeUserPresence(body.user)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<User>> getPresence() {
        List<User> list = presenceDriver.getUsersPresent();
        return ResponseEntity.ok(list);
    }
}
