package a15008377.opsc7312assign1_15008377;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Matthew Syrén on 2017/08/24.
 */

public class RouteListViewAdapter extends ArrayAdapter{
    //Declarations
    private Context context;
    private ArrayList<WayPoint> lstWayPoints;
    private ArrayList<Delivery> lstDeliveries;

    //Constructor
    public RouteListViewAdapter(Context context, ArrayList<WayPoint> lstWayPoints, ArrayList<Delivery> lstDeliveries) {
        super(context, R.layout.list_view_row_route_planner, lstWayPoints);
        this.context = context;
        this.lstWayPoints = lstWayPoints;
        this.lstDeliveries = lstDeliveries;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        //View declarations
        TextView txtDeliveryID;
        TextView txtClientID;
        TextView txtClientPhoneNumber;
        TextView txtClientAddress;
        TextView txtLegDistance;
        TextView txtLegDuration;
        ImageButton btnMarkDeliveryAsComplete;
        ImageButton btnPhoneClient;

        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_view_row_route_planner, parent, false);

        //Component assignments
        txtDeliveryID = (TextView) convertView.findViewById(R.id.text_delivery_id);
        txtClientID = (TextView) convertView.findViewById(R.id.text_client_id);
        txtClientPhoneNumber = (TextView) convertView.findViewById(R.id.text_client_phone_number);
        txtClientAddress = (TextView) convertView.findViewById(R.id.text_client_address);
        txtLegDistance = (TextView) convertView.findViewById(R.id.text_leg_distance);
        txtLegDuration = (TextView) convertView.findViewById(R.id.text_leg_duration);
        btnMarkDeliveryAsComplete = (ImageButton) convertView.findViewById(R.id.button_mark_as_complete);
        btnPhoneClient = (ImageButton) convertView.findViewById(R.id.button_call_client);

        if(position > 0 && position < lstWayPoints.size() - 1) {

            //Displays the data in the appropriate Views
            Resources resources = context.getResources();
            txtDeliveryID.setText(resources.getString(R.string.delivery_id, lstWayPoints.get(position).getDeliveryID()));
            txtClientID.setText(resources.getString(R.string.client_id, lstWayPoints.get(position).getClientID()));
            txtClientPhoneNumber.setText(resources.getString(R.string.client_phone_number, lstWayPoints.get(position).getClientPhoneNumber()));
            txtClientAddress.setText(resources.getString(R.string.client_address, lstWayPoints.get(position).getClientAddress()));
            txtLegDistance.setText(resources.getString(R.string.route_leg_dustance, lstWayPoints.get(position).getLegDistance()));
            txtLegDuration.setText(resources.getString(R.string.route_leg_duration, lstWayPoints.get(position).getLegDuration()));
        }
        else{
            if(position == 0){
                txtDeliveryID.setText("Starting Address: " + lstWayPoints.get(position).getClientAddress());
            }
            else{
                txtDeliveryID.setText("Ending Address: " + lstWayPoints.get(position).getClientAddress());
            }
            btnMarkDeliveryAsComplete.setVisibility(View.INVISIBLE);
            btnPhoneClient.setVisibility(View.INVISIBLE);
        }


        //Marks the Delivery as complete in the database and removes the Delivery from the lstDeliveries ArrayList
        btnMarkDeliveryAsComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String action = "update";
                    Delivery completedDelivery = null;
                    for(Delivery delivery : lstDeliveries){
                        if(delivery.getDeliveryID().equals(lstWayPoints.get(position).getDeliveryID())){
                            completedDelivery = delivery;
                        }
                    }
                    completedDelivery.setDeliveryComplete(1);
                    completedDelivery.requestWriteOfDelivery(context, action, new DataReceiver(new Handler()));
                    lstWayPoints.remove(position);
                    notifyDataSetChanged();
                }
                catch(Exception exc){
                    Toast.makeText(context, exc.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
         btnPhoneClient.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 try {
                     Intent intent = new Intent(Intent.ACTION_DIAL);
                     intent.setData(Uri.parse("tel:" + lstWayPoints.get(position).getClientPhoneNumber()));
                     context.startActivity(intent);
                 }
                 catch(Exception exc){
                     Toast.makeText(context, exc.getMessage(), Toast.LENGTH_LONG).show();
                 }
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
            if(resultCode == FirebaseService.ACTION_WRITE_DELIVERY_RESULT_CODE){
                boolean success = resultData.getBoolean(FirebaseService.ACTION_WRITE_DELIVERY);

                if(success){
                    Toast.makeText(context, "Delivery marked as complete", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
