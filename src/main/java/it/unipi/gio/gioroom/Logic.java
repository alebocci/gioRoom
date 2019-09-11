package it.unipi.gio.gioroom;

import it.unipi.gio.gioroom.management.Planner;
import it.unipi.gio.gioroom.management.Schedule;
import it.unipi.gio.gioroom.model.Goal;
import it.unipi.gio.gioroom.model.SchedulePolicy;
import it.unipi.gio.gioroom.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class Logic implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Logic.class);


    private Thread logicThread;
    private CopyOnWriteArrayList<Goal> goalList;
    private Schedule schedule;
    private Policy policy;
    private SchedulePolicy schedulePolicy;
    private List<User> users;
    private Planner planner;
    private AtomicBoolean goalActive = new AtomicBoolean(true);
    private LocalTime activationTime;

    private final int secondsLoop = 90;

    public enum Policy{AVG, PRIORITY, SCHEDULE}

    public Logic(CopyOnWriteArrayList<Goal> goalList, List<User> userList, Planner planner){
        this.goalList = goalList;
        policy = Policy.AVG;
        schedulePolicy = new SchedulePolicy();
        schedule = new Schedule();
        users = userList;
        this.planner = planner;
        activationTime = LocalTime.MAX;
        //autostart this runnable
        new Thread(this).start();
    }

    @Override
    public void run() {
        LOG.info("Logic started.");
        logicThread = Thread.currentThread();
        int secondsOfWorkLoop = secondsLoop;
        while(true){
            try {
                if(checkActivation()) {
                    secondsOfWorkLoop = planner.plan(users,policy,schedulePolicy, schedule);
                //LOG.info("Schedule:\n"+schedule.toString());
                }else{
                    LOG.info("Goal system non active");
                    secondsOfWorkLoop = secondsLoop;
                }
                LOG.info("Sleep for {} seconds", secondsOfWorkLoop);
                Thread.sleep(secondsOfWorkLoop * 1000);
            } catch (InterruptedException e) {
                LOG.info("Logic thread interrupt");
            }
        }
    }

    /**
     *
     * GOALS
     */

    public synchronized List<Goal> getGoals(){return goalList;}

    public synchronized Goal getGoalById(Integer id){
        for(Goal g :goalList){
            if(id.equals(g.getGoalId())){
                return g;
            }
        }
        return null;
    }

    public synchronized List<Goal> getGoalsByUser(String user){
        return goalList.stream().filter(g->g.getUser().getName().equals(user))
                .collect(Collectors.toList());
    }

    public synchronized List<Goal> getGoalsByUserId(Integer userId){
        return goalList.stream().filter(g->g.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    public synchronized boolean checkUser(User u){
        return users.contains(u);
    }

    public synchronized Goal addGoal(Goal newG){
        activateGoal(LocalTime.MIN);
        //check for conflict with same user previously set goals
        for(int i=0; i<goalList.size();i++){
            Goal g = goalList.get(i);
            //if same user add a new goal with condition in conflict with a previous one, remove the properties in common
            if(g.getUser().equals(newG.getUser()) && g.getCondition().isContainedCondition(newG.getCondition())){
                g.resolveConflict(newG);
                //if this goal has all properties removed, delete it
                if(g.getBounds().size()==0){
                    goalList.remove(i);
                    i--;
                }
            }
        }

        goalList.add(newG);
        goalList.sort(Comparator.comparing(g -> g.getCondition().getTime().getStart()));
        schedule = new Schedule(goalList); //TODO (maybe) don't recreate schedule, but modify it
        logicThread.interrupt();
        return newG ;
    }

    public synchronized boolean deleteGoalById(int id){
        for(Goal g : goalList){
            if(g.getGoalId()==id){
                goalList.remove(g);
                schedule = new Schedule(goalList);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean deleteGoalByUser(String user){
        boolean found = false;
        for(Goal g : goalList){
            if(g.getUser().getName().equals(user)){
                goalList.remove(g);
                found = true;
            }
        }
        if(found){
            schedule = new Schedule(goalList);
        }
        return found;
    }

    public synchronized boolean deleteGoalByUser(Integer userId){
        boolean found = false;
        for(Goal g : goalList){
            if(g.getUser().getId().equals(userId)){
                goalList.remove(g);
                found = true;
            }
        }
        if(found){
            schedule = new Schedule(goalList);
        }
        return found;
    }

    /**
     *
     * POLICIES
     */

    public synchronized String getPolicy() {
        return policy.name();
    }

    public synchronized void setPolicy(Policy newPolicy, SchedulePolicy policyUsers){
        if(policyUsers!=null) {
            this.users.clear();
            this.users.addAll(policyUsers.getUserList());
            // delete goals with users not of room
            List<Goal> toRemove = goalList.stream().filter(g -> !users.contains(g.getUser())).collect(Collectors.toList());
            if (toRemove.size() > 0) {
                goalList.removeAll(toRemove);
                schedule = new Schedule(goalList);
            }
            this.schedulePolicy=policyUsers;
        }

        if(this.policy!=newPolicy){
            this.policy=newPolicy;
        }
        logicThread.interrupt();
    }

    /**
     *
     * ACT AND REACTIVATE
     * */

    public synchronized void deactivateGoal(){
        if(!goalActive.get()){
            return;
        }
        goalActive.set(false);
    }

    public synchronized void activateGoal(LocalTime time){
        if(goalActive.get()){
            return;
        }
        if(time.compareTo(LocalTime.MIN)==0){
            goalActive.set(true);
            logicThread.interrupt();
        }else {
            activationTime = LocalTime.now().plusHours(time.getHour()).plusMinutes(time.getMinute());
        }
    }
    
    private synchronized boolean checkActivation(){
        if(goalActive.get()){
            return true;
        }
        if(LocalTime.now().compareTo(activationTime)>0){
            activationTime = LocalTime.MAX;
            goalActive.set(true);
            return true;
        }
        return false;
    }

}
