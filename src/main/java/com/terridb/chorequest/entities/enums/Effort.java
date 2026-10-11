package com.terridb.chorequest.entities.enums;

import lombok.Getter;

@Getter
public enum Effort {
    NONE(5),
    SMALL(10),
    MEDIUM(15),
    BIG(30);

    private final int points;

    Effort(int points) {
        this.points = points;
    }

}
