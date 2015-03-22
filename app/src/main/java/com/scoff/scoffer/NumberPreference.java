package com.scoff.scoffer;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

public class NumberPreference extends DialogPreference {

    private int currentPickerValue = 0;
    private NumberPicker np = null;

    public NumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected View onCreateDialogView() {
        np = new NumberPicker(getContext());
        np.setMinValue(1);
        np.setMaxValue(100);
        np.setWrapSelectorWheel(true);

        LinearLayout.LayoutParams pickerParams = new LinearLayout.LayoutParams
                (RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
        pickerParams.gravity = Gravity.CENTER;
        np.setLayoutParams(pickerParams);

        LinearLayout layout = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);

        layout.addView(np);
        return layout;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        np.setMaxValue(60);
        np.setValue(currentPickerValue);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            currentPickerValue = np.getValue();
            String valueToSave = String.valueOf(currentPickerValue);
            if (callChangeListener(valueToSave)) {
                persistString(valueToSave);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String valueToRestore = null;

        if (restoreValue) {
            if (defaultValue == null) {
                valueToRestore = getPersistedString("1");
            } else {
                valueToRestore = getPersistedString(defaultValue.toString());
            }
        } else {
            valueToRestore = defaultValue.toString();
        }
        currentPickerValue = Integer.parseInt(valueToRestore);
    }

    public int getEntry() {
        return currentPickerValue;
    }

}