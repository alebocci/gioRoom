package it.unipi.gio.gioroom.drivers;

import it.unipi.gio.gioroom.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PresenceDriver {
    private Map<User,LocalTime> usersPresent;
    private LocalTime allExpiration=LocalTime.MAX;
    private List<User> users;

    @Autowired
    public PresenceDriver(List<User> userList){
        usersPresent = new ConcurrentHashMap<>();
        //users = userList;
        users=null;

    }

    public List<User> getUsersPresent(){
        checkExpiration();
        return new ArrayList<>(usersPresent.keySet());
    }

    public boolean userPresent(User u){
        LocalTime exp = usersPresent.get(u);
        return !(exp==null || exp.compareTo(allExpiration)>0 || exp.compareTo(LocalTime.now())>0);
    }

    public synchronized boolean addUserPresence(User u, LocalTime duration){
        checkExpiration();
        if(users!=null && !users.contains(u)){
            return false;
        }
        LocalTime now = LocalTime.now();
        LocalTime expiration = LocalTime.now().plusHours(duration.getHour()).plusMinutes(duration.getMinute());
        if(expiration.compareTo(now)<=0 || expiration.compareTo(allExpiration)>0){
            return false;
        }
        usersPresent.put(u,expiration);
        return true;
    }

    public synchronized boolean removeUserPresence(User u){
        checkExpiration();
        if(users!=null && !users.contains(u)){
            return false;
        }
        return usersPresent.remove(u)!=null;
    }


    private void checkExpiration(){
        List<User> toRemove;
        if(users!=null){
            List<User> usersNow = new ArrayList<>(users); //to avoid concurrent change on original list
            toRemove = usersPresent.keySet().stream().filter(u->!usersNow.contains(u)).collect(Collectors.toList());
            usersPresent.keySet().removeAll(toRemove);
        }
        LocalTime now = LocalTime.now();
        if(now.compareTo(allExpiration)>0){
            usersPresent.clear();
            return;
        }
        toRemove = usersPresent.keySet().stream()
                .filter(user->usersPresent.get(user).compareTo(now)<=0)
                .collect(Collectors.toList());
        if(!toRemove.isEmpty()){
            usersPresent.keySet().removeAll(toRemove);
        }
    }

}
