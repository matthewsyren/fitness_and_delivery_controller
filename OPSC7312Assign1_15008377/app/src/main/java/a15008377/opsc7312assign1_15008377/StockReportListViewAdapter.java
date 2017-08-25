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
                                        //Removes the Stock item from the Stock.txt text file
                                        String stockID = lstStock.get(position).getStockID();
                                        //new Stock().deleteStockItem(stockID, context);
                                        requestWriteOfStockItem(lstStock.get(position), "delete");
                                        lstStock.remove(position);
                                        notifyDataSetChanged();
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

    //Method calls the FirebaseService class and passes in a Stock object that must be written to the Firebase database
    public void requestWriteOfStockItem(Stock stock, String action){
        try{
            //Requests location information from the LocationService class
            String firebaseKey = new User(context).getUserKey();
            Intent intent = new Intent(context, FirebaseService.class);
            intent.putExtra(FirebaseService.FIREBASE_KEY, firebaseKey);
            intent.setAction(FirebaseService.ACTION_WRITE_STOCK);
            intent.putExtra(FirebaseService.ACTION_WRITE_STOCK, stock);
            intent.putExtra(FirebaseService.ACTION_WRITE_STOCK_INFORMATION, action);
            intent.putExtra(FirebaseService.RECEIVER, new DataReceiver(new Handler()));
            context.startService(intent);
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
            if(resultCode == FirebaseService.ACTION_WRITE_STOCK_RESULT_CODE) {
                boolean success = resultData.getBoolean(FirebaseService.ACTION_WRITE_STOCK);

                if (success) {
                    Toast.makeText(context, "Stock Item deleted successfully", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
