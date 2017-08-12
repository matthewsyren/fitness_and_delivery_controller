package a15008377.opsc7312assign1_15008377;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Question1A extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_question1a);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(Question1A.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            getLocation();
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
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

    //Creates a ResultReceiver to retrieve information from the LocationService
    private class LocationReceiver extends ResultReceiver{
        private LocationReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            String result = resultData.getString(LocationService.RESULT_KEY);
            Toast.makeText(getApplicationContext(), "Address is " + result, Toast.LENGTH_LONG).show();
        }
    }
}