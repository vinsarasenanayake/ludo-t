package com.ludot.domain;

public interface MysteryCell {

    boolean isActive();

    boolean isAt(int cell);

    int location();

    int roundsRemaining();

    MysteryCell afterOneRound();
}