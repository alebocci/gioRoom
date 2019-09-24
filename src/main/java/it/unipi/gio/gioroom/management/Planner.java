package it.unipi.gio.gioroom.management;

import it.unipi.gio.gioroom.Logic;
import it.unipi.gio.gioroom.drivers.LightHourDriver;
import it.unipi.gio.gioroom.drivers.PresenceDriver;
import it.unipi.gio.gioroom.model.Goal;
import it.unipi.gio.gioroom.model.SchedulePolicy;
import it.unipi.gio.gioroom.model.State;
import it.unipi.gio.gioroom.model.User;
import it.unipi.gio.gioroom.rest.out.ShutterDriver;
import it.unipi.gio.gioroom.rest.out.VaseDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Planner {

    private static final Logger LOG = LoggerFactory.getLogger(Planner.class);


    private ShutterDriver shutter;
    private VaseDriver vase;
    private PresenceDriver presence;
    private LightHourDriver daytime;

    private ShutterDriver.LightLevel lastLevel= ShutterDriver.LightLevel.UNDEFINED;
    private LocalTime waitShutter=LocalTime.MIN;
    private LocalTime waitWatering=LocalTime.MIN;
    private final int secondsShutter = 30;
    private final int secondsWatering = 120;
    private final int secondsStandardWait = 90;
    private final int secondsErrorWait = 15;

    private float checkBrightness=0f;

    @Autowired
    public Planner(ShutterDriver shutter, VaseDriver vase, PresenceDriver presence, LightHourDriver daytime) {
        this.shutter = shutter;
        this.vase = vase;
        this.presence = presence;
        this.daytime = daytime;
    }

    public int plan(List<User> users ,Logic.Policy policy, SchedulePolicy schedulePolicy, Schedule schedule){
        List<List<User>> usersNow;
        //user list with priority at this time, if priority is enabled
        if(schedulePolicy.getUsersAtTime(LocalTime.now())!=null){
            usersNow = new ArrayList<>(schedulePolicy.getUsersAtTime(LocalTime.now()));
        }else{
            usersNow = new ArrayList<>();
            usersNow.add(users);
            policy = Logic.Policy.AVG;
        }

        Goal goalState = goalState(usersNow, policy,schedule);
        if(goalState==null){
            LOG.info("Goal state null");
            return secondsErrorWait;
        }

        State actualState = sense();
        if(actualState==null){
            LOG.info("Actual state null");
            return secondsErrorWait;
        }

        LOG.info("Actual state: "+actualState.toString());
        LOG.info("Goal state: "+goalState.toString());
        if(actualState.satisfiesGoal(goalState)){
            LOG.info("Actual state satisfy goal state");
            return secondsStandardWait;
        }
        Map<Goal.Property, Float> toAdjust = actualState.toAdjust(goalState);
        int waitSeconds = secondsStandardWait;
        for (Goal.Property g : toAdjust.keySet()){
            switch (g){
                case TEMPERATURE:
                    LOG.info("Adjust temperature by {}",toAdjust.get(g));
                    adjustTemperature(toAdjust.get(g));
                    break;
                case BRIGHTNESS:
                    LOG.info("Adjust brightness by {}",toAdjust.get(g));
                    adjustBrightness(toAdjust.get(g));
                    waitSeconds = Math.min(secondsShutter, waitSeconds);
                    break;
                case VASE_MOISTURE:
                    LOG.info("Adjust vase moisture by {}",toAdjust.get(g));
                    adjustVaseMoisture(toAdjust.get(g));
                    waitSeconds = Math.min(secondsWatering, waitSeconds);
                    break;
                case SHUTTER_HEIGHT:
                    break;
            }
        }
        return waitSeconds;
    }

    private void adjustTemperature(float delta){
        float sig = Math.signum(delta);
        float val = Math.abs(delta);
    }

    private void adjustBrightness(float delta){
        //wait time to stabilize precedent action
        if(LocalTime.now().compareTo(waitShutter)<0){
            LOG.info("Wait to precedent shutter action to became effective until "+waitShutter.toString());
            return;
        }
        float sig = Math.signum(delta);
        float val = Math.abs(delta);
        Integer height = shutter.getHeightValue();

        if(height==null){
            return;
        }
        if(daytime.getDaylight().containsTime(LocalTime.now())){
            if(sig<0){
                switch (lastLevel){
                    case BRIGHT:
                        lastLevel= ShutterDriver.LightLevel.MEDIUM;
                        break;
                    case MEDIUM:
                    case UNDEFINED:
                        lastLevel= ShutterDriver.LightLevel.LOW;
                        break;
                    case LOW:
                    case DARK:
                        if(shutter.getLastStatus()!= ShutterDriver.ShutterStatus.CLOSED) {
                            shutter.closeShutter();
                            LOG.info("Close Shutter");
                        }else{
                            LOG.info("Shutter already closed");
                        }
                        return;
                }


            }else {
                switch (lastLevel){
                    case LOW:
                    case UNDEFINED:
                        lastLevel= ShutterDriver.LightLevel.MEDIUM;
                        break;
                    case DARK:
                        lastLevel= ShutterDriver.LightLevel.LOW;
                        break;
                    case MEDIUM:
                    case BRIGHT:
                        if(shutter.getLastStatus()!= ShutterDriver.ShutterStatus.OPENED) {
                            shutter.openShutter();
                            LOG.info("Open shutter");
                        }else {
                            LOG.info("Shutter already opened");
                        }
                        return;
                }
            }


        }
        shutter.tiltShutter(lastLevel);
        waitShutter = LocalTime.now().plusSeconds(secondsShutter);
        LOG.info("Tilt shutter to {}",lastLevel);
    }

    private void adjustVaseMoisture(float delta){

        //wait time to stabilize precedent action
        if(LocalTime.now().compareTo(waitWatering)<0){
            LOG.info("Wait to precedent watering action to became effective");
            return;
        }

        float sig = Math.signum(delta);
        float val = Math.abs(delta);

        if(sig<0){
            vase.watering();
            waitWatering = LocalTime.now().plusSeconds(secondsWatering);
        }

    }
    private State sense(){
        Float temperature = vase.getTemperatureValue();
        Float moisture = vase.getMoistureValue();
        Float brightness = vase.getBrightnessValue();

        Integer height = shutter.getHeightValue();
        if(temperature==null || moisture==null || brightness==null || height==null){
            return null;
        }

        if(brightness>100.F){brightness=100.F;}
        if(brightness==0f){
            brightness=checkBrightness;
        }else{
            checkBrightness=brightness;
        }

        State actual = new State(temperature,brightness,moisture,height);


        if(actual.isValid()){
            return actual;
        } else {
            return null;
        }
    }

    private Goal goalState(List<List<User>> users, Logic.Policy policy, Schedule schedule){
        List<Goal> goalSlot =schedule.goalsAtTime(LocalTime.now()).stream()
                .filter(g->!g.getCondition().getPresence() || presence.userPresent(g.getUser()))
                .collect(Collectors.toList());
        if(goalSlot.isEmpty()){
            return null;
        }

        Goal goalState = new Goal();
        goalState.setBounds(new HashMap<>());
        for(Goal.Property p : Goal.Property.values()){
            //if it's nighttime, ignore brightness
            if(!daytime.getDaylight().containsTime(LocalTime.now()) && p==Goal.Property.BRIGHTNESS){
                continue;
            }
            Goal.Bound smallest = new Goal.Bound(-Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
            boolean empty = true;
            for(Goal g : goalSlot){
                Goal.Bound gBound = g.getBounds().get(p);
                if(gBound==null){continue;}
                else if(smallest.isConflict(gBound)){
                    switch (policy){
                        case AVG:
                            smallest = avg(p, goalSlot);
                            break;
                        case PRIORITY:
                        case SCHEDULE: //user list already selected
                            smallest = priority(p,goalSlot, users);
                            break;
                    }
                    empty=false;
                    break;
                }
                else if(smallest.contains(gBound)){
                    smallest=gBound;
                    empty=false;
                }else{
                    smallest = new Goal.Bound(Math.max(smallest.getLowerBound(), gBound.getLowerBound()),
                            Math.min(smallest.getUpperBound(),gBound.getUpperBound()));
                    empty=false;
                }
            }
            if(!empty) {
                goalState.getBounds().put(p, smallest);
            }
        }

        return goalState;
    }

    private Goal.Bound avg(Goal.Property property, List<Goal> goals){
        float denom = 0;
        float count = 0;
        float smallestGap = Float.MAX_VALUE;

        for(Goal g : goals){
            Goal.Bound b = g.getBounds().get(property);
            if(b!=null){
                denom++;
                count+= b.getCenter();
                float gap = (b.getUpperBound()-b.getLowerBound())/2;
                smallestGap = Math.min(smallestGap, gap);
            }
        }

        float avg = count/denom;
        return new Goal.Bound(avg-smallestGap,avg+smallestGap);
    }

    private Goal.Bound priority(Goal.Property property, List<Goal> goals, List<List<User>> users){
        //TODO avg
        for(List<User> list : users){
            for (User u : list) {
                for (Goal g : goals) {
                    if (g.getUser().equals(u)) {
                        Goal.Bound b = g.getBounds().get(property);
                        if (b != null) {
                            return b;
                        }
                    }
                }
            }

        }
        return null;

        /*for (User u : users) {
            for (Goal g : goals) {
                if (g.getUser().equals(u)) {
                    Goal.Bound b = g.getBounds().get(property);
                    if (b != null) {
                        return b;
                    }
                }
            }
        }
        return null;*/
    }


}
