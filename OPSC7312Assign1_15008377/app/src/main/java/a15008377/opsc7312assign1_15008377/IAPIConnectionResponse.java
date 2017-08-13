/**
 * Author: Matthew Syrén
 *
 * Date:   19 May 2017
 *
 * Description: Class used to receive the response from the APIConnection class
 */

package a15008377.opsc7312assign1_15008377;

@SuppressWarnings("WeakerAccess")
public interface IAPIConnectionResponse {
    //Method is used to parse JSON from an API. The class that needs the data will implement this interface, and the APIConnection class sends the data to the method once it has fetched the data
    public void getJsonResponse(String response);
}
