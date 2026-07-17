package com.amadeus.dandanxiuqiu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CalculatorEngineTest {
    @Test
    public void exactTableValueIsReturned() {
        assertEquals(60.0, CalculatorEngine.interpolateBasePower(12), 0.0001);
    }

    @Test
    public void fractionalDistanceIsInterpolated() {
        assertEquals(61.8, CalculatorEngine.interpolateBasePower(12.6), 0.0001);
    }

    @Test
    public void tailwindReducesRecommendedPower() {
        CalculatorEngine.Result result = CalculatorEngine.calculate(
                12, 0, 2, CalculatorEngine.WindDirection.TAILWIND, 65,
                0.55, 1.20, 0.65);
        assertEquals(58.9, result.recommendedPower, 0.0001);
        assertEquals(59, result.roundedPower);
    }

    @Test(expected = IllegalArgumentException.class)
    public void distanceOutsideTableIsRejected() {
        CalculatorEngine.interpolateBasePower(20.1);
    }
}
