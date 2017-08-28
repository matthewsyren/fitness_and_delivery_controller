/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class used to retrieve location information
 */

package a15008377.opsc7312assign1_15008377;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.widget.Toast;
import java.util.Locale;

public class LocationService extends IntentService {
    //Declarations
    public static final int LOCATION_ADDRESS_KEY = 2;
    public static final String LOCATION_DATA_KEY = "a15008377.opsc7312assign1_15008377.LOCATION";
    public static final String RESULT_KEY = "a15008377.opsc7312assign1_15008377.RESULT_KEY";
    public static final String RECEIVER = "a15008377.opsc7312assign1_15008377.RECEIVER";
    private ResultReceiver resultReceiver;

    //Constructor
    public LocationService() {
        super("LocationService");
    }

    //Method fetches the address for the current location
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try{
            //Fetches the data passed in via an Intent
            resultReceiver = intent.getParcelableExtra(RECEIVER);
            Location location = intent.getParcelableExtra(LOCATION_DATA_KEY);

            //Address for current location is retrieved
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
            Address address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);

            //The full address is saved into the addressString variable
            String addressString = "";
            for(int i = 0; i <= address.getMaxAddressLineIndex() ; i++) {
                addressString += address.getAddressLine(i);
            }

            //Result is returned
            returnResult(LOCATION_ADDRESS_KEY, addressString);
        }
        catch(Exception exc){
            Toast.makeText(getApplicationContext(), exc.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //Method returns the message back to the result receiver
    public void returnResult(int resultCode, String message) throws Exception{
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }
}