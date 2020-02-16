package com.appsuknow.sfcsouthshields;

public interface OnQuantityChangeListener {
    void onQuantityChange(int quantity, int type);
    //type 0 = item
    //type 1 = multi item
}
