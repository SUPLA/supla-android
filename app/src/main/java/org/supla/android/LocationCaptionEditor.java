package org.supla.android;

import android.content.Context;

import org.supla.android.db.DbHelper;
import org.supla.android.db.Location;

public class LocationCaptionEditor extends CaptionEditor {
    public LocationCaptionEditor(Context context) {
        super(context);
    }

    @Override
    protected int getTitle() {
        return R.string.location_name;
    }

    @Override
    protected String getCaption() {
        DbHelper dbH = DbHelper.getInstance(getContext());
        if (dbH!=null) {
            Location location = dbH.getLocation(getId());
            if (location != null && location.getCaption() != null) {
                return location.getCaption();
            }
        }
        return "";
    }

    @Override
    protected void applyChanged(String newCaption) {

    }
}
