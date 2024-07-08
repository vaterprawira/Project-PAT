package com.example.concertapp;

public class Payment {
    private int orderId;
    private String metodePembayaran;
    private String virtualAccount;
    private String status;

    public Payment(int orderId, String metodePembayaran, String virtualAccount, String status) {
        this.orderId = orderId;
        this.metodePembayaran = metodePembayaran;
        this.virtualAccount = virtualAccount;
        this.status = status;
    }

    public int getOrderId() { return orderId; }
    public String getMetodePembayaran() { return metodePembayaran; }
    public String getVirtualAccount() { return virtualAccount; }
    public String getStatus() { return status; }
}

