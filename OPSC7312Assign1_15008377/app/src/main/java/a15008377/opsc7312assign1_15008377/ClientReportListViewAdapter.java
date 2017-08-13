/**
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
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class ClientReportListViewAdapter extends ArrayAdapter{
    //Declarations
    private Context context;
    private ArrayList<Client> lstClients;

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
        TextView txtClientEmail;
        TextView txtClientAddress;
        ImageButton btnDeleteClient;

        //Inflates the list_row view for the ListView
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_view_row_client_report, parent, false);

        //Component assignments
        txtClientID = (TextView) convertView.findViewById(R.id.text_client_id);
        txtClientName = (TextView) convertView.findViewById(R.id.text_client_name);
        txtClientEmail = (TextView) convertView.findViewById(R.id.text_client_email);
        txtClientAddress = (TextView) convertView.findViewById(R.id.text_client_address);
        btnDeleteClient = (ImageButton) convertView.findViewById(R.id.button_delete_client);

        //Displays the data in the appropriate Views
        Resources resources = context.getResources();
        txtClientID.setText(resources.getString(R.string.client_id,  lstClients.get(position).getClientID()));
        txtClientName.setText(resources.getString(R.string.client_name,  lstClients.get(position).getClientName()));
        txtClientEmail.setText(resources.getString(R.string.client_email,  lstClients.get(position).getClientEmail()));
        txtClientAddress.setText(resources.getString(R.string.client_address,  lstClients.get(position).getClientAddress()));

        //Sets OnClickListener for the button_delete_client Button
        btnDeleteClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Are you sure you want to delete this Client?");
                /*
                //Creates OnClickListener for the Dialog message
                DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        switch(button){
                            //Checks if the username is valid (length > 0 and every character is an alphabetic character)
                            case AlertDialog.BUTTON_POSITIVE:
                                DBAdapter dbAdapter = new DBAdapter(context);
                                dbAdapter.open();
                                String clientID = lstClients.get(position).getClientID();

                                //Deletes Client and the Deliveries associated with that Client
                                if(dbAdapter.deleteClient(clientID)){
                                    dbAdapter.deleteClientDeliveries(clientID);
                                    lstClients.remove(position);
                                    Toast.makeText(context, "Client successfully deleted", Toast.LENGTH_LONG).show();
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

                //Assigns button and OnClickListener for the AlertDialog and displays the AlertDialog
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", dialogOnClickListener);
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", dialogOnClickListener);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show(); */
            }
        });

        return convertView;
    }
}