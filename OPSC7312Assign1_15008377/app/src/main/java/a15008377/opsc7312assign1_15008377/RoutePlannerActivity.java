package a15008377.opsc7312assign1_15008377;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;

public class RoutePlannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_planner);

        optimiseDeliveries();
    }

    public void optimiseDeliveries(){
        try{
            /*DBAdapter dbAdapter = new DBAdapter(this);
            dbAdapter.open();
            Cursor deliveryCursor = dbAdapter.getAllDeliveries();
            ArrayList<String> lstRoute = new ArrayList<>();

            if(deliveryCursor.moveToFirst()){
                do{
                    String clientID = deliveryCursor.getString(1);
                    Client client = dbAdapter.getClient(clientID);
                    Calendar calendar = Calendar.getInstance();
                    String currentDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);

                    //Adds Marker to lstMarkers if the Delivery is incomplete and scheduled for the current date
                    if(client != null && deliveryCursor.getInt(3) == 0 && deliveryCursor.getString(2).equals(currentDate)){
                        //Creates new Marker and title for Marker, and adds them to appropriate ArrayLists
                        LatLng clientLocation = new LatLng(client.getClientLatitude(), client.getClientLongitude());
                        String markerTitle = "Delivery: " + deliveryCursor.getString(0) + "     Client Name: " + client.getClientName();
                        lstRoute.add(markerTitle);
                    }
                }while(deliveryCursor.moveToNext());

                ListView listView = (ListView) findViewById(R.id.list_view_planned_route);
                ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), R.layout.list_view_row_route_planner, R.id.text_route_waypoint, lstRoute);
                listView.setAdapter(arrayAdapter);
            }
            dbAdapter.close(); */
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
