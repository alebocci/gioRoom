package it.unipi.gio.gioroom.model;


import java.util.HashMap;
import java.util.Map;

public class Goal {

    private Integer goalId;
    private User user;
    private Condition condition;
    private Map<Property, Bound> bounds=new HashMap<>();

    public enum Property{TEMPERATURE, BRIGHTNESS, VASE_MOISTURE, SHUTTER_HEIGHT}

    public static class Condition{
        private Slot time=new Slot();
        private Boolean presence;

        public Slot getTime() {
            return time;
        }

        public void setTime(Slot time) {
            this.time = time;
        }

        public Boolean getPresence() {
            return presence;
        }

        public void setPresence(Boolean presence) {
            this.presence = presence;
        }

        public boolean isContainedCondition(Condition c){
            return (presence==c.getPresence()) && (time.isOverlapping(c.getTime()));
        }

        public boolean coincide(Condition c){
            return (presence==c.getPresence()) && (time.getStart().equals(c.getTime().getStart()))
                    && (time.getStop().equals(c.getTime().getStart()));
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("(Time: "); sb.append(time.getStart()); sb.append(" ->"); sb.append(time.getStop());
            sb.append("Presence: "); sb.append(presence);
            sb.append(")");
            return sb.toString();
        }
    }

    public static class Bound{
        private Float lowerBound;
        private Float upperBound;

        public Bound(){};
        public Bound(Float l, Float u){
            lowerBound=l;
            upperBound=u;
        }

        public Float getLowerBound() {
            return lowerBound;
        }

        public void setLowerBound(Float lowerBound) {
               this.lowerBound = lowerBound;
        }

        public Float getUpperBound() {
            return upperBound;
        }

        public Float getCenter(){
            return (upperBound+lowerBound)/2;
        }

        public void setUpperBound(Float upperBound) {
            this.upperBound = upperBound;
        }

        public boolean isConflict(Bound b){
            return (lowerBound>b.getLowerBound() && lowerBound>b.getUpperBound()) ||
                    (upperBound<b.getLowerBound() && upperBound<b.upperBound);
        }

        public boolean contains(Bound b){
            return lowerBound>=b.getLowerBound() && upperBound <=b.getUpperBound();
        }

        @Override
        public String toString(){
            return "Low: "+lowerBound+" Up: "+upperBound;
        }
    }



    public Goal(){}
    public Goal(int id){
        this.goalId = id;
    }

    public Integer getGoalId() {
        return goalId;
    }

    public void setGoalId(Integer goalId) {
        this.goalId = goalId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Map<Property, Bound> getBounds() {
        return bounds;
    }

    public void setBounds(Map<Property, Bound> bounds) {
        this.bounds = bounds;
    }

    public boolean checkValidity(){
        if(this.condition==null || this.user==null|| bounds==null || bounds.size()==0){return false;}

        for(Bound b : bounds.values()){
            if(b.getLowerBound()==null && b.getUpperBound()==null){return false;}
        }
        return true;
    }

    //Eliminate from this properties present in g
    public void resolveConflict(Goal g){
       for(Property p: g.getBounds().keySet()){
           bounds.remove(p);
       }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("id: "); sb.append(goalId);
        sb.append(" user: ");
        if(user!=null) {
            sb.append(user.getName());
        }else{
            sb.append("null");
        }
        sb.append(" condition: ");
        if(condition!=null) {
            sb.append(condition.toString());
        }else{
            sb.append("null");
        }
        sb.append(" bounds: [ ");
        for (Property b : bounds.keySet()){
            sb.append(b); sb.append(" :");
            sb.append(bounds.get(b).toString());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
/*

    public boolean checkValidity(){
        if(this.condition==null || this.user==null|| bounds==null || bounds.length==0){return false;}
        for(int i=0;i<bounds.length;i++){
            if(bounds[i].getLowerBound()==null && bounds[i].getUpperBound()==null){return false;}
            if(propertyPresent(bounds[i].getProperty(),i+1)){return false;}
        }
        return true;
    }
*/
    //Eliminate from this properties present in g
   /* public void resolveConflict(Goal g){
        ArrayList<Bound> newBounds = new ArrayList<>();
        for(Bound b : bounds){
            boolean found = false;
            for (Bound newB : g.getBounds()) {
                if (newB.getProperty() == b.getProperty()) {
                    found=true;
                    break;
                }
            }
            if(!found){
                newBounds.add(b);
            }
        }
        bounds=newBounds.toArray(new Bound[0]);
    }*/
/*
    private boolean propertyPresent(Bound.Property p, int startIndex){
        return indexPropertyPresent(p,startIndex)!=bounds.length;
    }
    private int indexPropertyPresent(Bound.Property p, int startIndex){
        int i=startIndex;
        for(;i<bounds.length;i++){
            if(bounds[i].getProperty()==p){return i;}
        }
        return i;
    }
*/

}
