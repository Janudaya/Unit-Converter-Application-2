package com.example.unitconverter.converter;

import androidx.annotation.NonNull;

/**
 * Represents a specific unit of measurement within a category.
 */
public class Unit {
    private final String name;
    private final String symbol;
    private final double factorToBase;

    /**
     * @param name Full unit name (e.g. "Meter")
     * @param symbol Abbreviated unit symbol (e.g. "m")
     * @param factorToBase Factor to convert this unit into the category base unit.
     */
    public Unit(String name, String symbol, double factorToBase) {
        this.name = name;
        this.symbol = symbol;
        this.factorToBase = factorToBase;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getFactorToBase() {
        return factorToBase;
    }

    @NonNull
    @Override
    public String toString() {
        if (symbol == null || symbol.isEmpty()) {
            return name;
        }
        return name + " (" + symbol + ")";
    }
}
