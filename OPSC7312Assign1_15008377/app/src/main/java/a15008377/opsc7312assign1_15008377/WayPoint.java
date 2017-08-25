package a15008377.opsc7312assign1_15008377;

/**
 * Created by Matthew Syrén on 2017/08/24.
 */

public class WayPoint {
    private String deliveryID;
    private String clientID;
    private String clientPhoneNumber;
    private String clientAddress;
    private String legDistance;
    private String legDuration;

    public WayPoint(String deliveryID, String clientID, String clientPhoneNumber, String clientAddress, String legDistance, String legDuration) {
        this.deliveryID = deliveryID;
        this.clientID = clientID;
        this.clientPhoneNumber = clientPhoneNumber;
        this.clientAddress = clientAddress;
        this.legDistance = legDistance;
        this.legDuration = legDuration;
    }

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
