package com.example.facepay.Models;

public class ModelShop {
    public String  product, weight,id;
    public int amount , quantity;

    public ModelShop(String product, String weight, int amount, int quantity,String id) {
        this.product = product;
        this.weight = weight;
        this.id = id;
        this.amount = amount;
        this.quantity = quantity;
    }



    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
