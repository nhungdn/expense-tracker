package com.bachulun.Service;

public class EditHistory {
    private String time;
    private int transactionId;
    private double oldAmount;
    private double newAmount;
    private String oldDesc;
    private String newDesc;

    public EditHistory(String time, int transactionId, double oldAmount, double newAmount, String oldDesc, String newDesc) {
        this.time = time;
        this.transactionId = transactionId;
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
        this.oldDesc = oldDesc;
        this.newDesc = newDesc;
    }

    public String getTime() { return time; }
    public int getTransactionId() { return transactionId; }
    public double getOldAmount() { return oldAmount; }
    public double getNewAmount() { return newAmount; }
    public String getOldDesc() { return oldDesc; }
    public String getNewDesc() { return newDesc; }
}
