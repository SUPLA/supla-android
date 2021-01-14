package org.supla.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import org.supla.android.lib.SuplaClient;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;

public class ChannelDetailDigiglass extends DetailLayout implements View.OnClickListener, DigiglassController.OnSectionClickListener {

    Button btnAllTransparent;
    Button btnAllOpaque;
    DigiglassController controller;

    public ChannelDetailDigiglass(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailDigiglass(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailDigiglass(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailDigiglass(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();
        btnAllTransparent = findViewById(R.id.btnDgfTransparent);
        btnAllOpaque = findViewById(R.id.btnDgfOpaque);
        controller = findViewById(R.id.dgfController);

        btnAllOpaque.setOnClickListener(this);
        btnAllTransparent.setOnClickListener(this);
        controller.setOnSectionClickListener(this);
    }

    @Override
    public View inflateContentView() {
        return inflateLayout(R.layout.detail_digiglass);
    }

    @Override
    public void OnChannelDataChanged() {

    }

    @Override
    public void onClick(View v) {
        if (v == btnAllOpaque) {
            controller.setAllOpaque();
        } else if (v == btnAllTransparent) {
            controller.setAllTransparent();
        } else {
            return;
        }

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client != null) {
            client.setDfgTransparency(getRemoteId(), (short)controller.getTransparentSections(),
                    (short)0xFFFF);
        }
    }

    @Override
    public void onGlassSectionClick(DigiglassController controller, int section,
                                    boolean transparent) {
        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client != null) {
            short bit = (short)(1 << section);
            client.setDfgTransparency(getRemoteId(), transparent ? bit : 0, bit);
        }
    }
}
