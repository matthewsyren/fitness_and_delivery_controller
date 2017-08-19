package a15008377.opsc7312assign1_15008377;

import java.io.Serializable;

/**
 * Created by Matthew Syrén on 2017/08/12.
 */

public class Run implements Serializable{
    private String startTime;
    private String endTime;
    private double distanceCovered;
    private double averageSpeed;
    private String imageUrl;

    //Constructor
    public Run(String startTime, String endTime, double distanceCovered, double averageSpeed) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.distanceCovered = distanceCovered;
        this.averageSpeed = averageSpeed;
    }

    public Run(){}

    //Accessor methods
    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public double getDistanceCovered() {
        return distanceCovered;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
