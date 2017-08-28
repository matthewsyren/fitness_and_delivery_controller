/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class provides a basis for a WayPoint object
 */

package a15008377.opsc7312assign1_15008377;

public class WayPoint {
    //Declarations
    private String deliveryID;
    private String clientID;
    private String clientPhoneNumber;
    private String clientAddress;
    private String legDistance;
    private String legDuration;

    //Constructor
    public WayPoint(String deliveryID, String clientID, String clientPhoneNumber, String clientAddress, String legDistance, String legDuration) {
        this.deliveryID = deliveryID;
        this.clientID = clientID;
        this.clientPhoneNumber = clientPhoneNumber;
        this.clientAddress = clientAddress;
        this.legDistance = legDistance;
        this.legDuration = legDuration;
    }

    //Getter methods
    public String getDeliveryID() {
        return deliveryID;
    }

    public String getClientID() {
        return clientID;
    }

    public String getClientPhoneNumber() {
        return clientPhoneNumber;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public String getLegDistance() {
        return legDistance;
    }

    public String getLegDuration() {
        return legDuration;
    }
}