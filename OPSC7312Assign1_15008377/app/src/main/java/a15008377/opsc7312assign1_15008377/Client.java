/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class provides the basis for a Client object
 */

package a15008377.opsc7312assign1_15008377;

import android.content.Context;
import android.util.Patterns;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.Serializable;

@SuppressWarnings("WeakerAccess")
public class Client implements Serializable{
    //Declarations
    private String clientID;
    private String clientName;
    private String clientEmail;
    private String clientAddress;
    private double clientLatitude;
    private double clientLongitude;

    //Constructor
    public Client(String clientID, String clientName, String clientEmail, String clientAddress) {
        this.clientID = clientID;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.clientAddress = clientAddress;
    }

    public Client(String clientID, String clientName, String clientEmail, String clientAddress, double clientLatitude, double clientLongitude) {
        this(clientID, clientName, clientEmail, clientAddress);
        this.clientLatitude = clientLatitude;
        this.clientLongitude = clientLongitude;
    }

    public Client(){}

    //Getter methods
    public String getClientID() {
        return clientID;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public double getClientLatitude() {
        return clientLatitude;
    }

    public double getClientLongitude() {
        return clientLongitude;
    }

    //Setter methods
    public void setClientLongitude(double clientLongitude) {
        this.clientLongitude = clientLongitude;
    }

    public void setClientLatitude(double clientLatitude) {
        this.clientLatitude = clientLatitude;
    }

    //Method ensures that the Client has valid values for all of its fields. Returns true of Client is valid, otherwise returns false
    public boolean validateClient(Context context){
        boolean validClient = false;

        //If statements check numerous validation criteria for the Stock object.
        if(clientID.length() == 0){
            Toast.makeText(context, "Please enter a Client ID", Toast.LENGTH_LONG).show();
        }
        else if(clientID.contains(" ")){
            Toast.makeText(context, "Please remove all spaces from the Client ID", Toast.LENGTH_LONG).show();
        }
        else if(clientName.length() == 0){
            Toast.makeText(context, "Please enter a Client Name", Toast.LENGTH_LONG).show();
        }
        else if(clientEmail.length() == 0){
            Toast.makeText(context, "Please enter a Client Email", Toast.LENGTH_LONG).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(clientEmail).matches()){
            Toast.makeText(context, "Please enter a valid email address e.g. clientname@example.com", Toast.LENGTH_LONG).show();
        }
        else if(clientAddress.length() == 0){
            Toast.makeText(context, "Please enter a Client Address", Toast.LENGTH_LONG).show();
        }
        else{
            validClient = true;
        }
        return validClient;
    }

    //Method checks if the entered Client ID has already been taken. The method returns true if it has been taken, and false if it hasn't been taken
    public boolean checkClientID(Context context) throws IOException {
        boolean clientIDTaken = false;
        /*DBAdapter dbAdapter = new DBAdapter(context);
        dbAdapter.open();
        Client client = dbAdapter.getClient(clientID);
        if(client != null){
            clientIDTaken = true;
            Toast.makeText(context, "Client ID is taken, please choose another one", Toast.LENGTH_LONG).show();
        }
        dbAdapter.close(); */
        return clientIDTaken;
    }

    //Method parses the JSOn from the response String passed into the method, and returns a JSONObject if the JSON is valid, otherwise it returns null
    public static JSONObject getAddressCoordinates(String response, Context context) throws JSONException{
        if(response != null){
            JSONObject jsonObject = new JSONObject(response);

            if(jsonObject.getString("status").equals("OK")){
                //Returns JSONObject if JSON is valid
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                return jsonArray.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
            }
            else{
                //Displays error message saying address wasn't found
                Toast.makeText(context, "Google Maps was unable to locate the address you typed in, please ensure that the address you have typed in is correct", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(context, "Problem fetching data from Google Maps, please try again...", Toast.LENGTH_LONG).show();
        }
        return null;
    }
}