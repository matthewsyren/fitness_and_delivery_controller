/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class used to receive the response from the APIConnection class
 */

package a15008377.opsc7312assign1_15008377;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Question1A extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_question1a);

            //Displays ProgressBar
            toggleProgressBarVisibility(View.VISIBLE);

            //Displays Back button in ActionBar
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null){
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("Question 1A");
            }

            //Checks for permission to access current location, and requests the permission if it is not granted
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(Question1A.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            //Fetches user's current location
            getLocation();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Handles the user's response to the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) throws SecurityException{
        switch(requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Refreshes the current Activity
                    Intent intent = getIntent();
                    finish();

                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "In order to use this page, you will need to allow the app to access your current location...", Toast.LENGTH_LONG).show();
                }
                break;
        }
        toggleProgressBarVisibility(View.INVISIBLE);
    }

    //Takes the user back to the StartActivity when the back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try{
            int id = item.getItemId();

            //Takes the user back to the StartActivity if the button that was pressed was the back button
            if (id == android.R.id.home) {
                Intent intent = new Intent(Question1A.this, StartActivity.class);
                startActivity(intent);
            }
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    //Method registers a LocationManager and onLocationChanged listener
    public void getLocation(){
        try{
            //Creates LocationManager object and registers a LocationChangedListener
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    try{
                        //Requests location information from the LocationService class
                        Intent intent = new Intent(getApplicationContext(), LocationService.class);
                        intent.putExtra(LocationService.LOCATION_DATA_KEY, location);
                        intent.putExtra(LocationService.RECEIVER, new LocationReceiver(new Handler()));
                        startService(intent);
                    }
                    catch(Exception ioe){
                         Toast.makeText(getApplicationContext(), ioe.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

            //Requests location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
        catch(SecurityException sec){
            Toast.makeText(getApplicationContext(), sec.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method toggles the ProgressBar's visibility
    public void toggleProgressBarVisibility(int visibility){
        try{
            //Toggles ProgressBar visibility
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar) ;
            progressBar.setVisibility(visibility);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Creates a ResultReceiver to retrieve information from the LocationService
    private class LocationReceiver extends ResultReceiver{
        private LocationReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            //Updates the current address whenever the LocationService sends a new address
            String result = resultData.getString(LocationService.RESULT_KEY);
            TextView textView = (TextView) findViewById(R.id.text_address);
            Resources resources = getResources();
            textView.setText(resources.getString(R.string.current_address, result));

            //Hides ProgressBar
            toggleProgressBarVisibility(View.INVISIBLE);
        }
    }
}