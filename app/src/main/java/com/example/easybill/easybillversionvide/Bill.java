package com.example.easybill.easybillversionvide;

/**
 * Created by louis on 21/11/2017.
 */

public class Bill {

    private float price;
    private String place;
    private String date;

    public Bill(float price, String place, String date) {
        this.price = price;
        this.place = place;
        this.date = date;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
