package it.unipi.gio.gioroom.model;

import java.util.ArrayList;
import java.util.List;

public class PolicyScheduleEntry {
    private Slot slot;
    private List<User> users;

    public PolicyScheduleEntry(){
        users= new ArrayList<>();
    }

    public PolicyScheduleEntry(Slot slot, List<User> users) {
        this.slot = slot;
        this.users = users;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
