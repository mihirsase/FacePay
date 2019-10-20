package com.example.facepay;

public class ModelWallet {
    public String  purpose, date,time;
    public int amount , closingBalance;

    public ModelWallet() {

    }

    public ModelWallet( String purpose, String date, String time, int amount, int closingBalance) {

        this.purpose = purpose;
        this.date = date;
        this.time = time;
        this.amount = amount;
        this.closingBalance = closingBalance;
    }



    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(int closingBalance) {
        this.closingBalance = closingBalance;
    }
}
