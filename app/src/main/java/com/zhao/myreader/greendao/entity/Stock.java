package com.zhao.myreader.greendao.entity;



import androidx.annotation.Nullable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

/**
 * 章节
 * Created by zhao on 2017/7/24.
 */

@Entity
public class Stock implements Serializable {
    @Transient
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    private String name;
    private int quantity;
    private double cost;
    private double price;
    private double upperThreshold;
    private double lowerThreshold;
    private int sortCode;
    @Generated(hash = 558408070)
    public Stock(String id, String name, int quantity, double cost, double price,
            double upperThreshold, double lowerThreshold, int sortCode) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.cost = cost;
        this.price = price;
        this.upperThreshold = upperThreshold;
        this.lowerThreshold = lowerThreshold;
        this.sortCode = sortCode;
    }
    @Generated(hash = 1902438397)
    public Stock() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getQuantity() {
        return this.quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public double getCost() {
        return this.cost;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }
    public double getPrice() {
        return this.price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public double getUpperThreshold() {
        return this.upperThreshold;
    }
    public void setUpperThreshold(double upperThreshold) {
        this.upperThreshold = upperThreshold;
    }
    public double getLowerThreshold() {
        return this.lowerThreshold;
    }
    public void setLowerThreshold(double lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }
    public int getSortCode() {
        return this.sortCode;
    }
    public void setSortCode(int sortCode) {
        this.sortCode = sortCode;
    }
}
