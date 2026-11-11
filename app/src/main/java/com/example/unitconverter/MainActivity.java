package com.example.unitconverter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.unitconverter.converter.Category;
import com.example.unitconverter.converter.Unit;
import com.example.unitconverter.converter.UnitConverter;

import java.util.List;

/**
 * Main Activity for the Unit Converter application.
 * Manages category selection, unit spinners, numeric input validation,
 * unit conversion calculation, and result rendering.
 */
public class MainActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private EditText editTextInputValue;
    private ImageButton buttonClearInput;
    private Spinner spinnerSourceUnit;
    private Spinner spinnerTargetUnit;
    private ImageButton buttonSwapUnits;
    private Button buttonConvert;
    private TextView textViewResult;
    private TextView textViewFormulaDetails;
    private ImageButton buttonCopyResult;

    private Category currentCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupCategorySpinner();
        setupInputListeners();
        setupSwapButton();
        setupConvertButton();
        setupCopyButton();
    }

    /**
     * Finds and assigns view references.
     */
    private void initViews() {
        spinnerCategory = findViewById(R.id.spinnerCategory);
        editTextInputValue = findViewById(R.id.editTextInputValue);
        buttonClearInput = findViewById(R.id.buttonClearInput);
        spinnerSourceUnit = findViewById(R.id.spinnerSourceUnit);
        spinnerTargetUnit = findViewById(R.id.spinnerTargetUnit);
        buttonSwapUnits = findViewById(R.id.buttonSwapUnits);
        buttonConvert = findViewById(R.id.buttonConvert);
        textViewResult = findViewById(R.id.textViewResult);
        textViewFormulaDetails = findViewById(R.id.textViewFormulaDetails);
        buttonCopyResult = findViewById(R.id.buttonCopyResult);
    }

    /**
     * Initializes the category dropdown spinner with predefined measurement categories.
     */
    private void setupCategorySpinner() {
        List<Category> categories = UnitConverter.getCategories();
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = (Category) parent.getItemAtPosition(position);
                updateUnitSpinners(currentCategory);
                clearResultDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    /**
     * Updates the source and target unit dropdown spinners based on the chosen category.
     */
    private void updateUnitSpinners(Category category) {
        if (category == null || category.getUnits().isEmpty()) {
            return;
        }

        List<Unit> units = category.getUnits();
        ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                units
        );
        unitAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerSourceUnit.setAdapter(unitAdapter);
        spinnerTargetUnit.setAdapter(unitAdapter);

        // Set initial sensible defaults: Source at index 0, Target at index 1 (if available)
        spinnerSourceUnit.setSelection(0);
        if (units.size() > 1) {
            spinnerTargetUnit.setSelection(1);
        } else {
            spinnerTargetUnit.setSelection(0);
        }
    }

    /**
     * Configures input field listeners (e.g. quick clear button visibility).
     */
    private void setupInputListeners() {
        editTextInputValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    buttonClearInput.setVisibility(View.VISIBLE);
                } else {
                    buttonClearInput.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        buttonClearInput.setOnClickListener(v -> {
            editTextInputValue.setText("");
            clearResultDisplay();
        });
    }

    /**
     * Swaps the source unit and target unit selections.
     */
    private void setupSwapButton() {
        buttonSwapUnits.setOnClickListener(v -> {
            int sourcePos = spinnerSourceUnit.getSelectedItemPosition();
            int targetPos = spinnerTargetUnit.getSelectedItemPosition();

            spinnerSourceUnit.setSelection(targetPos);
            spinnerTargetUnit.setSelection(sourcePos);

            // If an input is already present, automatically recalculate
            String inputStr = editTextInputValue.getText().toString().trim();
            if (!inputStr.isEmpty()) {
                performConversion();
            }
        });
    }

    /**
     * Validates user input and computes conversion.
     */
    private void setupConvertButton() {
        buttonConvert.setOnClickListener(v -> performConversion());
    }

    /**
     * Validates input values and displays Toast errors or computes converted results.
     */
    private void performConversion() {
        String inputStr = editTextInputValue.getText().toString().trim();

        // 1. Validate empty input
        if (inputStr.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_input, Toast.LENGTH_SHORT).show();
            editTextInputValue.requestFocus();
            return;
        }

        // 2. Validate numeric format
        double inputValue;
        try {
            inputValue = Double.parseDouble(inputStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_invalid_input, Toast.LENGTH_SHORT).show();
            editTextInputValue.requestFocus();
            return;
        }

        // 3. Validate category-specific domain rules
        if (currentCategory == null) {
            currentCategory = (Category) spinnerCategory.getSelectedItem();
        }

        Unit sourceUnit = (Unit) spinnerSourceUnit.getSelectedItem();
        Unit targetUnit = (Unit) spinnerTargetUnit.getSelectedItem();

        if (sourceUnit == null || targetUnit == null) {
            Toast.makeText(this, "Please select source and target units", Toast.LENGTH_SHORT).show();
            return;
        }

        // Negative check for non-temperature categories
        if (inputValue < 0 && !currentCategory.allowsNegative()) {
            Toast.makeText(this, R.string.error_negative_value, Toast.LENGTH_SHORT).show();
            return;
        }

        // Absolute zero check for temperature
        if (UnitConverter.CAT_TEMPERATURE.equalsIgnoreCase(currentCategory.getName())) {
            if (!UnitConverter.isAboveAbsoluteZero(inputValue, sourceUnit)) {
                Toast.makeText(this, R.string.error_below_absolute_zero, Toast.LENGTH_LONG).show();
                return;
            }
        }

        // 4. Compute conversion
        double convertedValue = UnitConverter.convert(inputValue, sourceUnit, targetUnit, currentCategory);

        // 5. Render result and formula breakdown
        String formattedNumber = UnitConverter.formatNumber(convertedValue);
        String resultText = formattedNumber + " " + targetUnit.getSymbol();
        textViewResult.setText(resultText);

        String formula = UnitConverter.getFormulaDescription(inputValue, sourceUnit, convertedValue, targetUnit, currentCategory);
        textViewFormulaDetails.setText(formula);
        textViewFormulaDetails.setVisibility(View.VISIBLE);
    }

    /**
     * Clears the result card view.
     */
    private void clearResultDisplay() {
        textViewResult.setText("");
        textViewFormulaDetails.setText("");
        textViewFormulaDetails.setVisibility(View.GONE);
    }

    /**
     * Copies the converted result to clipboard.
     */
    private void setupCopyButton() {
        buttonCopyResult.setOnClickListener(v -> {
            CharSequence resultText = textViewResult.getText();
            if (resultText == null || resultText.length() == 0) {
                Toast.makeText(this, "No result to copy", Toast.LENGTH_SHORT).show();
                return;
            }

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Converted Unit Result", resultText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.copied_toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
