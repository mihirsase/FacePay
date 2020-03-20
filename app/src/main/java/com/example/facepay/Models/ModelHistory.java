package com.example.facepay.Models;

import java.util.List;

public class ModelHistory {

    String transactionId;
    List<CartItem> cartItemList;


    public ModelHistory(String transactionId, List<CartItem> cartItemList) {
        this.transactionId = transactionId;
        this.cartItemList = cartItemList;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<CartItem> getCartItemList() {
        return cartItemList;
    }

    public void setCartItemList(List<CartItem> cartItemList) {
        this.cartItemList = cartItemList;
    }

    public static class CartItem{

        String name,weight;
        int price,quantity;

        public CartItem(String name, String weight, int price, int quantity) {
            this.name = name;
            this.weight = weight;
            this.price = price;
            this.quantity = quantity;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
