/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class displays Delivery object information in the appropriate ListView
 */

package a15008377.opsc7312assign1_15008377;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class DeliveryReportListViewAdapter extends ArrayAdapter {
    //Declarations
    private Context context;
    private ArrayList<Delivery> lstDeliveries;
    private String action;
    private int deliveryToBeDeletedPosition;

    //Constructor
    public DeliveryReportListViewAdapter(Context context, ArrayList<Delivery> lstDeliveries){
        super(context, R.layout.list_view_row_delivery_report,lstDeliveries);
        this.context = context;
        this.lstDeliveries = lstDeliveries;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent){
        //View declarations
        TextView txtDeliveryID;
        TextView txtDeliveryClientID;
        TextView txtDeliveryDate;
        TextView txtDeliveryComplete;
        TextView txtDeliveryItems;
        ImageButton btnDeleteDelivery;
        ImageButton btnMarkDeliveryAsComplete;

        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_view_row_delivery_report, parent, false);

        //View assignments
        txtDeliveryID = (TextView) convertView.findViewById(R.id.text_delivery_id);
        txtDeliveryClientID = (TextView) convertView.findViewById(R.id.text_delivery_client_id);
        txtDeliveryDate = (TextView) convertView.findViewById(R.id.text_delivery_date);
        txtDeliveryComplete = (TextView) convertView.findViewById(R.id.text_delivery_complete);
        txtDeliveryItems = (TextView) convertView.findViewById(R.id.text_delivery_items);
        btnDeleteDelivery = (ImageButton) convertView.findViewById(R.id.button_delete_delivery);
        btnMarkDeliveryAsComplete = (ImageButton) convertView.findViewById(R.id.button_mark_delivery_as_complete);

        //Displays the data in the appropriate Views
        Resources resources = context.getResources();
        txtDeliveryID.setText(resources.getString(R.string.delivery_id, lstDeliveries.get(position).getDeliveryID()));
        txtDeliveryClientID.setText(resources.getString(R.string.delivery_client_id, lstDeliveries.get(position).getDeliveryClientID()));
        txtDeliveryDate.setText(resources.getString(R.string.delivery_date, lstDeliveries.get(position).getDeliveryDate()));
        txtDeliveryComplete.setText(resources.getString(R.string.delivery_complete, (lstDeliveries.get(position).getDeliveryComplete() == 0 ? "No" : "Yes") + "\n\n"));

        //Loops through all Delivery Items for the Delivery and displays them
        final ArrayList<DeliveryItem> lstDeliveryItems = lstDeliveries.get(position).getLstDeliveryItems();
        String itemText = "Delivery Items: \n";
        for(int i = 0; i < lstDeliveryItems.size(); i++){
            if(i != 0){
                itemText += "\n\n";
            }
            itemText += "Item ID: " + lstDeliveryItems.get(i).getDeliveryStockID() + "\nQuantity: " + lstDeliveryItems.get(i).getDeliveryItemQuantity();
        }
        txtDeliveryItems.setText(itemText);

        //Displays the buttons to mark a delivery as complete or delete a Delivery if the Delivery is incomplete, and hides those buttons if the Delivery is complete
        if(lstDeliveries.get(position).getDeliveryComplete() == 0){
            //Sets OnClickListener for the button_delete_delivery Button
            btnDeleteDelivery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Are you sure you want to delete this Delivery?");

                    //Creates OnClickListener for the Dialog message
                    DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            switch(button){
                                //Deletes Delivery item if the user chooses the 'Yes' option, and removes the Delivery from the Calendar
                                case AlertDialog.BUTTON_POSITIVE:
                                    action = "delete";
                                    lstDeliveries.get(position).requestWriteOfDelivery(context, action, new DataReceiver(new Handler()));
                                    deliveryToBeDeletedPosition = position;
                                    deleteDeliveryInCalendar();
                                    new Stock().requestStockItems(null, context, new DataReceiver(new Handler()));
                                    break;
                                case AlertDialog.BUTTON_NEGATIVE:
                                    Toast.makeText(context, "Deletion cancelled", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    };

                    //Assigns button an OnClickListener for the AlertDialog and displays the AlertDialog
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", dialogOnClickListener);
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", dialogOnClickListener);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
            });

            //Marks the Delivery as complete in the database and removes the Delivery from the lstDeliveries ArrayList
            btnMarkDeliveryAsComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        action = "update";
                        Delivery delivery = lstDeliveries.get(position);
                        delivery.setDeliveryComplete(1);
                        delivery.requestWriteOfDelivery(context, action, new DataReceiver(new Handler()));
                        lstDeliveries.remove(position);
                        notifyDataSetChanged();
                    }
                    catch(Exception exc){
                        Toast.makeText(context, exc.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            //Hides the buttons if the Delivery is complete
            btnDeleteDelivery.setVisibility(View.GONE);
            btnMarkDeliveryAsComplete.setVisibility(View.GONE);
        }

        return convertView;
    }

    //Method adds the items that were in the deleted Delivery back to the available Stock in the Firebase Database
    private void addItemsBackToStock(ArrayList<Stock> lstStock){
        try{
            ArrayList<DeliveryItem> lstDeliveryItems = lstDeliveries.get(deliveryToBeDeletedPosition).getLstDeliveryItems();
            ArrayList<Stock> lstStockToBeUpdated = new ArrayList<>();

            //Loops through all DeliveryItems and adds them back to Stock
            for(int i = 0; i < lstDeliveryItems.size(); i++){
                String deliveryItemID = lstDeliveryItems.get(i).getDeliveryStockID();
                for(int j = 0; j < lstStock.size(); j++){
                    String stockID = lstStock.get(j).getStockID();

                    //Adds the deleted Delivery's Delivery Item quantity to the appropriate Stock quantity
                    if(deliveryItemID.equals(stockID)){
                        int stockQuantity = lstStock.get(j).getStockQuantity();
                        lstStock.get(j).setStockQuantity(stockQuantity + lstDeliveryItems.get(i).getDeliveryItemQuantity());
                        lstStockToBeUpdated.add(lstStock.get(j));
                    }
                }
            }

            //Writes the updated Stock quantities to the Firebase Database
            updateStockLevels(lstStockToBeUpdated);
        }
        catch(Exception exc){
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method receives an ArrayList of Stock items with an updated quantity, and updates the available quantity of stock in the Firebase Database
    public void updateStockLevels(final ArrayList<Stock> lstStock){
        try{
            //Gets Firebase Database reference
            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = firebaseDatabase.getReference().child(new User(context).getUserKey()).child("stock");

            //Adds Listeners for when the data is changed
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Writes the updated Stock quantities to the Firebase Database
                    for(int i = 0; i < lstStock.size(); i++){
                        databaseReference.child(lstStock.get(i).getStockID()).setValue(lstStock.get(i));
                    }
                    databaseReference.removeEventListener(this);
                    Toast.makeText(context, "Stock levels updated", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.i("Data", "Error while writing data to Firebase");
                }
            });
        }
        catch(Exception exc){
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method deletes the Calendar entry for the Delivery
    public void deleteDeliveryInCalendar() throws SecurityException{
        try{
            //Checks for permission to write to calendar, and deletes the Calendar event if permission has been granted
            if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                ContentResolver contentResolver = context.getContentResolver();
                Cursor eventCursor = contentResolver.query(Uri.parse("content://com.android.calendar/events"), new String[] { "_id", "title"}, CalendarContract.Instances.CALENDAR_ID + " = ?", new String[] {"1"}, null);

                while (eventCursor.moveToNext()){
                    final long id = eventCursor.getLong(0);
                    final String title = eventCursor.getString(1);
                    if(title.equals("Delivery " + lstDeliveries.get(deliveryToBeDeletedPosition).getDeliveryID())){
                        //Deletes the Delivery from the Calendar
                        Uri eventsUri = Uri.parse("content://com.android.calendar/events");
                        Uri eventUri = ContentUris.withAppendedId(eventsUri, id);
                        context.getContentResolver().delete(eventUri, null, null);
                    }
                }
            }
        }
        catch(Exception exc){
            Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Creates a ResultReceiver to retrieve information from the FirebaseService
    private class DataReceiver extends ResultReceiver {
        private DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            //Processes the result when the Delivery has been written to the Firebase Database
            if(resultCode == FirebaseService.ACTION_WRITE_DELIVERY_RESULT_CODE){
                boolean success = resultData.getBoolean(FirebaseService.ACTION_WRITE_DELIVERY);

                //Displays appropriate message based on whether the Delivery was successfully written to the Firebase Database
                if(success){
                    if(action.equals("update")){
                        Toast.makeText(context, "Delivery marked as complete", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(context, "Delivery deleted successfully", Toast.LENGTH_LONG).show();
                    }
                }
            }
            //Processes the result when the Stock is fetched from the Firebase Database
            else if(resultCode == FirebaseService.ACTION_FETCH_STOCK_RESULT_CODE){
                //Adds deleted Delivery Items from the Delivery back to Stock
                ArrayList<Stock> lstStock = (ArrayList<Stock>) resultData.getSerializable(FirebaseService.ACTION_FETCH_STOCK);
                addItemsBackToStock(lstStock);

                //Removes the Delivery from the ListView
                lstDeliveries.remove(deliveryToBeDeletedPosition);
                notifyDataSetChanged();
            }
        }
    }
}