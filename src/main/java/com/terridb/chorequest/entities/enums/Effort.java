package com.terridb.chorequest.entities.enums;

public enum Effort {
    NONE(5),
    SMALL(10),
    MEDIUM(15),
    BIG(30);

    private final int points;

    Effort(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
