/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class provides a template for a Stock object
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.content.Intent;
import android.support.v4.os.ResultReceiver;
import android.widget.Toast;
import java.io.Serializable;

@SuppressWarnings("WeakerAccess")
public class Stock implements Serializable{
    //Declarations
    private String stockID;
    private String stockDescription;
    private int stockQuantity;

    //Constructor
    public Stock(String stockID, String stockDescription, int stockQuantity){
        this.stockID = stockID;
        this.stockDescription = stockDescription;
        this.stockQuantity = stockQuantity;
    }

    //Default constructor
    public Stock(){}

    //Getter methods
    public String getStockID() {
        return stockID;
    }

    public String getStockDescription() {
        return stockDescription;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    //Setter method
    public void setStockQuantity(int stockQuantity){
        this.stockQuantity = stockQuantity;
    }

    //Method returns true if the Stock object has valid values for all of its attributes, otherwise it returns false
    public boolean validateStock(Context context){
        boolean validStock = false;

        //If statements check numerous validation criteria for the Stock object.
        if(stockID.length() == 0){
            Toast.makeText(context, "Please enter a Stock ID", Toast.LENGTH_LONG).show();
        }
        else if(stockID.contains("|")){
            Toast.makeText(context, "Please remove all | symbols from the Stock ID", Toast.LENGTH_LONG).show();
        }
        else if(stockDescription.length() == 0){
            Toast.makeText(context, "Please enter a Stock Description", Toast.LENGTH_LONG).show();
        }
        else if(stockDescription.contains("|")){
            Toast.makeText(context, "Please remove all | symbols from the Stock Description", Toast.LENGTH_LONG).show();
        }
        else if(stockQuantity < 0){
            Toast.makeText(context, "Please enter a Stock Quantity that is more than or equal to 0", Toast.LENGTH_LONG).show();
        }
        else{
            validStock = true;
        }

        return validStock;
    }

    //Requests Stock Items from the Firebase Database
    public void requestStockItems(String searchTerm, Context context, ResultReceiver resultReceiver){
        try{
            //Requests location information from the LocationService class
            String firebaseKey = new User(context).getUserKey();
            Intent intent = new Intent(context, FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.putExtra(FirebaseService.SEARCH_TERM, searchTerm);
            intent.setAction(FirebaseService.ACTION_FETCH_STOCK);
            intent.putExtra(FirebaseService.RECEIVER, resultReceiver);
            context.startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method calls the FirebaseService class and passes in a Stock object that must be written to the Firebase database
    public void requestWriteOfStockItem(Context context, String action, ResultReceiver resultReceiver){
        try{
            //Requests location information from the LocationService class
            String firebaseKey = new User(context).getUserKey();
            Intent intent = new Intent(context, FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_WRITE_STOCK);
            intent.putExtra(FirebaseService.ACTION_WRITE_STOCK, this);
            intent.putExtra(FirebaseService.ACTION_WRITE_STOCK_INFORMATION, action);
            intent.putExtra(FirebaseService.RECEIVER, resultReceiver);
            context.startService(intent);
        }
        catch(Exception exc){
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}