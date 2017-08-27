/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class provides a template for a Delivery object
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Handler;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.os.ResultReceiver;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class Delivery implements Serializable {
    //Declarations
    private String deliveryID;
    private String deliveryClientID;
    private String deliveryDate;
    private int deliveryComplete;
    private ArrayList<DeliveryItem> lstDeliveryItems;

    //Constructor
    public Delivery(String deliveryID, String deliveryClientID, String deliveryDate, int deliveryComplete, ArrayList<DeliveryItem> lstDeliveryItems) {
        this.deliveryID = deliveryID;
        this.deliveryClientID = deliveryClientID;
        this.deliveryDate = deliveryDate;
        this.deliveryComplete = deliveryComplete;
        this.lstDeliveryItems = lstDeliveryItems;
    }

    public Delivery(){}

    //Getter methods
    public String getDeliveryID() {
        return deliveryID;
    }

    public String getDeliveryClientID() {
        return deliveryClientID;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public int getDeliveryComplete() {
        return deliveryComplete;
    }

    public ArrayList<DeliveryItem> getLstDeliveryItems() {
        return lstDeliveryItems;
    }

    //Setter methods
    public void setLstDeliveryItems(ArrayList<DeliveryItem> lstDeliveryItems) {
        this.lstDeliveryItems = lstDeliveryItems;
    }

    public void setDeliveryComplete(int deliveryComplete) {
        this.deliveryComplete = deliveryComplete;
    }

    //Method ensures that the Client has valid values for all of its fields
    public boolean validateDelivery(Context context){
        boolean validStock = false;

        //If statements check numerous validation criteria for the Stock object.
        if(deliveryID.length() == 0){
            Toast.makeText(context, "Please enter a Delivery ID", Toast.LENGTH_LONG).show();
        }
        else if(deliveryClientID.length() == 0){
            Toast.makeText(context, "Please enter a Delivery Client", Toast.LENGTH_LONG).show();
        }
        else if(deliveryDate.length() == 0){
            Toast.makeText(context, "Please enter a Delivery Date", Toast.LENGTH_LONG).show();
        }
        else if(lstDeliveryItems.size() == 0){
            Toast.makeText(context, "Please add at least one item to your delivery", Toast.LENGTH_LONG).show();
        }
        else{
            validStock = true;
        }

        return validStock;
    }

    //Method checks if the entered Client ID has already been taken. The method returns true if it has been taken, and false if it hasn't been taken
    public boolean checkDeliveryID(Context context) throws IOException {
        boolean deliveryIDTaken = false;
        /*DBAdapter dbAdapter = new DBAdapter(context);
        dbAdapter.open();
        Cursor cursor = dbAdapter.getDelivery(deliveryID);
        if(cursor.moveToFirst()){
            deliveryIDTaken = true;
            Toast.makeText(context, "Delivery ID is taken, please choose another one", Toast.LENGTH_LONG).show();
        }
        dbAdapter.close(); */

        return deliveryIDTaken;
    }

    //Method ensures that the user can't enter a date that is before today's date when adding a new Delivery
    public boolean checkDeliveryDate(Context context) throws ParseException {
        boolean validDate = true;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        Date currentDate = new Date();
        if(dateFormat.parse(deliveryDate).before(dateFormat.parse(dateFormat.format(currentDate)))){
            Toast.makeText(context, "Please enter a Delivery Date that is not before today's date", Toast.LENGTH_LONG).show();
            validDate = false;
        }

        return validDate;
    }

    //Method calls the FirebaseService class and requests the Clients from the Firebase Database
    public void requestDeliveries(String searchTerm, Context context, ResultReceiver resultReceiver, int deliveryComplete){
        try{
            //Requests location information from the LocationService class
            String firebaseKey = new User(context).getUserKey();
            Intent intent = new Intent(context, FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_FETCH_DELIVERIES);
            intent.putExtra(FirebaseService.DELIVERY_COMPLETE, deliveryComplete);
            intent.putExtra(FirebaseService.SEARCH_TERM, searchTerm);
            intent.putExtra(FirebaseService.RECEIVER, resultReceiver);
            context.startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method calls the FirebaseService class and passes in a Delivery object that must be written to the Firebase database
    public void requestWriteOfDelivery(Context context, String action, ResultReceiver resultReceiver){
        try{
            //Requests location information from the LocationService class
            String firebaseKey = new User(context).getUserKey();
            Intent intent = new Intent(context, FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_WRITE_DELIVERY);
            intent.putExtra(FirebaseService.ACTION_WRITE_DELIVERY, this);
            intent.putExtra(FirebaseService.ACTION_WRITE_DELIVERY_INFORMATION, action);
            intent.putExtra(FirebaseService.RECEIVER, resultReceiver);
            context.startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
