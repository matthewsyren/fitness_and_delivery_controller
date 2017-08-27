package a15008377.opsc7312assign1_15008377;

import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class RoutePlannerActivity extends AppCompatActivity implements IAPIConnectionResponse {
    ArrayList<Client> lstClients = new ArrayList<>();
    ArrayList<WayPoint> lstWayPoints;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_route_planner);

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Fetches the Client information from Firebase
            new Client().requestClients(null, getApplicationContext(), new DataReceiver(new Handler()));
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method fetches the data for the day's Deliveries that was passed from the Question2
    public void fetchDeliveryDetails(){
        try{
            Bundle bundle = getIntent().getExtras();
            String url = bundle.getString("routeURL", null);
            if(url != null){
                APIConnection api = new APIConnection();
                api.delegate = this;
                api.execute(url);
            }
            else{
                Toast.makeText(getApplicationContext(), "Null", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method parses the JSON data returned from the Google Maps Directions API, then adds the data to an ArrayList of WayPoint objects before displaying the ArrayList's contents
    @Override
    public void getJsonResponse(String response) {
        try{
            if(response != null){
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                Bundle bundle = getIntent().getExtras();
                ArrayList<Delivery> lstDeliveries = (ArrayList<Delivery>) bundle.getSerializable("lstDeliveries");
                JSONObject json = jsonArray.getJSONObject(0);
                JSONArray legs = json.getJSONArray("legs");
                JSONArray waypoints = json.getJSONArray("waypoint_order");
                JSONObject legObject;
                JSONObject distance;
                JSONObject duration;
                lstWayPoints = new ArrayList<>();
                String startAddress = "Current Location";
                WayPoint wayPoint = new WayPoint("Start", "Start", "", startAddress, "0", "0");
                lstWayPoints.add(wayPoint);

                 //Loops through the way points in the order specified by the Google Maps API (the optimised route), and adds the data to a WayPoint ArrayList
                for(int i = 0; i < waypoints.length(); i++){
                    Delivery delivery = lstDeliveries.get(waypoints.getInt(i));
                    String clientAddress = "";
                    String clientPhoneNumber = "";

                    for(Client client : lstClients){
                        if(client.getClientID().equals(delivery.getDeliveryClientID())){
                            clientAddress = client.getClientAddress();
                            clientPhoneNumber = client.getClientPhoneNumber();
                            break;
                        }
                    }
                    legObject = legs.getJSONObject(i);
                    distance = legObject.getJSONObject("distance");
                    duration = legObject.getJSONObject("duration");
                    wayPoint = new WayPoint(delivery.getDeliveryID(), delivery.getDeliveryClientID(), clientPhoneNumber, clientAddress, distance.getString("text"), duration.getString("text"));
                    lstWayPoints.add(wayPoint);
                }

                legObject = legs.getJSONObject(legs.length() - 1);
                distance = legObject.getJSONObject("distance");
                duration = legObject.getJSONObject("duration");
                String endAddress = legObject.getString("end_address");
                wayPoint = new WayPoint("End","End","", endAddress, distance.getString("text"), duration.getString("text"));
                lstWayPoints.add(wayPoint);

                //Displays the data from lstWayPoints
                final ListView listView = (ListView) findViewById(R.id.list_view_planned_route);
                final RouteListViewAdapter routeListViewAdapter = new RouteListViewAdapter(RoutePlannerActivity.this, lstWayPoints, lstDeliveries);
                listView.setAdapter(routeListViewAdapter);

                //Sets an OnItemClickListener, which will open the Google Maps app with the destination of the row that was clicked on
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String uri = "https://www.google.com/maps/dir/Current Location/";
                        uri += lstWayPoints.get(position).getClientAddress();

                        //Opens the Google Maps app with the destinations already entered
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(Intent.createChooser(intent, "Select an application"));
                    }
                });
                Toast.makeText(getApplicationContext(), "Route optimised", Toast.LENGTH_LONG).show();

                //Hides ProgressBar
                toggleProgressBarVisibility(View.INVISIBLE);
            }
            else{
                Toast.makeText(getApplicationContext(), "There was a problem with fetching the data from Google Maps, please try again...", Toast.LENGTH_LONG).show();
            }
        }
        catch(JSONException json){
            Toast.makeText(getApplicationContext(), json.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Opens the Google Maps app with a pre-populated route
    public void viewOnMapOnClick(View view){
        try{
            String uri = "https://www.google.com/maps/dir/";
            for(WayPoint wayPoint : lstWayPoints){
                uri += wayPoint.getClientAddress() + "/";
            }

            //Opens the Google Maps app with the destinations already entered
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(Intent.createChooser(intent, "Select an application"));
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method toggles the ProgressBar's visibility and disables touches when the ProgressBar is visible
    public void toggleProgressBarVisibility(int visibility){
        try{
            //Toggles ProgressBar visibility
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar) ;
            progressBar.setVisibility(visibility);

            //Enables touches on the screen if the ProgressBar is hidden, and disables touches on the screen when the ProgressBar is visible
            if(visibility == View.VISIBLE){
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            else{
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Creates a ResultReceiver to retrieve information from the FirebaseService
    private class DataReceiver extends ResultReceiver {
        private DataReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            if(resultCode == FirebaseService.ACTION_FETCH_CLIENTS_RESULT_CODE){
                lstClients = (ArrayList<Client>) resultData.getSerializable(FirebaseService.ACTION_FETCH_CLIENTS);
                if(lstClients != null){
                    fetchDeliveryDetails();
                }
            }
        }
    }
}