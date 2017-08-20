/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class displays DeliveryItem object information in the appropriate ListView
 */

package a15008377.opsc7312assign1_15008377;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public class DeliveryItemListViewAdapter extends ArrayAdapter{
    //Declarations
    private Context context;
    private ArrayList<DeliveryItem> lstDeliveryItems;

    //Constructor
    public DeliveryItemListViewAdapter(Context context, ArrayList<DeliveryItem> lstDeliveryItems) {
        super(context, R.layout.list_view_row_delivery_item_report, lstDeliveryItems);
        this.context = context;
        this.lstDeliveryItems = lstDeliveryItems;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        //View declarations
        TextView txtDeliveryItemID;
        TextView txtDeliveryItemQuantity;
        Button btnDecrementQuantity;
        Button btnIncrementQuantity;
        ImageButton btnRemoveItem;

        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_view_row_delivery_item_report, parent, false);

        //Component assignments
        txtDeliveryItemID = (TextView) convertView.findViewById(R.id.text_delivery_item_id);
        txtDeliveryItemQuantity = (TextView) convertView.findViewById(R.id.text_delivery_item_quantity);
        btnDecrementQuantity = (Button) convertView.findViewById(R.id.button_decrement_delivery_item);
        btnIncrementQuantity = (Button) convertView.findViewById(R.id.button_increment_delivery_item);
        btnRemoveItem = (ImageButton) convertView.findViewById(R.id.button_remove_delivery_item);

        //Sets onClickListeners for the buttons
        btnIncrementQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Increments the quantity of the item by 1
                lstDeliveryItems.get(position).setDeliveryItemQuantity(lstDeliveryItems.get(position).getDeliveryItemQuantity() + 1);
                notifyDataSetChanged();
            }
        });

        btnDecrementQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Decrements the quantity of the item by 1, and removes the item if the quantity is reduced to 0
                lstDeliveryItems.get(position).setDeliveryItemQuantity(lstDeliveryItems.get(position).getDeliveryItemQuantity() - 1);
                if(lstDeliveryItems.get(position).getDeliveryItemQuantity() == 0){
                    Toast.makeText(context, lstDeliveryItems.get(position).getDeliveryStockID() + " removed from Delivery", Toast.LENGTH_LONG).show();
                    lstDeliveryItems.remove(position);
                }
                notifyDataSetChanged();
            }
        });

        btnRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Removes the item from the ListView
                Toast.makeText(context, lstDeliveryItems.get(position).getDeliveryStockID() + " removed from Delivery", Toast.LENGTH_LONG).show();
                lstDeliveryItems.remove(position);
                notifyDataSetChanged();
            }
        });

        //Displays the data in the appropriate Views
        Resources resources = context.getResources();
        txtDeliveryItemID.setText(resources.getString(R.string.delivery_item__id,  lstDeliveryItems.get(position).getDeliveryStockID()));
        txtDeliveryItemQuantity.setText(resources.getString(R.string.delivery_item_quantity,  lstDeliveryItems.get(position).getDeliveryItemQuantity()));

        return convertView;
    }
}