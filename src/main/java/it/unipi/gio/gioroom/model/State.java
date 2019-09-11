package it.unipi.gio.gioroom.model;

import java.util.HashMap;
import java.util.Map;

public class State {
    private Float temperature;
    private Float brightness;
    private Float vase_moisture;
    private Integer shutter_height;

    public State(){}

    public State(Float temperature, Float brightness, Float vase_moisture, Integer shutter_height) {
        this.temperature = temperature;
        this.brightness = brightness;
        this.vase_moisture = vase_moisture;
        this.shutter_height = shutter_height;
    }

    public boolean isValid(){
        return temperature!=null && brightness!=null && vase_moisture!=null && shutter_height!=null;
    }

    public boolean satisfiesGoal(Goal g){
        boolean satisfied = false;
        for(Goal.Property p : g.getBounds().keySet()){
            Goal.Bound b = g.getBounds().get(p);
            switch (p){
                case TEMPERATURE:
                    satisfied = contains(b.getLowerBound(),b.getUpperBound(),temperature);
                    break;
                case BRIGHTNESS:
                    satisfied = contains(b.getLowerBound(),b.getUpperBound(),brightness);
                    break;
                case VASE_MOISTURE:
                    satisfied = contains(b.getLowerBound(),b.getUpperBound(),vase_moisture);
                    break;
                case SHUTTER_HEIGHT:
                    satisfied = contains(b.getLowerBound(),b.getUpperBound(),shutter_height);
                    break;
            }
            if(!satisfied){
                return false;
            }
        }
        return satisfied;
    }

    private boolean contains(float left, float right, float val){
        return left<=val && right>=val;
    }

    public Map<Goal.Property, Float> toAdjust(Goal g){
        Map<Goal.Property, Float> toAdjust = new HashMap<>();
        for(Goal.Property p : g.getBounds().keySet()){
            Goal.Bound b = g.getBounds().get(p);
            switch (p){
                case TEMPERATURE:
                    if(!contains(b.getLowerBound(),b.getUpperBound(),temperature)){
                        toAdjust.put(p, (b.getCenter()-temperature));
                    }
                    break;
                case BRIGHTNESS:
                    if(!contains(b.getLowerBound(),b.getUpperBound(),brightness)){
                        toAdjust.put(p, (b.getCenter()-brightness));
                    }
                    break;
                case VASE_MOISTURE:
                    if(!contains(b.getLowerBound(),b.getUpperBound(),vase_moisture)){
                        toAdjust.put(p, (b.getCenter()-vase_moisture));
                    }
                    break;
                case SHUTTER_HEIGHT:
                    if(!contains(b.getLowerBound(),b.getUpperBound(),shutter_height)){
                        toAdjust.put(p, (b.getCenter()-shutter_height));
                    }
                    break;
            }
        }
        return toAdjust;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Temperature: ").append(temperature);
        sb.append(" Brightness: ").append(brightness);
        sb.append(" Vase_Moisture: ").append(vase_moisture);
        sb.append(" Shutter_height: ").append(shutter_height);
        return sb.toString();
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getBrightness() {
        return brightness;
    }

    public void setBrightness(Float brightness) {
        this.brightness = brightness;
    }

    public Float getVase_moisture() {
        return vase_moisture;
    }

    public void setVase_moisture(Float vase_moisture) {
        this.vase_moisture = vase_moisture;
    }

    public Integer getShutter_height() {
        return shutter_height;
    }

    public void setShutter_height(Integer shutter_height) {
        this.shutter_height = shutter_height;
    }

}
