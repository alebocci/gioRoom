package it.unipi.gio.gioroom.management;

import it.unipi.gio.gioroom.model.Goal;
import it.unipi.gio.gioroom.model.Slot;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.*;

public class Schedule {
    private static final Logger LOG = LoggerFactory.getLogger(Schedule.class);

    private List<Pair<Slot, List<Goal>>> schedule;

    public Schedule(){
        schedule = new ArrayList<>();
        schedule.add(new Pair<>(new Slot(),new ArrayList<>()));
    }

    public Schedule(List<Goal> goalList){
        goalList.sort(Comparator.comparing(g -> g.getCondition().getTime().getStart()));
        schedule = new ArrayList<>();
        assignSlot(goalList,createSlots(goalList));
    }

    private List<Slot> createSlots(List<Goal> goalList){
        ArrayList<Slot> slots = new ArrayList<>();
        if(goalList.isEmpty()){
            slots.add(new Slot());
        }else{
            //lists with all the starts and stop of goals time
            LinkedList<LocalTime> starts = new LinkedList<>();
            LinkedList<LocalTime> stops = new LinkedList<>();
            starts.offer(goalList.get(0).getCondition().getTime().getStart());
            stops.offer(goalList.get(0).getCondition().getTime().getStop());
            for(Goal g : goalList){
                LocalTime start = g.getCondition().getTime().getStart();
                LocalTime stop = g.getCondition().getTime().getStop();
                if(!starts.peekLast().equals(start)){
                    starts.offer(start);
                }
                if(!stops.peekLast().equals(stop)){
                    stops.offer(stop);
                }
            }
            starts.sort(LocalTime::compareTo);
            stops.sort(LocalTime::compareTo);

            if(starts.isEmpty() || stops.isEmpty()){return slots;}
            LocalTime start = starts.pollFirst();
            if(start.compareTo(LocalTime.MIN)>0){
                slots.add(new Slot(LocalTime.MIN,start));
            }
            LocalTime stop=null;
            while(!(starts.isEmpty() && stops.isEmpty())){
                LocalTime first = starts.peekFirst();
                LocalTime second = stops.peekFirst();

                if(first==null){
                    stop = stops.pollFirst();
                }else if(second==null){
                    stop = starts.pollFirst();
                }else{
                    if(first.compareTo(second)<0){
                        stop=starts.pollFirst();
                    }else if(first.compareTo(second)>0){
                        stop=stops.pollFirst();
                    }else{
                        stop=starts.pollFirst();
                        stops.removeFirst();
                    }
                }
                slots.add(new Slot(start,stop));
                start=stop;
            }
            LocalTime max = LocalTime.of(23,59);
            if(stop.compareTo(max)<0){
                slots.add((new Slot(start,max)));
            }

        }
        return slots;
    }

    private void assignSlot(List<Goal> goalList, List<Slot> slotList){
        for(Slot s : slotList){
            List<Goal> goalInSlot = new ArrayList<>();
            for(Goal g : goalList){
                if(s.isOverlapping(g.getCondition().getTime())){
                    goalInSlot.add(g);
                }
            }
            Pair<Slot, List<Goal>> pair = new Pair<>(s,goalInSlot);
            schedule.add(pair);
        }
    }

    public List<Goal> goalsAtTime(LocalTime time){
        for(Pair<Slot, List<Goal>> pair : schedule){
            if(pair.getKey().containsTime(time)){
                return pair.getValue();
            }
        }
        return null;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Pair<Slot, List<Goal>> pair : schedule){
            sb.append('[');
            sb.append(pair.getKey().getStart());
            sb.append(" -> ");
            sb.append(pair.getKey().getStop());
            sb.append(']');
            sb.append("GOALS ID: ");
            if(pair.getValue().isEmpty()){
                sb.append("empty");
            }else{
                for (Goal g : pair.getValue()) {
                    sb.append(g.getGoalId());
                    sb.append(" - ");
                }
            }
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }


}
