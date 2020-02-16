package com.appsuknow.sfcsouthshields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BasketLogic {

    static String minOrder;
    static String deliveryCharge;
    static ArrayList<DealsBasket> deals = new ArrayList<>();
    static Map<String,ItemsBasket> singleItems = new HashMap<>(); //item_id : item object
    static Map<String,MultiItemsBasket> multiItems = new HashMap<>(); //item_id: item object

    //Func - Total price of all items
    public double priceOfAllItems() {
        double price = 0.0;

        if(!deals.isEmpty()) {
            for (DealsBasket deal:deals) {
                price += deal.price;
            }
        }

        if(!singleItems.isEmpty()) {
            for(Map.Entry<String, ItemsBasket> singleItem : singleItems.entrySet()) {
                ItemsBasket value = singleItem.getValue();
                price += (value.item_price * value.quantity);
            }
        }

        if(!multiItems.isEmpty()) {
            for(Map.Entry<String, MultiItemsBasket> multiItem : multiItems.entrySet()) {
                MultiItemsBasket value = multiItem.getValue();
                price += (value.multi_price * value.quantity);
            }
        }

        return price;
    }

    //Func - Total count of all items
    public int countOfAllItems() {
        int count = 0;

        if(!deals.isEmpty()) {
            count += deals.size();
        }

        if(!singleItems.isEmpty()) {
            for(Map.Entry<String, ItemsBasket> singleItem : singleItems.entrySet()) {
                ItemsBasket value = singleItem.getValue();
                count += value.quantity;
            }
        }

        if(!multiItems.isEmpty()) {
            for(Map.Entry<String, MultiItemsBasket> multiItem : multiItems.entrySet()) {
                MultiItemsBasket value = multiItem.getValue();
                count += value.quantity;
            }
        }

        return count;
    }
}
