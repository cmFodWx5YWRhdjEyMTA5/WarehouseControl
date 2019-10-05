package com.stdio.warehousecontrol;

public class DataModel {
    public String article, barcode, name, count, address;

    public DataModel() {
    }

    DataModel(String article, String barcode, String name, String count, String address) {
        this.article = article;
        this.barcode = barcode;
        this.name = name;
        this.count = count;
        this.address = address;
    }

    /*public String getIsComplete() {
        return isComplete;
    }*/

}