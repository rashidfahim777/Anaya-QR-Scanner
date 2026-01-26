package com.faheem.anayaqrscanner;

public class QROption {
    private String name;
    private int iconResId;
    private QRCodeType type;

    public QROption(String name, int iconResId, QRCodeType type) {
        this.name = name;
        this.iconResId = iconResId;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public QRCodeType getType() {
        return type;
    }
}