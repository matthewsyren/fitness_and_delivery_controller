/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class provides a basis for a Run object
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.content.Intent;
import android.support.v4.os.ResultReceiver;
import java.io.Serializable;

public class Run implements Serializable{
    //Declarations
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

    //Default constructor
    public Run(){}

    //Getter methods
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

    //Setter method
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    //Method fetches all the runs associated with the user's key, and adds them to an ArrayList
    public void requestRuns(Context context, ResultReceiver resultReceiver){
        String firebaseKey = new User(context).getUserKey();
        Intent intent = new Intent(context, FirebaseService.class);
        intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
        intent.setAction(FirebaseService.ACTION_FETCH_RUNS);
        intent.putExtra(FirebaseService.RECEIVER, resultReceiver);
        context.startService(intent);
    }
}