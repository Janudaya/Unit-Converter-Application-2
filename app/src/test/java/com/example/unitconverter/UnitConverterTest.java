package com.example.unitconverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.unitconverter.converter.Category;
import com.example.unitconverter.converter.Unit;
import com.example.unitconverter.converter.UnitConverter;

import org.junit.Test;

import java.util.List;

/**
 * Unit tests for UnitConverter logic, formulas, and category configurations.
 */
public class UnitConverterTest {

    @Test
    public void testCategoriesLoaded() {
        List<Category> categories = UnitConverter.getCategories();
        assertTrue("Should support at least 3 categories", categories.size() >= 3);

        boolean hasLength = false;
        boolean hasWeight = false;
        boolean hasTemperature = false;

        for (Category cat : categories) {
            if (UnitConverter.CAT_LENGTH.equals(cat.getName())) hasLength = true;
            if (UnitConverter.CAT_WEIGHT.equals(cat.getName())) hasWeight = true;
            if (UnitConverter.CAT_TEMPERATURE.equals(cat.getName())) hasTemperature = true;
        }

        assertTrue("Must include Length category", hasLength);
        assertTrue("Must include Weight category", hasWeight);
        assertTrue("Must include Temperature category", hasTemperature);
    }

    @Test
    public void testLengthConversions() {
        Category length = findCategory(UnitConverter.CAT_LENGTH);
        Unit cm = findUnit(length, "Centimeter");
        Unit m = findUnit(length, "Meter");
        Unit in = findUnit(length, "Inch");

        // 100 cm = 1.0 m
        assertEquals(1.0, UnitConverter.convert(100.0, cm, m, length), 0.0001);

        // 2.54 cm = 1.0 inch
        assertEquals(1.0, UnitConverter.convert(2.54, cm, in, length), 0.0001);

        // 1 inch = 2.54 cm
        assertEquals(2.54, UnitConverter.convert(1.0, in, cm, length), 0.0001);
    }

    @Test
    public void testWeightConversions() {
        Category weight = findCategory(UnitConverter.CAT_WEIGHT);
        Unit g = findUnit(weight, "Gram");
        Unit kg = findUnit(weight, "Kilogram");
        Unit lb = findUnit(weight, "Pound");

        // 1000 g = 1 kg
        assertEquals(1.0, UnitConverter.convert(1000.0, g, kg, weight), 0.0001);

        // 1 kg ~ 2.20462 lb
        assertEquals(2.20462, UnitConverter.convert(1.0, kg, lb, weight), 0.001);
    }

    @Test
    public void testTemperatureConversions() {
        Category temp = findCategory(UnitConverter.CAT_TEMPERATURE);
        Unit celsius = findUnit(temp, "Celsius");
        Unit fahrenheit = findUnit(temp, "Fahrenheit");
        Unit kelvin = findUnit(temp, "Kelvin");

        // 0 °C = 32 °F
        assertEquals(32.0, UnitConverter.convert(0.0, celsius, fahrenheit, temp), 0.0001);

        // 100 °C = 212 °F
        assertEquals(212.0, UnitConverter.convert(100.0, celsius, fahrenheit, temp), 0.0001);

        // -40 °C = -40 °F
        assertEquals(-40.0, UnitConverter.convert(-40.0, celsius, fahrenheit, temp), 0.0001);

        // 0 °C = 273.15 K
        assertEquals(273.15, UnitConverter.convert(0.0, celsius, kelvin, temp), 0.0001);

        // 300 K to °C
        assertEquals(26.85, UnitConverter.convert(300.0, kelvin, celsius, temp), 0.0001);
    }

    @Test
    public void testAbsoluteZeroValidation() {
        Category temp = findCategory(UnitConverter.CAT_TEMPERATURE);
        Unit celsius = findUnit(temp, "Celsius");
        Unit kelvin = findUnit(temp, "Kelvin");

        assertTrue(UnitConverter.isAboveAbsoluteZero(-273.15, celsius));
        assertFalse(UnitConverter.isAboveAbsoluteZero(-274.0, celsius));

        assertTrue(UnitConverter.isAboveAbsoluteZero(0.0, kelvin));
        assertFalse(UnitConverter.isAboveAbsoluteZero(-1.0, kelvin));
    }

    @Test
    public void testNumberFormatting() {
        assertEquals("0", UnitConverter.formatNumber(0.0));
        assertEquals("100", UnitConverter.formatNumber(100.0));
        assertEquals("39.3701", UnitConverter.formatNumber(39.3701));
    }

    private Category findCategory(String name) {
        for (Category cat : UnitConverter.getCategories()) {
            if (cat.getName().equalsIgnoreCase(name)) return cat;
        }
        throw new IllegalArgumentException("Category not found: " + name);
    }

    private Unit findUnit(Category category, String name) {
        for (Unit u : category.getUnits()) {
            if (u.getName().equalsIgnoreCase(name)) return u;
        }
        throw new IllegalArgumentException("Unit not found: " + name);
    }
}
