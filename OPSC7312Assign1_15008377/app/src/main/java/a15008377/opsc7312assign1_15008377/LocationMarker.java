package a15008377.opsc7312assign1_15008377;

import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Matthew Syrén on 2017/07/29.
 */

public class LocationMarker {
    private LatLng location;
    private String markerTitle;

    public LocationMarker(LatLng location, String markerTitle){
        this.location = location;
        this.markerTitle = markerTitle;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getMarkerTitle() {
        return markerTitle;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setMarkerTitle(String markerTitle) {
        this.markerTitle = markerTitle;
    }
}
