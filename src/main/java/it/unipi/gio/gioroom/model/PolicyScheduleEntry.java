package it.unipi.gio.gioroom.model;

import java.util.ArrayList;
import java.util.List;

public class PolicyScheduleEntry {
    private Slot slot;
    private List<List<User>> users;

    public PolicyScheduleEntry(){
        users= new ArrayList<>();
    }

    public PolicyScheduleEntry(Slot slot, List<List<User>> users) {
        this.slot = slot;
        this.users = users;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public List<List<User>> getUsers() {
        return users;
    }

    public void setUsers(List<List<User>> users) {
        this.users = users;
    }

    public boolean containsUser(User user){
        for(List<User> list : users){
            for(User u : list){
                if(u.equals(user)){
                    return true;
                }
            }
        }
        return false;
    }

}
