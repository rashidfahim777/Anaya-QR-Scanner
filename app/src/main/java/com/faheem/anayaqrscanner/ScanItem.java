package com.faheem.anayaqrscanner;

public class ScanItem {
    private int id;
    private int type;
    private String content;
    private String timestamp;

    // Constructors
    public ScanItem() {
        // Default constructor
    }

    public ScanItem(int type, String content, String timestamp) {
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
    }

    public ScanItem(int id, int type, String content, String timestamp) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ScanItem{" +
                "id=" + id +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}