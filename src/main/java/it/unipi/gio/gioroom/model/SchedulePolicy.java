package it.unipi.gio.gioroom.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SchedulePolicy {
    private List<PolicyScheduleEntry> schedule;

    public SchedulePolicy(){
        schedule= new ArrayList<>();
    }

    public List<PolicyScheduleEntry> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<PolicyScheduleEntry> schedule) {
        this.schedule = schedule;
    }

    public boolean addSlot(Slot slot, List<User> users){
        PolicyScheduleEntry newEntry =new PolicyScheduleEntry(slot, users);
        schedule.add(newEntry);
        if(!checkScheduleValidity()){
            schedule.remove(newEntry);
            return false;
        }
        return true;
    }

    public boolean checkScheduleValidity(){
        if(schedule==null || schedule.isEmpty()){
            return false;
        }

        if(schedule.size()==1){
            return !schedule.get(0).getUsers().isEmpty();
        }

        schedule.sort(Comparator.comparing(s -> s.getSlot().getStart()));
        for (int i=0;i<schedule.size()-1;i++){
            PolicyScheduleEntry thisEntry = schedule.get(i);
            PolicyScheduleEntry nextEntry = schedule.get(i+1);
            if(thisEntry.getSlot().isOverlapping(nextEntry.getSlot())){
                return false;
            }
            if(thisEntry.getUsers()==null || nextEntry.getUsers()==null || thisEntry.getUsers().isEmpty()){
                return false;
            }
            if(thisEntry.getUsers().size()!=nextEntry.getUsers().size()){
                return false;
            }

            for(User u : thisEntry.getUsers()){
                if(!nextEntry.getUsers().contains(u)){
                    return false;
                }
            }
        }
        return true;
    }

    public List<User> getUsersAtTime(LocalTime time){
        Slot now = new Slot(LocalTime.MIN, time);
        schedule.sort(Comparator.comparing(s -> s.getSlot().getStart()));
        for (PolicyScheduleEntry e : schedule){
            if(e.getSlot().containsTime(time)){
                return e.getUsers();
            }
            if(e.getSlot().isAfter(now)){
                return null;
            }
        }

        return null;
    }

    public List<User> getUserList(){
        return schedule.get(0).getUsers();
    }


}
