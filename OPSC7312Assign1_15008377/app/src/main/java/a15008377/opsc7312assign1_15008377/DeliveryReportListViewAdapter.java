/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class displays Delivery object information in the appropriate ListView
 */

package a15008377.opsc7312assign1_15008377;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class DeliveryReportListViewAdapter extends ArrayAdapter {
    //Declarations
    Context context;
    private ArrayList<Delivery> lstDeliveries;

    public DeliveryReportListViewAdapter(Context context, ArrayList<Delivery> lstDeliveries) {
        super(context, R.layout.list_view_row_delivery_report,lstDeliveries);
        this.context = context;
        this.lstDeliveries = lstDeliveries;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent)
    {
        //View declarations
        TextView txtDeliveryID;
        TextView txtDeliveryClientID;
        TextView txtDeliveryDate;
        TextView txtDeliveryComplete;
        TextView txtDeliveryItems;
        ImageButton btnDeleteDelivery;
        Button btnMarkDeliveryAsComplete;

        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_view_row_delivery_report, parent, false);

        //Component assignments
        txtDeliveryID = (TextView) convertView.findViewById(R.id.text_delivery_id);
        txtDeliveryClientID = (TextView) convertView.findViewById(R.id.text_delivery_client_id);
        txtDeliveryDate = (TextView) convertView.findViewById(R.id.text_delivery_date);
        txtDeliveryComplete = (TextView) convertView.findViewById(R.id.text_delivery_complete);
        txtDeliveryItems = (TextView) convertView.findViewById(R.id.text_delivery_items);
        btnDeleteDelivery = (ImageButton) convertView.findViewById(R.id.button_delete_delivery);
        btnMarkDeliveryAsComplete = (Button) convertView.findViewById(R.id.button_mark_delivery_as_complete);

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
        /*
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
                                //Checks if the username is valid (length > 0 and every character is an alphabetic character)
                                case AlertDialog.BUTTON_POSITIVE:
                                    DBAdapter dbAdapter = new DBAdapter(context);
                                    dbAdapter.open();

                                    String deliveryID = lstDeliveries.get(position).getDeliveryID();

                                    //Deletes the Delivery and all DeliveryItems for that Delivery
                                    if(dbAdapter.deleteDelivery(deliveryID)){
                                        dbAdapter.deleteDeliveryItems(deliveryID);
                                        addItemsBackToStock(lstDeliveries.get(position));
                                        lstDeliveries.remove(position);
                                        Toast.makeText(context, "Delivery successfully deleted", Toast.LENGTH_LONG).show();
                                        notifyDataSetChanged();
                                    }
                                    dbAdapter.close();
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
                        DBAdapter dbAdapter = new DBAdapter(context);
                        dbAdapter.open();
                        Delivery delivery = lstDeliveries.get(position);
                        delivery.setDeliveryComplete(1);
                        dbAdapter.updateDelivery(delivery);
                        lstDeliveries.remove(position);
                        Toast.makeText(context, "Delivery marked as complete", Toast.LENGTH_SHORT).show();
                        dbAdapter.close();
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
        */
        return convertView;
    }

    //Method adds the items that were in the deleted Delivery back to the Stock.txt text file
    private void addItemsBackToStock(Delivery delivery){
        try{
            ArrayList<Stock> lstStock = Stock.readStockItems(context);
            ArrayList<DeliveryItem> lstDeliveryItems = delivery.getLstDeliveryItems();

            //Loops through all DeliveryItems and adds them back to Stock
            for(int i = 0; i < lstDeliveryItems.size(); i++){
                String deliveryItemID = lstDeliveryItems.get(i).getDeliveryStockID();
                for(int j = 0; j < lstStock.size(); j++){
                    String stockID = lstStock.get(j).getStockID();

                    if(deliveryItemID.equals(stockID)){
                        int stockQuantity = lstStock.get(j).getStockQuantity();
                        lstStock.get(j).setStockQuantity(stockQuantity + lstDeliveryItems.get(i).getDeliveryItemQuantity());
                    }
                }
            }

            //Writes the updated Stock quantities to the Stock.txt text file
            new Stock().rewriteFile(lstStock, context);
        }
        catch(IOException ioe){
            Toast.makeText(context, ioe.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}