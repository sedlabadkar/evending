package com.mob.sachin.ev;

import java.io.Serializable;

/**
 * Created by Ankit on 04-Apr-16.
 */

public class ProductDetails implements Serializable {
    String productName;
    String quantityAvailable;
    String uniqueId;
    String selectedQuantity;
    String costOfProduct;
    String totalCost;

    public ProductDetails(){
        selectedQuantity = "0";
    }
}
