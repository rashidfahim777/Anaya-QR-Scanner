package com.faheem.anayaqrscanner;

public class SettingItem {
    public static final int TYPE_TOGGLE = 1;
    public static final int TYPE_ARROW = 2;
    public static final int TYPE_TEXT = 3;

    private String title;
    private String subtitle;
    private int type;
    private boolean toggleState;

    public SettingItem(String title, String subtitle, int type, boolean toggleState) {
        this.title = title;
        this.subtitle = subtitle;
        this.type = type;
        this.toggleState = toggleState;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getType() {
        return type;
    }

    public boolean getToggleState() {
        return toggleState;
    }

    public void setToggleState(boolean toggleState) {
        this.toggleState = toggleState;
    }
}