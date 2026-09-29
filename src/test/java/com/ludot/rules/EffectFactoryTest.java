package com.ludot.rules;

import com.ludot.domain.effect.BriefingEffect;
import com.ludot.domain.effect.EnergisedEffect;
import com.ludot.domain.effect.SickEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class EffectFactoryTest {

    private EffectFactory effectFactory;

    @BeforeEach
    void setUp() {
        effectFactory = new EffectFactory();
    }

    @Test
    void createsEnergisedEffect() {
        assertInstanceOf(EnergisedEffect.class, effectFactory.createEnergisedEffect());
    }

    @Test
    void createsSickEffect() {
        assertInstanceOf(SickEffect.class, effectFactory.createSickEffect());
    }

    @Test
    void createsBriefingEffect() {
        assertInstanceOf(BriefingEffect.class, effectFactory.createBriefingEffect());
    }

    @Test
    @DisplayName("Each piece gets its own effect, because effects count down their own rounds")
    void everyCallCreatesANewEffect() {
        assertNotSame(effectFactory.createBriefingEffect(), effectFactory.createBriefingEffect());
    }
}
