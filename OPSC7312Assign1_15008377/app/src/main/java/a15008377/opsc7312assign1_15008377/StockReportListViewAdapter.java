/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class displays Stock object information in the appropriate ListView
 */

package a15008377.opsc7312assign1_15008377;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;

public class StockReportListViewAdapter extends ArrayAdapter {
    //Declarations
    private Context context;
    private ArrayList<Stock> lstStock;
    private int stockToBeDeletedPosition;

    //Constructor
    public StockReportListViewAdapter(Context context, ArrayList<Stock> stock) {
        super(context, R.layout.list_view_row_stock_report, stock);
        this.context = context;
        lstStock = stock;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        //View declarations
        TextView txtStockID;
        TextView txtStockDescription;
        TextView txtStockQuantity;
        ImageButton btnDeleteStock;

        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_view_row_stock_report, parent, false);

        //Component assignments
        txtStockID = (TextView) convertView.findViewById(R.id.text_stock_id);
        txtStockDescription = (TextView) convertView.findViewById(R.id.text_stock_description);
        txtStockQuantity = (TextView) convertView.findViewById(R.id.text_stock_quantity);
        btnDeleteStock = (ImageButton) convertView.findViewById(R.id.button_delete_stock);

        //Displays the data in the appropriate Views
        Resources resources = context.getResources();
        txtStockID.setText(resources.getString(R.string.stock_id, lstStock.get(position).getStockID()));
        txtStockDescription.setText(resources.getString(R.string.stock_description, lstStock.get(position).getStockDescription()));
        txtStockQuantity.setText(resources.getString(R.string.stock_quantity, lstStock.get(position).getStockQuantity()));

        //Sets OnClickListener for the button_delete_stock Button
        btnDeleteStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Are you sure you want to delete this Stock item?");

                    //Creates OnClickListener for the Dialog message
                    DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int button) {
                            try {
                                switch (button) {
                                    case AlertDialog.BUTTON_POSITIVE:
                                        stockToBeDeletedPosition = position;
                                        new Delivery().requestDeliveries(null, context, new DataReceiver(new Handler()), 0);
                                        break;
                                    case AlertDialog.BUTTON_NEGATIVE:
                                        Toast.makeText(context, "Deletion cancelled", Toast.LENGTH_LONG).show();
                                        break;
                                }
                            }
                            catch(Exception exc){
                                Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    //Assigns button and OnClickListener for the AlertDialog and displays the AlertDialog
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", dialogOnClickListener);
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", dialogOnClickListener);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
            }
        });

        return convertView;
    }

    //Creates a ResultReceiver to retrieve information from the FirebaseService
    private class DataReceiver extends ResultReceiver {
        private DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            if(resultCode == FirebaseService.ACTION_WRITE_STOCK_RESULT_CODE) {
                boolean success = resultData.getBoolean(FirebaseService.ACTION_WRITE_STOCK);

                if (success) {
                    lstStock.remove(stockToBeDeletedPosition);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Stock Item deleted successfully", Toast.LENGTH_LONG).show();
                }
            }
            else if(resultCode == FirebaseService.ACTION_FETCH_DELIVERIES_RESULT_CODE){
                ArrayList<Delivery> lstDeliveries = (ArrayList<Delivery>) resultData.getSerializable(FirebaseService.ACTION_FETCH_DELIVERIES);
                boolean stockUsed = false;
                for(int i = 0; i < lstDeliveries.size() && stockUsed == false; i++){
                    Delivery delivery = lstDeliveries.get(i);
                    for(DeliveryItem deliveryItem : delivery.getLstDeliveryItems()){
                        if(deliveryItem.getDeliveryStockID().equals(lstStock.get(stockToBeDeletedPosition).getStockID())){
                            stockUsed = true;
                            break;
                        }
                    }
                }

                if(!stockUsed){
                    lstStock.get(stockToBeDeletedPosition).requestWriteOfStockItem(context, "delete", new DataReceiver(new Handler()));
                }
                else{
                    Toast.makeText(context, "There are Deliveries that use this Stock Item, please remove this Stock Item from all Deliveries before deleting it.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
