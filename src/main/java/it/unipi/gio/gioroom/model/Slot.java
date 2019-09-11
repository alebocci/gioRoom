package it.unipi.gio.gioroom.model;

import java.time.LocalTime;

public class Slot{
    private LocalTime start=LocalTime.MIN;
    private LocalTime stop=LocalTime.of(LocalTime.MAX.getHour(),LocalTime.MAX.getMinute());

    public Slot(LocalTime start, LocalTime stop){
        if(start.compareTo(stop)>0){throw new IllegalArgumentException("Start time must be before stop time.");}
        this.start=start;
        this.stop=stop;
    }

    public Slot(){}

    public Slot(Slot s){
        this.start = s.getStop();
        this.stop = s.getStop();
    }

    public void setStart(LocalTime start) {
        if(start.compareTo(stop)>0){throw new IllegalArgumentException("Start time must be before stop time.");}
        this.start = start;
    }

    public void setStop(LocalTime stop) {
        if(start.compareTo(stop)>0){throw new IllegalArgumentException("Start time must be before stop time.");}
        this.stop = stop;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getStop() {
        return stop;
    }

    //this slot is overlapping with at least a part of other slot
    public boolean isOverlapping(Slot other){
                return  (this.start.compareTo(other.start)<=0 && this.stop.compareTo(other.start)>0)    ||
                        (this.start.compareTo(other.stop)<0 && this.stop.compareTo(other.stop)>=0)      ||
                        this.isContained(other);
    }

    public boolean containsSlot(Slot other){
        return (this.start.compareTo(other.start)<=0 && this.stop.compareTo(other.stop)>=0);
    }

    public boolean containsTime(LocalTime time){
        return (this.start.compareTo(time)<=0 && this.stop.compareTo(time)>=0);
    }

    public boolean isContained(Slot other){
        return (this.start.compareTo(other.start)>=0 && this.stop.compareTo(other.stop)<=0);
    }

    public boolean isBefore(Slot other){
        return !isOverlapping(other) && this.start.compareTo(other.start)>0 && this.start.compareTo(other.stop)>0;
    }

    public  boolean isAfter(Slot other){
        return !isOverlapping(other) && this.start.compareTo(other.start)<0 && this.start.compareTo(other.stop)<0;
    }
}