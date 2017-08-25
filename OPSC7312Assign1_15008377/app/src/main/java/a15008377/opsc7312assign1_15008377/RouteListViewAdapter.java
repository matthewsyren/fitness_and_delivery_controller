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


    //Constructor
    public RouteListViewAdapter(Context context, ArrayList<WayPoint> lstWayPoints) {
        super(context, R.layout.list_view_row_route_planner, lstWayPoints);
        this.context = context;
        this.lstWayPoints = lstWayPoints;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if(position != 0){
            //View declarations
            TextView txtDeliveryID;
            TextView txtClientID;
            TextView txtClientPhoneNumber;
            TextView txtClientAddress;
            TextView txtLegDistance;
            TextView txtLegDuration;

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


            //Displays the data in the appropriate Views
            Resources resources = context.getResources();
            txtDeliveryID.setText(resources.getString(R.string.delivery_id, lstWayPoints.get(position).getDeliveryID()));
            txtClientID.setText(resources.getString(R.string.client_id, lstWayPoints.get(position).getClientID()));
            txtClientPhoneNumber.setText(resources.getString(R.string.client_phone_number, lstWayPoints.get(position).getClientPhoneNumber()));
            txtClientAddress.setText(resources.getString(R.string.client_address, lstWayPoints.get(position).getClientAddress()));
            txtLegDistance.setText(resources.getString(R.string.route_leg_dustance, lstWayPoints.get(position).getLegDistance()));
            txtLegDuration.setText(resources.getString(R.string.route_leg_duration, lstWayPoints.get(position).getLegDuration()));
        }

        return convertView;
    }
}
