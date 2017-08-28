/*
 * Author: Matthew Syrén
 *
 * Date:   29 August 2017
 *
 * Description: Class provides a template for a DeliveryItem object
 */

package a15008377.opsc7312assign1_15008377;

import java.io.Serializable;
@SuppressWarnings("WeakerAccess")
public class DeliveryItem implements Serializable{
    //Declarations
    private String deliveryStockID;
    private int deliveryItemQuantity;

    //Constructor
    public DeliveryItem(String deliveryStockID, int deliveryItemQuantity) {
        this.deliveryStockID = deliveryStockID;
        this.deliveryItemQuantity = deliveryItemQuantity;
    }

    //Default constructor
    public DeliveryItem(){}

    //Getter methods
    public int getDeliveryItemQuantity() {
        return deliveryItemQuantity;
    }

    public String getDeliveryStockID() {
        return deliveryStockID;
    }

    //Setter method
    public void setDeliveryItemQuantity(int deliveryItemQuantity) {
        this.deliveryItemQuantity = deliveryItemQuantity;
    }
}
