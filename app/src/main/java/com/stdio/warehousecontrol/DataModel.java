package com.stdio.warehousecontrol;

public class DataModel {
    public String article, barcode, name, count, size, key;

    public DataModel() {
    }

    DataModel(String article, String barcode, String name, String count, String size, String key) {
        this.article = article;
        this.barcode = barcode;
        this.name = name;
        this.count = count;
        this.size = size;
        this.key = key;
    }

    /*public String getIsComplete() {
        return isComplete;
    }*/

}