/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class provides a template for a Delivery object
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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

    //Method fetches the Deliveries that match the search result and sends them to the displayDeliveries method
    public static ArrayList<Delivery> searchDeliveries(String searchTerm, Context context, int complete) throws SQLException{
        /*DBAdapter dbAdapter = new DBAdapter(context);
        dbAdapter.open();
        Cursor cursor = dbAdapter.searchDelivery(searchTerm); */
        ArrayList<Delivery> lstSearchResults = new ArrayList<>();
        /*ArrayList<DeliveryItem> lstDeliveryItems = new ArrayList<>();

        if(cursor.moveToFirst()){
            do{
                Delivery delivery = new Delivery(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), lstDeliveryItems);
                if(delivery.getDeliveryComplete() == complete){
                    lstSearchResults.add(delivery);
                }
            }while(cursor.moveToNext());
        }
        dbAdapter.close(); */

        return lstSearchResults;
    }

    //Method returns an ArrayList filled with either completed Deliveries on incomplete Deliveries
    public static ArrayList<Delivery> getDeliveries(Context context, int complete){
      /*  DBAdapter dbAdapter = new DBAdapter(context);
        dbAdapter.open();
        Cursor deliveryCursor  = dbAdapter.getAllDeliveries(); */
        final ArrayList<Delivery> lstDeliveries = new ArrayList<>();
        /*ArrayList<DeliveryItem> lstDeliveryItems = new ArrayList<>();

        if(deliveryCursor.moveToFirst()){
            do{
                Delivery delivery = new Delivery(deliveryCursor.getString(0),deliveryCursor.getString(1), deliveryCursor.getString(2), deliveryCursor.getInt(3), lstDeliveryItems);

                //Adds Deliveries to the lstDeliveries ArrayList if the Delivery hasn't been completed
                if(delivery.getDeliveryComplete() == complete){
                    lstDeliveries.add(delivery);
                }
            }while(deliveryCursor.moveToNext());
        }
        dbAdapter.close(); */

        return lstDeliveries;
    }
}
