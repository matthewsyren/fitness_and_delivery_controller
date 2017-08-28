/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class displays Deliveries for the current date on a map using Google Maps Markers, and allows the user to optimise their route (if they have fewer than 23 Deliveries)
 */

package a15008377.opsc7312assign1_15008377;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.Calendar;

public class Question2 extends BaseActivity implements OnMapReadyCallback {
    //Declarations
    private GoogleMap gMap;
    private ArrayList<LocationMarker> lstDestinations;
    private ArrayList<Delivery> lstDeliveries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);

            //Sets the NavigationDrawer for the Activity and sets the selected item in the NavigationDrawer to Home
            super.onCreateDrawer();
            super.setSelectedNavItem(R.id.nav_home);

            //Checks for permission to access current location, and asks for permission if it hasn't been granted
            if(ContextCompat.checkSelfPermission(Question2.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Question2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
            }

            //Hides FloatingActionButton
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setVisibility(View.INVISIBLE);

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Requests the Deliveries from the Firebase Database
            new Delivery().requestDeliveries(null, this, new DataReceiver(new Handler()), 0);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Displays Markers on a Map, with each Marker representing the destination of a Delivery that needs to be made
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        try{
            //Requests permission to access location if the permission is not granted, otherwise enables location tracking
            if(ContextCompat.checkSelfPermission(Question2.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Question2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
            }
            else{
                googleMap.setMyLocationEnabled(true);
            }

            //Sets the map and declares a LatLngBounds Builder (used to set zoom level when displaying all Deliveries for the day)
            gMap = googleMap;
            final LatLngBounds.Builder builder = new LatLngBounds.Builder();

            //Displays the markers and sets their titles
            for(int i = 0; i < lstDestinations.size(); i++){
                googleMap.addMarker(new MarkerOptions().position(lstDestinations.get(i).getLocation()).title(lstDestinations.get(i).getMarkerTitle()));
                builder.include(lstDestinations.get(i).getLocation());
            }

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    //Hides FloatingActionButton while a Marker displays its details (as the FloatingActionButton would cover the navigation button if it were visible)
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    fab.setVisibility(View.INVISIBLE);
                    return false;
                }
            });

            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    //Displays FloatingActionButton when the user clicks away from a Marker
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    fab.setVisibility(View.VISIBLE);
                }
            });

            //Animates camera to zoom in on Markers once the map has been loaded
            final RelativeLayout layout = (RelativeLayout) findViewById(R.id.content_home);
            layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try{
                        //Displays Markers for today's Deliveries, otherwise outputs a message saying no Deliveries have been scheduled for the current date
                        if(lstDestinations.size() != 0){
                            //Bounds ensures all Deliveries will be visible once zoomed in
                            LatLngBounds bounds = builder.build();
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250));
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "You have no deliveries for today... If you would like to add a delivery, go to the Delivery Control page", Toast.LENGTH_LONG).show();
                        }
                    }
                    catch(IllegalStateException ise){
                        Toast.makeText(getApplicationContext(), ise.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    //Removes OnGlobalLayoutListener to prevent recurrent animations on the map
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
        catch(NullPointerException exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
            exc.printStackTrace();
        }
    }

    //Method creates an ArrayList of markers to be displayed on the map
    public void displayMarkers(ArrayList<Delivery> lstDeliveries, ArrayList<Client> lstClients){
        try{
            //Declarations
            final ArrayList<LocationMarker> lstMarkers = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            String currentDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);

            //Loops through the Deliveries and sets the title for each Marker, and adds each Marker to lstMarkers
            for(Delivery delivery : lstDeliveries){
                if(delivery.getDeliveryDate().equals(currentDate)){
                    for(Client client : lstClients){
                        if(client.getClientID().equals(delivery.getDeliveryClientID())){
                            LatLng latLng = new LatLng(client.getClientLatitude(), client.getClientLongitude());
                            String markerTitle = "Delivery: " + delivery.getDeliveryID() + "     Client Name: " + client.getClientName();
                            lstMarkers.add(new LocationMarker(latLng, markerTitle));
                        }
                    }
                }
            }

            lstDestinations = lstMarkers;
            if(lstMarkers.size() > 0){
                //Displays FloatingActionButton if Deliveries are found for today
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setVisibility(View.VISIBLE);
            }

            //Sets up the Map for this Activity
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method sends the route information to the Google Maps API, which will then return the most efficient route between the destinations
    public void optimiseRoute(View view){
        if(lstDestinations.size() <= 23){
            //Ensure that the device has location access permissions, before trying to optimise the route
            if(ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                //Declares a LocationManager and uses the current location as the final destination of the trip (the Delivery vehicle goes back to the same place that it started at the end of the day)
                final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Intent intent = new Intent(Question2.this, RoutePlannerActivity.class);

                //Passes the Delivery data to the Google Maps APU
                String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + latLng.latitude + ", " + latLng.longitude + "&destination=" + latLng.latitude + ", " + latLng.longitude + "&waypoints=optimize:true";
                for(int i = 0; i < lstDestinations.size(); i++){
                    url += "|" + lstDestinations.get(i).getLocation().latitude + "," + lstDestinations.get(i).getLocation().longitude;
                }
                url += "&key=AIzaSyB-hYaZ4URR-NVjYV0vpgIAUYb4B3Z9Y2g";
                intent.putExtra("routeURL", url);
                intent.putExtra("lstDeliveries", lstDeliveries);
                startActivity(intent);
            }
            else{
                Toast.makeText(getApplicationContext(), "Please enable location tracking before using this feature", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Unable to calculate directions and optimise route, as too manny Deliveries have been entered for today. If you would like to optimise the route, you will need to have less than 23 Deliveries scheduled for today.", Toast.LENGTH_LONG).show();
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
            //Processes the result when the Deliveries are fetched from the Firebase Database
            if(resultCode == FirebaseService.ACTION_FETCH_DELIVERIES_RESULT_CODE){
                lstDeliveries = (ArrayList<Delivery>) resultData.getSerializable(FirebaseService.ACTION_FETCH_DELIVERIES);
                new Client().requestClients(null, getApplicationContext(), this );
            }
            //Processes the result when the Clients are fetched from the Firebase Database
            else if(resultCode == FirebaseService.ACTION_FETCH_CLIENTS_RESULT_CODE){
                ArrayList<Client> lstClients = (ArrayList<Client>) resultData.getSerializable(FirebaseService.ACTION_FETCH_CLIENTS);
                displayMarkers(lstDeliveries, lstClients);
            }

            //Hides the ProgressBar
            toggleProgressBarVisibility(View.INVISIBLE);
        }
    }
}