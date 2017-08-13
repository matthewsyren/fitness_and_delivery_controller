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
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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
                                        new Stock().deleteStockItem(stockID, context);
                                        lstStock.remove(position);

                                        //Removes all Delivery Items that used that Stock item
                                        /*DBAdapter dbAdapter = new DBAdapter(context);
                                        dbAdapter.open();
                                        dbAdapter.deleteDeliveryItemsByStockID(stockID);
                                        dbAdapter.close(); */
                                        notifyDataSetChanged();
                                        break;
                                    case AlertDialog.BUTTON_NEGATIVE:
                                        Toast.makeText(context, "Deletion cancelled", Toast.LENGTH_LONG).show();
                                        break;
                                }
                            }
                            catch(IOException ioe){
                                Toast.makeText(context, ioe.getMessage(), Toast.LENGTH_LONG).show();
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
}
