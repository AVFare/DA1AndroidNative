package com.example.da1androidnative.data.model;

public class Notification {
    private Long id;
    private String type;     // reminder o info
    private String payload;
    private String deliverAt;

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getPayload() { return payload; }
    public String getDeliverAt() { return deliverAt; }
}
