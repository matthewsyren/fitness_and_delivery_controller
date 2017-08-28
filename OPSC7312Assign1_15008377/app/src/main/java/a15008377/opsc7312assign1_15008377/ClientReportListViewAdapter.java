/*
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class displays the Client information in the appropriate ListView
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
import android.support.v4.os.ResultReceiver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class ClientReportListViewAdapter extends ArrayAdapter{
    //Declarations
    private Context context;
    private ArrayList<Client> lstClients;
    private int clientToBeDeletedPosition;

    //Constructor
    public ClientReportListViewAdapter(Context context, ArrayList<Client> lstClients) {
        super(context, R.layout.list_view_row_client_report, lstClients);
        this.context = context;
        this.lstClients = lstClients;
    }

    //Method populates the appropriate Views with the appropriate data (stored in the shows ArrayList)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //View declarations
        TextView txtClientID;
        TextView txtClientName;
        TextView txtClientPhoneNumber;
        TextView txtClientAddress;
        ImageButton btnDeleteClient;

        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_view_row_client_report, parent, false);

        //Component assignments
        txtClientID = (TextView) convertView.findViewById(R.id.text_client_id);
        txtClientName = (TextView) convertView.findViewById(R.id.text_client_name);
        txtClientPhoneNumber = (TextView) convertView.findViewById(R.id.text_client_phone_number);
        txtClientAddress = (TextView) convertView.findViewById(R.id.text_client_address);
        btnDeleteClient = (ImageButton) convertView.findViewById(R.id.button_delete_client);

        //Displays the data in the appropriate Views
        Resources resources = context.getResources();
        txtClientID.setText(resources.getString(R.string.client_id,  lstClients.get(position).getClientID()));
        txtClientName.setText(resources.getString(R.string.client_name,  lstClients.get(position).getClientName()));
        txtClientPhoneNumber.setText(resources.getString(R.string.client_phone_number,  lstClients.get(position).getClientPhoneNumber()));
        txtClientAddress.setText(resources.getString(R.string.client_address,  lstClients.get(position).getClientAddress()));

        //Sets OnClickListener for the button_delete_client Button
        btnDeleteClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Are you sure you want to delete this Client?");

                //Creates OnClickListener for the Dialog message
                DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        switch(button){
                            //Attempts to delete the Client if the user chooses the 'Yes' option in the AlertDialog
                            case AlertDialog.BUTTON_POSITIVE:
                                //Sets the position in the ArrayList of the Client that is to be deleted
                                clientToBeDeletedPosition = position;

                                //Requests Deliveries from the Firebase Database (the Client will not be deleted if there are any incomplete Deliveries for them)
                                new Delivery().requestDeliveries(null, context, new DataReceiver(new Handler()), 0);
                                break;
                            case AlertDialog.BUTTON_NEGATIVE:
                                Toast.makeText(context, "Deletion cancelled", Toast.LENGTH_LONG).show();
                                break;
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
            //Processes the result when the Client is written to the Firebase Database
            if(resultCode == FirebaseService.ACTION_WRITE_CLIENT_RESULT_CODE){
                boolean success = resultData.getBoolean(FirebaseService.ACTION_WRITE_CLIENT);

                //Removes the Client from the ListView if the Client was deleted successfully
                if(success){
                    Toast.makeText(context, "Client deleted successfully", Toast.LENGTH_LONG).show();
                    lstClients.remove(clientToBeDeletedPosition);
                    notifyDataSetChanged();
                }
                else{
                    Toast.makeText(context, "An error occurred while deleting the Client, please try again", Toast.LENGTH_LONG).show();
                }
            }
            //Processes the result when the Deliveries are fetched from the Firebase Database
            else if(resultCode == FirebaseService.ACTION_FETCH_DELIVERIES_RESULT_CODE){
                ArrayList<Delivery> lstDeliveries = (ArrayList<Delivery>) resultData.getSerializable(FirebaseService.ACTION_FETCH_DELIVERIES);
                boolean clientUsed = false;

                //Ensures that any incomplete Deliveries aren't for the Client that the user wants to delete
                for(Delivery delivery : lstDeliveries){
                    if(delivery.getDeliveryClientID().equals(lstClients.get(clientToBeDeletedPosition).getClientID())){
                        clientUsed = true;
                        break;
                    }
                }

                //Requests the deletion of the Client if there are no outstanding Deliveries for the Client
                if(!clientUsed){
                    lstClients.get(clientToBeDeletedPosition).requestWriteOfClient( "delete", context, new DataReceiver(new Handler()));
                }
                else{
                    Toast.makeText(context, "There are Deliveries that are scheduled for this Client, please remove all Deliveries for this Client before deleting the Client.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}