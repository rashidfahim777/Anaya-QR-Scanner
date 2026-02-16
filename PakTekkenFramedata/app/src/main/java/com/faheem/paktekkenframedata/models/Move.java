package com.faheem.paktekkenframedata.models;

import java.util.List;

public class Move {
    private String command;
    private String hitLevel;
    private String damage;
    private String startup;
    private String block;
    private String hit;
    private String counterHit;
    private List<String> notes;

    public Move() {
    }

    public Move(String command, String hitLevel, String damage, String startup,
                String block, String hit, String counterHit, List<String> notes) {
        this.command = command;
        this.hitLevel = hitLevel;
        this.damage = damage;
        this.startup = startup;
        this.block = block;
        this.hit = hit;
        this.counterHit = counterHit;
        this.notes = notes;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getHitLevel() {
        return hitLevel;
    }

    public void setHitLevel(String hitLevel) {
        this.hitLevel = hitLevel;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public String getStartup() {
        return startup;
    }

    public void setStartup(String startup) {
        this.startup = startup;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getHit() {
        return hit;
    }

    public void setHit(String hit) {
        this.hit = hit;
    }

    public String getCounterHit() {
        return counterHit;
    }

    public void setCounterHit(String counterHit) {
        this.counterHit = counterHit;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    // Helper method to get color based on block advantage
    public int getBlockAdvantageColor() {
        if (block == null || block.isEmpty()) return 0xFF9E9E9E; // Grey

        try {
            String blockStr = block.replace("+", "").replace("-", "");
            int value = Integer.parseInt(blockStr);

            if (block.contains("-")) {
                // Negative is bad for you
                if (value >= 10) return 0xFFF44336; // Red - very unsafe
                if (value >= 5) return 0xFFFF9800; // Orange - unsafe
                return 0xFFFFC107; // Yellow - slightly unsafe
            } else {
                // Positive is good for you
                if (value >= 5) return 0xFF4CAF50; // Green - very safe
                if (value >= 3) return 0xFF8BC34A; // Light green - safe
                return 0xFFCDDC39; // Lime - slightly safe
            }
        } catch (NumberFormatException e) {
            return 0xFF9E9E9E; // Grey
        }
    }

    // Helper method to get color based on hit level
    public int getHitLevelColor() {
        if (hitLevel == null) return 0xFF9E9E9E;

        if (hitLevel.contains("h")) return 0xFF2196F3; // Blue - high
        if (hitLevel.contains("m")) return 0xFFFF9800; // Orange - mid
        if (hitLevel.contains("l")) return 0xFF4CAF50; // Green - low
        if (hitLevel.contains("t")) return 0xFF9C27B0; // Purple - throw
        return 0xFF9E9E9E; // Grey
    }
}