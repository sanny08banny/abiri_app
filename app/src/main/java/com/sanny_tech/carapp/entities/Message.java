package com.sanny_tech.carapp.entities;

public class Message {
    private long id;
    private String data;
    private boolean isRead;

    public Message(long id, String data, boolean isRead) {
        this.id = id;
        this.data = data;
        this.isRead = isRead;
    }

    // Getters and setters
    public long getId() { return id; }
    public String getData() { return data; }
    public boolean isRead() { return isRead; }

    public void setId(long id) { this.id = id; }
    public void setData(String data) { this.data = data; }
    public void setRead(boolean read) { isRead = read; }
}

