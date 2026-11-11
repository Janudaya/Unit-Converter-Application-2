package com.example.unitconverter.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a measurement category (e.g. Length, Weight, Temperature, Volume, Speed).
 */
public class Category {
    private final String name;
    private final List<Unit> units;
    private final boolean allowsNegative;

    public Category(String name, List<Unit> units, boolean allowsNegative) {
        this.name = name;
        this.units = new ArrayList<>(units);
        this.allowsNegative = allowsNegative;
    }

    public String getName() {
        return name;
    }

    public List<Unit> getUnits() {
        return Collections.unmodifiableList(units);
    }

    public boolean allowsNegative() {
        return allowsNegative;
    }

    @Override
    public String toString() {
        return name;
    }
}
