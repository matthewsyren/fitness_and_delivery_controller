/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class connects to a web page specified in the URL that is passed in, and sends the response from
 *              that website to the getJsonResponse method in the delegate class
 */

package a15008377.opsc7312assign1_15008377;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
@SuppressWarnings("WeakerAccess")
public class APIConnection extends AsyncTask<String, Void, String> {

    //Declares an object of the IAPIConnectionResponse interface, which will be used to send the JSON back to the main thread
    public IAPIConnectionResponse delegate = null;

    //Method retrieves the JSON returned from the API
    protected String doInBackground(String... urls) {
        HttpURLConnection urlConnection = null;
        try {
            BufferedReader bufferedReader = null;
            StringBuilder stringBuilder = new StringBuilder();

            //Saves the response to a StringBuilder object
            for(int i = 0; i < urls.length; i++){
                URL url = new URL(urls[i]);
                urlConnection = (HttpURLConnection) url.openConnection();
                bufferedReader  = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if(urls.length > 1 && i < urls.length - 1){
                        stringBuilder.append(line).append(",\n");
                    }
                    else{
                        stringBuilder.append(line).append("\n");
                    }
                }
            }

            bufferedReader.close();
            return stringBuilder.toString();
        }
        catch(Exception exc){
           exc.printStackTrace();
        }
        finally{
            urlConnection.disconnect();
        }
        return null;
    }

    //Method passes the JSON back to the Main thread (to the class from which this class was instantiated)
    protected void onPostExecute(String response) {
        if(delegate != null){
            delegate.getJsonResponse(response);
        }
    }
}