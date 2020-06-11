package org.supla.android.db;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class IncrementalMeasurementItem extends MeasurementItem {
    protected boolean Calculated;
    protected boolean Divided;
    protected boolean Complement;

    public IncrementalMeasurementItem() {
        super();
        Calculated = false;
        Divided = false;
        Complement = false;
    }

    public IncrementalMeasurementItem(IncrementalMeasurementItem src) {
        super(src);
        Calculated = src.Calculated;
        Divided = src.Divided;
        Complement = src.Complement;
    }

    public boolean isCalculated() {
        return Calculated;
    }

    public boolean isDivided() {
        return Divided;
    }

    public boolean isComplement() {
        return Complement;
    }

    public void setComplement(boolean complement) {
        Complement = complement;
    }

    public abstract void AssignJSONObject(JSONObject obj) throws JSONException;

    public abstract void DivideBy(long div);

    public abstract void Calculate(IncrementalMeasurementItem item);
}
