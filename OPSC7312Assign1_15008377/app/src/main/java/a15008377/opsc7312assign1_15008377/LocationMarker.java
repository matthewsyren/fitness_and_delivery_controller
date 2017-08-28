/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class used to as a basis for a LocationMarker object
 */

package a15008377.opsc7312assign1_15008377;

import com.google.android.gms.maps.model.LatLng;

@SuppressWarnings("WeakerAccess")
public class LocationMarker{
    //Declarations
    private LatLng location;
    private String markerTitle;

    //Constructor
    public LocationMarker(LatLng location, String markerTitle){
        this.location = location;
        this.markerTitle = markerTitle;
    }

    //Getter methods
    public LatLng getLocation() {
        return location;
    }

    public String getMarkerTitle() {
        return markerTitle;
    }

    //Setter method
    public void setLocation(LatLng location) {
        this.location = location;
    }
}