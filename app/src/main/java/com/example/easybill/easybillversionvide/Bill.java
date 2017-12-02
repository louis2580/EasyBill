package com.example.easybill.easybillversionvide;

import java.util.ArrayList;

/**
 * Created by louis on 21/11/2017.
 */

public class Bill {

    private float price;
    private String place;
    private String date;
    private String folder;
    private String path;
    private String id;

    public Bill(float price, String place, String date, String folder) {
        this.price = price;
        this.place = place;
        this.date = date;
        this.folder = folder;
        this.path = "N/C";
    }

    public Bill(float price, String place, String date, String folder, String path) {
        this.price = price;
        this.place = place;
        this.date = date;
        this.folder = folder;
        this.path = path;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolder() { return folder; }

    public void setFolder(String folder) { this.folder = folder; }

    public String getId() { return id; }

    public void setId(String Id) { this.id = Id; }

}
