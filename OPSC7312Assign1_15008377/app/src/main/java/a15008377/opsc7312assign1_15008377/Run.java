package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.view.View;
import android.widget.ProgressBar;

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

    //Method fetches all the runs associated with the user's key, and adds them to an ArrayList. The data is then displayed in a ListView
    public void requestRuns(Context context, ResultReceiver resultReceiver){
        //Requests location information from the LocationService class
        String firebaseKey = new User(context).getUserKey();
        Intent intent = new Intent(context, FirebaseService.class);
        intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
        intent.setAction(FirebaseService.ACTION_FETCH_RUNS);
        intent.putExtra(FirebaseService.RECEIVER, resultReceiver);
        context.startService(intent);
    }
}
