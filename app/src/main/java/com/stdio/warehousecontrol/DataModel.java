package com.stdio.warehousecontrol;

public class DataModel {
    public String article, barcode, name, count, size;

    public DataModel() {
    }

    DataModel(String article, String barcode, String name, String count, String size) {
        this.article = article;
        this.barcode = barcode;
        this.name = name;
        this.count = count;
        this.size = size;
    }

    /*public String getIsComplete() {
        return isComplete;
    }*/

}