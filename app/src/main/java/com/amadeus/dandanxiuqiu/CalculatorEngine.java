package com.amadeus.dandanxiuqiu;

import java.util.Locale;

/** Pure calculation logic kept separate from Android UI so it can be unit-tested. */
public final class CalculatorEngine {
    public static final double REFERENCE_ANGLE = 65.0;
    public static final double DEFAULT_WIND_COEFFICIENT = 0.55;
    public static final double DEFAULT_HEIGHT_COEFFICIENT = 1.20;
    public static final double DEFAULT_ANGLE_COEFFICIENT = 0.65;

    // The 65-degree, zero-wind, equal-height table supplied by the game guide.
    private static final double[] BASE_POWER = {
            0, 11, 19, 26, 31, 35, 39, 43, 47, 50, 54,
            57, 60, 63, 66, 69, 72, 74, 77, 80, 82
    };

    private CalculatorEngine() {}

    public enum WindDirection {
        TAILWIND("顺风", -1),
        CALM("无风", 0),
        HEADWIND("逆风", 1);

        public final String label;
        private final int sign;

        WindDirection(String label, int sign) {
            this.label = label;
            this.sign = sign;
        }
    }

    public static Result calculate(
            double distance,
            double heightDifference,
            double windStrength,
            WindDirection windDirection,
            double angle,
            double windCoefficient,
            double heightCoefficient,
            double angleCoefficient) {

        requireRange(distance, 1, 20, "距离必须在 1～20 屏之间");
        requireRange(heightDifference, -10, 10, "高度差必须在 -10～10 之间");
        requireRange(windStrength, 0, 20, "风力必须在 0～20 之间");
        requireRange(angle, 20, 80, "角度必须在 20°～80°之间");
        requireRange(windCoefficient, 0, 5, "风力系数必须在 0～5 之间");
        requireRange(heightCoefficient, 0, 5, "高度系数必须在 0～5 之间");
        requireRange(angleCoefficient, 0, 5, "角度系数必须在 0～5 之间");
        if (windDirection == null) {
            throw new IllegalArgumentException("请选择风向");
        }

        double base = interpolateBasePower(distance);
        double windCorrection = windDirection.sign * windStrength * windCoefficient;
        double heightCorrection = heightDifference * heightCoefficient;
        double angleCorrection = (angle - REFERENCE_ANGLE) * angleCoefficient;
        double raw = base + windCorrection + heightCorrection + angleCorrection;
        double recommended = clamp(raw, 1, 100);
        int rounded = (int) Math.round(recommended);
        int low = Math.max(1, rounded - 1);
        int high = Math.min(100, rounded + 1);

        return new Result(base, windCorrection, heightCorrection, angleCorrection,
                recommended, rounded, low, high);
    }

    public static double interpolateBasePower(double distance) {
        requireRange(distance, 1, 20, "距离必须在 1～20 屏之间");
        int lower = (int) Math.floor(distance);
        int upper = (int) Math.ceil(distance);
        if (lower == upper) {
            return BASE_POWER[lower];
        }
        double fraction = distance - lower;
        return BASE_POWER[lower] + (BASE_POWER[upper] - BASE_POWER[lower]) * fraction;
    }

    private static void requireRange(double value, double min, double max, String message) {
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new IllegalArgumentException(message);
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class Result {
        public final double basePower;
        public final double windCorrection;
        public final double heightCorrection;
        public final double angleCorrection;
        public final double recommendedPower;
        public final int roundedPower;
        public final int lowPower;
        public final int highPower;

        Result(double basePower, double windCorrection, double heightCorrection,
               double angleCorrection, double recommendedPower, int roundedPower,
               int lowPower, int highPower) {
            this.basePower = basePower;
            this.windCorrection = windCorrection;
            this.heightCorrection = heightCorrection;
            this.angleCorrection = angleCorrection;
            this.recommendedPower = recommendedPower;
            this.roundedPower = roundedPower;
            this.lowPower = lowPower;
            this.highPower = highPower;
        }

        public String detailText() {
            return String.format(Locale.CHINA,
                    "基础 %.1f  ·  风力 %+.1f  ·  高度 %+.1f  ·  角度 %+.1f",
                    basePower, windCorrection, heightCorrection, angleCorrection);
        }
    }
}
