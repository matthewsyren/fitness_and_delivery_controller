package a15008377.opsc7312assign1_15008377;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

public class LocationService extends IntentService {
    //Result codes
    public static final int LOCATION_ADDRESS_KEY = 2;
    public static final String LOCATION_DATA_KEY = "a15008377.opsc7312assign1_15008377.LOCATION";
    public static final String RESULT_KEY = "a15008377.opsc7312assign1_15008377.RESULT_KEY";
    public static final String RECEIVER = "a15008377.opsc7312assign1_15008377.RECEIVER";
    public static final int SUCCESS = 1;
    private ResultReceiver resultReceiver;

    public LocationService() {
        super("LocationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try{
            resultReceiver = intent.getParcelableExtra(RECEIVER);
            Location location = intent.getParcelableExtra(LOCATION_DATA_KEY);
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
            Address address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);

            String addressString = "";

            for(int i = 0; i <= address.getMaxAddressLineIndex() ; i++) {
                addressString += address.getAddressLine(i);
            }

            returnResult(LOCATION_ADDRESS_KEY, SUCCESS, addressString);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method returns the message back to the result receiver
    public void returnResult(int resultCode, int success, String message) throws Exception{
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }
}
