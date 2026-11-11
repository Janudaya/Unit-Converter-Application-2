package com.example.unitconverter.converter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles unit conversion logic, predefined categories, units, and formatting.
 */
public class UnitConverter {

    public static final String CAT_LENGTH = "Length";
    public static final String CAT_WEIGHT = "Weight";
    public static final String CAT_TEMPERATURE = "Temperature";
    public static final String CAT_VOLUME = "Volume";
    public static final String CAT_SPEED = "Speed";

    private static final List<Category> CATEGORIES = new ArrayList<>();

    static {
        // 1. Length Units (Base: Meter)
        List<Unit> lengthUnits = Arrays.asList(
                new Unit("Millimeter", "mm", 0.001),
                new Unit("Centimeter", "cm", 0.01),
                new Unit("Meter", "m", 1.0),
                new Unit("Kilometer", "km", 1000.0),
                new Unit("Inch", "in", 0.0254),
                new Unit("Foot", "ft", 0.3048),
                new Unit("Yard", "yd", 0.9144),
                new Unit("Mile", "mi", 1609.344)
        );
        CATEGORIES.add(new Category(CAT_LENGTH, lengthUnits, false));

        // 2. Weight / Mass Units (Base: Kilogram)
        List<Unit> weightUnits = Arrays.asList(
                new Unit("Milligram", "mg", 0.000001),
                new Unit("Gram", "g", 0.001),
                new Unit("Kilogram", "kg", 1.0),
                new Unit("Metric Ton", "t", 1000.0),
                new Unit("Ounce", "oz", 0.028349523125),
                new Unit("Pound", "lb", 0.45359237),
                new Unit("Stone", "st", 6.35029318)
        );
        CATEGORIES.add(new Category(CAT_WEIGHT, weightUnits, false));

        // 3. Temperature Units (Affine transformations)
        List<Unit> tempUnits = Arrays.asList(
                new Unit("Celsius", "°C", 0.0),
                new Unit("Fahrenheit", "°F", 0.0),
                new Unit("Kelvin", "K", 0.0)
        );
        CATEGORIES.add(new Category(CAT_TEMPERATURE, tempUnits, true));

        // 4. Volume Units (Base: Liter)
        List<Unit> volumeUnits = Arrays.asList(
                new Unit("Milliliter", "mL", 0.001),
                new Unit("Liter", "L", 1.0),
                new Unit("Cubic Meter", "m³", 1000.0),
                new Unit("Fluid Ounce (US)", "fl oz", 0.0295735295625),
                new Unit("Cup (US)", "cup", 0.2365882365),
                new Unit("Pint (US)", "pt", 0.473176473),
                new Unit("Quart (US)", "qt", 0.946352946),
                new Unit("Gallon (US)", "gal", 3.785411784)
        );
        CATEGORIES.add(new Category(CAT_VOLUME, volumeUnits, false));

        // 5. Speed Units (Base: Meter per second)
        List<Unit> speedUnits = Arrays.asList(
                new Unit("Meter per second", "m/s", 1.0),
                new Unit("Kilometer per hour", "km/h", 1.0 / 3.6),
                new Unit("Mile per hour", "mph", 0.44704),
                new Unit("Knot", "kn", 0.5144444444444444),
                new Unit("Foot per second", "ft/s", 0.3048)
        );
        CATEGORIES.add(new Category(CAT_SPEED, speedUnits, false));
    }

    public static List<Category> getCategories() {
        return CATEGORIES;
    }

    /**
     * Converts a value from sourceUnit to targetUnit in the specified Category.
     */
    public static double convert(double value, Unit sourceUnit, Unit targetUnit, Category category) {
        if (sourceUnit.equals(targetUnit) || sourceUnit.getName().equals(targetUnit.getName())) {
            return value;
        }

        if (CAT_TEMPERATURE.equalsIgnoreCase(category.getName())) {
            return convertTemperature(value, sourceUnit, targetUnit);
        }

        // Linear conversion via base unit
        double baseValue = value * sourceUnit.getFactorToBase();
        return baseValue / targetUnit.getFactorToBase();
    }

    private static double convertTemperature(double value, Unit source, Unit target) {
        String from = source.getSymbol();
        String to = target.getSymbol();

        // 1. Convert source to Celsius
        double celsius;
        switch (from) {
            case "°F":
                celsius = (value - 32.0) * (5.0 / 9.0);
                break;
            case "K":
                celsius = value - 273.15;
                break;
            case "°C":
            default:
                celsius = value;
                break;
        }

        // 2. Convert Celsius to target
        switch (to) {
            case "°F":
                return (celsius * (9.0 / 5.0)) + 32.0;
            case "K":
                return celsius + 273.15;
            case "°C":
            default:
                return celsius;
        }
    }

    /**
     * Checks if temperature is at or above absolute zero.
     */
    public static boolean isAboveAbsoluteZero(double value, Unit unit) {
        String sym = unit.getSymbol();
        if ("°C".equals(sym)) {
            return value >= -273.15;
        } else if ("°F".equals(sym)) {
            return value >= -459.67;
        } else if ("K".equals(sym)) {
            return value >= 0.0;
        }
        return true;
    }

    /**
     * Formats a double value with sensible precision and comma groupings.
     */
    public static String formatNumber(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }
        if (value == 0.0) {
            return "0";
        }

        double abs = Math.abs(value);
        DecimalFormat df;
        if (abs < 0.0001 || abs >= 100_000_000.0) {
            df = new DecimalFormat("0.######E0");
        } else {
            df = new DecimalFormat("#,##0.######");
        }
        return df.format(value);
    }

    /**
     * Returns an informative formula explanation string.
     */
    public static String getFormulaDescription(double inputVal, Unit sourceUnit, double resultVal, Unit targetUnit, Category category) {
        if (CAT_TEMPERATURE.equalsIgnoreCase(category.getName())) {
            String from = sourceUnit.getSymbol();
            String to = targetUnit.getSymbol();
            if (from.equals(to)) {
                return "Identical units (no conversion needed).";
            }
            if (from.equals("°C") && to.equals("°F")) {
                return "Formula: (" + formatNumber(inputVal) + "°C × 9/5) + 32 = " + formatNumber(resultVal) + "°F";
            } else if (from.equals("°F") && to.equals("°C")) {
                return "Formula: (" + formatNumber(inputVal) + "°F - 32) × 5/9 = " + formatNumber(resultVal) + "°C";
            } else if (from.equals("°C") && to.equals("K")) {
                return "Formula: " + formatNumber(inputVal) + "°C + 273.15 = " + formatNumber(resultVal) + " K";
            } else if (from.equals("K") && to.equals("°C")) {
                return "Formula: " + formatNumber(inputVal) + " K - 273.15 = " + formatNumber(resultVal) + "°C";
            } else if (from.equals("°F") && to.equals("K")) {
                return "Formula: (" + formatNumber(inputVal) + "°F - 32) × 5/9 + 273.15 = " + formatNumber(resultVal) + " K";
            } else if (from.equals("K") && to.equals("°F")) {
                return "Formula: (" + formatNumber(inputVal) + " K - 273.15) × 9/5 + 32 = " + formatNumber(resultVal) + "°F";
            }
        }

        if (sourceUnit.getName().equals(targetUnit.getName())) {
            return "Identical units (ratio 1 : 1)";
        }

        double unitRatio = sourceUnit.getFactorToBase() / targetUnit.getFactorToBase();
        return "Formula: 1 " + sourceUnit.getSymbol() + " = " + formatNumber(unitRatio) + " " + targetUnit.getSymbol()
                + " (multiply by " + formatNumber(unitRatio) + ")";
    }
}
