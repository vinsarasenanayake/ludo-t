package com.ludot.rules;

import com.ludot.domain.effect.BriefingEffect;
import com.ludot.domain.effect.EnergisedEffect;
import com.ludot.domain.effect.PieceEffect;
import com.ludot.domain.effect.SickEffect;

public class EffectFactory {

    public PieceEffect createEnergisedEffect() {
        return new EnergisedEffect();
    }

    public PieceEffect createSickEffect() {
        return new SickEffect();
    }

    public PieceEffect createBriefingEffect() {
        return new BriefingEffect();
    }
}