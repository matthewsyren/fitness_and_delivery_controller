package a15008377.opsc7312assign1_15008377;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Question1B extends FragmentActivity implements OnMapReadyCallback {
    //Declarations
    private GoogleMap mMap;
    private PolylineOptions polylineOptions = new PolylineOptions();
    private final int LOCATION_PERMISSION_KEY = 12345;
    private LocationListener locationListener = null;
    private long startTime = 0;
    private Location previousLocation;
    private double distanceTravelled = 0;
    private StorageReference mStorageRef;
    private LocationManager locationManager;
    private LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private int latLngBoundsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_question1b);

            //Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mStorageRef = FirebaseStorage.getInstance().getReference();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method will either begin or end location tracking (based on whether location tracking is currently being performed)
    public void checkTracking(View view){
        try{
            if(locationListener == null){
                if(ContextCompat.checkSelfPermission(Question1B.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(Question1B.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_KEY);
                }
                else{
                    beginLocationTracking();
                }
            }
            else{
                endLocationTracking();
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void toggleFloatingActionButtonImage(){
        try{
            //Changes the icon of the FloatingActionButton
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floating_action_button_track_route);
            if(fab.getTag().equals("Stop")){
                fab.setImageResource(R.drawable.ic_run);
                fab.setTag("Run");
            }
            else{
                fab.setImageResource(R.drawable.ic_stop);
                fab.setTag("Stop");
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method displays the map
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        try{
            mMap = googleMap;
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method begins tracking the user's location, with a polyline being drawn over the path that the user moves (whenever the location changes, a polyline is drawn between the previous location and the new location)
    public void beginLocationTracking() throws SecurityException{
        try{
            mMap.setMyLocationEnabled(true);
            startTime = System.currentTimeMillis();

            //Creates LocationManager object and registers a LocationChangedListener
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            zoomToLocation(latLng);

            //Changes the icon of the FloatingActionButton
            toggleFloatingActionButtonImage();
            Toast.makeText(getApplicationContext(), "Route tracking started", Toast.LENGTH_LONG).show();

            //Defines a LocationListener
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    builder.include(latLng);
                    latLngBoundsCount++;
                    zoomToLocation(latLng);
                    drawPolyline(latLng);
                    if(previousLocation != null){
                        //Updates the total distance travelled by the user
                        distanceTravelled += location.distanceTo(previousLocation);
                    }
                    previousLocation = location;
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            //Requests location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Handles the user's response to the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) throws SecurityException{
        switch(requestCode){
            case LOCATION_PERMISSION_KEY:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    beginLocationTracking();
                }
                else{
                    Toast.makeText(getApplicationContext(), "In order to use this page, you will need to allow the app to access your current location...", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    //Method zooms the camera to the specified LatLng marker
    public void zoomToLocation(LatLng location){
        try{
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 250);
            mMap.animateCamera(cameraUpdate);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method draws a polyline between the points that the user has visited while the app has been open
    public void drawPolyline(LatLng position){
        try{
            polylineOptions.add(position);
            polylineOptions.width(5).color(Color.BLUE);
            mMap.addPolyline(polylineOptions);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method ends the location tracking of the user, and asks them if they would like to save their route details
    public void endLocationTracking(){
        try{
            locationManager.removeUpdates(locationListener);
            locationListener = null;
            Toast.makeText(getApplicationContext(), "Route tracking stopped", Toast.LENGTH_LONG).show();

            if(latLngBoundsCount > 0){
                //Zooms out to display the entire route taken by the user
                LatLngBounds bounds = builder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 250);
                mMap.animateCamera(cameraUpdate);

                //Changes the icon of the FloatingActionButton
                toggleFloatingActionButtonImage();
                promptToSaveUserDetails();
            }
            else{
                Toast.makeText(getApplicationContext(), "No movement was detected, therefore there is no route to save", Toast.LENGTH_LONG).show();
                toggleFloatingActionButtonImage();
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method saves the details of the route taken by the user while tracking their location
    public void promptToSaveUserDetails(){
        try{
            AlertDialog alertDialog = new AlertDialog.Builder(Question1B.this).create();
            alertDialog.setTitle("Save Route?");
            alertDialog.setMessage("Would you like to save the details of the route you took today?");

            //Creates OnClickListener for the Dialog message
            DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int button) {
                    switch(button){
                        case AlertDialog.BUTTON_POSITIVE:
                            saveUserDetails();
                            break;
                        case AlertDialog.BUTTON_NEGATIVE:
                            Toast.makeText(getApplicationContext(), "Route information not saved", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            };

            //Assigns buttons and OnClickListener for the AlertDialog and displays the AlertDialog
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", dialogOnClickListener);
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", dialogOnClickListener);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
            toggleFloatingActionButtonImage();
        }
    }

    //Method saves the user details to Firebase
    public void saveUserDetails(){
        try{
            //Calculates the Run's details
            long endTime = System.currentTimeMillis();
            long timeDifference = endTime - startTime;
            double averageSpeed = distanceTravelled / (timeDifference / 1000) * 3.6;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(startTime);
            String startDate = simpleDateFormat.format(calendar.getTime());
            calendar.setTimeInMillis(endTime);
            String endDate = simpleDateFormat.format(calendar.getTime());
            averageSpeed = Math.round(averageSpeed);
            distanceTravelled = Math.round(distanceTravelled);

            //Gets a reference to the Firebase Database (with a randomised key for the Run), creates a Run object and writes the data to the Firebase Database and the image to Firebase Storage
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference().child(new User(this).getUserKey()).child("runs");
            String key = databaseReference.push().getKey();
            Run run = new Run(startDate, endDate, distanceTravelled, averageSpeed);
            saveScreenshot(databaseReference, key, run);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
            toggleFloatingActionButtonImage();
        }
    }

    //Method opens the RouteHistoryActivity
    public void viewHistoryOnClick(View view){
        try{
            Intent intent = new Intent(Question1B.this, RouteHistoryActivity.class);
            startActivity(intent);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method saves a screenshot of the map to Firebase Storage
    public void saveScreenshot(final DatabaseReference databaseReference, final String key, final Run run){
        try{
            GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    try{
                        //Converts the Bitmap image (screenshot of the map) to a byte array
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

                        //Saves the image to Firebase Storage (with the name of [key for run].jpg
                        StorageReference storageReference = mStorageRef.child(key + ".jpg");
                        storageReference.putBytes(bytes.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                saveRunDetails(databaseReference, key, run);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    catch(Exception ioe){
                        Toast.makeText(getApplicationContext(), ioe.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            };
            //Steps the SnapshotReadyCallback function for the map
            mMap.snapshot(callback);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
            toggleFloatingActionButtonImage();
        }
    }

    //Method writes the information to the Firebase database
    public void saveRunDetails(DatabaseReference databaseReference, String key, Run run){
        try{
            databaseReference.child(key).setValue(run);
            Toast.makeText(getApplicationContext(), "Run information saved", Toast.LENGTH_LONG).show();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}